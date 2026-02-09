import Flutter
import UIKit
import CoreLocation
import IGolfViewer3D

class IGolfWrapperView: UIView {
    private var _renderView: CourseRenderView?
    private var _loader: CourseRenderViewLoader?
    private var _pendingLoader: CourseRenderViewLoader?
    weak var pluginDelegate: CourseRenderViewDelegate?
    
    // Callback to notify parent when render view is successfully attached
    var onRenderViewAttached: (() -> Void)?
    
    var renderView: CourseRenderView? {
        return _renderView
    }

    override init(frame: CGRect) {
        super.init(frame: frame)
        // print("[IGolfViewer3D-Flutter] IGolfWrapperView initialized with frame: \(frame)")
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    override func touchesBegan(_ touches: Set<UITouch>, with event: UIEvent?) {
        // print("[IGolfViewer3D-Flutter] IGolfWrapperView received touchesBegan. Count: \(touches.count)")
        super.touchesBegan(touches, with: event)
    }
    
    override func layoutSubviews() {
        super.layoutSubviews()
        // print("[IGolfViewer3D-Flutter] layoutSubviews called. Frame: \(frame), Bounds: \(bounds)")
        
        // Ensure we have valid dimensions before attempting to show the render view
        if bounds.width > 0 && bounds.height > 0 {
            if _renderView == nil {
                // print("[IGolfViewer3D-Flutter] Valid dimensions detected. Initializing render view pending loader...")
                initializeRenderView()
            } else {
                // Update existing render view frame
                _renderView?.frame = bounds
                _renderView?.updateViewportSize(false)
                // print("[IGolfViewer3D-Flutter] Updated render view frame to: \(bounds) and updated viewport")
            }
        } else {
            // print("[IGolfViewer3D-Flutter] Keeping render view hidden - invalid dimensions")
        }
    }
    
    func setLoader(_ loader: CourseRenderViewLoader) {
        // print("[IGolfViewer3D-Flutter] setLoader called")
        _pendingLoader = loader
        
        // If we already have a displayable size, initialize immediately
        if bounds.width > 0 && bounds.height > 0 {
             initializeRenderView()
        } else {
            // print("[IGolfViewer3D-Flutter] Waiting for valid frame to initialize render view...")
        }
    }
    
    private func initializeRenderView() {
        guard let loader = _pendingLoader else {
            // print("[IGolfViewer3D-Flutter] No pending loader to initialize")
            return
        }
        
        if _renderView != nil {
             // print("[IGolfViewer3D-Flutter] Render view already initialized")
             return
        }
        
        // print("[IGolfViewer3D-Flutter] Creating CourseRenderView with frame: \(bounds)")
        let renderView = CourseRenderView(frame: bounds)
        renderView.autoresizingMask = [.flexibleWidth, .flexibleHeight]
        
        // Connect delegate
        if let delegate = pluginDelegate {
            // print("[IGolfViewer3D-Flutter] Setting renderView delegate")
            renderView.delegate = delegate
        }
        
        addSubview(renderView)
        _renderView = renderView
        _loader = loader
        
        // Slight delay to ensure the view is fully in the hierarchy and laid out before loading content
        // print("[IGolfViewer3D-Flutter] Scheduling renderView.load(with: loader) with 0.1s delay")
        DispatchQueue.main.asyncAfter(deadline: .now() + 0.1) { [weak self, weak renderView] in
            guard let self = self, let renderView = renderView else { return }
            // print("[IGolfViewer3D-Flutter] Executing delayed load")
            renderView.load(with: loader)
            
            // print("[IGolfViewer3D-Flutter] Enabling drawing and updating viewport")
            renderView.setDrawingEnabled(true)
            renderView.updateViewportSize(false)
            
            self.onRenderViewAttached?()
        }
    }
    
    func cleanup() {
        // print("[IGolfViewer3D-Flutter] Cleaning up resources")
        _renderView?.invalidate()
        _renderView?.removeFromSuperview()
        _renderView = nil
        _loader = nil
        _pendingLoader = nil
        pluginDelegate = nil
    }
}

class FlutterIgolfView: NSObject, FlutterPlatformView, CourseRenderViewDelegate {
    private var _wrapperView: IGolfWrapperView
    private var _loader: CourseRenderViewLoader?
    private var _hasApplied3DMode = false
    private var _eventStreamHandler: CourseViewerEventStreamHandler
    private var _methodChannel: FlutterMethodChannel?

