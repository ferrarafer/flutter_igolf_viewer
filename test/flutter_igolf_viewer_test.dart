import 'package:flutter_igolf_viewer/flutter_igolf_viewer.dart';
import 'package:flutter_test/flutter_test.dart';

void main() {
  test('includes clamped free cam zoom in platform creation params', () {
    const viewer = FlutterIgolfViewer(
      apiKey: 'api',
      secretKey: 'secret',
      courseId: 'course',
      freeCamZoom: 120,
    );

    expect(viewer.creationParams['freeCamZoom'], 100);
  });
}
