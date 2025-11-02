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
  private let networkClient = IGolfNetworkClient()
  private var apiKey: String = ""
  private var secretKey: String = ""
  private var isInitialized = false

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
      handleInitialize(call, result: result)
    case "getCountryList":
      guard ensureInitialized(result: result) else { return }
      handleGetCountryList(call, result: result)
    case "getCourseDetails":
      guard ensureInitialized(result: result) else { return }
      handleGetCourseDetails(call, result: result)
    case "getCourseList":
      guard ensureInitialized(result: result) else { return }
      handleGetCourseList(call, result: result)
    case "getCourseTeeDetails":
      guard ensureInitialized(result: result) else { return }
      handleGetCourseTeeDetails(call, result: result)
    case "getCourseScorecardDetails":
      guard ensureInitialized(result: result) else { return }
      handleGetCourseScorecardDetails(call, result: result)
    case "getCourseScorecardList":
      guard ensureInitialized(result: result) else { return }
      handleGetCourseScorecardList(call, result: result)
    case "getStateList":
      guard ensureInitialized(result: result) else { return }
      handleGetStateList(call, result: result)
    case "getCourseGPSDetails":
      guard ensureInitialized(result: result) else { return }
      handleGetCourseGPSDetails(call, result: result)
    case "getCourseGPSVectorDetails":
      guard ensureInitialized(result: result) else { return }
      handleGetCourseGPSVectorDetails(call, result: result)
    case "getTypedCourseList":
      guard ensureInitialized(result: result) else { return }
      handleGetTypedCourseList(call, result: result)
    default:
      result(FlutterMethodNotImplemented)
    }
  }

  private func handleInitialize(_ call: FlutterMethodCall, result: @escaping FlutterResult) {
    guard let arguments = call.arguments as? [String: Any] else {
      result(FlutterError(code: "INVALIDARGS", message: "You must provide valid api and secret keys", details: nil))
      return
    }

    guard let apiKey = arguments["apiKey"] as? String, !apiKey.isEmpty,
          let secretKey = arguments["secretKey"] as? String, !secretKey.isEmpty else {
      result(FlutterError(code: "INVALIDARGS", message: "You must provide valid api and secret keys", details: nil))
      return
    }

    self.apiKey = apiKey
    self.secretKey = secretKey
    isInitialized = true
    result("Flutter iGolf Viewer plugin initialized")
  }

  private func handleGetCountryList(_ call: FlutterMethodCall, result: @escaping FlutterResult) {
    let arguments = call.arguments as? [String: Any] ?? [:]
    let continentId = (arguments["id_continent"] as? String) ?? "All"
    guard !continentId.isEmpty else {
      result(FlutterError(code: "INVALIDARGS", message: "You must provide valid continent ID", details: nil))
      return
    }

    performRequest(endpoint: "CountryList", body: ["id_continent": continentId], result: result)
  }

  private func handleGetCourseDetails(_ call: FlutterMethodCall, result: @escaping FlutterResult) {
    let arguments = call.arguments as? [String: Any] ?? [:]
    guard let courseId = arguments["id_course"] as? String, !courseId.isEmpty else {
      result(FlutterError(code: "INVALIDARGS", message: "You must provide valid course ID", details: nil))
      return
    }

    let body: [String: Any?] = [
      "countryFormat": intValue(arguments["countryFormat"]),
      "id_course": courseId,
      "detailLevel": intValue(arguments["detailLevel"]),
      "stateFormat": intValue(arguments["stateFormat"])
    ]

    performRequest(endpoint: "CourseDetails", body: body, result: result)
  }

  private func handleGetCourseList(_ call: FlutterMethodCall, result: @escaping FlutterResult) {
    let arguments = call.arguments as? [String: Any] ?? [:]
    let active = intValue(arguments["active"]) ?? 1

    let body: [String: Any?] = [
      "active": active,
      "city": arguments["city"] as? String,
      "countryFormat": intValue(arguments["countryFormat"]),
      "courseName": arguments["courseName"] as? String,
      "id_country": intValue(arguments["id_country"]),
      "id_state": intValue(arguments["id_state"]),
      "radius": arguments["radius"] as? String,
      "referenceLatitude": doubleValue(arguments["referenceLatitude"]),
      "referenceLongitude": doubleValue(arguments["referenceLongitude"]),
      "stateFormat": intValue(arguments["stateFormat"]),
      "zipcode": arguments["zipcode"] as? String,
      "page": intValue(arguments["page"]),
      "resultsPerPage": intValue(arguments["resultsPerPage"])
    ]

    performRequest(endpoint: "CourseList", body: body, result: result)
  }

  private func handleGetCourseTeeDetails(_ call: FlutterMethodCall, result: @escaping FlutterResult) {
    let arguments = call.arguments as? [String: Any] ?? [:]
    guard let courseId = arguments["id_course"] as? String, !courseId.isEmpty else {
      result(FlutterError(code: "INVALIDARGS", message: "You must provide valid course ID", details: nil))
      return
    }

    let body: [String: Any?] = [
      "id_course": courseId,
      "detailLevel": intValue(arguments["detailLevel"])
    ]

    performRequest(endpoint: "CourseTeeDetails", body: body, result: result)
  }

  private func handleGetCourseScorecardDetails(_ call: FlutterMethodCall, result: @escaping FlutterResult) {
    let arguments = call.arguments as? [String: Any] ?? [:]
    guard let courseId = arguments["id_course"] as? String, !courseId.isEmpty else {
      result(FlutterError(code: "INVALIDARGS", message: "You must provide valid course ID", details: nil))
      return
    }

    performRequest(endpoint: "CourseScorecardDetails", body: ["id_course": courseId], result: result)
  }

  private func handleGetCourseScorecardList(_ call: FlutterMethodCall, result: @escaping FlutterResult) {
    let arguments = call.arguments as? [String: Any] ?? [:]
    let courseIds = stringArray(from: arguments["id_courseArray"])
    let courseName = intValue(arguments["courseName"]) ?? 1

    let body: [String: Any?] = [
      "id_courseArray": courseIds,
      "courseName": courseName
    ]

    performRequest(endpoint: "CourseScorecardList", body: body, result: result)
  }

  private func handleGetStateList(_ call: FlutterMethodCall, result: @escaping FlutterResult) {
    let arguments = call.arguments as? [String: Any] ?? [:]
    guard let countryId = intValue(arguments["id_country"]) else {
      result(FlutterError(code: "INVALIDARGS", message: "You must provide valid country ID", details: nil))
      return
    }

    performRequest(endpoint: "StateList", body: ["id_country": countryId], result: result)
  }

  private func handleGetCourseGPSDetails(_ call: FlutterMethodCall, result: @escaping FlutterResult) {
    let arguments = call.arguments as? [String: Any] ?? [:]
    guard let courseId = arguments["id_course"] as? String, !courseId.isEmpty else {
      result(FlutterError(code: "INVALIDARGS", message: "You must provide valid course ID", details: nil))
      return
    }

    performRequest(endpoint: "CourseGPSDetails", body: ["id_course": courseId], result: result)
  }

  private func handleGetCourseGPSVectorDetails(_ call: FlutterMethodCall, result: @escaping FlutterResult) {
    let arguments = call.arguments as? [String: Any] ?? [:]
    guard let courseId = arguments["id_course"] as? String, !courseId.isEmpty else {
      result(FlutterError(code: "INVALIDARGS", message: "You must provide valid course ID", details: nil))
      return
    }

    performRequest(endpoint: "CourseGPSVectorDetails", body: ["id_course": courseId], result: result)
  }

  private func handleGetTypedCourseList(_ call: FlutterMethodCall, result: @escaping FlutterResult) {
    let arguments = call.arguments as? [String: Any] ?? [:]
    let active = intValue(arguments["active"]) ?? 1
    guard let zipcode = arguments["zipcode"] as? String, !zipcode.isEmpty else {
      result(FlutterError(code: "INVALIDARGS", message: "You must provide a zipcode", details: nil))
      return
    }

    let body: [String: Any?] = [
      "active": active,
      "zipcode": zipcode
    ]

    performRequest(
      endpoint: "CourseList",
      body: body,
      transform: { response in
        self.parseTotalCourses(from: response)
      },
      result: result
    )
  }

  private func ensureInitialized(result: @escaping FlutterResult) -> Bool {
    guard isInitialized else {
      result(FlutterError(code: "NOT_INITIALIZED", message: "Flutter iGolf Viewer plugin is not initialized.", details: nil))
      return false
    }
    return true
  }

  private func performRequest(
    endpoint: String,
    body: [String: Any?],
    transform: ((String) -> Any?)? = nil,
    result: @escaping FlutterResult
  ) {
    networkClient.sendRequest(endpoint: endpoint, body: body, apiKey: apiKey, secretKey: secretKey) { response in
      DispatchQueue.main.async {
        switch response {
        case .success(let payload):
          if let transform = transform {
            result(transform(payload))
          } else {
            result(payload)
          }
        case .failure(let error):
          if let networkError = error as? IGolfNetworkClient.NetworkError,
             networkError == .invalidURL {
            result(FlutterError(code: "SIGNATURE_ERROR", message: "Failed to build request signature", details: nil))
          } else {
            result(FlutterError(code: "NETWORK_ERROR", message: error.localizedDescription, details: nil))
          }
        }
      }
    }
  }

  private func intValue(_ value: Any?) -> Int? {
    if let int = value as? Int {
      return int
    }
    if let number = value as? NSNumber {
      return number.intValue
    }
    if let string = value as? String {
      return Int(string)
    }
    return nil
  }

  private func doubleValue(_ value: Any?) -> Double? {
    if let double = value as? Double {
      return double
    }
    if let number = value as? NSNumber {
      return number.doubleValue
    }
    if let string = value as? String {
      return Double(string)
    }
    return nil
  }

  private func stringArray(from value: Any?) -> [String] {
    if let array = value as? [String] {
      return array
    }
    if let array = value as? [Any] {
      return array.compactMap { $0 as? String }
    }
    return []
  }

  private func parseTotalCourses(from response: String) -> String? {
    guard let data = response.data(using: .utf8),
          let json = try? JSONSerialization.jsonObject(with: data, options: []) as? [String: Any],
          let totalCourses = json["totalCourses"] else {
      return nil
    }
    return stringValue(from: totalCourses)
  }

  private func stringValue(from value: Any) -> String? {
    if let string = value as? String {
      return string
    }
    if let number = value as? NSNumber {
      let doubleValue = number.doubleValue
      if doubleValue.rounded() == doubleValue {
        return String(number.intValue)
      }
      return number.stringValue
    }
    if let int = value as? Int {
      return String(int)
    }
    if let double = value as? Double {
      if double.rounded() == double {
        return String(Int(double))
      }
      return String(double)
    }
    return nil
  }
}
