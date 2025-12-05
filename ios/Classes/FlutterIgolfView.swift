import Flutter
import UIKit
import IGolfViewer3D

class FlutterIgolfView: NSObject, FlutterPlatformView {
    var renderView: CourseRenderView
    var loader: CourseRenderViewLoader?

    init(
        frame: CGRect,
        viewIdentifier viewId: Int64,
        arguments args: Any?,
        binaryMessenger messenger: FlutterBinaryMessenger?
    ) {
        renderView = CourseRenderView(frame: frame)
        super.init()

        // Parse arguments and configure the viewer
        if let arguments = args as? [String: Any] {
            configureViewer(with: arguments)
        }
    }

    func view() -> UIView {
        return renderView
    }

    private func configureViewer(with arguments: [String: Any]) {
        guard let apiKey = arguments["apiKey"] as? String,
              let secretKey = arguments["secretKey"] as? String,
              let courseId = arguments["courseId"] as? String else {
            print("FlutterIgolfView: Missing required parameters (apiKey, secretKey, courseId)")
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
        loader.autoAdvanceActive = false
        loader.draw3DCentralLine = false

        // Store loader reference
        loader = loader

        // Create loading indicator
        let loadingView = UIActivityIndicatorView(style: .large)
        loadingView.color = .orange
        loadingView.startAnimating()
        loader.setLoading(loadingView)

        // Preload course data asynchronously
        loader.preload(
            completionHandler: { [weak self] in
                guard let self = self else { return }
                // Load the course data into the render view
                self.renderView.load(with: loader)
            },
            errorHandler: { error in
                if let error = error {
                    print("FlutterIgolfView: Failed to load course - \(error.localizedDescription)")
                } else {
                    print("FlutterIgolfView: Failed to load course - Unknown error")
                }
            }
        )
    }

    deinit {
        // Clean up resources
        renderView.invalidate()
        loader = nil
    }
}
