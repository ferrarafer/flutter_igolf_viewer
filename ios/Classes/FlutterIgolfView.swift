import Flutter
import UIKit
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
        print("[IGolfViewer3D-Flutter] IGolfWrapperView initialized with frame: \(frame)")
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    override func touchesBegan(_ touches: Set<UITouch>, with event: UIEvent?) {
        print("[IGolfViewer3D-Flutter] IGolfWrapperView received touchesBegan. Count: \(touches.count)")
        super.touchesBegan(touches, with: event)
    }
    
    override func layoutSubviews() {
        super.layoutSubviews()
        print("[IGolfViewer3D-Flutter] layoutSubviews called. Frame: \(frame), Bounds: \(bounds)")
        
        // Ensure we have valid dimensions before attempting to show the render view
        if bounds.width > 0 && bounds.height > 0 {
            if _renderView == nil {
                print("[IGolfViewer3D-Flutter] Valid dimensions detected. Initializing render view pending loader...")
                initializeRenderView()
            } else {
                // Update existing render view frame
                _renderView?.frame = bounds
                _renderView?.updateViewportSize(false)
                print("[IGolfViewer3D-Flutter] Updated render view frame to: \(bounds) and updated viewport")
            }
        } else {
            print("[IGolfViewer3D-Flutter] Keeping render view hidden - invalid dimensions")
        }
    }
    
    func setLoader(_ loader: CourseRenderViewLoader) {
        print("[IGolfViewer3D-Flutter] setLoader called")
        _pendingLoader = loader
        
        // If we already have a displayable size, initialize immediately
        if bounds.width > 0 && bounds.height > 0 {
             initializeRenderView()
        } else {
            print("[IGolfViewer3D-Flutter] Waiting for valid frame to initialize render view...")
        }
    }
    
    private func initializeRenderView() {
        guard let loader = _pendingLoader else {
            print("[IGolfViewer3D-Flutter] No pending loader to initialize")
            return
        }
        
        if _renderView != nil {
             print("[IGolfViewer3D-Flutter] Render view already initialized")
             return
        }
        
        print("[IGolfViewer3D-Flutter] Creating CourseRenderView with frame: \(bounds)")
        let renderView = CourseRenderView(frame: bounds)
        renderView.autoresizingMask = [.flexibleWidth, .flexibleHeight]
        
        // Connect delegate
        if let delegate = pluginDelegate {
            print("[IGolfViewer3D-Flutter] Setting renderView delegate")
            renderView.delegate = delegate
        }
        
        addSubview(renderView)
        _renderView = renderView
        _loader = loader
        
        // Slight delay to ensure the view is fully in the hierarchy and laid out before loading content
        print("[IGolfViewer3D-Flutter] Scheduling renderView.load(with: loader) with 0.1s delay")
        DispatchQueue.main.asyncAfter(deadline: .now() + 0.1) { [weak self, weak renderView] in
            guard let self = self, let renderView = renderView else { return }
            print("[IGolfViewer3D-Flutter] Executing delayed load")
            renderView.load(with: loader)
            
            print("[IGolfViewer3D-Flutter] Enabling drawing and updating viewport")
            renderView.setDrawingEnabled(true)
            renderView.updateViewportSize(false)
            
            self.onRenderViewAttached?()
        }
    }
    
    func cleanup() {
        print("[IGolfViewer3D-Flutter] Cleaning up resources")
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

    init(
        frame: CGRect,
        viewIdentifier viewId: Int64,
        arguments args: Any?,
        binaryMessenger messenger: FlutterBinaryMessenger?,
        eventStreamHandler: CourseViewerEventStreamHandler
    ) {
        print("[IGolfViewer3D-Flutter] FlutterIgolfView init. Frame: \(frame)")
        _wrapperView = IGolfWrapperView(frame: frame)
        _eventStreamHandler = eventStreamHandler
        super.init()

        _wrapperView.pluginDelegate = self

        // Parse arguments and configure the viewer
        if let arguments = args as? [String: Any] {
            configureViewer(with: arguments)
        } else {
             print("[IGolfViewer3D-Flutter] No arguments provided for configuration")
        }
    }

    func view() -> UIView {
        return _wrapperView
    }
    
    // MARK: - CourseRenderViewDelegate

    func courseRenderViewDidLoadCourseData() {
        print("[IGolfViewer3D-Flutter] Delegate: courseRenderViewDidLoadCourseData")

        guard let loader = _loader else {
            print("[IGolfViewer3D-Flutter] Warning: No loader available for course data event")
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

        print("[IGolfViewer3D-Flutter] Sending COURSE_DATA_LOADED event: \(eventData)")
        _eventStreamHandler.send(eventData)
    }

    func courseRenderViewDidLoadHoleData() {
        print("[IGolfViewer3D-Flutter] Delegate: courseRenderViewDidLoadHoleData")

        // Send CURRENT_COURSE_CHANGED event (equivalent to Android's setCurrentCourseChangedListener)
        // This signals that the course is fully loaded and ready for interaction
        print("[IGolfViewer3D-Flutter] Sending CURRENT_COURSE_CHANGED event")
        _eventStreamHandler.send(["event": "CURRENT_COURSE_CHANGED"])

        // Also send HOLE_LOADING_FINISHED to match Android behavior
        print("[IGolfViewer3D-Flutter] Sending HOLE_LOADING_FINISHED event")
        _eventStreamHandler.send(["event": "HOLE_LOADING_FINISHED"])

        guard !_hasApplied3DMode, let renderView = _wrapperView.renderView else { return }
        _hasApplied3DMode = true
        print("[IGolfViewer3D-Flutter] Setting NavigationMode to 3D FreeCam (post hole load)")
        renderView.navigationMode = .modeFreeCam
    }

    func courseRenderViewDidUpdateCurrentHole(_ currentHole: UInt) {
        print("[IGolfViewer3D-Flutter] Delegate: courseRenderViewDidUpdateCurrentHole: \(currentHole)")
        let eventData: [String: Any] = [
            "event": "CURRENT_HOLE_CHANGED",
            "hole": Int(currentHole)
        ]
        _eventStreamHandler.send(eventData)
    }

    func courseRenderViewFlyoverFinished() {
        print("[IGolfViewer3D-Flutter] Delegate: courseRenderViewFlyoverFinished")
        _eventStreamHandler.send(["event": "FLYOVER_FINISHED"])
    }

    func courseRenderViewDidUpdateDistances(toFrontGreen frontGreen: Double, toCenterGreen centerGreen: Double, toBackGreen backGreen: Double) {
        print("[IGolfViewer3D-Flutter] Delegate: courseRenderViewDidUpdateDistances - front: \(frontGreen), center: \(centerGreen), back: \(backGreen)")
        let eventData: [String: Any] = [
            "event": "GPS_DISTANCES_UPDATED",
            "front": Int(frontGreen),
            "center": Int(centerGreen),
            "back": Int(backGreen)
        ]
        _eventStreamHandler.send(eventData)
    }

    func courseRenderViewDidChange(_ navigationMode: NavigationMode) {
        print("[IGolfViewer3D-Flutter] Delegate: courseRenderViewDidChangeNavigationMode: \(navigationMode.rawValue)")

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
        print("[IGolfViewer3D-Flutter] Delegate: courseRenderViewDidFailWithError: \(error.localizedDescription)")
    }

    func courseRenderViewDidReceiveTap(withDistanceToUser distanceToUser: Double, distanceToFlag: Double) {
        print("[IGolfViewer3D-Flutter] Delegate: USER_TAPS_VIEWER - toUser: \(distanceToUser), toFlag: \(distanceToFlag)")
        let eventData: [String: Any] = [
            "event": "USER_TAPS_VIEWER",
            "targetToUser": Int(distanceToUser),
            "targetToFlag": Int(distanceToFlag)
        ]
        _eventStreamHandler.send(eventData)
    }

    private func configureViewer(with arguments: [String: Any]) {
        print("[IGolfViewer3D-Flutter] configureViewer started")
        guard let apiKey = arguments["apiKey"] as? String,
              let secretKey = arguments["secretKey"] as? String,
              let courseId = arguments["courseId"] as? String else {
            print("[IGolfViewer3D-Flutter] Missing required parameters (apiKey, secretKey, courseId)")
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
        loader.rotateHoleOnLocationChanged = true
        loader.autoAdvanceActive = true
        loader.draw3DCentralLine = false
        
        print("[IGolfViewer3D-Flutter] Loader configured. Starting preload...")

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
                print("[IGolfViewer3D-Flutter] Preload completed successfully")
                // Pass the ready loader to the wrapper view
                // The wrapper view will decide when to attach based on frame availability
                DispatchQueue.main.async {
                    self._wrapperView.setLoader(loader)
                }
            },
            errorHandler: { error in
                if let error = error {
                    print("[IGolfViewer3D-Flutter] Failed to load course - \(error.localizedDescription)")
                } else {
                    print("[IGolfViewer3D-Flutter] Failed to load course - Unknown error")
                }
            }
        )
    }

    deinit {
        print("[IGolfViewer3D-Flutter] Deinit")
        _wrapperView.cleanup()
    }
}
