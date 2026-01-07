# iOS Viewer Integration - Change Proposal Summary

## Quick Links
- [proposal.md](./proposal.md) - Why and what changes
- [design.md](./design.md) - Technical decisions and architecture
- [tasks.md](./tasks.md) - Implementation checklist (9 sections, 60+ tasks)
- [specs/ios-viewer/spec.md](./specs/ios-viewer/spec.md) - Requirements and scenarios

## Overview
This change proposal adds native iOS viewer integration to achieve feature parity with the existing Android platform view implementation. The integration brings 3D golf course visualization, GPS mapping, and interactive course navigation to iOS devices.

## Key Components

### 1. IGolfViewer3D.xcframework
- **Source**: `/Users/danemackier/Code/clients/pinnedgolf/viewers/CourseViewerDemo/IGolfViewer3D.xcframework`
- **Architecture**: XCFramework with device (arm64) and simulator (x86_64) slices
- **Size**: ~5-10MB including textures
- **Requirements**: iOS 15.6+, OpenGL ES, GLKit

### 2. Texture Assets (83 files)
- **Location**: `ios/Assets/3DViewer/textures/`
- **Contents**:
  - 82 PNG textures (terrain, vegetation, markers, UI elements)
  - 1 TTF font (sans.ttf)
- **Categories**: 3D view textures (v3d_*), 2D view textures (v2d_*), environment, markers

### 3. New Swift Classes
- **FlutterIgolfViewerFactory**: Implements FlutterPlatformViewFactory for creating platform views
- **FlutterIgolfView**: Implements FlutterPlatformView wrapper around CourseRenderView

### 4. Updated Classes
- **SwiftFlutterIgolfViewerPlugin**: Register platform view factory, remove placeholder nil returns

### 5. Configuration Files
- **flutter_igolf_viewer.podspec**: Framework embedding, resource bundles, dependencies

## Reference Implementation
The CourseViewerDemo app at `/Users/danemackier/Code/clients/pinnedgolf/viewers/CourseViewerDemo` provides a working reference for:
- Framework integration patterns
- Texture bundling and loading
- CourseRenderViewLoader configuration
- Async preload pattern
- View lifecycle management

## Technical Highlights

### Async Preload Pattern
```swift
let loader = CourseRenderViewLoader(apiKey: key, secretKey: secret, idCourse: id)
loader.measurementSystem = .metric
loader.initialNavigationMode = .modeFreeCam
// ... configure other properties

loader.preload { [weak self] in
    self?.renderView.load(with: loader)
} errorHandler: { error in
    print("Failed to load course: \(error?.localizedDescription ?? "unknown")")
}
```

### Platform View Architecture
```
Flutter Widget (UiKitView)
    ↓
FlutterPlatformViewFactory
    ↓ creates
FlutterIgolfView (FlutterPlatformView)
    ↓ wraps
CourseRenderView (IGolfViewer3D)
    ↓ uses
CourseRenderViewLoader
```

### Parameter Mapping
| Flutter | iOS Loader | Type |
|---------|-----------|------|
| apiKey | applicationAPIKey | String |
| secretKey | applicationSecretKey | String |
| courseId | idCourse | String |
| startingHole | initialHole | Int |
| initialTeeBox | initialTeeBox | Int |
| isMetricUnits | measurementSystem | Enum (.metric/.imperial) |

## Implementation Phases

1. **Framework & Asset Setup** (Tasks 1-2)
   - Copy framework and textures
   - Configure podspec
   - Verify pod lib lint

2. **Platform View Implementation** (Tasks 3-4)
   - Create factory and view classes
   - Implement basic view lifecycle

3. **Loader Integration** (Tasks 5-6)
   - Configure CourseRenderViewLoader
   - Map Flutter parameters to native properties

4. **Testing & Validation** (Task 7)
   - Device and simulator testing
   - Memory leak detection
   - Error handling validation

5. **Documentation & Cleanup** (Tasks 8-9)
   - Update README and CHANGELOG
   - Code documentation
   - Remove placeholders

## Validation Checklist

Before marking this change as complete:

- [ ] `openspec validate add-ios-viewer-integration --strict` passes
- [ ] All tasks in tasks.md are marked `[x]`
- [ ] iOS example app displays 3D course viewer
- [ ] Works on both device and simulator
- [ ] No memory leaks (Instruments validation)
- [ ] Pod lib lint passes without warnings
- [ ] Documentation updated (README, CHANGELOG)

## Known Limitations & Open Questions

### Open Questions (from design.md)
1. Does IGolfViewer3D support custom par data injection?
2. What is golferIconIndex used for in iOS framework?
3. Does framework automatically handle texture path resolution in plugin context?
4. Are there framework debugging/logging modes?
5. Should event channel integration match Android implementation?

### Limitations
- iOS 15.6+ only (excludes ~5% of iOS users)
- Framework uses deprecated OpenGL ES (works on iOS 18, but long-term risk)
- ~5-10MB bundle size increase
- Some Flutter parameters may not have iOS framework equivalents (TBD during implementation)

## Next Steps

1. **Review**: Team review of proposal, design, and spec deltas
2. **Clarify**: Resolve open questions with iGolf SDK documentation or support
3. **Approve**: Get stakeholder approval before implementation
4. **Implement**: Follow tasks.md sequentially
5. **Test**: Complete validation checklist
6. **Archive**: After deployment, archive change and update specs

## Related Files

### Existing iOS Implementation
- `ios/Classes/SwiftFlutterIgolfViewerPlugin.swift` (current placeholder)
- `ios/Classes/FlutterIgolfViewerPlugin.h/m` (Obj-C bridge)
- `ios/flutter_igolf_viewer.podspec` (current config)

### Existing Flutter Interface
- `lib/flutter_igolf_viewer.dart` (UiKitView widget)
- `lib/flutter_igolf_course_details_api_method_channel.dart` (API methods)
- `lib/flutter_igolf_course_details_api_platform_interface.dart` (platform interface)

### Android Reference (for parity)
- `android/src/main/kotlin/.../FlutterIgolfViewerPlugin.kt`
- `android/src/main/kotlin/.../FlutterIgolfViewerFactory.kt`
- `android/src/main/kotlin/.../FlutterIgolfViewer.kt`

## Success Metrics

- iOS platform achieves feature parity with Android
- Zero MissingPluginException errors on iOS
- 3D course viewer renders correctly with all textures
- Users can visualize golf courses on iOS devices
- Plugin consumers require zero manual iOS configuration
