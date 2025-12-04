# Design: iOS Native Viewer Integration

## Context
The Flutter plugin needs iOS platform parity with the existing Android implementation. The IGolfViewer3D framework provides native OpenGL ES-based 3D golf course rendering with GPS mapping capabilities. The framework is distributed as an XCFramework supporting both device (arm64) and simulator (x86_64-simulator) architectures.

**Reference Implementation**: `/Users/danemackier/Code/clients/pinnedgolf/viewers/CourseViewerDemo` demonstrates working integration patterns.

**Constraints**:
- Framework requires minimum iOS 15.6 deployment target
- Textures must be bundled at specific path structure for framework to locate them
- Framework uses synchronous preload pattern with async completion handlers
- OpenGL ES rendering requires proper view lifecycle management

**Stakeholders**: iOS users of PinnedGolf app, Flutter plugin consumers

## Goals / Non-Goals

**Goals**:
- Achieve feature parity with Android platform view integration
- Enable 3D golf course visualization on iOS devices
- Support all viewer configuration options exposed in Flutter API
- Maintain efficient memory usage and proper cleanup
- Follow Flutter plugin best practices for platform views

**Non-Goals**:
- Modifying the IGolfViewer3D framework itself (treat as black box)
- Implementing custom texture generation or modification
- Supporting iOS versions below 15.6
- Web or desktop platform implementations
- Custom rendering engine (use provided framework)

## Decisions

### Decision 1: XCFramework Integration via CocoaPods
**What**: Embed IGolfViewer3D.xcframework in the Flutter plugin's iOS directory and link via podspec configuration.

**Why**:
- XCFramework format provides unified binary for device + simulator architectures
- CocoaPods is the standard Flutter iOS dependency mechanism
- Embedding in plugin ensures distribution with pub package
- No external dependency resolution required by plugin consumers

**Alternatives Considered**:
- Swift Package Manager: Not fully supported by Flutter plugin tooling
- Manual framework linking: Fragile, requires consumer configuration
- CocoaPods external podspec: Adds external dependency, complicates distribution

**Implementation**:
```ruby
# flutter_igolf_viewer.podspec
s.vendored_frameworks = 'IGolfViewer3D.xcframework'
s.platform = :ios, '15.6'
s.frameworks = 'OpenGLES', 'GLKit'
```

### Decision 2: Texture Asset Bundling Strategy
**What**: Bundle textures in plugin's iOS Assets directory and include via podspec `s.resource_bundles`.

**Why**:
- Ensures textures are namespaced to plugin (avoids conflicts with host app)
- Framework expects textures at specific relative path: `3DViewer/textures/`
- Resource bundles are automatically included in plugin pod installation
- Reduces manual setup required by plugin consumers

**Alternatives Considered**:
- Direct `s.resources`: Can cause namespace collisions with host app assets
- Host app responsibility: Unacceptable UX, requires manual consumer setup
- Framework modification: Not possible, closed-source framework

**Implementation**:
```ruby
s.resource_bundles = {
  'flutter_igolf_viewer_assets' => ['Assets/3DViewer/textures/*']
}
```

**Runtime Loading**:
```swift
// Access bundled textures from plugin bundle
let bundle = Bundle(for: FlutterIgolfView.self)
let texturePath = bundle.path(forResource: "3DViewer/textures", ofType: nil)
```

### Decision 3: Platform View Architecture
**What**: Implement FlutterPlatformViewFactory + FlutterIgolfView wrapper pattern, similar to Android's PlatformView + Factory approach.

**Why**:
- Matches existing Android architecture for consistency
- Factory pattern allows per-instance configuration from Flutter
- Wrapper class encapsulates CourseRenderView lifecycle management
- Enables clean separation of Flutter integration from native view logic

**Class Responsibilities**:
- **FlutterIgolfViewerFactory**: Creates view instances, parses Flutter arguments, manages messenger reference
- **FlutterIgolfView**: Wraps CourseRenderView, handles loader configuration, manages async preload lifecycle

