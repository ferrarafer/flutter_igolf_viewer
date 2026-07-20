package com.filledstacks.plugins.flutter_igolf_viewer

import android.content.Context
import android.graphics.Color
import android.location.Location
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.annotation.VisibleForTesting
import com.filledstacks.plugins.flutter_igolf_viewer.channels.CourseViewerEventChannel
import com.filledstacks.plugins.flutter_igolf_viewer.lifecycle.PausableViewer
import com.filledstacks.plugins.flutter_igolf_viewer.lifecycle.ViewerLifecycleRegistry
import com.filledstacks.plugins.flutter_igolf_viewer.network.Network
import com.google.gson.Gson
import com.l1inc.viewer.Course3DRenderer
import com.l1inc.viewer.Course3DRendererBase
import com.l1inc.viewer.Course3DRendererBase.NavigationMode
import com.l1inc.viewer.Course3DViewer
import com.l1inc.viewer.HoleWithinCourse
import com.l1inc.viewer.common.Viewer.CurrentHoleChangedListener
import com.l1inc.viewer.common.Viewer.HoleLoadingStateChangedListener
import com.l1inc.viewer.drawing.custom.CustomOverlay
import io.flutter.plugin.common.BinaryMessenger
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.EventChannel
import io.flutter.plugin.platform.PlatformView
import org.json.JSONObject
import java.io.File
import java.lang.RuntimeException

/**
 * Maps a [zoom] value in the inclusive range [0, 100] to a free-camera zoom scale
 * in the inclusive range [1.0, 5.0]. 100 → 1.0 (no zoom-out), 0 → 5.0 (max zoom-out).
 * Mirrors `freeCamZoomScale(from:)` in `ios/Classes/FlutterIgolfView.swift`.
 */
@VisibleForTesting
internal fun freeCamZoomScale(zoom: Int): Float {
    val clamped = zoom.coerceIn(0, 100)
    val zoomOutAmount = (100 - clamped) / 100.0
    return (1.0 + zoomOutAmount * 4.0).toFloat()
}

