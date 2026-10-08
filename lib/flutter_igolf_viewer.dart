import 'package:flutter/foundation.dart';
import 'package:flutter/gestures.dart';
import 'package:flutter/rendering.dart';
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
        return _buildAndroidView(viewType);
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

  /// Hybrid composition, not a plain [AndroidView]: the viewer is a
  /// GLSurfaceView, so [AndroidView] falls back to a virtual display that
  /// Flutter recreates on every pause/resume. On MediaTek hwcomposers (the
  /// Caddie) that recreate crashes surfaceflinger and restarts the system UI.
  Widget _buildAndroidView(String viewType) {
    return PlatformViewLink(
      viewType: viewType,
      surfaceFactory: (context, controller) => AndroidViewSurface(
        controller: controller as AndroidViewController,
        gestureRecognizers: const <Factory<OneSequenceGestureRecognizer>>{},
        hitTestBehavior: PlatformViewHitTestBehavior.opaque,
      ),
      onCreatePlatformView: (params) {
        return PlatformViewsService.initExpensiveAndroidView(
          id: params.id,
          viewType: viewType,
          layoutDirection: TextDirection.ltr,
          creationParams: creationParams,
          creationParamsCodec: const StandardMessageCodec(),
          onFocus: () => params.onFocusChanged(true),
        )
          ..addOnPlatformViewCreatedListener(params.onPlatformViewCreated)
          ..create();
      },
    );
  }
}