    init(
        frame: CGRect,
        viewIdentifier viewId: Int64,
        arguments args: Any?,
        binaryMessenger messenger: FlutterBinaryMessenger?,
        eventStreamHandler: CourseViewerEventStreamHandler
    ) {
        // print("[IGolfViewer3D-Flutter] FlutterIgolfView init. Frame: \(frame)")
        _wrapperView = IGolfWrapperView(frame: frame)
        _eventStreamHandler = eventStreamHandler
        super.init()

        _wrapperView.pluginDelegate = self

        // Setup method channel for this view (matching Android)
        if let messenger = messenger {
            let channelName = "plugins.filledstacks.flutter_igolf_viewer/course_viewer_method"
            _methodChannel = FlutterMethodChannel(name: channelName, binaryMessenger: messenger)
            _methodChannel?.setMethodCallHandler { [weak self] call, result in
                self?.handleMethodCall(call, result: result)
            }
            // print("[IGolfViewer3D-Flutter] Method channel registered: \(channelName)")
        }

        // Parse arguments and configure the viewer
        if let arguments = args as? [String: Any] {
            configureViewer(with: arguments)
        } else {
             // print("[IGolfViewer3D-Flutter] No arguments provided for configuration")
        }
    }

    func view() -> UIView {
        return _wrapperView
    }
    
    // MARK: - CourseRenderViewDelegate

    func courseRenderViewDidLoadCourseData() {
        // print("[IGolfViewer3D-Flutter] Delegate: courseRenderViewDidLoadCourseData")

        guard let loader = _loader else {
            // print("[IGolfViewer3D-Flutter] Warning: No loader available for course data event")
            return
        }

        // Build vectorDataJsonMap matching Android format
        var vectorDataJsonMap: [String: Any] = [:]
        vectorDataJsonMap["COURSE_ID"] = loader.idCourse

        let gpsDetails = loader.courseGPSDetailsResponse
        if !gpsDetails.isEmpty {
            // Convert to JSON string to match Android format
            if let jsonData = try? JSONSerialization.data(withJSONObject: gpsDetails),
               let jsonString = String(data: jsonData, encoding: .utf8) {
                vectorDataJsonMap["GPS_DETAILS"] = jsonString
            }
        }

        let vectorDetails = loader.courseGPSVectorDetailsResponse
        if !vectorDetails.isEmpty {
            // Convert to JSON string to match Android format
            if let jsonData = try? JSONSerialization.data(withJSONObject: vectorDetails),
               let jsonString = String(data: jsonData, encoding: .utf8) {
                vectorDataJsonMap[loader.idCourse] = jsonString
            }
        }

        let eventData: [String: Any] = [
            "event": "COURSE_DATA_LOADED",
            "courseId": loader.idCourse,
            "vectorDataJsonMap": vectorDataJsonMap
        ]

        // print("[IGolfViewer3D-Flutter] Sending COURSE_DATA_LOADED event: \(eventData)")
        _eventStreamHandler.send(eventData)
    }

    func courseRenderViewDidLoadHoleData() {
        // print("[IGolfViewer3D-Flutter] Delegate: courseRenderViewDidLoadHoleData")

        // Send CURRENT_COURSE_CHANGED event (equivalent to Android's setCurrentCourseChangedListener)
        // This signals that the course is fully loaded and ready for interaction
        // print("[IGolfViewer3D-Flutter] Sending CURRENT_COURSE_CHANGED event")
        _eventStreamHandler.send(["event": "CURRENT_COURSE_CHANGED"])

        // Also send HOLE_LOADING_FINISHED to match Android behavior
        // print("[IGolfViewer3D-Flutter] Sending HOLE_LOADING_FINISHED event")
        _eventStreamHandler.send(["event": "HOLE_LOADING_FINISHED"])

        guard !_hasApplied3DMode, let renderView = _wrapperView.renderView else { return }
        _hasApplied3DMode = true
        // print("[IGolfViewer3D-Flutter] Setting NavigationMode to 3D FreeCam (post hole load)")
        renderView.navigationMode = .modeFreeCam
    }

