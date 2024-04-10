import 'package:plugin_platform_interface/plugin_platform_interface.dart';

import 'flutter_igolf_course_details_api_method_channel.dart';

abstract class FlutterIgolfCourseDetailsApiPlatform extends PlatformInterface {
  /// Constructs a FlutterIgolfCourseDetailsApiPlatform.
  FlutterIgolfCourseDetailsApiPlatform() : super(token: _token);

  static final Object _token = Object();

  static FlutterIgolfCourseDetailsApiPlatform _instance =
      MethodChannelFlutterIgolfCourseDetailsApi();

  /// The default instance of [FlutterIgolfCourseDetailsApiPlatform] to use.
  ///
  /// Defaults to [FlutterIgolfCourseDetailsApiPlatform].
  static FlutterIgolfCourseDetailsApiPlatform get instance => _instance;

  /// Platform-specific implementations should set this with their own
  /// platform-specific class that extends
  /// [FlutterIgolfCourseDetailsApiPlatform] when they register themselves.
  static set instance(FlutterIgolfCourseDetailsApiPlatform instance) {
    PlatformInterface.verifyToken(instance, _token);
    _instance = instance;
  }

  Future<String?> initialize({
    required String apiKey,
    required String secretKey,
  }) {
    throw UnimplementedError('initialize() has not been implemented.');
  }

  Future<String?> getCourseDetails({
    int? countryFormat,
    required String courseId,
    int? detailLevel,
    int? stateFormat,
  }) {
    throw UnimplementedError('getCourseDetails() has not been implemented.');
  }

  Future<String?> getCourseTeeDetails({
    required String courseId,
    int? detailLevel,
  }) {
    throw UnimplementedError('getCourseTeeDetails() has not been implemented.');
  }

  Future<String?> getCourseScorecardDetails({
    required String courseId,
  }) {
    throw UnimplementedError(
      'getCourseScorecardDetails() has not been implemented.',
    );
  }

  Future<String?> getCourseScorecardList({
    required List<String> coursesIds,
    required int courseName,
  }) {
    throw UnimplementedError(
      'getCourseScorecardList() has not been implemented.',
    );
  }

  Future<String?> getCountryList({required String continentId}) {
    throw UnimplementedError('getCountryList() has not been implemented.');
  }

  Future<String?> getStateList({required int countryId}) {
    throw UnimplementedError('getStateList() has not been implemented.');
  }

  Future<String?> getCourseList({
    int active = 1,
    String? city,
    int? countryFormat,
    String? courseName,
    int? countryId,
    int? stateId,
    String? radius,
    double? referenceLatitude,
    double? referenceLongitude,
    int? stateFormat,
    String? zipcode,
    int? page,
    int? resultsPerPage,
  }) {
    throw UnimplementedError('getCourseList() has not been implemented.');
  }

  Future<String?> getTypedCourseList({
    int active = 1,
    String? city,
    int? countryFormat,
    String? courseName,
    int? countryId,
    int? stateId,
    String? radius,
    double? referenceLatitude,
    double? referenceLongitude,
    int? stateFormat,
    String? zipcode,
    int? page,
    int? resultsPerPage,
  }) {
    throw UnimplementedError('getTypedCourseList() has not been implemented.');
  }
}
