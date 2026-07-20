package com.filledstacks.plugins.flutter_igolf_viewer

import android.content.Context
import com.filledstacks.plugins.flutter_igolf_viewer.channels.CourseViewerEventChannel
import com.filledstacks.plugins.flutter_igolf_viewer.FlutterIgolfViewer
import com.filledstacks.plugins.flutter_igolf_viewer.lifecycle.ViewerLifecycleRegistry
import io.flutter.plugin.common.BinaryMessenger
import io.flutter.plugin.common.EventChannel
import io.flutter.plugin.common.StandardMessageCodec
import io.flutter.plugin.platform.PlatformView
import io.flutter.plugin.platform.PlatformViewFactory
import io.flutter.plugin.platform.PlatformViewRegistry

internal class FlutterIgolfViewerFactory(
    private val messenger: BinaryMessenger,
    private val eventChannel: CourseViewerEventChannel,
    private val lifecycleRegistry: ViewerLifecycleRegistry
) : PlatformViewFactory(StandardMessageCodec.INSTANCE) {

    override fun create(context: Context, viewId: Int, args: Any?): PlatformView {
        val params = args as Map<String?, Any?>?
        return FlutterIgolfViewer(context, messenger, eventChannel, lifecycleRegistry, viewId, params)
    }

}
