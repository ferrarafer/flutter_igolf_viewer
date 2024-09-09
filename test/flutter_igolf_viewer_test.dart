// import 'package:flutter_igolf_viewer/flutter_igolf_viewer.dart';
// import 'package:flutter_test/flutter_test.dart';
// import 'package:plugin_platform_interface/plugin_platform_interface.dart';

// class MockFlutterIgolfViewerPlatform
//     with MockPlatformInterfaceMixin
//     implements FlutterIgolfViewerPlatform {
//   @override
//   Future<String?> getPlatformVersion() => Future.value('42');
// }

// void main() {
//   final FlutterIgolfViewerPlatform initialPlatform =
//       FlutterIgolfViewerPlatform.instance;

//   test('$MethodChannelFlutterIgolfViewer is the default instance', () {
//     expect(initialPlatform, isInstanceOf<MethodChannelFlutterIgolfViewer>());
//   });

//   test('getPlatformVersion', () async {
//     FlutterIgolfViewer flutterIgolfViewerPlugin = const FlutterIgolfViewer(
//       apiKey: '123456',
//       secretKey: '123456',
//       courseId: '123456',
//     );
//     MockFlutterIgolfViewerPlatform fakePlatform =
//         MockFlutterIgolfViewerPlatform();
//     FlutterIgolfViewerPlatform.instance = fakePlatform;

//     expect(await flutterIgolfViewerPlugin.getPlatformVersion(), '42');
//   });
// }
