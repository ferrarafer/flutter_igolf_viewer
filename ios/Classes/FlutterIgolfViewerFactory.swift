import Flutter
import UIKit

final class FlutterIgolfViewerFactory: NSObject, FlutterPlatformViewFactory {
  private let messenger: FlutterBinaryMessenger
  private weak var eventStreamHandler: CourseViewerEventStreamHandler?

  init(messenger: FlutterBinaryMessenger, eventStreamHandler: CourseViewerEventStreamHandler) {
    self.messenger = messenger
    self.eventStreamHandler = eventStreamHandler
    super.init()
  }

  func createArgsCodec() -> FlutterMessageCodec & NSObjectProtocol {
    FlutterStandardMessageCodec.sharedInstance()
  }

  func create(
    withFrame frame: CGRect,
    viewIdentifier viewId: Int64,
    arguments args: Any?
  ) -> FlutterPlatformView {
    FlutterIgolfViewerPlatformView(
      frame: frame,
      viewId: viewId,
      arguments: args,
      messenger: messenger,
      eventStreamHandler: eventStreamHandler
    )
  }
}

private final class FlutterIgolfViewerPlatformView: NSObject, FlutterPlatformView {
  private let containerView: UIView
  private let methodChannel: FlutterMethodChannel
  private weak var eventStreamHandler: CourseViewerEventStreamHandler?

  init(
    frame: CGRect,
    viewId: Int64,
    arguments: Any?,
    messenger: FlutterBinaryMessenger,
    eventStreamHandler: CourseViewerEventStreamHandler?
  ) {
    containerView = UIView(frame: frame)
    containerView.backgroundColor = .black

    self.eventStreamHandler = eventStreamHandler
    methodChannel = FlutterMethodChannel(
      name: "plugins.filledstacks.flutter_igolf_viewer/course_viewer_method",
      binaryMessenger: messenger
    )

    super.init()

    methodChannel.setMethodCallHandler(handle)
    setupPlaceholderView(with: arguments)
  }

  deinit {
    methodChannel.setMethodCallHandler(nil)
  }

  func view() -> UIView {
    containerView
  }

  private func setupPlaceholderView(with arguments: Any?) {
    let placeholder = UILabel()
    placeholder.translatesAutoresizingMaskIntoConstraints = false
    placeholder.textAlignment = .center
    placeholder.numberOfLines = 0
    placeholder.textColor = .white
    placeholder.font = .systemFont(ofSize: 14, weight: .medium)
    placeholder.text = "flutter_igolf_viewer iOS view is not yet implemented"

    containerView.addSubview(placeholder)
    NSLayoutConstraint.activate([
      placeholder.centerXAnchor.constraint(equalTo: containerView.centerXAnchor),
      placeholder.centerYAnchor.constraint(equalTo: containerView.centerYAnchor),
      placeholder.leadingAnchor.constraint(greaterThanOrEqualTo: containerView.leadingAnchor, constant: 16),
      placeholder.trailingAnchor.constraint(lessThanOrEqualTo: containerView.trailingAnchor, constant: -16)
    ])

    if let args = arguments as? [String: Any],
       let courseId = args["courseId"] as? String {
      sendInitializationEvent(courseId: courseId)
    }
  }

  private func sendInitializationEvent(courseId: String) {
    let payload: [String: Any] = [
      "event": "VIEWER_UNAVAILABLE",
      "courseId": courseId
    ]
    eventStreamHandler?.send(payload)
  }

  private func handle(_ call: FlutterMethodCall, result: FlutterResult) {
    result(FlutterMethodNotImplemented)
  }
}
