package com.filledstacks.plugins.flutter_igolf_viewer

import android.content.Context
import android.graphics.Color
import android.location.Location
import android.view.View
import android.widget.TextView
import com.filledstacks.plugins.flutter_igolf_viewer.network.Network
import com.l1inc.viewer.Course3DRenderer
import com.l1inc.viewer.Course3DRendererBase
import com.l1inc.viewer.Course3DViewer
import io.flutter.plugin.common.BinaryMessenger
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.platform.PlatformView

internal class FlutterIgolfViewer(
    context: Context,
    messenger: BinaryMessenger,
    id: Int,
    creationParams: Map<String?, Any?>?
) : PlatformView, MethodChannel.MethodCallHandler {
    private val course3DViewer: Course3DViewer
    private val methodChannel: MethodChannel = MethodChannel(
        messenger, "plugins.filledstacks/flutter_igolf_viewer"
    )

    init {
        if (creationParams == null) {
            throw RuntimeException("API and Secret keys are required")
        }

        methodChannel.setMethodCallHandler(this)

        course3DViewer = Course3DViewer(context)

        course3DViewer.viewer.setCurrentCourseChangedListener {
            println("=========== setCurrentCourseChangedListener FINISHED ==========")
            println("=========== setCurrentCourseChangedListener FINISHED ==========")
            println("=========== setCurrentCourseChangedListener FINISHED ==========")
        }

        course3DViewer.viewer.setFrameRenderListener {
            println("=========== setFrameRenderListener FINISHED ==========")
            println("=========== setFrameRenderListener FINISHED ==========")
            println("=========== setFrameRenderListener FINISHED ==========")
        }

        loadCourseData(
            creationParams.get("apiKey"),
            creationParams.get("secretKey"),
            creationParams.get("courseId")
        )
    }

    override fun onMethodCall(call: MethodCall, result: MethodChannel.Result) {
        when (call.method) {
            "getCurrentHole" -> getCurrentHole(call, result)
            "getNavigationMode" -> getNavigationMode(call, result)
            "getNumHoles" -> getNumHoles(call, result)
            "getPendingNavigationMode" -> getPendingNavigationMode(call, result)
            "setCurrentHole" -> setCurrentHole(call, result)
            "set2DNavigationMode" -> set2DNavigationMode(call, result)
            "set3DNavigationMode" -> set3DNavigationMode(call, result)
            "setFlyoverNavigationMode" -> setFlyoverNavigationMode(call, result)
            "setNextNavigationMode" -> setNextNavigationMode(call, result)
            "setCartLocationVisible" -> setCartLocationVisible(call, result)
            "setTeeBoxAsCurrentLocation" -> setTeeBoxAsCurrentLocation(call, result)
            "setCurrentLocationGPS" -> setCurrentLocationGPS(call, result)
            "setFrameRenderListener" -> setFrameRenderListener(call, result)
            else -> result.notImplemented()
        }
    }

    override fun getView(): View {
        return course3DViewer
    }

    override fun dispose() {
        course3DViewer.viewer.onDestroy()
    }

    private fun loadCourseData(apiKey: Any?, secretKey: Any?, courseId: Any?) {
        if (apiKey !is String || apiKey.isBlank()) {
            throw RuntimeException("API key is required")
        }

        if (secretKey !is String || secretKey.isBlank()) {
            throw RuntimeException("Secret key is required")
        }

        if (courseId !is String || courseId.isBlank()) {
            throw RuntimeException("Course ID is required")
        }

        Network().loadCourseData(apiKey, secretKey, courseId) { parDataMap, vectorDataJsonMap ->
            initAndShowViewer(parDataMap, vectorDataJsonMap)
        }
    }

    private fun initAndShowViewer(
        parDataMap: Map<String?, Array<Int>?>,
        vectorDataJsonMap: HashMap<String?, String?>
    ) {
        course3DViewer.viewer.init(
            vectorDataJsonMap,
            false,
            true,
            parDataMap,
            null,
            false,
            null
        )
        course3DViewer.viewer.setCurrentHole(
            1,
            Course3DRendererBase.NavigationMode.NavigationMode2D,
            false,
            0
        )
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

    private fun setCurrentHole(call: MethodCall, result: MethodChannel.Result) {
        val hole = call.argument<Int>("hole") ?: -1
        val initialTeeBox = call.argument<Int>("initialTeeBox") ?: null
        println(hole)
        println(initialTeeBox)
        course3DViewer.viewer.setCurrentHole(
            hole,
            Course3DRendererBase.NavigationMode.NavigationMode2D,
            true,
            initialTeeBox,
        )
        result.success("Set current hole to $hole")
    }

    private fun setNavigationMode(call: MethodCall, result: MethodChannel.Result) {
        val mode = call.argument<String>("mode")
        result.success("Result from native side for $mode")
/**
        course3DViewer.viewer.setNavigationMode(Course3DRenderer.DEFAULT_OVERALL_MODE)

        course3DViewer.viewer.setCurrentHole(
            2,
            Course3DRendererBase.NavigationMode.NavigationMode2D,
            false,
            2
        )
        **/
    }

    private fun set2DNavigationMode(call: MethodCall, result: MethodChannel.Result) {
        course3DViewer.viewer.setNavigationMode(Course3DRendererBase.NavigationMode.NavigationMode2D)
    }

    private fun set3DNavigationMode(call: MethodCall, result: MethodChannel.Result) {
        course3DViewer.viewer.setNavigationMode(Course3DRendererBase.NavigationMode.OverallHole3)
    }

    private fun setFlyoverNavigationMode(call: MethodCall, result: MethodChannel.Result) {
        course3DViewer.viewer.setNavigationMode(Course3DRendererBase.NavigationMode.Flyover)
    }

    private fun setNextNavigationMode(call: MethodCall, result: MethodChannel.Result) {
        val nextMode = course3DViewer.viewer.getPendingNavigationMode()
        course3DViewer.viewer.setNavigationMode(nextMode)
    }

    private fun setCartLocationVisible(call: MethodCall, result: MethodChannel.Result) {
        val isCartLocationVisible = call.argument<Boolean>("cartLocationVisible") ?: false
        course3DViewer.viewer.setCartLocationVisible(isCartLocationVisible)
    }

    private fun setTeeBoxAsCurrentLocation(call: MethodCall, result: MethodChannel.Result) {
        val selectedTeeBox = call.argument<Int>("selectedTeeBox") ?: 0
        course3DViewer.viewer.setTeeboxAsCurrentLocation(selectedTeeBox)
    }

    private fun setCurrentLocationGPS(call: MethodCall, result: MethodChannel.Result) {
        val location = Location("")
        location.setLatitude(call.argument<Double>("latitude") ?: 0.0)
        location.setLongitude(call.argument<Double>("longitude") ?: 0.0)
        val updateCameraPos = call.argument<Boolean>("updateCameraPos") ?: false
        course3DViewer.viewer.setCurrentLocationGPS(
            location,
            updateCameraPos,
        );
    }

    /// Listener, which will be called when frame finished rendering.
    private fun setFrameRenderListener(call: MethodCall, result: MethodChannel.Result) {
        course3DViewer.viewer.setFrameRenderListener {
            println("=========== setFrameRenderListener FINISHED ==========")
            println("=========== setFrameRenderListener FINISHED ==========")
            println("=========== setFrameRenderListener FINISHED ==========")
        }
    }
}
