# iOS Viewer Capability - Spec Delta

## ADDED Requirements

### Requirement: IGolfViewer3D Framework Integration
The iOS plugin SHALL embed and link the IGolfViewer3D.xcframework to provide native 3D golf course rendering capabilities using OpenGL ES.

#### Scenario: Framework loads successfully on device
- **WHEN** the plugin is built for iOS device (arm64 architecture)
- **THEN** the IGolfViewer3D framework SHALL be loaded from the ios-arm64 slice
- **AND** no MissingPluginException or framework loading errors SHALL occur

#### Scenario: Framework loads successfully on simulator
- **WHEN** the plugin is built for iOS simulator (x86_64 architecture)
- **THEN** the IGolfViewer3D framework SHALL be loaded from the ios-x86_64-simulator slice
- **AND** no framework loading errors SHALL occur

#### Scenario: Minimum iOS version requirement
- **WHEN** the plugin is used in an app with deployment target below iOS 15.6
- **THEN** the build SHALL fail with clear error message indicating minimum version requirement

### Requirement: Texture Asset Bundling
The iOS plugin SHALL bundle all required texture assets (82 PNG files and sans.ttf font) at the path structure `3DViewer/textures/` accessible from the plugin resource bundle.

#### Scenario: All texture files are bundled
- **WHEN** the plugin is installed via CocoaPods
- **THEN** all 82 PNG texture files SHALL be included in the plugin resource bundle
- **AND** the sans.ttf font file SHALL be included in the plugin resource bundle

#### Scenario: Textures are accessible at runtime
- **WHEN** the CourseRenderView attempts to load textures
- **THEN** the framework SHALL successfully locate and load textures from the plugin bundle path
- **AND** no missing texture errors SHALL occur

#### Scenario: Texture categories are complete
- **WHEN** rendering 3D course elements
- **THEN** textures SHALL be available for terrain types (fairway, green, sand, water)
- **AND** textures SHALL be available for vegetation (trees with 3D and 2D variants)
- **AND** textures SHALL be available for markers (distance targets, location pins, callouts)
- **AND** textures SHALL be available for environment (sky, clouds, backgrounds)

### Requirement: Platform View Factory Registration
The iOS plugin SHALL implement and register a FlutterPlatformViewFactory that creates native CourseRenderView instances for Flutter platform view integration.

#### Scenario: Factory is registered with correct view type ID
- **WHEN** the plugin is attached to Flutter engine
- **THEN** a FlutterIgolfViewerFactory SHALL be registered with view type ID "flutter_igolf_viewer"
- **AND** the factory SHALL be accessible for platform view creation

#### Scenario: Factory creates view instances with arguments
- **WHEN** Flutter requests platform view creation with creation parameters
- **THEN** the factory SHALL parse arguments (apiKey, secretKey, courseId, etc.)
- **AND** the factory SHALL instantiate FlutterIgolfView with parsed configuration
- **AND** the factory SHALL return a valid FlutterPlatformView instance

### Requirement: Native Platform View Lifecycle
The iOS plugin SHALL implement FlutterIgolfView as a FlutterPlatformView wrapper that manages CourseRenderView lifecycle, configuration, and rendering.

#### Scenario: View initialization with frame and arguments
- **WHEN** FlutterIgolfView is initialized with frame, viewId, arguments, and messenger
- **THEN** a CourseRenderView instance SHALL be created with the provided frame
- **AND** the view SHALL be stored as the primary rendering surface

#### Scenario: View returns CourseRenderView from view() protocol method
- **WHEN** Flutter platform view system calls the view() method
- **THEN** the CourseRenderView instance SHALL be returned
- **AND** the view SHALL be ready for embedding in Flutter view hierarchy

#### Scenario: View cleanup on dealloc
- **WHEN** FlutterIgolfView is deallocated
- **THEN** CourseRenderView resources SHALL be released
- **AND** CourseRenderViewLoader references SHALL be cleared
- **AND** no memory leaks SHALL occur

