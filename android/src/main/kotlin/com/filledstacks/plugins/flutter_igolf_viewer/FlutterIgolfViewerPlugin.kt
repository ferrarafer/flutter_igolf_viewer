package com.filledstacks.plugins.flutter_igolf_viewer

import android.util.Log
import androidx.annotation.NonNull
import com.filledstacks.plugins.flutter_igolf_viewer.channels.CourseDetailsApiMethodChannel
import com.filledstacks.plugins.flutter_igolf_viewer.channels.CourseViewerEventChannel
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import io.flutter.plugin.common.EventChannel

class FlutterIgolfViewerPlugin: FlutterPlugin {

  private lateinit var flutterIgolfViewerFactory: FlutterIgolfViewerFactory
  private lateinit var courseDetailsApiMethodChannel: CourseDetailsApiMethodChannel
  private lateinit var courseViewerEventChannel: CourseViewerEventChannel

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
      courseViewerEventChannel
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
}
