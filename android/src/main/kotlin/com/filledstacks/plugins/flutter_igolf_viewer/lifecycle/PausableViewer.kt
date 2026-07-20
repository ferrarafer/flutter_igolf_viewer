package com.filledstacks.plugins.flutter_igolf_viewer.lifecycle

/**
 * Rendering-lifecycle seam between the host activity and a live course viewer.
 *
 * The iGolf SDK renders through a [android.opengl.GLSurfaceView], whose
 * contract requires the host to forward the activity's pause/resume so the GL
 * thread releases its EGL surface (disconnecting the BufferQueue producer)
 * before the surface is torn down or recreated. Implemented by
 * [com.filledstacks.plugins.flutter_igolf_viewer.FlutterIgolfViewer]; kept as
 * an interface so [ViewerLifecycleRegistry] stays JVM-unit-testable.
 */
internal interface PausableViewer {
    fun pauseRendering()
    fun resumeRendering()
}