### Requirement: Course Data Loader Configuration
The iOS plugin SHALL configure CourseRenderViewLoader with API credentials and course parameters received from Flutter widget arguments.

#### Scenario: Loader initialization with required credentials
- **WHEN** FlutterIgolfView receives apiKey, secretKey, and courseId arguments
- **THEN** a CourseRenderViewLoader SHALL be initialized with these credentials
- **AND** the loader SHALL be configured for iGolf API authentication

#### Scenario: Measurement system configuration
- **WHEN** isMetricUnits parameter is true
- **THEN** loader.measurementSystem SHALL be set to .metric
- **WHEN** isMetricUnits parameter is false
- **THEN** loader.measurementSystem SHALL be set to .imperial

#### Scenario: Initial navigation mode configuration
- **WHEN** no specific navigation mode is provided
- **THEN** loader.initialNavigationMode SHALL default to .modeFreeCam
- **AND** the viewer SHALL start in free camera mode

#### Scenario: Optional viewer feature configuration
- **WHEN** loader is configured before preload
- **THEN** showCalloutOverlay SHALL be set based on viewer requirements
- **AND** drawCentralPathMarkers SHALL be configured
- **AND** drawDogLegMarker SHALL be configured
- **AND** areFrontBackMarkersDynamic SHALL be configured
- **AND** rotateHoleOnLocationChanged SHALL be configured
- **AND** autoAdvanceActive SHALL be configured

### Requirement: Asynchronous Course Preloading
The iOS plugin SHALL implement async course data preloading pattern using CourseRenderViewLoader.preload() with completion and error handlers.

#### Scenario: Successful course preload and view load
- **WHEN** loader.preload() is called with valid API credentials and courseId
- **THEN** the loader SHALL asynchronously fetch course data from iGolf API
- **AND** upon successful completion, the completion handler SHALL be invoked
- **AND** renderView.load(with: loader) SHALL be called to load course into view
- **AND** the course SHALL be rendered on screen

#### Scenario: Preload error handling
- **WHEN** loader.preload() fails due to network error or invalid credentials
- **THEN** the error handler SHALL be invoked with error details
- **AND** an appropriate error message SHALL be logged or communicated
- **AND** the view SHALL not crash or enter invalid state

#### Scenario: Preload does not block UI thread
- **WHEN** loader.preload() is executing network requests
- **THEN** the UI thread SHALL remain responsive
- **AND** no ANR (Application Not Responding) errors SHALL occur

### Requirement: Parameter Mapping from Flutter to Native
The iOS plugin SHALL map all Flutter widget parameters to corresponding CourseRenderViewLoader properties or view configuration.

#### Scenario: Starting hole parameter mapping
- **WHEN** startingHole parameter is provided (e.g., 5)
- **THEN** the loader SHALL configure the viewer to start at hole 5
- **AND** the initial view SHALL display hole 5

#### Scenario: Initial tee box parameter mapping
- **WHEN** initialTeeBox parameter is provided (e.g., 2)
- **THEN** the loader SHALL configure the viewer to use tee box index 2
- **AND** yardage calculations SHALL be based on that tee box

#### Scenario: Golfer icon index parameter
- **WHEN** golferIconIndex parameter is provided (e.g., 6)
- **THEN** the viewer SHALL use the specified icon index for golfer marker representation
- **OR** if unsupported by iOS framework, SHALL log warning and use default

#### Scenario: Optional data parameters
- **WHEN** parData, gpsDetails, or vectorGpsObject parameters are provided
- **THEN** the implementation SHALL attempt to configure loader with these values
- **OR** if unsupported by iOS framework, SHALL log info message and proceed with API-fetched data

### Requirement: CocoaPods Integration Specification
The flutter_igolf_viewer.podspec SHALL be configured to properly embed the framework, include resources, and declare dependencies for iOS platform.

