import 'package:flutter/material.dart';
import 'package:provider/provider.dart';

import 'NativeCallBack.dart';
import 'dart:developer' as developer;

class ShowMRZ extends StatelessWidget {
  final String codeMRZ;

  const ShowMRZ({Key key, this.codeMRZ}) : super(key: key);

  @override
  Widget build(BuildContext context) {
    return
      ChangeNotifierProvider<NativeCallBack>(
        create: (context) => NativeCallBack(),
        child:
        Center(
            child: Consumer<NativeCallBack>(builder: (context, mymodel, child) {
          return WillPopScope(
            child: Scaffold(
                appBar: AppBar(
                  title: Text("Show MRZcode"),
                ),
                body:
                    Text(codeMRZ)),
            onWillPop: () async {
              Navigator.pop(context, true);
              return true;
            },
          );
        })));
  }
}
