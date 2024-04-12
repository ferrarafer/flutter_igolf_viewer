package com.filledstacks.plugins.flutter_igolf_viewer.channels

import com.filledstacks.plugins.flutter_igolf_viewer.network.Network
import com.filledstacks.plugins.flutter_igolf_viewer.network.request.CourseDetailsRequest
import com.filledstacks.plugins.flutter_igolf_viewer.network.request.CourseListRequest
import com.filledstacks.plugins.flutter_igolf_viewer.network.request.CourseScorecardListRequest
import com.filledstacks.plugins.flutter_igolf_viewer.network.request.CourseTeeDetailsRequest
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import io.flutter.plugin.common.BinaryMessenger

class CourseViewerMethodChannel(binaryMessenger: BinaryMessenger)
    //: MethodCallHandler
{
    /**
    private val channel: MethodChannel = MethodChannel(
        binaryMessenger,
        //"plugins.filledstacks/flutter_igolf_course_details_api"
        "plugins.filledstacks.flutter_igolf_viewer/course_viewer"
    )

    private var apiKey = ""
    private var secretKey = ""
    private var initialized = false

    init {
        channel.setMethodCallHandler(this)
    }

    fun cleanup() {
        channel.setMethodCallHandler(null)
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
    **/
}
