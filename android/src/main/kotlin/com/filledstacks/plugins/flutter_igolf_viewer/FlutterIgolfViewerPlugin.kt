package com.filledstacks.plugins.flutter_igolf_viewer

import android.app.Activity
import android.app.Application
import android.os.Bundle
import android.util.Log
import androidx.annotation.NonNull
import com.filledstacks.plugins.flutter_igolf_viewer.channels.CourseDetailsApiMethodChannel
import com.filledstacks.plugins.flutter_igolf_viewer.channels.CourseViewerEventChannel
import com.filledstacks.plugins.flutter_igolf_viewer.lifecycle.ViewerLifecycleRegistry
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import io.flutter.plugin.common.EventChannel

class FlutterIgolfViewerPlugin: FlutterPlugin, ActivityAware {

  private lateinit var flutterIgolfViewerFactory: FlutterIgolfViewerFactory
  private lateinit var courseDetailsApiMethodChannel: CourseDetailsApiMethodChannel
  private lateinit var courseViewerEventChannel: CourseViewerEventChannel

  // Forwards the host activity's pause/resume to every live viewer so each
  // GLSurfaceView releases its EGL surface (the Flutter texture ImageReader's
  // BufferQueue producer) before the surface is torn down or recreated across
  // a pause/resume. Without this, the resumed renderer's reconnect is
  // rejected ("connect: already connected") and the queue is left with no
  // producer — freezing the Flutter raster thread.
  private val viewerLifecycleRegistry = ViewerLifecycleRegistry()

  private var boundActivity: Activity? = null
  private var activityLifecycleCallbacks: Application.ActivityLifecycleCallbacks? = null

  private var apiKey = ""
  private var secretKey = ""
  private var initialized = false

  override fun onAttachedToEngine(binding: FlutterPlugin.FlutterPluginBinding) {
    /// Register CourseDetailsApiMethodChannel implementation
    courseDetailsApiMethodChannel = CourseDetailsApiMethodChannel(binding.binaryMessenger)

    /// Register EventChannel implementation
    courseViewerEventChannel = CourseViewerEventChannel(binding.binaryMessenger)

    /// Register PlatformView
    flutterIgolfViewerFactory = FlutterIgolfViewerFactory(
      binding.binaryMessenger,
      courseViewerEventChannel,
      viewerLifecycleRegistry
    )
    binding.platformViewRegistry.registerViewFactory(
      "flutter_igolf_viewer",
      flutterIgolfViewerFactory
    )
  }

  override fun onDetachedFromEngine(binding: FlutterPlugin.FlutterPluginBinding) {
    // Unregister PlatformViewFactory
    //binding.platformViewRegistry.unregisterViewFactory("flutter_igolf_viewer")

    // Cleanup resources when the plugin is detached from the engine
    courseDetailsApiMethodChannel.cleanup()
    courseViewerEventChannel.cleanup()
  }

  override fun onAttachedToActivity(binding: ActivityPluginBinding) {
    observeActivityLifecycle(binding.activity)
  }

  override fun onDetachedFromActivityForConfigChanges() {
    stopObservingActivityLifecycle()
  }

  override fun onReattachedToActivityForConfigChanges(binding: ActivityPluginBinding) {
    observeActivityLifecycle(binding.activity)
  }

  override fun onDetachedFromActivity() {
    stopObservingActivityLifecycle()
  }

  private fun observeActivityLifecycle(activity: Activity) {
    stopObservingActivityLifecycle()
    boundActivity = activity
    val callbacks = object : Application.ActivityLifecycleCallbacks {
      override fun onActivityPaused(activity: Activity) {
        if (activity === boundActivity) viewerLifecycleRegistry.onActivityPaused()
      }

      override fun onActivityResumed(activity: Activity) {
        if (activity === boundActivity) viewerLifecycleRegistry.onActivityResumed()
      }

      override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}
      override fun onActivityStarted(activity: Activity) {}
      override fun onActivityStopped(activity: Activity) {}
      override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
      override fun onActivityDestroyed(activity: Activity) {}
    }
    activity.application.registerActivityLifecycleCallbacks(callbacks)
    activityLifecycleCallbacks = callbacks
  }

  private fun stopObservingActivityLifecycle() {
    val callbacks = activityLifecycleCallbacks ?: return
    boundActivity?.application?.unregisterActivityLifecycleCallbacks(callbacks)
    activityLifecycleCallbacks = null
    boundActivity = null
  }
}