**Alternatives Considered**:
- Direct CourseRenderView exposure: Leaks native API details, harder to maintain
- Single monolithic class: Violates SRP, harder to test

### Decision 4: Async Preload Pattern
**What**: Use framework's `preload()` method with completion/error handlers before loading view.

**Why**:
- Framework downloads course data from iGolf API (GPS vectors, textures, elevation)
- Blocking UI thread during network requests causes poor UX
- Framework provides async preload API specifically for this purpose
- Matches reference implementation pattern from CourseViewerDemo

**Sequence**:
```swift
// 1. Configure loader
let loader = CourseRenderViewLoader(apiKey: key, secretKey: secret, idCourse: id)
loader.measurementSystem = .metric
// ... other config

// 2. Preload async
loader.preload { [weak self] in
    // 3. Load into view on success
    self?.renderView.load(with: loader)
} errorHandler: { error in
    print("Failed to load course: \(error?.localizedDescription ?? "unknown")")
}
```

**Alternatives Considered**:
- Synchronous loading: Would block UI thread during network calls
- Flutter-side preloading: Can't access native framework APIs from Dart
- Lazy loading on first render: Causes visible delay and poor UX

### Decision 5: Configuration Parameter Mapping
**What**: Map Flutter widget parameters directly to CourseRenderViewLoader properties in FlutterIgolfView initializer.

**Parameter Mappings**:
| Flutter Parameter | Loader Property | Notes |
|-------------------|-----------------|-------|
| apiKey | applicationAPIKey | Required for API auth |
| secretKey | applicationSecretKey | Required for API auth |
| courseId | idCourse | Required course identifier |
| startingHole | initialHole | Default: 1 (verify property name) |
| initialTeeBox | initialTeeBox | Default: 0 (verify property name) |
| isMetricUnits | measurementSystem | .metric or .imperial |
| golferIconIndex | (TBD) | Research framework support |
| parData | (TBD) | Research framework support |
| gpsDetails | (TBD) | Research framework support |
| vectorGpsObject | (TBD) | Research framework support |

**Why**: Direct mapping maintains consistency with Android implementation and provides predictable behavior.

**Unknowns**: Need to verify if `parData`, `gpsDetails`, `vectorGpsObject` parameters are supported by iOS framework (Android may have custom implementation).

### Decision 6: Memory Management & Cleanup
**What**: Implement explicit cleanup in FlutterIgolfView dealloc to release CourseRenderView and loader resources.

**Why**:
- CourseRenderView holds OpenGL ES context and graphics resources
- Loader may cache network data and textures
- Prevents memory leaks when Flutter rebuilds widget tree
- iOS doesn't have Android's explicit onDispose callback, must use deinit

**Implementation**:
```swift
deinit {
    renderView.cleanup() // If framework provides cleanup method
    loader = nil
    renderView = nil
}
```

## Risks / Trade-offs

### Risk: Framework API Undocumented
**Severity**: Medium
**Description**: IGolfViewer3D framework header files show API surface, but some properties and methods lack documentation.
**Mitigation**:
- Use CourseViewerDemo as authoritative reference implementation
- Test each configuration parameter to verify behavior
- Add inline comments documenting observed behavior
- Contact iGolf SDK support for clarification on ambiguous APIs

### Risk: Texture Path Assumptions
**Severity**: Medium
**Description**: Framework may have hard-coded assumptions about texture bundle paths that differ between standalone app and plugin context.
**Mitigation**:
- Maintain exact directory structure from CourseViewerDemo: `3DViewer/textures/`
- Test texture loading in plugin context early
- Add runtime logging to verify texture path resolution
- Consider using framework debugging mode if available

