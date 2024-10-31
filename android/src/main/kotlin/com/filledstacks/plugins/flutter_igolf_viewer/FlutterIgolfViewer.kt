package com.filledstacks.plugins.flutter_igolf_viewer

import android.content.Context
import android.graphics.Color
import android.location.Location
import android.view.View
import android.widget.TextView
import com.filledstacks.plugins.flutter_igolf_viewer.channels.CourseViewerEventChannel
import com.filledstacks.plugins.flutter_igolf_viewer.network.Network
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.l1inc.viewer.Course3DRenderer
import com.l1inc.viewer.Course3DRendererBase
import com.l1inc.viewer.Course3DRendererBase.NavigationMode
import com.l1inc.viewer.Course3DViewer
import com.l1inc.viewer.HoleWithinCourse
import com.l1inc.viewer.common.Viewer.CurrentHoleChangedListener
import com.l1inc.viewer.common.Viewer.HoleLoadingStateChangedListener
import io.flutter.plugin.common.BinaryMessenger
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.EventChannel
import io.flutter.plugin.platform.PlatformView
import java.io.File
import java.lang.RuntimeException

internal class FlutterIgolfViewer(
    context: Context,
    messenger: BinaryMessenger,
    eventChannel: CourseViewerEventChannel,
    id: Int,
    creationParams: Map<String?, Any?>?
) : PlatformView, MethodChannel.MethodCallHandler {

    private val course3DViewer: Course3DViewer

    private val methodChannel: MethodChannel = MethodChannel(
        messenger, "plugins.filledstacks.flutter_igolf_viewer/course_viewer_method"
    )

    private val event : CourseViewerEventChannel = eventChannel

    init {
        if (creationParams == null) {
            throw RuntimeException("API and Secret keys are required")
        }

        methodChannel.setMethodCallHandler(this)

        course3DViewer = Course3DViewer(context)

        course3DViewer.viewer.setOnGPSDistancesUpdatedListener { front, center, back, cursorInsideGreen ->
            eventChannel.sendEvent(mapOf(
                "event" to "GPS_DISTANCES_UPDATED",
                "front" to front,
                "center" to center,
                "back" to back,
                "cursorInsideGreen" to cursorInsideGreen
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
            creationParams.get("parData"),
            creationParams.get("gpsDetails"),
            creationParams.get("vectorGpsObject"),
            creationParams.get("golferIconIndex"),
            creationParams.get("isMetricUnits")
        )
    }

    override fun getView(): View {
        return course3DViewer
    }

    override fun dispose() {
        course3DViewer.viewer.onDestroy()
        methodChannel.setMethodCallHandler(null)
    }

    private fun loadCourseData(
        apiKey: Any?,
        secretKey: Any?,
        courseId: Any?,
        parData: Any?,
        gpsDetails: Any?,
        vectorGpsObject: Any?,
        golferIconIndex: Any?,
        isMetricUnits: Any?
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

        if (golferIconIndex !is Int) {
            throw RuntimeException("golferIconIndex should be Integer")
        }

        if (isMetricUnits !is Boolean) {
            throw RuntimeException("isMetricUnits should be Boolean")
        }

        if (parData != null && gpsDetails != null && vectorGpsObject != null) {
            val vectorGpsMap = HashMap<String?, String?>()
            vectorGpsMap[courseId] = vectorGpsObject as String
            vectorGpsMap["GPS_DETAILS"] = gpsDetails as String
            vectorGpsMap["COURSE_ID"] = courseId

            initAndShowViewer(convertParData(parData as String), vectorGpsMap, golferIconIndex, isMetricUnits)
            return
        }

        Network().loadCourseData(apiKey, secretKey, courseId) { parDataMap, vectorDataJsonMap ->
            initAndShowViewer(parDataMap, vectorDataJsonMap, golferIconIndex, isMetricUnits)
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
        golferIconIndex: Int,
        isMetricUnits: Boolean
    ) {
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

        course3DViewer.viewer.setCurrentHole(
            1,
            NavigationMode.Flyover,
            false,
            0
        )
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
        val gson = GsonBuilder()
            .registerTypeAdapter(object : TypeToken<Map<String?, Array<Int>?>>() {}.type, MapDeserializer())
            .create()
        val type = object : TypeToken<Map<String?, Array<Int>?>>() {}.type
        return gson.fromJson(jsonString, type)
    }

    private fun writeStringToFile(context: Context, content: String, filename: String) {
        // Create a file in internal storage
        val file = File(context.filesDir, filename)
        // Write content to the file
        file.writeText(content)
    }
}