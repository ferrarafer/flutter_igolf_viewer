import 'package:flutter/foundation.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

class FlutterIgolfViewer extends StatelessWidget {
  final String apiKey;
  final String secretKey;
  final String courseId;
  final int startingHole;
  final int initialTeeBox;
  final String? parData;
  final String? gpsDetails;
  final String? vectorGpsObject;
  final int golferIconIndex;
  final bool isMetricUnits;
  final int freeCamZoom;

  const FlutterIgolfViewer({
    super.key,
    required this.apiKey,
    required this.secretKey,
    required this.courseId,
    this.startingHole = 1,
    this.initialTeeBox = 0,
    this.parData,
    this.gpsDetails,
    this.vectorGpsObject,
    this.golferIconIndex = 6,
    this.isMetricUnits = true,
    this.freeCamZoom = 100,
  });

  @visibleForTesting
  Map<String, dynamic> get creationParams => {
        "apiKey": apiKey,
        "secretKey": secretKey,
        "courseId": courseId,
        "startingHole": startingHole,
        "initialTeeBox": initialTeeBox,
        "parData": parData,
        "gpsDetails": gpsDetails,
        "vectorGpsObject": vectorGpsObject,
        "golferIconIndex": golferIconIndex,
        "isMetricUnits": isMetricUnits,
        "freeCamZoom": freeCamZoom.clamp(0, 100),
      };

  @override
  Widget build(BuildContext context) {
    // This is used in the platform side to register the view.
    const String viewType = 'flutter_igolf_viewer';

    switch (defaultTargetPlatform) {
      case TargetPlatform.android:
        return AndroidView(
          viewType: viewType,
          layoutDirection: TextDirection.ltr,
          creationParams: creationParams,
          creationParamsCodec: const StandardMessageCodec(),
        );
      case TargetPlatform.iOS:
        return UiKitView(
          viewType: viewType,
          layoutDirection: TextDirection.ltr,
          creationParams: creationParams,
          creationParamsCodec: const StandardMessageCodec(),
        );
      default:
        throw UnsupportedError(
          'Platform $defaultTargetPlatform is not yet supported by flutter_igolf_viewer.',
        );
    }
  }
}
