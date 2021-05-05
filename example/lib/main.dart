import 'package:flutter/material.dart';
import 'dart:async';

import 'package:flutter/services.dart';
import 'package:flutter_plugin_scan_mrz/camera_kit_view.dart';
import 'package:flutter_plugin_scan_mrz/flutter_plugin_scan_mrz.dart';
import 'package:flutter_plugin_scan_mrz/camera_kit_controller.dart';

import 'NativeCallBack.dart';
import 'package:provider/provider.dart';
import 'package:permission_handler/permission_handler.dart';


void main() {
  runApp(MyApp());
}

class MyApp extends StatefulWidget {
  @override
  _MyAppState createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  final cameraKitController = CameraKitController();


  String _platformVersion = 'Unknown';

  @override
  void initState() {
    super.initState();
    initPlatformState();
  }

  // Platform messages are asynchronous, so we initialize in an async method.
  Future<void> initPlatformState() async {
    String platformVersion;
    // Platform messages may fail, so we use a try/catch PlatformException.
    try {
      platformVersion = await FlutterPluginScanMrz.platformVersion;
    } on PlatformException {
      platformVersion = 'Failed to get platform version.';
    }
    if (!mounted) return;

    setState(() {
      _platformVersion = platformVersion;
    });
  }

  Future<void> permission() async {
    Permission.camera.request();
  }


  @override
  Widget build(BuildContext context) {
    permission();
    return ChangeNotifierProvider<NativeCallBack>(
        create: (context) => NativeCallBack(),
    child: MaterialApp(
      home: Scaffold(
        body: Stack(children: [
          Positioned.fill(
            child:
            Consumer<NativeCallBack>(
              builder: (context, mymodel, child) {
                // mymodel.btnPermission();
                return CameraKitView(
                    hasFaceDetection: true,
                    cameraKitController: cameraKitController,
                    cameraPosition: CameraPosition.front,
                    showTextResult: (String text) {
                      if (mymodel.stopCallBack) {
                        mymodel.notifyText(text, context);
                        mymodel.stopCallBack = false;
                      }
                    });
              },
            ),
          ),
          _buildCropBox(),
          _buildCropBoxDetail(),
        ]),
      ),
    ));
  }
}


Widget _buildCropBox() {
  return Positioned.fill(
      child: Container(
        child: ColorFiltered(
          colorFilter:
          ColorFilter.mode(Colors.black26.withOpacity(0.5), BlendMode.srcOut),
          child: Stack(
            fit: StackFit.expand,
            children: [
              Container(
                decoration: BoxDecoration(
                    color: Colors.black, backgroundBlendMode: BlendMode.dstOut),
              ),
              Align(
                alignment: Alignment.centerLeft,
                child: Container(
                  constraints: BoxConstraints(
                    maxWidth: 450,
                  ),
                  margin: const EdgeInsets.only(
                      top: 50, left: 20, right: 200, bottom: 20),
                  child: AspectRatio(
                    aspectRatio: 0.2,
                    child: Container(
                      decoration: BoxDecoration(
                        color: Colors.red,
                        // borderRadius: BorderRadius.circular(20),
                      ),
                    ),
                  ),
                ),
              ),
            ],
          ),
        ),
      ));
}

Positioned _buildCropBoxDetail() {
  return Positioned.fill(
    child: Stack(
      fit: StackFit.expand,
      children: [
        Align(
          alignment: Alignment.centerLeft,
          child: Container(
            constraints: BoxConstraints(
              maxWidth: 450,
            ),
            margin: const EdgeInsets.only(
                top: 50, left: 20, right: 200, bottom: 20),
            child: AspectRatio(
              aspectRatio: 0.2,
              child: Container(
                  decoration: BoxDecoration(
                    border: Border.all(width: 2, color: Colors.white),
                    //
                    // color: Colors.amber
                  ),
                  child: Opacity(
                    opacity: 0.8,
                    child: Container(
                        padding: const EdgeInsets.symmetric(
                            horizontal: 8, vertical: 8),
                        decoration: BoxDecoration(
                        ),
                        alignment: Alignment.center,
                        child: RotatedBox(
                          quarterTurns: 1,
                          child: Text(
                            "I < < < < < < < < < < < < < < < < < < < < < < < < < < < < < < < < < < < < < < < < < < < < < < < < < < < < < < < < < < < < < < < < < < < < < < < < < < < < < < < <",
                            style: TextStyle(
                                color: Colors.white,
                                fontSize: 27,
                                fontFamily: "Times New Roman"),
                          ),
                        )),
                  )),
            ),
          ),
        ),
        Align(
          alignment: Alignment.centerLeft,
          child: Container(
            constraints: BoxConstraints(
              maxWidth: 550,
            ),
            margin: const EdgeInsets.only(
                top: 50, left: 20, right: 20, bottom: 20),
            child: AspectRatio(
              aspectRatio: 0.45,
              child: Container(
                  decoration: BoxDecoration(
                    border: Border.all(width: 2, color: Colors.white),
                  ),
                  child: Opacity(
                    opacity: 0.8,
                    child: Container(
                      padding: const EdgeInsets.symmetric(
                          horizontal: 16, vertical: 8),
                      constraints:
                      const BoxConstraints(maxHeight: 60, maxWidth: 200),
                      decoration: BoxDecoration(
                        // color: Colors.black87,
                        borderRadius: BorderRadius.circular(20),
                      ),
                      alignment: Alignment.center,
                    ),
                  )),
            ),
          ),
        ),
        Align(
          alignment: Alignment.bottomRight,
          child: Container(
              margin: const EdgeInsets.only(right: 40, bottom: 40),
              child: Consumer<NativeCallBack>(
                  builder: (context, mymodel, child) {
                    return IconButton(
                      icon: Icon(
                        mymodel.flashicon,
                        color: Colors.white,
                        size: 49,
                      ),
                      onPressed: () {
                        mymodel.btnFlashlight();
                      },
                    );
                  })),
        ),
      ],
    ),
  );
}