    func courseRenderViewDidUpdateCurrentHole(_ currentHole: UInt) {
        // print("[IGolfViewer3D-Flutter] Delegate: courseRenderViewDidUpdateCurrentHole: \(currentHole)")
        let eventData: [String: Any] = [
            "event": "CURRENT_HOLE_CHANGED",
            "hole": Int(currentHole)
        ]
        _eventStreamHandler.send(eventData)
    }

    func courseRenderViewFlyoverFinished() {
        // print("[IGolfViewer3D-Flutter] Delegate: courseRenderViewFlyoverFinished")
        _eventStreamHandler.send(["event": "FLYOVER_FINISHED"])
    }

    func courseRenderViewDidUpdateDistances(toFrontGreen frontGreen: Double, toCenterGreen centerGreen: Double, toBackGreen backGreen: Double) {
        // print("[IGolfViewer3D-Flutter] Delegate: courseRenderViewDidUpdateDistances - front: \(frontGreen), center: \(centerGreen), back: \(backGreen)")
        let eventData: [String: Any] = [
            "event": "GPS_DISTANCES_UPDATED",
            "front": Int(frontGreen),
            "center": Int(centerGreen),
            "back": Int(backGreen)
        ]
        _eventStreamHandler.send(eventData)
    }

    func courseRenderViewDidChange(_ navigationMode: NavigationMode) {
        // print("[IGolfViewer3D-Flutter] Delegate: courseRenderViewDidChangeNavigationMode: \(navigationMode.rawValue)")

        let modeString: String
        switch navigationMode {
        case .mode2DView:
            modeString = "NavigationMode2D"
        case .mode2DGreenView:
            modeString = "GreenView2D"
        case .mode3DGreenView:
            modeString = "GreenView3D"
        case .modeOverallHole:
            modeString = "OverallHole"
        case .modeFreeCam:
            modeString = "FreeCam"
        case .modeFlyover:
            modeString = "Flyover"
        case .modeFlyoverPause:
            modeString = "FlyoverPause"
        @unknown default:
            modeString = "Unknown"
        }

        let eventData: [String: Any] = [
            "event": "NAVIGATION_MODE_CHANGED",
            "mode": modeString
        ]
        _eventStreamHandler.send(eventData)
    }

    func courseRenderViewDidFailWithError(_ error: Error) {
        // print("[IGolfViewer3D-Flutter] Delegate: courseRenderViewDidFailWithError: \(error.localizedDescription)")
    }

    // MARK: - Method Channel Handlers

    private func handleMethodCall(_ call: FlutterMethodCall, result: @escaping FlutterResult) {
        // print("[IGolfViewer3D-Flutter] Method call: \(call.method)")

        switch call.method {
        case "getCurrentHole":
            getCurrentHole(result: result)
        case "getNavigationMode":
            getNavigationMode(result: result)
        case "getNumHoles":
            getNumHoles(result: result)
        case "isMetricUnits":
            isMetricUnits(result: result)
        case "setCurrentHole":
            setCurrentHole(call: call, result: result)
        case "setNavigationMode":
            setNavigationMode(call: call, result: result)
        case "setCurrentLocationGPS":
            setCurrentLocationGPS(call: call, result: result)
        case "setMeasurementSystem":
            setMeasurementSystem(call: call, result: result)
        case "setCartLocationVisible":
            setCartLocationVisible(call: call, result: result)
        default:
            result(FlutterMethodNotImplemented)
        }
    }

    private func getCurrentHole(result: FlutterResult) {
        guard let renderView = _wrapperView.renderView else {
            result(FlutterError(code: "NO_VIEW", message: "Render view not initialized", details: nil))
            return
        }
        result(Int(renderView.currentHole))
    }

    private func getNavigationMode(result: FlutterResult) {
        guard let renderView = _wrapperView.renderView else {
            result(FlutterError(code: "NO_VIEW", message: "Render view not initialized", details: nil))
            return
        }
        let modeString = navigationModeToString(renderView.navigationMode)
        result(modeString)
    }

    private func getNumHoles(result: FlutterResult) {
        guard let renderView = _wrapperView.renderView else {
            result(FlutterError(code: "NO_VIEW", message: "Render view not initialized", details: nil))
            return
        }
        result(Int(renderView.numberOfHoles))
    }

    private func isMetricUnits(result: FlutterResult) {
        guard let renderView = _wrapperView.renderView else {
            result(FlutterError(code: "NO_VIEW", message: "Render view not initialized", details: nil))
            return
        }
        result(renderView.measurementSystem == .metric)
    }

