package com.filledstacks.plugins.flutter_igolf_viewer.lifecycle

import java.util.concurrent.CopyOnWriteArrayList

/**
 * Tracks live viewer instances and forwards the host activity's pause/resume
 * to each of them, exactly once per transition.
 *
 * Why this exists: the plugin's platform view wraps a GLSurfaceView that
 * renders into the Flutter texture's ImageReader (virtual-display platform
 * view). Without pause forwarding, the GL thread keeps its EGL surface —
 * the ImageReader BufferQueue's producer — attached across an activity
 * pause/resume. When the surface is then torn down and recreated, the new
 * EGL surface's connect is rejected ("connect: already connected"), the old
 * producer detaches afterwards, and the queue is left with no producer while
 * the render loop keeps dequeuing — freezing the Flutter raster thread
 * (5–28 s frame times, app unresponsive). Forwarding pause makes the GL
 * thread release the EGL surface *before* any teardown, so the resume-time
 * reconnect always finds a disconnected queue.
 *
 * State rules:
 * - Pause/resume are forwarded only on real transitions (duplicate pause or a
 *   resume without a prior pause are no-ops — a fresh GLSurfaceView already
 *   starts resumed).
 * - A viewer registered while the activity is paused (e.g. created under a
 *   runtime-permission dialog) is paused immediately so it can't race the
 *   resume-time reconnect.
 */
internal class ViewerLifecycleRegistry {
    private val viewers = CopyOnWriteArrayList<PausableViewer>()

    @Volatile
    private var isActivityPaused = false

    fun register(viewer: PausableViewer) {
        viewers.add(viewer)
        if (isActivityPaused) {
            viewer.pauseRendering()
        }
    }

    fun unregister(viewer: PausableViewer) {
        viewers.remove(viewer)
    }

    fun onActivityPaused() {
        if (isActivityPaused) return
        isActivityPaused = true
        viewers.forEach { it.pauseRendering() }
    }

    fun onActivityResumed() {
        if (!isActivityPaused) return
        isActivityPaused = false
        viewers.forEach { it.resumeRendering() }
    }
}
