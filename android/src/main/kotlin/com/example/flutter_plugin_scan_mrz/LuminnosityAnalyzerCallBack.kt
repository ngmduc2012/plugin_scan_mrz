package com.example.flutter_plugin_scan_mrz

import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.text.Text

interface LuminnosityAnalyzerCallBack {
    fun onChangeTextResult(imageProxy: ImageProxy)

}