internal class FlutterIgolfViewer(
    context: Context,
    messenger: BinaryMessenger,
    eventChannel: CourseViewerEventChannel,
    private val lifecycleRegistry: ViewerLifecycleRegistry,
    id: Int,
    creationParams: Map<String?, Any?>?
) : PlatformView, MethodChannel.MethodCallHandler, PausableViewer {

    private val course3DViewer: Course3DViewer

    private val methodChannel: MethodChannel = MethodChannel(
        messenger, "plugins.filledstacks.flutter_igolf_viewer/course_viewer_method"
    )

    private val event : CourseViewerEventChannel = eventChannel

    // Guard so the async init's main-thread continuation doesn't fire on a
    // viewer the host has already disposed (user navigated away mid-init).
    @Volatile private var isDisposed = false
    private val mainHandler = Handler(Looper.getMainLooper())

    // Reference to the background init thread, so dispose() can attempt to
    // interrupt it if it's still inside viewer.init(...). The SDK is opaque
    // and may or may not honor interrupt(), but it's harmless to try — at
    // worst it's a no-op and we fall through to the try/catch.
    @Volatile private var initThread: Thread? = null

    init {
        if (creationParams == null) {
            throw RuntimeException("API and Secret keys are required")
        }

        methodChannel.setMethodCallHandler(this)

        course3DViewer = Course3DViewer(context)

        // Keep the EGL context (shaders, programs, textures) alive across an
        // activity pause. Only the EGL *surface* is released — which is the
        // part that must disconnect from the Flutter texture's ImageReader —
        // so resume skips a multi-second course reload and the SDK never
        // deletes/recreates GL objects against a torn-down context (the
        // glDeleteShader/glDeleteProgram 0x501 spam seen on pause).
        course3DViewer.viewer.preserveEGLContextOnPause = true

        // GLSurfaceView contract: the host must forward activity pause/resume
        // so the GL thread releases its EGL surface (the ImageReader
        // BufferQueue producer) before the surface is torn down or recreated.
        // The registry is driven by the plugin's ActivityAware callbacks.
        lifecycleRegistry.register(this)

        course3DViewer.viewer.setOnGPSDistancesUpdatedListener { front, center, back, cursorInsideGreen ->
            eventChannel.sendEvent(mapOf(
                "event" to "GPS_DISTANCES_UPDATED",
                "front" to front,
                "center" to center,
                "back" to back,
                "cursorInsideGreen" to cursorInsideGreen
                ))
        }

        course3DViewer.viewer.setOnUserTapsViewerListener { targetToUser, targetToFlag, targetLatitude, targetLongitude ->
            eventChannel.sendEvent(mapOf(
                "event" to "USER_TAPS_VIEWER",
                "targetToUser" to targetToUser,
                "targetToFlag" to targetToFlag,
                "targetLatitude" to targetLatitude,
                "targetLongitude" to targetLongitude
            ))
        }

        course3DViewer.viewer.setCurrentCourseChangedListener {
            eventChannel.sendEvent(mapOf(
                "event" to "CURRENT_COURSE_CHANGED"
            ))
        }

        course3DViewer.viewer.setCurrentHoleChangedListener (object : CurrentHoleChangedListener {
            override fun onHoleChanged(hole: HoleWithinCourse?) {
                hole ?: return
                eventChannel.sendEvent(mapOf(
                    "event" to "CURRENT_HOLE_CHANGED",
                    "hole" to hole.holeNumber
                ))
            }

            override fun onHoleFailed() {}
        })

        course3DViewer.viewer.setFlyoverFinishListener {
            eventChannel.sendEvent(mapOf(
                "event" to "FLYOVER_FINISHED"
            ))
        }

        course3DViewer.viewer.setGreenPositionChangeListener { greenPosition ->
            eventChannel.sendEvent(mapOf(
                "event" to "GREEN_POSITION_CHANGED",
                "position" to "$greenPosition"
            ))
        }

        course3DViewer.viewer.setHoleLoadingStateChangedListener (object : HoleLoadingStateChangedListener {
            override fun onStartLoading() {
                eventChannel.sendEvent(mapOf(
                    "event" to "HOLE_LOADING_STARTED"
                ))
            }

            override fun onFinishLoading() {
                eventChannel.sendEvent(mapOf(
                    "event" to "HOLE_LOADING_FINISHED"
                ))
            }
        })

        course3DViewer.viewer.setNavigationModeChangedListener { mode ->
            eventChannel.sendEvent(mapOf(
                "event" to "NAVIGATION_MODE_CHANGED",
                "mode" to "$mode"
            ))
        }

        course3DViewer.viewer.isHoleRotationOnDynamicFrontBackEnabled = true

        loadCourseData(
            creationParams.get("apiKey"),
            creationParams.get("secretKey"),
            creationParams.get("courseId"),
            creationParams.get("startingHole"),
            creationParams.get("initialTeeBox"),
            creationParams.get("parData"),
            creationParams.get("gpsDetails"),
            creationParams.get("vectorGpsObject"),
            creationParams.get("golferIconIndex"),
            creationParams.get("isMetricUnits"),
            creationParams.get("freeCamZoom")
        )
    }

    override fun getView(): View {
        return course3DViewer
    }

    override fun pauseRendering() {
        if (isDisposed) return
        // Blocks until the GL thread has released its EGL surface, which
        // disconnects the BufferQueue producer from the Flutter texture's
        // ImageReader. This must complete before the surface is torn down /
        // recreated, otherwise the resume-time reconnect is rejected with
        // "connect: already connected" and the queue is left producer-less.
        course3DViewer.viewer.onPause()
    }

    override fun resumeRendering() {
        if (isDisposed) return
        course3DViewer.viewer.onResume()
    }

    override fun dispose() {
        isDisposed = true
        lifecycleRegistry.unregister(this)
        mainHandler.removeCallbacksAndMessages(null)
        // Best-effort: if the background viewer.init thread is still inside
        // the SDK call, ask it to bail. The SDK is closed-source and may not
        // honor interrupt(), in which case init() finishes its mutations
        // before we tear the viewer down here; the try/catch in the init
        // thread + isDisposed guard on the main-thread continuation keep
        // the window from crashing, but there's a small window where init
        // may leak whatever resources it allocated before we called
        // onDestroy(). Acceptable given the window is narrow and the SDK
        // is opaque.
        initThread?.interrupt()
        // Park the GL thread and release the EGL surface first, so the
        // ImageReader producer is disconnected and no onDrawFrame can be
        // in flight while onDestroy() tears the renderer's GL objects down.
        course3DViewer.viewer.onPause()
        course3DViewer.viewer.onDestroy()
    }

    private fun loadCourseData(
        apiKey: Any?,
        secretKey: Any?,
        courseId: Any?,
        startingHole: Any?,
        initialTeeBox: Any?,
        parData: Any?,
        gpsDetails: Any?,
        vectorGpsObject: Any?,
        golferIconIndex: Any?,
        isMetricUnits: Any?,
        freeCamZoom: Any?
    ) {
        if (apiKey !is String || apiKey.isBlank()) {
            throw RuntimeException("API key is required")
        }

        if (secretKey !is String || secretKey.isBlank()) {
            throw RuntimeException("Secret key is required")
        }

        if (courseId !is String || courseId.isBlank()) {
            throw RuntimeException("Course ID is required")
        }

        if (startingHole !is Int) {
            throw RuntimeException("startingHole should be Integer")
        }

        if (initialTeeBox !is Int) {
            throw RuntimeException("initialTeeBox should be Integer")
        }

        if (golferIconIndex !is Int) {
            throw RuntimeException("golferIconIndex should be Integer")
        }

        if (isMetricUnits !is Boolean) {
            throw RuntimeException("isMetricUnits should be Boolean")
        }

        // Mirror iOS contract: missing freeCamZoom defaults to 100 (no zoom-out).
        // Wrong type is a programming error and surfaces like other validations.
        val resolvedFreeCamZoom: Int = when (freeCamZoom) {
            null -> 100
            is Int -> freeCamZoom
            else -> throw RuntimeException("freeCamZoom should be Integer")
        }

        if (parData != null && gpsDetails != null && vectorGpsObject != null) {
            val vectorGpsMap = HashMap<String?, String?>()
            vectorGpsMap[courseId] = vectorGpsObject as String
            vectorGpsMap["GPS_DETAILS"] = gpsDetails as String
            vectorGpsMap["COURSE_ID"] = courseId

            initAndShowViewer(
                convertParData(parData as String),
                vectorGpsMap,
                startingHole,
                initialTeeBox,
                golferIconIndex,
                isMetricUnits,
                resolvedFreeCamZoom
            )
            return
        }

        Network().loadCourseData(apiKey, secretKey, courseId) { parDataMap, vectorDataJsonMap ->
            initAndShowViewer(parDataMap, vectorDataJsonMap, startingHole, initialTeeBox, golferIconIndex, isMetricUnits, resolvedFreeCamZoom)
            event.sendEvent(buildMap {
                put("event", "COURSE_DATA_LOADED")
                put("courseId", courseId)
                put("parDataMap", Gson().toJson(parDataMap).toString())
                put("vectorDataJsonMap", vectorDataJsonMap)
            })
        }
    }

    private fun initAndShowViewer(
        parDataMap: Map<String?, Array<Int>?>,
        vectorDataJsonMap: HashMap<String?, String?>,
        startingHole: Int,
        initialTeeBox: Int,
        golferIconIndex: Int,
        isMetricUnits: Boolean,
        freeCamZoom: Int
    ) {
        // viewer.init() is mostly CPU-bound data prep — Gson decode, elevation
        // parsing, asset Typeface loading, plus setting flags the GL thread
        // reads in onDrawFrame. Running it on the UI thread freezes the
        // CircularProgressIndicator on the loading screen for the duration of
        // the parse. Move it to a background thread; bounce the View-touching
        // setters (zoom + setCurrentHole, which fires listeners that
        // ultimately go through the Flutter event channel) back to main.
        val thread = Thread({
            try {
                course3DViewer.viewer.init(
                    vectorDataJsonMap,
                    false,
                    golferIconIndex,
                    isMetricUnits,
                    parDataMap,
                    null,
                    false,
                    null
                )
            } catch (t: Throwable) {
                Log.e(
                    "FlutterIgolfViewer",
                    "viewer.init failed on background thread",
                    t
                )
                initThread = null
                return@Thread
            }

            initThread = null

            mainHandler.post {
                if (isDisposed) return@post
                // Apply zoom AFTER viewer.init so the underlying renderer doesn't reset it.
                // Mirrors iOS, which re-applies the scale inside setLoader after the renderer is bound.
                course3DViewer.viewer.setFreeCamZoomScale(freeCamZoomScale(freeCamZoom))

                course3DViewer.viewer.setCurrentHole(
                    startingHole,
                    NavigationMode.FreeCam,
                    true,
                    initialTeeBox
                )
            }
        }, "iGolf-viewer-init")
        initThread = thread
        thread.start()
    }

    override fun onMethodCall(call: MethodCall, result: MethodChannel.Result) {
        try {
            when (call.method) {
                "getCurrentHole" -> getCurrentHole(call, result)
                "getNavigationMode" -> getNavigationMode(call, result)
                "getNumHoles" -> getNumHoles(call, result)
                "getPendingNavigationMode" -> getPendingNavigationMode(call, result)
                "isMetricUnits" -> isMetricUnits(call, result)
                "setCurrentHole" -> setCurrentHole(call, result)
                "setNavigationMode" -> setNavigationMode(call, result)
                "setCartLocationVisible" -> setCartLocationVisible(call, result)
                "setTeeBoxAsCurrentLocation" -> setTeeBoxAsCurrentLocation(call, result)
                "setCurrentLocationGPS" -> setCurrentLocationGPS(call, result)
                "setMeasurementSystem" -> setMeasurementSystem(call, result)
                "setFreeCamZoom" -> setFreeCamZoom(call, result)
                "drawShotVision" -> drawShotVision(call, result)
                "clearShotVision" -> clearShotVision(call, result)
                else -> result.notImplemented()
            }
        } catch (e: RuntimeException) {
            result.error("onMethodCall Exception","method:${call.method}, arguments:${call.arguments}, error:$e", null)
        }
    }

    private fun getCurrentHole(call: MethodCall, result: MethodChannel.Result) {
        val currentHole = course3DViewer.viewer.getCurrentHole()
        result.success(currentHole)
    }

    private fun getNavigationMode(call: MethodCall, result: MethodChannel.Result) {
        result.success(course3DViewer.viewer.getNavigationMode())
    }

    private fun getPendingNavigationMode(call: MethodCall, result: MethodChannel.Result) {
        result.success(course3DViewer.viewer.getPendingNavigationMode())
    }

    private fun getNumHoles(call: MethodCall, result: MethodChannel.Result) {
        result.success(course3DViewer.viewer.getNumHoles())
    }

    private fun isMetricUnits(call: MethodCall, result: MethodChannel.Result) {
        result.success(course3DViewer.viewer.isMetricUnits())
    }

    private fun setMeasurementSystem(call: MethodCall, result: MethodChannel.Result) {
        val isMetricUnits = call.argument<Boolean>("isMetricUnits") ?: false
        course3DViewer.viewer.setMeasurementSystem(isMetricUnits)
        result.success(null)
    }

    private fun setCurrentHole(call: MethodCall, result: MethodChannel.Result) {
        val hole = call.argument<Int>("hole") ?: -1
        val navigationMode = call.argument<String>("navigationMode") ?: ""
        val initialTeeBox = call.argument<Int>("initialTeeBox") ?: null

        course3DViewer.viewer.setCurrentHole(
            hole,
            getTypedNavigationMode(navigationMode),
            false,
            initialTeeBox,
        )
        result.success("Set current hole to $hole with NavigationMode $navigationMode")
    }

    private fun setNavigationMode(call: MethodCall, result: MethodChannel.Result) {
        val mode = call.argument<String>("mode")
        val typedMode = getTypedNavigationMode(mode)
        course3DViewer.viewer.setNavigationMode(typedMode)
        result.success("Result from native side for $mode")
    }

    private fun setCartLocationVisible(call: MethodCall, result: MethodChannel.Result) {
        val isCartLocationVisible = call.argument<Boolean>("cartLocationVisible") ?: false
        course3DViewer.viewer.setCartLocationVisible(isCartLocationVisible)
        result.success(null)
    }

    private fun setTeeBoxAsCurrentLocation(call: MethodCall, result: MethodChannel.Result) {
        val selectedTeeBox = call.argument<Int>("selectedTeeBox") ?: 0
        course3DViewer.viewer.setTeeboxAsCurrentLocation(selectedTeeBox)
        result.success(null)
    }

    private fun setCurrentLocationGPS(call: MethodCall, result: MethodChannel.Result) {
        val location = Location("")
        location.setLatitude(call.argument<Double>("latitude") ?: 0.0)
        location.setLongitude(call.argument<Double>("longitude") ?: 0.0)
        val updateCameraPos = call.argument<Boolean>("updateCameraPos") ?: false
        val response = course3DViewer.viewer.setCurrentLocationGPS(
            location,
            updateCameraPos,
        );
        result.success(response)
    }

    private fun setFreeCamZoom(call: MethodCall, result: MethodChannel.Result) {
        val zoom = call.argument<Int>("zoom")
        if (zoom == null) {
            result.error("INVALID_ARGS", "Missing zoom parameter", null)
            return
        }
        course3DViewer.viewer.setFreeCamZoomScale(freeCamZoomScale(zoom))
        result.success(null)
    }

    // --- Shot Vision (tap-to-aim overlay) ---
    // Stable id so each new tap replaces the previous ring rather than stacking.
    private val shotVisionDotId = 9002

    /**
     * Draws the Shot Vision overlay: a 3D rising flight arc from the tapped
     * point up to the green centre (rendered natively as real geometry, so it
     * leaves the ground like a shot tracer), plus a ground ring marking the
     * tapped point. Green coords come from Dart; if absent, only the ring shows.
     */
    private fun drawShotVision(call: MethodCall, result: MethodChannel.Result) {
        val targetLatitude = call.argument<Double>("targetLatitude")
        val targetLongitude = call.argument<Double>("targetLongitude")
        if (targetLatitude == null || targetLongitude == null) {
            result.error("INVALID_ARGS", "Missing target latitude/longitude", null)
            return
        }

        val target = Location("").apply {
            latitude = targetLatitude
            longitude = targetLongitude
        }

        val ringBorderColor = Color.argb(255, 7, 197, 255)
        val ringFillColor = Color.argb(110, 7, 197, 255)

        // Replace any prior shot.
        course3DViewer.viewer.clearShotArc()
        course3DViewer.viewer.removeAllDotArrays()

        // 3D rising flight arc from the user's position up to the tapped target.
        // The arc START is the viewer's own golfer position (native), so only the
        // tapped point + look knobs (apex/width/colour) come from Dart — the
        // knobs are tunable via hot reload without rebuilding this AAR.
        val apexFraction = call.argument<Double>("arcApexFraction") ?: 0.16
        val lineWidth = (call.argument<Double>("arcLineWidth") ?: 8.0).toFloat()
        val color = call.argument<Number>("arcColor")?.toInt()
            ?: Color.argb(255, 7, 197, 255)
        course3DViewer.viewer.setShotArc(
            targetLatitude,
            targetLongitude,
            apexFraction,
            lineWidth,
            color
        )

        // Ground ring marking the tapped target.
        course3DViewer.viewer.addDotArray(
            CustomOverlay.DotArrayBuilder(shotVisionDotId, arrayListOf(target))
                .borderColor(ringBorderColor)
                .fillColor(ringFillColor)
                .borderWidth(0.8f)
                .dotRadius(3.0f)
                .build()
        )

        result.success(null)
    }

    private fun clearShotVision(call: MethodCall, result: MethodChannel.Result) {
        course3DViewer.viewer.clearShotArc()
        course3DViewer.viewer.removeAllSegmentLines()
        course3DViewer.viewer.removeAllDotArrays()
        result.success(null)
    }

    private fun getTypedNavigationMode(mode: String?): NavigationMode {
        val typedMode = when (mode) {
            "flyover" -> NavigationMode.Flyover
            "flyoverPause" -> NavigationMode.FlyoverPause
            "followGolfer" -> NavigationMode.FollowGolfer
            "freeCam" -> NavigationMode.FreeCam
            "freeCamCart" -> NavigationMode.FreeCamCart
            "greenView2D" -> NavigationMode.GreenView2D
            "greenView3D" -> NavigationMode.GreenView3D
            "overallHole" -> NavigationMode.OverallHole
            "navigationMode2D" -> NavigationMode.NavigationMode2D
            else -> {
                Course3DRendererBase.NavigationMode.NavigationMode2D
            }
        }

        return typedMode
    }

    private fun convertParData(jsonString: String): Map<String?, Array<Int>?> {
        val jsonObject = JSONObject(jsonString)
        val resultMap = mutableMapOf<String?, Array<Int>?>()

        val keys = jsonObject.keys()
        while (keys.hasNext()) {
            val key = keys.next()
            val jsonArray = jsonObject.optJSONArray(key)
            if (jsonArray == null) {
                resultMap[key] = emptyArray()
                continue
            }

            val values = Array(jsonArray.length()) { index ->
                jsonArray.optInt(index, 0)
            }
            resultMap[key] = values
        }

        return resultMap
    }

    private fun writeStringToFile(context: Context, content: String, filename: String) {
        // Create a file in internal storage
        val file = File(context.filesDir, filename)
        // Write content to the file
        file.writeText(content)
    }
}
