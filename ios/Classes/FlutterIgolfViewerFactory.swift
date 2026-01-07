import Flutter
import UIKit

class FlutterIgolfViewerFactory: NSObject, FlutterPlatformViewFactory {
    private var messenger: FlutterBinaryMessenger
    private var eventStreamHandler: CourseViewerEventStreamHandler

    init(messenger: FlutterBinaryMessenger, eventStreamHandler: CourseViewerEventStreamHandler) {
        self.messenger = messenger
        self.eventStreamHandler = eventStreamHandler
        super.init()
    }

    func create(
        withFrame frame: CGRect,
        viewIdentifier viewId: Int64,
        arguments args: Any?
    ) -> FlutterPlatformView {
        return FlutterIgolfView(
            frame: frame,
            viewIdentifier: viewId,
            arguments: args,
            binaryMessenger: messenger,
            eventStreamHandler: eventStreamHandler
        )
    }

    func createArgsCodec() -> FlutterMessageCodec & NSObjectProtocol {
        return FlutterStandardMessageCodec.sharedInstance()
    }
}