    private func setCurrentHole(call: FlutterMethodCall, result: FlutterResult) {
        guard let renderView = _wrapperView.renderView,
              let args = call.arguments as? [String: Any],
              let hole = args["hole"] as? Int else {
            result(FlutterError(code: "INVALID_ARGS", message: "Missing hole parameter", details: nil))
            return
        }

        let navigationModeString = args["navigationMode"] as? String ?? "freeCam"
        let navigationMode = stringToNavigationMode(navigationModeString)
        let requestedHole = UInt(max(hole, 0))

        if renderView.currentHole == requestedHole && renderView.navigationMode == navigationMode {
            NSLog("[IGolfViewer3D-Flutter] setCurrentHole no-op (hole=%d, mode=%@)", hole, navigationModeString)
            result("No-op: current hole and navigation mode already set")
            return
        }

        NSLog(
            "[IGolfViewer3D-Flutter] setCurrentHole apply (from hole=%d/mode=%@ to hole=%d/mode=%@)",
            Int(renderView.currentHole),
            navigationModeToString(renderView.navigationMode),
            hole,
            navigationModeString
        )

        renderView.currentHole = requestedHole
        renderView.navigationMode = navigationMode

        // print("[IGolfViewer3D-Flutter] Set current hole to \(hole) with navigation mode \(navigationModeString)")
        result("Set current hole to \(hole) with NavigationMode \(navigationModeString)")
    }

    private func setNavigationMode(call: FlutterMethodCall, result: FlutterResult) {
        guard let renderView = _wrapperView.renderView,
              let args = call.arguments as? [String: Any],
              let modeString = args["mode"] as? String else {
            result(FlutterError(code: "INVALID_ARGS", message: "Missing mode parameter", details: nil))
            return
        }

        let navigationMode = stringToNavigationMode(modeString)
        renderView.navigationMode = navigationMode

        // print("[IGolfViewer3D-Flutter] Set navigation mode to \(modeString)")
        result("Result from native side for \(modeString)")
    }

    private func setCurrentLocationGPS(call: FlutterMethodCall, result: FlutterResult) {
        guard let renderView = _wrapperView.renderView,
              let args = call.arguments as? [String: Any],
              let latitude = args["latitude"] as? Double,
              let longitude = args["longitude"] as? Double else {
            result(FlutterError(code: "INVALID_ARGS", message: "Missing latitude/longitude parameters", details: nil))
            return
        }

        let location = CLLocation(latitude: latitude, longitude: longitude)

        // Set the current location on the render view
        renderView.currentLocation = location

        // Also set simulated location if updateCameraPos is true
        let updateCameraPos = args["updateCameraPos"] as? Bool ?? false
        if updateCameraPos {
            renderView.setSimulatedLocation(location)
        }

        NSLog(
            "[IGolfViewer3D-Flutter] setCurrentLocationGPS lat=%.6f lon=%.6f updateCameraPos=%@",
            latitude,
            longitude,
            updateCameraPos ? "true" : "false"
        )

        // print("[IGolfViewer3D-Flutter] Set current location GPS: lat=\(latitude), lon=\(longitude), updateCameraPos=\(updateCameraPos)")
        result(true)
    }

    private func setMeasurementSystem(call: FlutterMethodCall, result: FlutterResult) {
        guard let renderView = _wrapperView.renderView,
              let args = call.arguments as? [String: Any],
              let isMetric = args["isMetricUnits"] as? Bool else {
            result(FlutterError(code: "INVALID_ARGS", message: "Missing isMetricUnits parameter", details: nil))
            return
        }

        renderView.measurementSystem = isMetric ? .metric : .imperial

        // print("[IGolfViewer3D-Flutter] Set measurement system to \(isMetric ? "metric" : "imperial")")
        result(nil)
    }

    private func setCartLocationVisible(call: FlutterMethodCall, result: FlutterResult) {
        guard let renderView = _wrapperView.renderView,
              let args = call.arguments as? [String: Any],
              let isVisible = args["cartLocationVisible"] as? Bool else {
            result(FlutterError(code: "INVALID_ARGS", message: "Missing cartLocationVisible parameter", details: nil))
            return
        }

        renderView.showCartGpsPosition = isVisible

        // print("[IGolfViewer3D-Flutter] Set cart location visible to \(isVisible)")
        result(nil)
    }

