import Flutter
import UIKit

private final class CourseViewerEventStreamHandler: NSObject, FlutterStreamHandler {
  private var eventSink: FlutterEventSink?

  func onListen(withArguments arguments: Any?, eventSink events: @escaping FlutterEventSink) -> FlutterError? {
    eventSink = events
    return nil
  }

  func onCancel(withArguments arguments: Any?) -> FlutterError? {
    eventSink = nil
    return nil
  }

  func send(_ event: Any?) {
    eventSink?(event)
  }
}

@objcMembers
public final class SwiftFlutterIgolfViewerPlugin: NSObject, FlutterPlugin {
  private var viewerChannel: FlutterMethodChannel?
  private var courseDetailsChannel: FlutterMethodChannel?
  private var eventChannel: FlutterEventChannel?
  private let eventStreamHandler = CourseViewerEventStreamHandler()

  public static func register(with registrar: FlutterPluginRegistrar) {
    let instance = SwiftFlutterIgolfViewerPlugin()
    instance.registerChannels(with: registrar)
  }

  private func registerChannels(with registrar: FlutterPluginRegistrar) {
    let viewerChannel = FlutterMethodChannel(
      name: "flutter_igolf_viewer",
      binaryMessenger: registrar.messenger()
    )
    registrar.addMethodCallDelegate(self, channel: viewerChannel)
    self.viewerChannel = viewerChannel

    let courseDetailsChannel = FlutterMethodChannel(
      name: "plugins.filledstacks.flutter_igolf_course/course_details_api",
      binaryMessenger: registrar.messenger()
    )
    registrar.addMethodCallDelegate(self, channel: courseDetailsChannel)
    self.courseDetailsChannel = courseDetailsChannel

    let eventChannel = FlutterEventChannel(
      name: "plugins.filledstacks.flutter_igolf_viewer/course_viewer_event",
      binaryMessenger: registrar.messenger()
    )
    eventChannel.setStreamHandler(eventStreamHandler)
    self.eventChannel = eventChannel
  }

  public func handle(_ call: FlutterMethodCall, result: @escaping FlutterResult) {
    switch call.method {
    case "getPlatformVersion":
      result("iOS " + UIDevice.current.systemVersion)
    case "initialize":
      result("Flutter iGolf Viewer plugin initialized")
    case "getCountryList",
         "getCourseDetails",
         "getCourseList",
         "getCourseScorecardDetails",
         "getCourseScorecardList",
         "getCourseTeeDetails",
         "getStateList",
         "getCourseGPSDetails",
         "getCourseGPSVectorDetails",
         "getTypedCourseList":
      result(nil)
    default:
      result(FlutterMethodNotImplemented)
    }
  }
}
