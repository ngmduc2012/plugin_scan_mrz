
import 'dart:async';

import 'package:flutter/services.dart';

class FlutterPluginScanMrz {
  static const MethodChannel _channel =
      const MethodChannel('flutter_plugin_scan_mrz');

  static Future<String> get platformVersion async {
    final String version = await _channel.invokeMethod('getPlatformVersion');
    return version;
  }
  static Future<String> get getMRZkey async {
    final String result = await _channel.invokeMethod('callBack');
    return result;
  }
}