    private func stringToNavigationMode(_ mode: String) -> NavigationMode {
        switch mode.lowercased() {
        case "flyover":
            return .modeFlyover
        case "flyoverpause":
            return .modeFlyoverPause
        case "freecam":
            return .modeFreeCam
        case "greenview2d":
            return .mode2DGreenView
        case "greenview3d":
            return .mode3DGreenView
        case "overallhole":
            return .modeOverallHole
        case "navigationmode2d":
            return .mode2DView
        default:
            return .modeFreeCam
        }
    }

    private func navigationModeToString(_ mode: NavigationMode) -> String {
        switch mode {
        case .mode2DView:
            return "NavigationMode2D"
        case .mode2DGreenView:
            return "GreenView2D"
        case .mode3DGreenView:
            return "GreenView3D"
        case .modeOverallHole:
            return "OverallHole"
        case .modeFreeCam:
            return "FreeCam"
        case .modeFlyover:
            return "Flyover"
        case .modeFlyoverPause:
            return "FlyoverPause"
        @unknown default:
            return "Unknown"
        }
    }

    // Method name as expected by the framework's Swift bridging header
    func courseRenderViewDidReceiveTapWithDistance(toUser distanceToUser: Double, distanceToFlag: Double) {
        // print("[IGolfViewer3D-Flutter] Delegate: USER_TAPS_VIEWER - toUser: \(distanceToUser), toFlag: \(distanceToFlag)")
        let eventData: [String: Any] = [
            "event": "USER_TAPS_VIEWER",
            "targetToUser": Int(distanceToUser),
            "targetToFlag": Int(distanceToFlag)
        ]
        _eventStreamHandler.send(eventData)
    }

    private func configureViewer(with arguments: [String: Any]) {
        // print("[IGolfViewer3D-Flutter] configureViewer started")
        guard let apiKey = arguments["apiKey"] as? String,
              let secretKey = arguments["secretKey"] as? String,
              let courseId = arguments["courseId"] as? String else {
            // print("[IGolfViewer3D-Flutter] Missing required parameters (apiKey, secretKey, courseId)")
            return
        }

        // Create the loader with API credentials
        let loader = CourseRenderViewLoader(
            applicationAPIKey: apiKey,
            applicationSecretKey: secretKey,
            idCourse: courseId
        )

        // Configure measurement system
        if let isMetricUnits = arguments["isMetricUnits"] as? Bool {
            loader.measurementSystem = isMetricUnits ? .metric : .imperial
        } else {
            loader.measurementSystem = .metric // Default
        }

        // Configure starting hole
        if let startingHole = arguments["startingHole"] as? Int {
            loader.hole = UInt(startingHole)
        }

        // Configure viewer features
        loader.initialNavigationMode = .modeFreeCam
        loader.showCalloutOverlay = false
        loader.drawCentralPathMarkers = false
        loader.drawDogLegMarker = true
        loader.areFrontBackMarkersDynamic = true
        // Keep Dart as single source of truth for auto-advance and hole-rotation decisions.
        loader.rotateHoleOnLocationChanged = false
        loader.autoAdvanceActive = false
        loader.draw3DCentralLine = false
        
        // print("[IGolfViewer3D-Flutter] Loader configured. Starting preload...")

        // Store loader reference
        _loader = loader

        // Create loading indicator
        let loadingView = UIActivityIndicatorView(style: .large)
        loadingView.color = .orange
        loadingView.startAnimating()
        loader.setLoading(loadingView)

        // Preload course data asynchronously
        loader.preload(
            completionHandler: { [weak self] in
                guard let self = self else { return }
                // print("[IGolfViewer3D-Flutter] Preload completed successfully")
                // Pass the ready loader to the wrapper view
                // The wrapper view will decide when to attach based on frame availability
                DispatchQueue.main.async {
                    self._wrapperView.setLoader(loader)
                }
            },
            errorHandler: { error in
                if let error = error {
                    // print("[IGolfViewer3D-Flutter] Failed to load course - \(error.localizedDescription)")
                } else {
                    // print("[IGolfViewer3D-Flutter] Failed to load course - Unknown error")
                }
            }
        )
    }

    deinit {
        // print("[IGolfViewer3D-Flutter] Deinit")
        _methodChannel?.setMethodCallHandler(nil)
        _wrapperView.cleanup()
    }
}
