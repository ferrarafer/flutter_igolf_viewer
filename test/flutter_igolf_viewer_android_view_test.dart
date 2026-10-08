import 'package:flutter/services.dart';
import 'package:flutter/widgets.dart';
import 'package:flutter_igolf_viewer/flutter_igolf_viewer.dart';
import 'package:flutter_test/flutter_test.dart';

void main() {
  const viewer = FlutterIgolfViewer(
    apiKey: 'api',
    secretKey: 'secret',
    courseId: 'course',
  );

  final platformViewCalls = <MethodCall>[];

  setUp(() {
    platformViewCalls.clear();
    TestDefaultBinaryMessengerBinding.instance.defaultBinaryMessenger
        .setMockMethodCallHandler(SystemChannels.platform_views, (call) async {
      platformViewCalls.add(call);
      return call.method == 'create' ? 0 : null;
    });
  });

  tearDown(() {
    TestDefaultBinaryMessengerBinding.instance.defaultBinaryMessenger
        .setMockMethodCallHandler(SystemChannels.platform_views, null);
  });

  // A plain AndroidView renders the GLSurfaceView through a virtual display,
  // which Flutter tears down and recreates on every pause/resume. On the
  // Caddie's MediaTek hwcomposer that recreate crashes surfaceflinger and
  // restarts the whole system UI. Hybrid composition has no virtual display.
  testWidgets('embeds the Android view with hybrid composition',
      (tester) async {
    await tester.pumpWidget(
      const Directionality(textDirection: TextDirection.ltr, child: viewer),
    );

    expect(find.byType(AndroidView), findsNothing);
    expect(find.byType(PlatformViewLink), findsOneWidget);

    final create = platformViewCalls.firstWhere((c) => c.method == 'create');
    final arguments = create.arguments as Map<Object?, Object?>;
    expect(arguments['viewType'], 'flutter_igolf_viewer');
    expect(arguments['hybrid'], isTrue);
  }, variant: TargetPlatformVariant.only(TargetPlatform.android));
}
