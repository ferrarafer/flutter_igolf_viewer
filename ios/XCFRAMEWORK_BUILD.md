# IGolfViewer3D.xcframework — Build Provenance

This file records how the bundled `IGolfViewer3D.xcframework` was produced so future
rebuilds are reproducible. Update it whenever the xcframework binaries are replaced.

## Upstream source

- Repo: https://github.com/FilledStacks/iGolfExampleiOS
- Branch: `main`
- Base commit: `33c7d740b9c3f5433672c446c69c56578b84ee28`
  ("Gets the course loaded and the user tap events working in the viewer")
- Working tree at build time also contained uncommitted modifications to:
  `CourseRenderView.h/.m`, `CourseRenderViewLoader.h/.m`,
  `drawing/Callouts.h/.m`, `drawing/Camera.h/.m`, `drawing/Cart.m`,
  `drawing/CartPositionMarker.m`, `drawing/DistanceMarker.m`,
  `drawing/DistanceMarker3D.m`, `extensions/GLKTextureInfo+Extensions.h/.m`,
  `helpers/IGolfViewer3DResources.h/.m`. These contain the `freeCamZoomScale`
  and tap-to-move (`Callouts setTargetPosition:`) implementations. For full
  reproducibility, commit these changes upstream and update the SHA above.

## Build configuration

- Build script: `build_framework.sh` in the upstream repo
- Xcode scheme: `IGolfViewer3D-Enterprise`
- Configuration: `Enterprise-Release`
- Architectures: `arm64` (device, iphoneos SDK), `x86_64` (simulator)
- Flags: `BUILD_LIBRARY_FOR_DISTRIBUTION=YES`
- Output: `./build/IGolfViewer3D.xcframework` (then copied into `ios/` of this plugin)

## Bundled feature symbols

The current binary contains symbols for both PR #17 and PR #18:

- `-[CourseRenderView setFreeCamZoomScale:]`, `-[Camera setFreeCamZoomScale:]`,
  `-[Camera applyFreeCamZoomScale]` — free-cam zoom (PR #17)
- `-[Callouts setTargetPosition:]`, `-[GreenViewCursor setPosition:]` —
  2D tap-to-move target (PR #18)

Verify with:

```sh
nm -arch arm64 ios/IGolfViewer3D.xcframework/ios-arm64/IGolfViewer3D.framework/IGolfViewer3D \
  | grep -E "setFreeCamZoomScale|setTargetPosition"
```

## Platform parity

- Free-cam zoom: implemented on **iOS and Android** (Android handler in
  `android/.../FlutterIgolfViewer.kt`, see PR #17).
- 2D tap-to-move: implemented on **iOS only**. Android parity is intentionally
  deferred — Android's viewer uses a different gesture pipeline and the work
  was not in scope for this release. Track in a follow-up before promoting
  tap-to-move to a documented cross-platform API.
