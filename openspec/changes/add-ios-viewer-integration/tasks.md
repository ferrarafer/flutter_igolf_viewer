# Implementation Tasks

## 1. Framework Integration
- [ ] 1.1 Copy IGolfViewer3D.xcframework from CourseViewerDemo to `ios/IGolfViewer3D.xcframework/`
- [ ] 1.2 Update `flutter_igolf_viewer.podspec` to embed and link the xcframework
- [ ] 1.3 Configure podspec with proper framework search paths and linker flags
- [ ] 1.4 Update minimum iOS deployment target to 15.6 in podspec
- [ ] 1.5 Verify xcframework includes both device (arm64) and simulator (x86_64) slices

## 2. Texture Assets
- [ ] 2.1 Create directory structure: `ios/3DViewer/textures/`
- [ ] 2.2 Copy all 82 PNG texture files from `CourseViewerDemo/CourseViewerDemo/3DViewer/textures/` to `ios/3DViewer/textures/`
- [ ] 2.3 Copy sans.ttf font file from source textures directory to `ios/3DViewer/textures/`
- [ ] 2.4 Update podspec `s.resources` or `s.resource_bundles` to include `3DViewer/textures` directory
- [ ] 2.5 Verify textures are accessible from plugin bundle at runtime path `3DViewer/textures/`

## 3. Platform View Factory
- [ ] 3.1 Create `ios/Classes/FlutterIgolfViewerFactory.swift`
- [ ] 3.2 Implement `FlutterPlatformViewFactory` protocol
- [ ] 3.3 Store reference to FlutterBinaryMessenger for method/event channel communication
- [ ] 3.4 Implement `create(withFrame:viewIdentifier:arguments:)` method to instantiate FlutterIgolfView
- [ ] 3.5 Parse creation arguments (apiKey, secretKey, courseId, etc.) from Flutter side
- [ ] 3.6 Return FlutterIgolfView instance wrapped in FlutterPlatformView protocol

## 4. Platform View Implementation
- [ ] 4.1 Create `ios/Classes/FlutterIgolfView.swift`
- [ ] 4.2 Implement `FlutterPlatformView` protocol with `view()` method returning CourseRenderView
- [ ] 4.3 Add private properties: `renderView: CourseRenderView`, `loader: CourseRenderViewLoader`
- [ ] 4.4 Implement initializer accepting frame, viewId, args, and messenger
- [ ] 4.5 Create CourseRenderView instance with proper frame
- [ ] 4.6 Configure CourseRenderViewLoader with API credentials from args
- [ ] 4.7 Set loader properties: measurementSystem, navigationMode, callouts, markers, etc.
- [ ] 4.8 Implement async preload pattern with completion/error handlers
- [ ] 4.9 Call `renderView.load(with: loader)` on successful preload
- [ ] 4.10 Add dealloc/cleanup method to release resources properly

## 5. Plugin Registration
- [ ] 5.1 Update `SwiftFlutterIgolfViewerPlugin.swift` to import IGolfViewer3D
- [ ] 5.2 Create FlutterIgolfViewerFactory instance in `register(with:)` method
- [ ] 5.3 Register platform view factory with registrar: `registrar.register(_:withId:)`
- [ ] 5.4 Use view type ID: "flutter_igolf_viewer" (must match Dart side)
- [ ] 5.5 Store factory reference if needed for cleanup
- [ ] 5.6 Verify method channel registration doesn't conflict with platform view

## 6. Configuration Mapping
- [ ] 6.1 Map Flutter widget parameters to loader properties:
  - [ ] apiKey → loader.applicationAPIKey
  - [ ] secretKey → loader.applicationSecretKey
  - [ ] courseId → loader.idCourse
  - [ ] startingHole → loader.initialHole (verify property name)
  - [ ] initialTeeBox → loader.initialTeeBox (verify property name)
  - [ ] isMetricUnits → loader.measurementSystem (.metric or .imperial)
  - [ ] golferIconIndex → (verify if loader has this property)
- [ ] 6.2 Handle optional parameters (parData, gpsDetails, vectorGpsObject) - research if loader supports these
- [ ] 6.3 Set default loader configurations matching CourseViewerDemo best practices

## 7. Testing & Validation
- [ ] 7.1 Build iOS example app with new implementation
- [ ] 7.2 Verify framework loads without MissingPluginException
- [ ] 7.3 Test on physical iOS device (arm64 architecture)
- [ ] 7.4 Test on iOS simulator (x86_64 architecture)
- [ ] 7.5 Verify textures render correctly in 3D view
- [ ] 7.6 Test course loading with valid API credentials
- [ ] 7.7 Verify navigation modes (FreeCam, 3DGreenView, etc.) work
- [ ] 7.8 Test view lifecycle (appear, disappear, memory cleanup)
- [ ] 7.9 Validate metric/imperial unit toggle
- [ ] 7.10 Test error handling for invalid course IDs or API failures

## 8. Documentation
- [ ] 8.1 Update README.md with iOS setup instructions
- [ ] 8.2 Document framework installation requirements
- [ ] 8.3 Add iOS-specific configuration notes (minimum version, capabilities)
- [ ] 8.4 Document texture asset requirements and paths
- [ ] 8.5 Add example iOS implementation code snippet
- [ ] 8.6 Update CHANGELOG.md with iOS viewer integration details

## 9. Cleanup & Polish
- [ ] 9.1 Remove placeholder nil returns from SwiftFlutterIgolfViewerPlugin if no longer needed
- [ ] 9.2 Add proper Swift documentation comments to new classes
- [ ] 9.3 Run Swift linter/formatter on new code
- [ ] 9.4 Verify no Xcode warnings in plugin build
- [ ] 9.5 Check for memory leaks using Instruments (Leaks template)
- [ ] 9.6 Optimize framework embedding (strip debug symbols if possible)
