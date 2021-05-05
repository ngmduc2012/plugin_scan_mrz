
import 'dart:async';

import 'package:flutter/services.dart';
import 'dart:developer' as developer;

class FlutterPluginScanMrz {

  static const MethodChannel _channel =
      const MethodChannel('flutter_plugin_scan_mrz');



  static Future<String> get platformVersion async {
    final String version = await _channel.invokeMethod('getPlatformVersion');
    return version;
  }

  static const platform = const MethodChannel('FlashLight');

  static Future<bool> get flashlight async {
    try {
      bool isturnon = !await platform.invokeMethod('btnFlashLight');
      return isturnon;
    } on PlatformException catch (e) {
      return false;
    }

  }





}
