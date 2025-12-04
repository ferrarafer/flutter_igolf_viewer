# Project Context

## Purpose
Flutter plugin that provides a native platform view integration for the iGolf course viewer SDK, enabling golf course visualization with GPS data, hole layouts, and interactive mapping features on Android and iOS platforms. The plugin also exposes a comprehensive Course Details API for querying golf course information including scorecards, tee details, GPS coordinates, and location data.

## Tech Stack
- **Flutter/Dart** (SDK: >=3.2.3 <4.0.0, Flutter: >=3.3.0)
- **Android**: Kotlin with native Android SDK integration
- **iOS**: Swift with native iOS SDK integration
- **Platform Communication**: Method Channels and Event Channels
- **Networking**: Retrofit (Android), native networking for API calls
- **Dependencies**:
  - plugin_platform_interface: ^2.0.2
  - flutter_lints: ^2.0.0

## Project Conventions

### Code Style
- **Dart**: Follows flutter_lints rules (package:flutter_lints/flutter.yaml)
- **Kotlin**: Standard Kotlin conventions with package structure under com.filledstacks.plugins.flutter_igolf_viewer
- **Swift**: Modern Swift conventions with class-based architecture
- **Naming Conventions**:
  - Method channels: `plugins.filledstacks.flutter_igolf_course/course_details_api`
  - Event channels: `plugins.filledstacks.flutter_igolf_viewer/course_viewer_event`
  - Platform view type: `flutter_igolf_viewer`

### Architecture Patterns
- **Platform Views**: Uses AndroidView and UiKitView for native embedding
- **Method Channel Pattern**: Bidirectional communication between Dart and native code
- **Event Channel Pattern**: Stream-based communication for course viewer events
- **Platform Interface Pattern**: Abstract platform interface with method channel implementation
- **Factory Pattern**: FlutterIgolfViewerFactory for creating platform view instances
- **Separation of Concerns**:
  - Network layer (Network.kt, NetworkService.kt, iGolfService.kt)
  - Request/Response models (network/request/, network/response/)
  - Channel handlers (CourseDetailsApiMethodChannel, CourseViewerEventChannel)
  - Platform-specific plugin implementations

### Testing Strategy
- Unit tests for plugin functionality (test/flutter_igolf_viewer_test.dart)
- Method channel tests (test/flutter_igolf_viewer_method_channel_test.dart)
- Integration tests (example/integration_test/plugin_integration_test.dart)
- Platform-specific tests (Android: FlutterIgolfViewerPluginTest.kt, iOS: RunnerTests.swift)

### Git Workflow
- **Branch**: main (primary branch)
- **Commit Convention**: Conventional commits format
  - `feat:` for new features
  - `chore:` for maintenance tasks
  - `change:` for behavior modifications
- **Version Management**: Semantic versioning (current: 0.6.4)

## Domain Context
- **Golf Domain**: Integration with iGolf SDK for professional golf course data
- **GPS/Mapping**: Handles both simple GPS data (points) and vector GPS data (polygons/lines)
- **Course Data**: Scorecards, tee box details, par information, handicap data
- **Geographic Data**: Country/state listings, course search by location/radius
- **Viewer Features**:
  - Starting hole configuration
  - Tee box selection (initialTeeBox)
  - Golfer icon customization
  - Metric/Imperial units toggle
  - Auto-advance functionality
  - FreeCam view with zoom controls

## Important Constraints
- **API Authentication**: Requires apiKey and secretKey for all iGolf API operations
- **Platform Support**: Android and iOS only (no web, desktop, or other platforms)
- **API Limits**: Course list queries limited to 100 results per page, maximum 5 pages
- **Course Array Limits**: Scorecard list queries limited to 20 courses per request
- **iOS Implementation**: Currently returns nil for most API methods (placeholders)
- **Android Implementation**: Full API implementation with Retrofit networking

## External Dependencies
- **iGolf API**: External golf course data provider requiring authentication
- **iGolf SDK**: Native Android/iOS libraries for course visualization
  - Android: Native viewer library integration
  - iOS: Native viewer library integration
- **API Endpoints**: RESTful API for course data retrieval
  - Country/State listings
  - Course search and details
  - Scorecard and tee information
  - GPS data (simple and vector formats)
