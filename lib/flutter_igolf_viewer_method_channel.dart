import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';

import 'flutter_igolf_viewer_platform_interface.dart';

/// An implementation of [FlutterIgolfViewerPlatform] that uses method channels.
class MethodChannelFlutterIgolfViewer extends FlutterIgolfViewerPlatform {
  /// The method channel used to interact with the native platform.
  @visibleForTesting
  final methodChannel = const MethodChannel('flutter_igolf_viewer');

  @override
  Future<String?> initialize({
    required String apiKey,
    required String secretKey,
  }) async {
    final arguments = {'apiKey': apiKey, 'secretKey': secretKey};
    return await methodChannel.invokeMethod<String>('initialize', arguments);
  }

  @override
  Future<String?> getCourseDetails({required String courseId}) async {
    final arguments = {'courseId': courseId};
    return await methodChannel.invokeMethod<String>(
      'getCourseDetails',
      arguments,
    );
  }

  @override
  Future<String?> getCourseList({required String zipcode}) async {
    // final arguments = {'active': 1, 'zipcode': '92108'};
    final arguments = {'active': 1, 'zipcode': zipcode};
    return await methodChannel.invokeMethod<String>('getCourseList', arguments);
  }

  @override
  Future<String?> getTypedCourseList({required String zipcode}) async {
    final arguments = {'active': 1, 'zipcode': zipcode};
    return await methodChannel.invokeMethod<String>(
      'getTypedCourseList',
      arguments,
    );
  }
}
