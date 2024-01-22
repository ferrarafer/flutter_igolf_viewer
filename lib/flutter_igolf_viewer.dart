import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

class FlutterIgolfViewer extends StatelessWidget {
  final String apiKey;
  final String secretKey;
  final String courseId;
  const FlutterIgolfViewer({
    super.key,
    required this.apiKey,
    required this.secretKey,
    required this.courseId,
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
    };

    return AndroidView(
      viewType: viewType,
      layoutDirection: TextDirection.ltr,
      creationParams: creationParams,
      creationParamsCodec: const StandardMessageCodec(),
    );
  }
}
