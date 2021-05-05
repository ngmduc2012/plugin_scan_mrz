import 'package:flutter/cupertino.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_plugin_scan_mrz/flutter_plugin_scan_mrz.dart';
import 'package:mrz_parser/mrz_parser.dart';
import 'ShowMRZ.dart';
import 'dart:developer' as developer;


class NativeCallBack with ChangeNotifier {
  String codeMRZ = 'No MRZ';
  bool stopCallBack = true;


  void notifyText(String text, BuildContext buildContext) async {

    // developer.log(text, name: "ok");
    var mrz = [text.substring(0,30), text.substring(30,60), text.substring(60,90)];
    // developer.log(text.substring(0,30), name: "ok");
    // developer.log(text.substring(30,60), name: "ok");
    // developer.log(text.substring(60,90), name: "ok");
    var result = MRZParser.tryParse(mrz);
    // developer.log(result?.mrzKey, name: "ok");
    codeMRZ = result?.mrzKey;
    if(codeMRZ.isNotEmpty){
      stopCallBack = await Navigator.push(buildContext,
          MaterialPageRoute(builder: (context) => ShowMRZ(codeMRZ: codeMRZ)));
    }
    else{
      stopCallBack = true;
    }
  }

  bool hasflashlight = false;
  bool isturnon = false;
  IconData flashicon = Icons.flash_off;

  Future<void> btnFlashlight() async {
    try {
      isturnon = await FlutterPluginScanMrz.flashlight;
      // isturnon = !isturnon;
      if (isturnon) {
        flashicon = Icons.flash_on;
      } else {
        flashicon = Icons.flash_off;
      }
      notifyListeners();
    } on PlatformException catch (e) {}
  }

}
