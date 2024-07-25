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

class CourseDetailsApiMethodChannel(binaryMessenger: BinaryMessenger) : MethodCallHandler {
    private val channel: MethodChannel = MethodChannel(
        binaryMessenger,
        "plugins.filledstacks.flutter_igolf_course/course_details_api"
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
        if (call.method != "initialize" && !initialized) {
            throw RuntimeException("Flutter iGolf Viewer plugin is not initialized.")
        }

        when (call.method) {
            "initialize" -> onInitialize(call, result)
            "getCountryList" -> onGetCountryList(call, result)
            "getCourseDetails" -> onGetCourseDetails(call, result)
            "getCourseList" -> onGetCourseList(call, result)
            "getCourseTeeDetails" -> onGetCourseTeeDetails(call, result)
            "getCourseScorecardDetails" -> onGetCourseScorecardDetails(call, result)
            "getCourseScorecardList" -> onGetCourseScorecardList(call, result)
            "getStateList" -> onGetStateList(call, result)
            "getTypedCourseList" -> onGetTypedCourseList(call, result)
            /// Course GPS API
            "getCourseGPSDetails" -> onGetCourseGPSDetails(call, result)
            "getCourseGPSVectorDetails" -> onGetCourseGPSVectorDetails(call, result)
//            "getCourseElevationDataDetails" -> onGetCourseElevationDataDetails(call, result)
//            "getCourseElevationDataOneMeterDetails" -> onGetCourseElevationDataOneMeterDetails(call, result)
//            "getCourseElevationDataThreeMeterDetails" -> onGetCourseElevationDataThreeMeterDetails(call, result)
            else -> result.notImplemented()
        }

    }

    private fun onInitialize(call: MethodCall, result: MethodChannel.Result) {
        apiKey = call.argument("apiKey") ?: ""
        secretKey = call.argument("secretKey") ?: ""
        if (apiKey.length == 0 || secretKey.length == 0) {
            result.error("INVALIDARGS", "You must provide valid api and secret keys", null)
            return
        }

        initialized = true
        result.success("Flutter iGolf Viewer plugin initialized")
    }

    private fun onGetCountryList(call: MethodCall, result: MethodChannel.Result ) {
        val continentId = call.argument<String>("id_continent") ?: "All"
        if (continentId.length == 0) {
            result.error("INVALIDARGS", "You must provide valid continent ID", null)
            return
        }
        Network().getCountryList(apiKey, secretKey, continentId) { countryList ->
            result.success(countryList)
        }
    }

    private fun onGetCourseDetails(call: MethodCall, result: MethodChannel.Result ) {
        val courseId = call.argument("id_course") ?: ""
        if (courseId.length == 0) {
            result.error("INVALIDARGS", "You must provide valid course ID", null)
            return
        }

        val courseDetailsRequest = CourseDetailsRequest(
            countryFormat = call.argument("countryFormat"),
            courseId = courseId,
            detailLevel = call.argument("detailLevel"),
            stateFormat = call.argument("stateFormat")
        )

        Network().getCourseDetails(apiKey, secretKey, courseDetailsRequest) { courseDetails ->
            result.success(courseDetails)
        }
    }

    private fun onGetCourseList(call: MethodCall, result: MethodChannel.Result ) {
        val active = call.argument("active") ?: 1
        val courseListRequest = CourseListRequest(
            active = active,
            city = call.argument("city"),
            courseName = call.argument("courseName"),
            radius = call.argument("radius"),
            referenceLatitude = call.argument("referenceLatitude"),
            referenceLongitude = call.argument("referenceLongitude"),
            zipcode = call.argument("zipcode")
        )

        Network().getCourseList(apiKey, secretKey, courseListRequest) { courseList ->
            result.success(courseList)
        }
    }

    private fun onGetCourseTeeDetails(call: MethodCall, result: MethodChannel.Result ) {
        val courseId = call.argument("id_course") ?: ""
        if (courseId.length == 0) {
            result.error("INVALIDARGS", "You must provide valid course ID", null)
            return
        }

        val courseTeeDetailsRequest = CourseTeeDetailsRequest(
            courseId = courseId,
            detailLevel = call.argument("detailLevel"),
        )

        Network().getCourseTeeDetails(apiKey, secretKey, courseTeeDetailsRequest) { courseTeeDetails ->
            result.success(courseTeeDetails)
        }
    }

    private fun onGetCourseScorecardDetails(call: MethodCall, result: MethodChannel.Result ) {
        val courseId = call.argument("id_course") ?: ""
        if (courseId.length == 0) {
            result.error("INVALIDARGS", "You must provide valid course ID", null)
            return
        }

        Network().getCourseScorecardDetails(apiKey, secretKey, courseId) { courseScorecardDetails ->
            result.success(courseScorecardDetails)
        }
    }

    private fun onGetCourseScorecardList(call: MethodCall, result: MethodChannel.Result ) {
        val courseScorecardListRequest = CourseScorecardListRequest(
            coursesIds = call.argument("id_courseArray") ?: ArrayList(),
            courseName = call.argument("courseName") ?: 1,
        )

        Network().getCourseScorecardList(apiKey, secretKey, courseScorecardListRequest) { courseScorecardList ->
            result.success(courseScorecardList)
        }
    }

    private fun onGetStateList(call: MethodCall, result: MethodChannel.Result ) {
        val countryId = call.argument<Int>("id_country") ?: -1
        if (countryId == -1) {
            result.error("INVALIDARGS", "You must provide valid country ID", null)
            return
        }
        Network().getStateList(apiKey, secretKey, countryId) { stateList ->
            result.success(stateList)
        }
    }

    private fun onGetCourseGPSDetails(call: MethodCall, result: MethodChannel.Result ) {
        val courseId = call.argument("id_course") ?: ""
        if (courseId.length == 0) {
            result.error("INVALIDARGS", "You must provide valid course ID", null)
            return
        }

        Network().loadGPSdetails(apiKey, secretKey, courseId) { courseGpsDetails ->
            result.success(courseGpsDetails)
        }
    }

    private fun onGetCourseGPSVectorDetails(call: MethodCall, result: MethodChannel.Result ) {
        val courseId = call.argument("id_course") ?: ""
        if (courseId.length == 0) {
            result.error("INVALIDARGS", "You must provide valid course ID", null)
            return
        }

        Network().loadVectorDetails(apiKey, secretKey, courseId) { courseGpsVectorDetails ->
            result.success(courseGpsVectorDetails)
        }
    }

    private fun onGetTypedCourseList(call: MethodCall, result: MethodChannel.Result ) {
        val active = call.argument("active") ?: 1
        val zipcode = call.argument("zipcode") ?: ""
        if (zipcode.length == 0) {
            result.error("INVALIDARGS", "You must provide a zipcode", null)
            return
        }

        val courseListRequest = CourseListRequest(active = active, zipcode = zipcode)
        Network().getTypedCourseList(apiKey, secretKey, courseListRequest) { courseList ->
            result.success("${courseList.totalCourses}")
        }
    }
}
