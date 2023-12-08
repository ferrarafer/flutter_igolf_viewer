import 'package:flutter_test/flutter_test.dart';
import 'package:flutter_igolf_viewer/flutter_igolf_viewer.dart';
import 'package:flutter_igolf_viewer/flutter_igolf_viewer_platform_interface.dart';
import 'package:flutter_igolf_viewer/flutter_igolf_viewer_method_channel.dart';
import 'package:plugin_platform_interface/plugin_platform_interface.dart';

class MockFlutterIgolfViewerPlatform
    with MockPlatformInterfaceMixin
    implements FlutterIgolfViewerPlatform {

  @override
  Future<String?> getPlatformVersion() => Future.value('42');
}

void main() {
  final FlutterIgolfViewerPlatform initialPlatform = FlutterIgolfViewerPlatform.instance;

  test('$MethodChannelFlutterIgolfViewer is the default instance', () {
    expect(initialPlatform, isInstanceOf<MethodChannelFlutterIgolfViewer>());
  });

  test('getPlatformVersion', () async {
    FlutterIgolfViewer flutterIgolfViewerPlugin = FlutterIgolfViewer();
    MockFlutterIgolfViewerPlatform fakePlatform = MockFlutterIgolfViewerPlatform();
    FlutterIgolfViewerPlatform.instance = fakePlatform;

    expect(await flutterIgolfViewerPlugin.getPlatformVersion(), '42');
  });
}