### Risk: OpenGL ES Deprecation
**Severity**: Low (long-term)
**Description**: Apple deprecated OpenGL ES in iOS 12, recommends Metal. Framework uses legacy rendering.
**Impact**: Framework may not support future iOS versions; performance may degrade.
**Mitigation**:
- Document known dependency on OpenGL ES
- Monitor framework updates from iGolf for Metal migration
- No immediate action required (framework still works on iOS 18)

### Risk: XCFramework Size Impact
**Severity**: Low
**Description**: Framework binary + textures add ~5-10MB to app bundle size.
**Impact**: Increases download size for end users.
**Mitigation**:
- Accept trade-off (native rendering requires native assets)
- Future: Request iGolf provide texture-on-demand loading
- No viable alternative for current implementation

### Trade-off: iOS 15.6 Minimum Version
**Impact**: Excludes iOS 14.x and earlier devices (~5% of iOS users as of 2024).
**Justification**: Framework requirement, non-negotiable.
**Communication**: Document clearly in README and plugin metadata.

## Migration Plan

### Phase 1: Framework & Asset Setup
1. Copy IGolfViewer3D.xcframework to `ios/` directory
2. Copy texture assets to `ios/Assets/3DViewer/textures/`
3. Update podspec with framework and resource configuration
4. Verify pod lib lint passes

### Phase 2: Platform View Implementation
1. Implement FlutterIgolfViewerFactory
2. Implement FlutterIgolfView with basic configuration
3. Register factory in SwiftFlutterIgolfViewerPlugin
4. Test basic view creation (empty view is success)

### Phase 3: Loader Integration
1. Add CourseRenderViewLoader configuration
2. Implement preload pattern with completion handlers
3. Map Flutter parameters to loader properties
4. Test with known-good course ID from CourseViewerDemo

### Phase 4: Testing & Refinement
1. Device testing (arm64)
2. Simulator testing (x86_64)
3. Error handling validation
4. Memory leak detection
5. Configuration parameter verification

### Rollback Strategy
If critical issues discovered post-merge:
1. iOS platform view can return error state without crashing
2. Plugin remains functional on Android
3. Can temporarily disable iOS platform view registration
4. No database migrations or persistent state changes involved

## Open Questions

### Q1: Does IGolfViewer3D support custom par data injection?
**Context**: Flutter API exposes `parData` parameter. Android may fetch from API, iOS may support override.
**Action**: Test with CourseViewerDemo, check framework headers for relevant properties.
**Assigned**: Implementation phase

### Q2: What is golferIconIndex used for?
**Context**: Flutter widget has parameter, unclear how iOS framework uses it.
**Action**: Search framework headers for icon/marker configuration APIs.
**Assigned**: Implementation phase

### Q3: Does framework automatically handle texture path resolution in plugin context?
**Context**: CourseViewerDemo uses app bundle, plugin uses resource bundle.
**Action**: Early testing in Phase 2 to verify texture loading.
**Assigned**: Framework & Asset Setup phase

### Q4: Are there framework debugging/logging modes?
**Context**: Would help troubleshoot texture loading and API issues.
**Action**: Review IGolfViewer3D.h and supporting headers for debug properties.
**Assigned**: Implementation phase

### Q5: Should event channel integration match Android?
**Context**: Android has CourseViewerEventChannel for streaming viewer events. iOS SwiftFlutterIgolfViewerPlugin has placeholder event channel setup.
**Action**: Check if CourseRenderView has delegate methods for viewer events (navigation changes, location taps, etc.).
**Assigned**: Future enhancement (not required for initial integration)

## Success Criteria

- [ ] iOS example app displays 3D golf course viewer
- [ ] All textures render correctly (trees, greens, fairways, etc.)
- [ ] Course loads with same API credentials as Android
- [ ] Metric/Imperial units toggle works
- [ ] No memory leaks detected with Instruments
- [ ] No Xcode build warnings
- [ ] Pod lib lint passes
- [ ] Works on both physical device and simulator
- [ ] Plugin consumers can install without manual framework configuration
