package com.filledstacks.plugins.flutter_igolf_viewer

import android.content.Context
import android.graphics.Color
import android.view.View
import android.widget.TextView
import com.filledstacks.plugins.flutter_igolf_viewer.network.Network
import com.l1inc.viewer.Course3DRenderer
import com.l1inc.viewer.Course3DRendererBase
import com.l1inc.viewer.Course3DViewer
import io.flutter.plugin.platform.PlatformView

internal class FlutterIgolfViewer(context: Context, id: Int, creationParams: Map<String?, Any?>?) : PlatformView {
    private val courseId = "x61wljOp5MS7"
    private val course3DViewer: Course3DViewer

    init {
        if (creationParams == null) {
            throw RuntimeException("API and Secret keys are required")
        }
        course3DViewer = Course3DViewer(context)
        loadCourseData(creationParams.get("apiKey"), creationParams.get("secretKey"))
    }

    override fun getView(): View {
        return course3DViewer
    }

    override fun dispose() {
        course3DViewer.viewer.onDestroy()
    }

    private fun loadCourseData(apiKey: Any?, secretKey: Any?) {
        if (apiKey !is String || apiKey.isBlank()) {
            throw RuntimeException("API key is required")
        }

        if (secretKey !is String || secretKey.isBlank()) {
            throw RuntimeException("Secret key is required")
        }

        Network().loadCourseData(apiKey, secretKey, courseId) { parDataMap, vectorDataJsonMap ->
            initAndShowViewer(parDataMap, vectorDataJsonMap)
        }
    }

    private fun initAndShowViewer(
        parDataMap: Map<String?, Array<Int>?>,
        vectorDataJsonMap: HashMap<String?, String?>
    ) {
        course3DViewer.visibility = View.VISIBLE
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
            Course3DRendererBase.NavigationMode.Flyover,
            false,
            0
        )
    }
}
