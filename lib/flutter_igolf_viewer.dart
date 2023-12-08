
import 'flutter_igolf_viewer_platform_interface.dart';

class FlutterIgolfViewer {
  Future<String?> getPlatformVersion() {
    return FlutterIgolfViewerPlatform.instance.getPlatformVersion();
  }
}
