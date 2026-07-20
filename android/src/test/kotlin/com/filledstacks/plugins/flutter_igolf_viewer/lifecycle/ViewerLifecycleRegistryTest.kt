package com.filledstacks.plugins.flutter_igolf_viewer.lifecycle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

/**
 * Unit tests for the activity pause/resume forwarding state machine that keeps
 * the iGolf GLSurfaceView's EGL surface lifecycle in sync with the host
 * activity. Regression coverage for the Android freeze where the renderer
 * reconnected to the Flutter texture's ImageReader while the old producer was
 * still attached ("connect: already connected" → "no connected producer").
 */
internal class ViewerLifecycleRegistryTest {

    private class FakeViewer : PausableViewer {
        val events = mutableListOf<String>()

        override fun pauseRendering() {
            events.add("pause")
        }

        override fun resumeRendering() {
            events.add("resume")
        }
    }

    @Test
    fun activityPause_forwardsPauseToEveryRegisteredViewer() {
        val registry = ViewerLifecycleRegistry()
        val first = FakeViewer()
        val second = FakeViewer()
        registry.register(first)
        registry.register(second)

        registry.onActivityPaused()

        assertEquals(listOf("pause"), first.events)
        assertEquals(listOf("pause"), second.events)
    }

    @Test
    fun activityResume_afterPause_forwardsResume() {
        val registry = ViewerLifecycleRegistry()
        val viewer = FakeViewer()
        registry.register(viewer)

        registry.onActivityPaused()
        registry.onActivityResumed()

        assertEquals(listOf("pause", "resume"), viewer.events)
    }

    @Test
    fun activityResume_withoutPriorPause_isNoOp() {
        // The first onActivityResumed after startup must not touch the viewer:
        // a fresh GLSurfaceView is already in the resumed state.
        val registry = ViewerLifecycleRegistry()
        val viewer = FakeViewer()
        registry.register(viewer)

        registry.onActivityResumed()

        assertEquals(emptyList(), viewer.events)
    }

    @Test
    fun duplicatePause_forwardsOnlyOnce() {
        val registry = ViewerLifecycleRegistry()
        val viewer = FakeViewer()
        registry.register(viewer)

        registry.onActivityPaused()
        registry.onActivityPaused()

        assertEquals(listOf("pause"), viewer.events)
    }

    @Test
    fun registerWhilePaused_pausesTheNewViewerImmediately() {
        // A platform view can be created while the activity is paused (e.g. a
        // runtime permission dialog is up). It must join in the paused state so
        // its GL thread doesn't race the resumed reconnect.
        val registry = ViewerLifecycleRegistry()
        registry.onActivityPaused()

        val viewer = FakeViewer()
        registry.register(viewer)

        assertEquals(listOf("pause"), viewer.events)

        registry.onActivityResumed()
        assertEquals(listOf("pause", "resume"), viewer.events)
    }

    @Test
    fun registerWhilePaused_whenInitialPauseFails_doesNotRetainViewer() {
        val registry = ViewerLifecycleRegistry()
        registry.onActivityPaused()
        var resumeCount = 0
        val viewer = object : PausableViewer {
            override fun pauseRendering() {
                throw IllegalStateException("pause failed")
            }

            override fun resumeRendering() {
                resumeCount++
            }
        }

        assertFailsWith<IllegalStateException> {
            registry.register(viewer)
        }

        registry.onActivityResumed()

        assertEquals(0, resumeCount)
    }

    @Test
    fun unregisteredViewer_receivesNoFurtherEvents() {
        val registry = ViewerLifecycleRegistry()
        val viewer = FakeViewer()
        registry.register(viewer)
        registry.unregister(viewer)

        registry.onActivityPaused()
        registry.onActivityResumed()

        assertEquals(emptyList(), viewer.events)
    }

    @Test
    fun pauseResumeCycle_repeats() {
        val registry = ViewerLifecycleRegistry()
        val viewer = FakeViewer()
        registry.register(viewer)

        registry.onActivityPaused()
        registry.onActivityResumed()
        registry.onActivityPaused()
        registry.onActivityResumed()

        assertEquals(listOf("pause", "resume", "pause", "resume"), viewer.events)
    }
}
