import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

class FlutterIgolfViewer extends StatelessWidget {
  final String apiKey;
  final String secretKey;
  final String courseId;
  final String? parData;
  final String? gpsDetails;
  final String? vectorGpsObject;
  final bool isMetricUnits;
  const FlutterIgolfViewer({
    super.key,
    required this.apiKey,
    required this.secretKey,
    required this.courseId,
    this.parData,
    this.gpsDetails,
    this.vectorGpsObject,
    this.isMetricUnits = true,
  });

  @override
  Widget build(BuildContext context) {
    // This is used in the platform side to register the view.
    const String viewType = 'flutter_igolf_viewer';

    // Pass parameters to the platform side.
    final Map<String, dynamic> creationParams = {
      "apiKey": apiKey,
      "secretKey": secretKey,
      "courseId": courseId,
      "parData": parData,
      "gpsDetails": gpsDetails,
      "vectorGpsObject": vectorGpsObject,
      "isMetricUnits": isMetricUnits,
    };

    return AndroidView(
      viewType: viewType,
      layoutDirection: TextDirection.ltr,
      creationParams: creationParams,
      creationParamsCodec: const StandardMessageCodec(),
    );
  }
}
