import 'package:plugin_platform_interface/plugin_platform_interface.dart';

import 'flutter_igolf_viewer_method_channel.dart';

abstract class FlutterIgolfViewerPlatform extends PlatformInterface {
  /// Constructs a FlutterIgolfViewerPlatform.
  FlutterIgolfViewerPlatform() : super(token: _token);

  static final Object _token = Object();

  static FlutterIgolfViewerPlatform _instance = MethodChannelFlutterIgolfViewer();

  /// The default instance of [FlutterIgolfViewerPlatform] to use.
  ///
  /// Defaults to [MethodChannelFlutterIgolfViewer].
  static FlutterIgolfViewerPlatform get instance => _instance;

  /// Platform-specific implementations should set this with their own
  /// platform-specific class that extends [FlutterIgolfViewerPlatform] when
  /// they register themselves.
  static set instance(FlutterIgolfViewerPlatform instance) {
    PlatformInterface.verifyToken(instance, _token);
    _instance = instance;
  }

  Future<String?> getPlatformVersion() {
    throw UnimplementedError('platformVersion() has not been implemented.');
  }
}
