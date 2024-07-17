package com.filledstacks.plugins.flutter_igolf_viewer

import android.content.Context
import android.graphics.Color
import android.location.Location
import android.view.View
import android.widget.TextView
import com.filledstacks.plugins.flutter_igolf_viewer.channels.CourseViewerEventChannel
import com.filledstacks.plugins.flutter_igolf_viewer.network.Network
import com.l1inc.viewer.Course3DRenderer
import com.l1inc.viewer.Course3DRendererBase
import com.l1inc.viewer.Course3DViewer
import io.flutter.plugin.common.BinaryMessenger
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.EventChannel
import io.flutter.plugin.platform.PlatformView
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

        course3DViewer.viewer.setCurrentCourseChangedListener {
            eventChannel.sendEvent("CURRENT_COURSE_CHANGED")
        }

//        course3DViewer.viewer.setCurrentHoleChangedListener {
//            eventChannel.sendEvent("CURRENT_HOLE_CHANGED")
//        }

        course3DViewer.viewer.setFlyoverFinishListener {
            eventChannel.sendEvent("FLYOVER_FINISHED")
        }

        course3DViewer.viewer.setGreenPositionChangeListener { greenPosition ->
            eventChannel.sendEvent("GREEN_POSITION_CHANGED")
        }

//        course3DViewer.viewer.setHoleLoadingStateChangedListener {
//            eventChannel.sendEvent("HOLE_LOADING_STATE_CHANGED")
//        }

        course3DViewer.viewer.setNavigationModeChangedListener {
            eventChannel.sendEvent("NAVIGATION_MODE_CHANGED")
        }

        loadCourseData(
            creationParams.get("apiKey"),
            creationParams.get("secretKey"),
            creationParams.get("courseId"),
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

    private fun loadCourseData(apiKey: Any?, secretKey: Any?, courseId: Any?, isMetricUnits: Any?) {
        if (apiKey !is String || apiKey.isBlank()) {
            throw RuntimeException("API key is required")
        }

        if (secretKey !is String || secretKey.isBlank()) {
            throw RuntimeException("Secret key is required")
        }

        if (courseId !is String || courseId.isBlank()) {
            throw RuntimeException("Course ID is required")
        }

        if (isMetricUnits !is Boolean) {
            throw RuntimeException("isMetricUnits should be Boolean")
        }

        Network().loadCourseData(apiKey, secretKey, courseId) { parDataMap, vectorDataJsonMap ->
            initAndShowViewer(parDataMap, vectorDataJsonMap, isMetricUnits)
            event.sendEvent(vectorDataJsonMap)
        }
    }

    private fun initAndShowViewer(
        parDataMap: Map<String?, Array<Int>?>,
        vectorDataJsonMap: HashMap<String?, String?>,
        isMetricUnits: Boolean
    ) {
        course3DViewer.viewer.init(
            vectorDataJsonMap,
            false,
            isMetricUnits,
            parDataMap,
            null,
            true,
            null
        )
        course3DViewer.viewer.setCurrentHole(
            1,
            Course3DRendererBase.NavigationMode.FollowGolfer,
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

        var mode = Course3DRendererBase.NavigationMode.NavigationMode2D
        if (navigationMode == "overallHole") {
            mode = Course3DRendererBase.NavigationMode.OverallHole
        }

        course3DViewer.viewer.setCurrentHole(
            hole,
            mode,
            false,
            initialTeeBox,
        )
        result.success("Set current hole to $hole with NavigationMode $navigationMode")
    }

    private fun setNavigationMode(call: MethodCall, result: MethodChannel.Result) {
        val mode = call.argument<String>("mode")
        val typedMode = when (mode) {
            "flyover" -> Course3DRendererBase.NavigationMode.Flyover
            "flyoverPause" -> Course3DRendererBase.NavigationMode.FlyoverPause
            "followGolfer" -> Course3DRendererBase.NavigationMode.FollowGolfer
            "freeCam" -> Course3DRendererBase.NavigationMode.FreeCam
            "freeCamCart" -> Course3DRendererBase.NavigationMode.FreeCamCart
            "greenView2D" -> Course3DRendererBase.NavigationMode.GreenView2D
            "greenView3D" -> Course3DRendererBase.NavigationMode.GreenView3D
            "overallHole" -> Course3DRendererBase.NavigationMode.OverallHole
            "navigationMode2D" -> Course3DRendererBase.NavigationMode.NavigationMode2D
            else -> {
                Course3DRendererBase.NavigationMode.NavigationMode2D
            }
        }
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
}