package com.filledstacks.plugins.flutter_igolf_viewer

import android.util.Log
import androidx.annotation.NonNull
import com.filledstacks.plugins.flutter_igolf_viewer.network.Network
import com.filledstacks.plugins.flutter_igolf_viewer.network.request.CourseDetailsRequest
import com.filledstacks.plugins.flutter_igolf_viewer.network.request.CourseListRequest
import com.filledstacks.plugins.flutter_igolf_viewer.network.request.CourseScorecardListRequest
import com.filledstacks.plugins.flutter_igolf_viewer.network.request.CourseTeeDetailsRequest
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result

/** FlutterIgolfViewerPlugin */
class FlutterIgolfViewerPlugin: FlutterPlugin, MethodCallHandler {
  /// The MethodChannel that will the communication between Flutter and native Android
  ///
  /// This local reference serves to register the plugin with the Flutter Engine and unregister it
  /// when the Flutter Engine is detached from the Activity
  private lateinit var channel : MethodChannel

  private var apiKey = ""
  private var secretKey = ""
  private var initialized = false

  override fun onAttachedToEngine(flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
    channel = MethodChannel(
      flutterPluginBinding.binaryMessenger,
      "plugins.filledstacks/flutter_igolf_course_details_api"
    )
    channel.setMethodCallHandler(this)

    /// PlatformView registration
    flutterPluginBinding.platformViewRegistry.registerViewFactory(
      "flutter_igolf_viewer",
      FlutterIgolfViewerFactory(flutterPluginBinding.binaryMessenger)
    )
  }

  override fun onMethodCall(call: MethodCall, result: Result) {
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
      else -> result.notImplemented()
    }

  }

  override fun onDetachedFromEngine(binding: FlutterPlugin.FlutterPluginBinding) {
    channel.setMethodCallHandler(null)
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
    // val stateId = call.argument("id_state") ?: -1
    // if (stateId == -1) {
    //   result.error("INVALIDARGS", "You must provide a valid state ID", null)
    //   return
    // }

    // val radius = call.argument("radius") ?: ""
    // if (radius.length == 0) {
    //   result.error("INVALIDARGS", "You must provide a valid radius", null)
    //   return
    // }

    // val latitude = call.argument("referenceLatitude") ?: -1.0
    // if (latitude == -1.0) {
    //   result.error("INVALIDARGS", "You must provide a valid latitude", null)
    //   return
    // }

    // val longitude = call.argument("referenceLongitude") ?: -1.0
    // if (longitude == -1.0) {
    //   result.error("INVALIDARGS", "You must provide a valid longitude", null)
    //   return
    // }
    
    // val courseListRequest = CourseListRequest(active = active, stateId = stateId)
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
    // val coursesIds: Array<String> = call.argument("id_courseArray") ?: emptyArray()
    // val coursesIds = call.argument("id_courseArray") ?: 
    // if (coursesIds.isEmpty) {
    //   result.error("INVALIDARGS", "You must provide valid course ID", null)
    //   return
    // }

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
