package com.filledstacks.plugins.flutter_igolf_viewer

import android.content.Context
import com.filledstacks.plugins.flutter_igolf_viewer.FlutterIgolfViewer
import io.flutter.plugin.common.StandardMessageCodec
import io.flutter.plugin.platform.PlatformView
import io.flutter.plugin.platform.PlatformViewFactory

class FlutterIgolfViewerFactory : PlatformViewFactory(StandardMessageCodec.INSTANCE) {
    override fun create(context: Context, viewId: Int, args: Any?): PlatformView {
        val creationParams = args as Map<String?, Any?>?
        return FlutterIgolfViewer(context, viewId, creationParams)
    }
}