#### Scenario: Framework vendoring configuration
- **WHEN** podspec is processed by CocoaPods
- **THEN** IGolfViewer3D.xcframework SHALL be included via s.vendored_frameworks
- **AND** the framework SHALL be embedded and code-signed in host app

#### Scenario: Resource bundle configuration
- **WHEN** podspec is processed by CocoaPods
- **THEN** texture assets SHALL be included via s.resource_bundles or s.resources
- **AND** resources SHALL be namespaced to avoid conflicts with host app

#### Scenario: Platform version requirement
- **WHEN** podspec declares platform requirement
- **THEN** s.platform SHALL specify :ios, '15.6' as minimum
- **AND** pod lib lint SHALL validate successfully

#### Scenario: System framework dependencies
- **WHEN** podspec is processed
- **THEN** OpenGLES framework SHALL be linked (if required by IGolfViewer3D)
- **AND** GLKit framework SHALL be linked (if required by IGolfViewer3D)
- **AND** other required system frameworks SHALL be declared

### Requirement: UiKitView Integration Compatibility
The iOS native implementation SHALL be compatible with Flutter's UiKitView widget configuration for platform view embedding.

#### Scenario: View type ID matches Flutter declaration
- **WHEN** Flutter creates UiKitView with viewType "flutter_igolf_viewer"
- **THEN** the iOS factory registered with same ID SHALL handle view creation
- **AND** the view SHALL be embedded in Flutter widget tree

#### Scenario: Creation parameters codec compatibility
- **WHEN** Flutter passes creationParams using StandardMessageCodec
- **THEN** the iOS factory SHALL successfully decode parameters
- **AND** all typed parameters SHALL be correctly parsed (String, int, bool, double)

#### Scenario: Layout direction support
- **WHEN** UiKitView specifies layoutDirection (ltr/rtl)
- **THEN** the native view SHALL respect layout direction if applicable
- **OR** SHALL ignore if CourseRenderView doesn't support RTL

### Requirement: Build Configuration and Architecture Support
The iOS plugin build configuration SHALL support both device and simulator architectures with proper code signing and optimization settings.

#### Scenario: Device architecture build
- **WHEN** building for iOS device
- **THEN** arm64 architecture SHALL be used
- **AND** framework's ios-arm64 slice SHALL be linked
- **AND** code signing SHALL succeed

#### Scenario: Simulator architecture build
- **WHEN** building for iOS simulator
- **THEN** x86_64 architecture SHALL be used (Intel Macs) or arm64 (Apple Silicon Macs)
- **AND** framework's ios-x86_64-simulator slice SHALL be linked on Intel
- **AND** i386 architecture SHALL be excluded

#### Scenario: Swift version compatibility
- **WHEN** plugin code is compiled
- **THEN** Swift 5.0 or later SHALL be used
- **AND** all Swift code SHALL compile without version compatibility errors

### Requirement: Error Handling and Debugging Support
The iOS implementation SHALL provide clear error messages and logging for common failure scenarios during initialization and runtime.

#### Scenario: Missing API credentials error
- **WHEN** apiKey or secretKey are nil or empty
- **THEN** an error SHALL be logged indicating missing credentials
- **AND** the view SHALL fail gracefully without crashing

#### Scenario: Invalid course ID error
- **WHEN** courseId is invalid or course doesn't exist
- **THEN** preload error handler SHALL receive API error
- **AND** error message SHALL be logged with course ID for debugging

#### Scenario: Texture loading failure warning
- **WHEN** framework cannot locate texture files
- **THEN** a warning SHALL be logged indicating missing texture path
- **AND** the implementation SHALL provide guidance on texture bundle configuration

#### Scenario: Framework version logging
- **WHEN** plugin initializes
- **THEN** IGolfViewer3D framework version SHALL be logged (if accessible)
- **AND** plugin version SHALL be logged for debugging
