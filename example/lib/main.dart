import 'package:flutter/material.dart';
import 'dart:async';

import 'package:flutter/services.dart';
import 'package:flutter_igolf_viewer/flutter_igolf_course_details_api_method_channel.dart';
import 'package:flutter_igolf_viewer/flutter_igolf_viewer.dart';

const _igolfApiKey = String.fromEnvironment('IGOLF_API_KEY');
const _igolfSecretKey = String.fromEnvironment('IGOLF_SECRET_KEY');

void main() {
  runApp(const MyApp());
}

class MyApp extends StatefulWidget {
  const MyApp({super.key});

  @override
  State<MyApp> createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  final _channel = MethodChannelFlutterIgolfCourseDetailsApi();

  @override
  void initState() {
    super.initState();
    initPlatformState();
  }

  // Platform messages are asynchronous, so we initialize in an async method.
  Future<void> initPlatformState() async {
    String data;
    // Platform messages may fail, so we use a try/catch PlatformException.
    // We also handle the message potentially returning null.
    try {
      await _channel.initialize(
        apiKey: _igolfApiKey,
        secretKey: _igolfSecretKey,
      );

      data = await _channel.getTypedCourseList(zipcode: '92108') ?? '';
      debugPrint(data);
    } on PlatformException catch (e) {
      data = 'PlatformException ${e.code}. ${e.message}';
    }

    // If the widget was removed from the tree while the asynchronous platform
    // message was in flight, we want to discard the reply rather than calling
    // setState to update our non-existent appearance.
    if (!mounted) return;

    setState(() {});
  }

  @override
  Widget build(BuildContext context) {
    return const MaterialApp(
      home: Scaffold(
        body: FlutterIgolfViewer(
          apiKey: _igolfApiKey,
          secretKey: _igolfSecretKey,
          courseId: "Rr049ibjL111",
        ),
      ),
    );
  }
}
