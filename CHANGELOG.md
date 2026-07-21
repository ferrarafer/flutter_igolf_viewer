## 0.8.9

* feat(ios): Shot Vision parity — `drawShotVision`/`clearShotVision` method-channel handlers now work on iOS, backed by new `CourseRenderView setShotArcWithTargetLatitude:…`/`clearShotArc` in the vendored `IGolfViewer3D.xcframework` (build 2026.07.22.1; source in `FilledStacks/iGolfExampleiOS`). The arc is a rising triangle-strip ribbon from the golfer marker to the target with terrain-following elevation; unlike Android the target ground ring is part of the same native drawable, so clearing is inherently scoped to Shot Vision's own overlay. Same knobs and defaults as Android (`arcApexFraction` 0.16, `arcLineWidth`, `arcColor` 0xAARRGGBB).
* chore(ios): sync podspec version with the package version (was lagging at 0.8.7).

## 0.8.8

* feat(android): Shot Vision overlay — new `drawShotVision`/`clearShotVision` method-channel handlers draw a 3D rising flight-arc ribbon from the viewer's own golfer position up to a tapped GPS target, plus a ground ring at the target. Arc apex fraction, ribbon width (metres) and colour are passed per call so consumers can tune the look from Dart without a plugin rebuild. Backed by new native `SurfaceViewer.setShotArc`/`clearShotArc` in `iGolfViewerStandard.aar` (release build; source in `viewer_sample_app` — the arc is real 3D geometry in the main render pass, so unlike the custom-overlay dots/lines it is not baked flat into the terrain). Android-only; iOS returns `notImplemented`.

## 0.8.7

* fix(ios): smooth flyover heading updates by clamping rotation overshoot in the vendored `IGolfViewer3D.xcframework`

## 0.8.6

* feat(android): bring free-camera zoom to parity with iOS — `freeCamZoom` is now applied at init from creation params (defaults to 100 / no zoom-out when absent, matching iOS) and the runtime `setFreeCamZoom` method channel now routes to the underlying `Course3DViewer.viewer.setFreeCamZoomScale(...)` instead of silently no-oping
* refactor(android): extract pure `freeCamZoomScale(Int): Float` helper so the zoom→scale formula has a single source of truth (mirrors iOS `freeCamZoomScale(from:)`)
* fix(android): `setFreeCamZoom` now returns `FlutterError("INVALID_ARGS", "Missing zoom parameter", null)` when `zoom` is absent, matching iOS behavior (was silently defaulting to 100)
* test(android): unit tests for `freeCamZoomScale` (boundaries, midpoint, clamping, monotonicity)

## 0.6.4

* fix: prevent iOS `MissingPluginException` by registering placeholder method and event channels

## 0.6.3

* change: set autoAdvanceEnabled to false by default

## 0.6.2

* change: zoom out FreeCam to show the golfer above sheet

## 0.6.1

*  change: removes the draw call for the target

## 0.6.0

* Adds callback for user tap event

## 0.5.1

* change: modify logging level to HEADERS

## 0.5.0

* change: update viewer library
    * Replaced iGolf library with a Pinned version
    * Synced the navigation modes
* fix: update the example app
* fix: add Course GPS API support
    * Added getCourseGPSDetails method
    * Added getCourseGPSVectorDetails method
* fix: update FlutterIgolfViewer
    * Added setOnGPSDistancesUpdatedListener
    * Added option to pass parData, gpsDetails and vectorGpsObject to the Viewer to load the course without network request
* fix: update getCourseList on Network
    * When there is no internet connection, should return an error response with an empty courses list
* fix: update FlutterIgolfViewer class
    * Updated viewer listeners
    * Set isHoleRotationOnDynamicFrontBackEnabled to true
    * Refactored setCurrentHole method
* fix: update viewer library
* feat: replaced the images assets with Pinned assets
* feat: add distance to green on tap marker
* feat: add option to configure user location marker image

## 0.4.0

* fix: update channels
* fix: update FlutterIgolfViewer
    * Added a result call to every MethodChannel call because the completer needs to be completed
* feat: update FlutterIgolfViewer
    * Added isMetricUnits method
    * Added setMeasurementSystem method
    * Added isMetricUnits to creationParams to allow initialization in meters or yards
* fix: add navigationMode argument to setCurrentHole
* fix: add try and catch at onMethodCall
* refactor: update methods for navigation mode

## 0.3.0

* chore: clean code
* feat: add new methods to the network layer
* refactor: update architecture structure

## 0.2.0

* chore: add MIT license
* chore: add iGolfViewerStandard library
* chore: add Network module for iGolf communication
* chore: enable multidex support on example app
* chore: update Gradle version to 8.0.0
* feat: add PlatformView and MethodChannel
* feat: add new methods to the network layer
    * Added getCountryList
    * Added getStateList

## 0.1.0

* chore: init `flutter_igolf_viewer` plugin.
