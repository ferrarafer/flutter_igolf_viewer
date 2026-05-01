package com.filledstacks.plugins.flutter_igolf_viewer

import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Unit tests for the pure-math helper used by the free-camera zoom feature.
 *
 * Run from `example/android/` with `./gradlew testDebugUnitTest`, or directly
 * from any IDE that supports JUnit (Android Studio, IntelliJ).
 */
internal class FlutterIgolfViewerPluginTest {

    @Test
    fun freeCamZoomScale_zoom100_returnsBaselineScale() {
        assertEquals(1.0f, freeCamZoomScale(100))
    }

    @Test
    fun freeCamZoomScale_zoom0_returnsMaxZoomOutScale() {
        assertEquals(5.0f, freeCamZoomScale(0))
    }

    @Test
    fun freeCamZoomScale_zoom50_returnsMidpointScale() {
        assertEquals(3.0f, freeCamZoomScale(50))
    }

    @Test
    fun freeCamZoomScale_clampsBelowZero() {
        // -10 clamps up to 0 → max zoom-out (5.0)
        assertEquals(freeCamZoomScale(0), freeCamZoomScale(-10))
    }

    @Test
    fun freeCamZoomScale_clampsAboveOneHundred() {
        // 250 clamps down to 100 → baseline (1.0)
        assertEquals(freeCamZoomScale(100), freeCamZoomScale(250))
    }

    @Test
    fun freeCamZoomScale_isMonotonicallyDecreasing() {
        // Higher zoom → smaller (more zoomed-in) scale.
        var previous = freeCamZoomScale(0)
        for (zoom in 1..100) {
            val current = freeCamZoomScale(zoom)
            assert(current <= previous) {
                "Expected scale to decrease as zoom increases, but $zoom produced $current vs previous $previous"
            }
            previous = current
        }
    }
}
