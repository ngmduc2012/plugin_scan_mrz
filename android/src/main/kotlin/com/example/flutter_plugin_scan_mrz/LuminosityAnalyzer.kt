package com.example.flutter_plugin_scan_mrz

import android.graphics.Color
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy


class LuminosityAnalyzer(private val listener: LuminnosityAnalyzerCallBack) :
    ImageAnalysis.Analyzer {


    override fun analyze(image: ImageProxy) {

        listener.onChangeTextResult(image)
    }

}