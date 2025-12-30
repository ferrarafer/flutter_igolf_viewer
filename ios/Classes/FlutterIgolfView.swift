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

    init(
        frame: CGRect,
        viewIdentifier viewId: Int64,
        arguments args: Any?,
        binaryMessenger messenger: FlutterBinaryMessenger?
    ) {
        print("[IGolfViewer3D-Flutter] FlutterIgolfView init. Frame: \(frame)")
        _wrapperView = IGolfWrapperView(frame: frame)
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
    }

    func courseRenderViewDidLoadHoleData() {
        print("[IGolfViewer3D-Flutter] Delegate: courseRenderViewDidLoadHoleData")
        guard !_hasApplied3DMode, let renderView = _wrapperView.renderView else { return }
        _hasApplied3DMode = true
        print("[IGolfViewer3D-Flutter] Setting NavigationMode to 3D FreeCam (post hole load)")
        renderView.navigationMode = .modeFreeCam
    }
    
    func courseRenderViewDidChange(_ navigationMode: NavigationMode) {
        print("[IGolfViewer3D-Flutter] Delegate: courseRenderViewDidChangeNavigationMode: \(navigationMode.rawValue)")
    }
    
    func courseRenderViewDidFailWithError(_ error: Error) {
        print("[IGolfViewer3D-Flutter] Delegate: courseRenderViewDidFailWithError: \(error.localizedDescription)")
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
