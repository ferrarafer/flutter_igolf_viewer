import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';

import 'flutter_igolf_course_details_api_platform_interface.dart';

/// An implementation of [FlutterIgolfCourseDetailsApiPlatform] that uses
/// method channels.
class MethodChannelFlutterIgolfCourseDetailsApi
    extends FlutterIgolfCourseDetailsApiPlatform {
  /// The method channel used to interact with the native platform.
  @visibleForTesting
  final methodChannel = const MethodChannel(
    'plugins.filledstacks.flutter_igolf_course/course_details_api',
  );

  /// Sets the Api Key and Secret Key to communicate with Api.
  @override
  Future<String?> initialize({
    required String apiKey,
    required String secretKey,
  }) async {
    return await methodChannel.invokeMethod<String>('initialize', {
      'apiKey': apiKey,
      'secretKey': secretKey,
    });
  }

  /// Returns location, address, operations information about a particular
  /// course.
  @override
  Future<String?> getCourseDetails({
    int? countryFormat,
    int? detailLevel,
    required String courseId,
    int? stateFormat,
  }) async {
    return await methodChannel.invokeMethod<String>('getCourseDetails', {
      'countryFormat': countryFormat,
      'detailLevel': detailLevel,
      'id_course': courseId,
      'stateFormat': stateFormat,
    });
  }

  /// Returns slope, rating, name, color, yardage for men's and women's
  /// teeboxes at a course.
  @override
  Future<String?> getCourseTeeDetails({
    int? detailLevel,
    required String courseId,
  }) async {
    return await methodChannel.invokeMethod<String>('getCourseTeeDetails', {
      'detailLevel': detailLevel,
      'id_course': courseId,
    });
  }

  /// Request returns par and handicap information for a particular course.
  @override
  Future<String?> getCourseScorecardDetails({
    required String courseId,
  }) async {
    return await methodChannel.invokeMethod<String>(
      'getCourseScorecardDetails',
      {'id_course': courseId},
    );
  }

  /// Request returns par and handicap information for an array of up to 20
  /// courses submitted.
  @override
  Future<String?> getCourseScorecardList({
    required List<String> coursesIds,
    required int courseName,
  }) async {
    return await methodChannel.invokeMethod<String>('getCourseScorecardList', {
      'id_courseArray': coursesIds,
      'courseName': courseName,
    });
  }

  /// Returns the list of countries used in the iGolf system.
  ///
  /// Possible values:
  ///   AF (Africa)
  ///   AS (Asia)
  ///   EU (Europe)
  ///   NA (North America)
  ///   OC (Oceania)
  ///   SA (South America)
  ///   All (All Countries)
  @override
  Future<String?> getCountryList({String continentId = 'All'}) async {
    return await methodChannel.invokeMethod<String>('getCountryList', {
      'id_continent': continentId,
    });
  }

  /// Returns the list of states used in the iGolf system.
  @override
  Future<String?> getStateList({required int countryId}) async {
    return await methodChannel.invokeMethod<String>('getStateList', {
      'id_country': countryId,
    });
  }

  /// Returns a list of courses (with a maximum of 100 results per page,
  /// maximum of 5 pages).
  @override
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
  }) async {
    return await methodChannel.invokeMethod<String>('getCourseList', {
      'active': active,
      'city': city,
      'countryFormat': countryFormat,
      'courseName': courseName,
      'id_country': countryId,
      'id_state': stateId,
      'radius': radius,
      'referenceLatitude': referenceLatitude,
      'referenceLongitude': referenceLongitude,
      'stateFormat': stateFormat,
      'zipcode': zipcode,
      'page': page,
      'resultsPerPage': resultsPerPage,
    });
  }

  /// /// Returns a list of courses (with a maximum of 100 results per page,
  /// maximum of 5 pages).
  @override
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
  }) async {
    return await methodChannel.invokeMethod<String>(
      'getTypedCourseList',
      {
        'active': active,
        'city': city,
        'countryFormat': countryFormat,
        'courseName': courseName,
        'id_country': countryId,
        'id_state': stateId,
        'radius': radius,
        'referenceLatitude': referenceLatitude,
        'referenceLongitude': referenceLongitude,
        'stateFormat': stateFormat,
        'zipcode': zipcode,
        'page': page,
        'resultsPerPage': resultsPerPage,
      },
    );
  }
}
