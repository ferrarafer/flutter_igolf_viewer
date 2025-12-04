# Change: Add iOS Native Viewer Integration

## Why
The Flutter plugin currently has placeholder iOS implementation that returns nil for most API methods. The Android platform has full native viewer integration using the iGolf SDK. To achieve feature parity and enable iOS users to visualize golf courses with 3D rendering, GPS data, and interactive mapping features, we need to integrate the IGolfViewer3D.xcframework and implement the native platform view for iOS.

## What Changes
- **Add IGolfViewer3D.xcframework** to the iOS plugin directory with proper embedding configuration
- **Bundle 82 texture assets** (PNG files) and font file (sans.ttf) required by the 3D viewer at expected path `3DViewer/textures/`
- **Implement FlutterIgolfViewerFactory** for iOS to create native CourseRenderView instances via platform view pattern
- **Create FlutterIgolfView** wrapper class that manages CourseRenderView lifecycle and configuration
- **Update SwiftFlutterIgolfViewerPlugin** to register platform view factory and handle initialization
- **Implement CourseRenderViewLoader** configuration with proper API authentication and viewer settings
- **Add proper cleanup** for native view lifecycle (dealloc, detach handlers)
- **Update podspec** to include framework dependency, texture resources, and minimum iOS version requirements
- **Add Info.plist settings** if required for OpenGL ES rendering permissions

## Impact
- **Affected specs**: ios-viewer (new capability)
- **Affected code**:
  - `ios/flutter_igolf_viewer.podspec` - Framework and resource dependencies
  - `ios/Classes/SwiftFlutterIgolfViewerPlugin.swift` - Platform view registration
  - `ios/Classes/FlutterIgolfViewerFactory.swift` - NEW: Factory for creating platform views
  - `ios/Classes/FlutterIgolfView.swift` - NEW: Platform view implementation
  - `ios/IGolfViewer3D.xcframework/` - NEW: Native iOS framework (copied from CourseViewerDemo)
  - `ios/3DViewer/textures/` - NEW: 82 texture PNGs + sans.ttf font (copied from CourseViewerDemo/CourseViewerDemo/3DViewer/textures/)
  - `lib/flutter_igolf_viewer.dart` - Verify UiKitView configuration matches native implementation
- **Breaking changes**: None (adds functionality to existing iOS placeholder)
- **Platform requirements**: Minimum iOS 15.6 (framework requirement), OpenGL ES support
- **Bundle size impact**: ~5-10MB for framework + textures
