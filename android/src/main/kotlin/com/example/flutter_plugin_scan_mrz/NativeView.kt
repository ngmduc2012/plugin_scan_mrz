package com.example.flutter_plugin_scan_mrz


import android.annotation.SuppressLint
import android.content.ContentValues.TAG
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.os.Build
import android.os.Parcel
import android.os.Parcelable

import android.util.Log
import android.view.View
import androidx.annotation.RequiresApi
import androidx.camera.core.*

import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import com.example.flutter_plugin_scan_mrz.*
import com.example.flutter_plugin_scan_mrz.R
import com.google.mlkit.vision.common.InputImage

import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition

import io.flutter.plugin.common.BinaryMessenger
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.platform.PlatformView
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers



import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


typealias LumaListener = (luma: Double) -> Unit

class NativeView(private val context: Context, messenger: BinaryMessenger,
                 id: Int, private val creationParams: Map<String?, Any?>?) : PlatformView, MethodChannel.MethodCallHandler,
        LifecycleOwner, LuminnosityAnalyzerCallBack {

    private val lifecycleRegistry: LifecycleRegistry = LifecycleRegistry(this)
    private val cameraKitView: View = View.inflate(context, R.layout.activity_main, null)
    private val cameraPreview: PreviewView
    private var cameraExecutor: ExecutorService
    private var imageCapture: ImageCapture? = null
    private var cameraProvider: ProcessCameraProvider? = null

    private val methodChannel: MethodChannel = MethodChannel(messenger, "camera/camera_$id")
    private val methodChannel1: MethodChannel = MethodChannel(messenger, "FlashLight")

    private var hasFaceDetection: Boolean = false
    private var cameraPosition: String = "F"
    private var changeFlashlight: Boolean = true

    private var mSelectedImage: Bitmap? = null
    private var resizedBitmap: Bitmap? = null

    // Max width (portrait mode)
    private var mImageMaxWidth: Int? = null

    // Max height (portrait mode)
    private var mImageMaxHeight: Int? = null

    private var mGraphicOverlay: GraphicOverlay? = null

//    private val textGraphic: GraphicOverlay.Graphic? = null
    var textTest: Text? = null


    override fun getView(): View {
        return cameraKitView
    }

    override fun dispose() {
        cameraProvider?.unbindAll()
        cameraExecutor.shutdown()
    }

    init {
   
        lifecycleRegistry.currentState = Lifecycle.State.CREATED
        cameraPreview = cameraKitView.findViewById(R.id.viewFinder)
        mGraphicOverlay = cameraKitView.findViewById(R.id.graphic_overlay)
        methodChannel.setMethodCallHandler(this)
        cameraExecutor = Executors.newSingleThreadExecutor()

    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener(Runnable {
            // Used to bind the lifecycle of cameras to the lifecycle owner
            if (cameraProvider == null) {
                cameraProvider = cameraProviderFuture.get()
            }
            // Preview
            val preview = Preview.Builder()
                    .build()
                    .also {
                        it.setSurfaceProvider(cameraPreview.surfaceProvider)
                    }

            imageCapture = ImageCapture.Builder()
                    .build()

            val imageAnalyzer = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()
                    .also {
                        it.setAnalyzer(cameraExecutor, LuminosityAnalyzer(this))
                    }


            // Select back camera as a default
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                // Unbind use cases before rebinding
                cameraProvider?.unbindAll()

                // Bind use cases to camera
                val cameraFlashLight = cameraProvider?.bindToLifecycle(
                        this, cameraSelector, preview, imageCapture, imageAnalyzer
                )
                methodChannel1.setMethodCallHandler() { call, result ->
                    if (call.method == "btnFlashLight") {
                        cameraFlashLight?.cameraInfo?.hasFlashUnit()
                        cameraFlashLight?.cameraControl?.enableTorch(changeFlashlight)
                        changeFlashlight = !changeFlashlight
                        result.success(changeFlashlight)
                    } else {
                        result.notImplemented()
                    }
                }


            } catch (exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(context))
    }

    override fun onMethodCall(call: MethodCall, result: MethodChannel.Result) {
        when (call.method) {
            "initCamera" -> {
//                Log.d("ok", "initCamera")
                hasFaceDetection = call.argument<Boolean>("hasFaceDetection") ?: false
                cameraPosition = call.argument<String>("cameraPosition") ?: "F"
                lifecycleRegistry.currentState = Lifecycle.State.RESUMED
                startCamera()
            }
            "flipCamera" -> {
                cameraPosition = if (cameraPosition == "F") {
                    "B"
                } else {
                    "F"
                }
                startCamera()
            }
            "resumeCamera" -> {
                lifecycleRegistry.currentState = Lifecycle.State.RESUMED
                startCamera()
            }
            "pauseCamera" -> {
                cameraProvider?.unbindAll();
            }
            else -> result.notImplemented()
        }
    }

    override fun getLifecycle(): Lifecycle {
        return lifecycleRegistry
    }

    // Ham nay tra ve kq, lay ra text
    private fun processTextRecognitionResult(texts: Text) {
        textTest = texts
        val blocks = texts.textBlocks
        if (blocks.size == 0) {
            mGraphicOverlay!!.clear()
            return
        }
        mGraphicOverlay!!.clear()
        for (block in texts.textBlocks) {
            Log.d("ok", block.text)

        }
        for (i in blocks.indices) {
            val lines = blocks[i].lines
            for (j in lines.indices) {
                val elements = lines[j].elements
                for (k in elements.indices) {
//                    Log.d("ok", "================"+elements[k].text)
                    if(elements[k].text.length >= 29 && elements[k].text.contains("<<")){
                        val textGraphic: GraphicOverlay.Graphic =
                                TextGraphic(mGraphicOverlay, elements[k] ,getImageMaxWidth(),getImageMaxHeight())
                        mGraphicOverlay?.add(textGraphic)
                    }
                }
            }
        }

    }


    private fun getImageMaxWidth(): Int {
        if (mImageMaxWidth == null) {
            // Calculate the max width in portrait mode. This is done lazily since we need to
            // wait for
            // a UI layout pass to get the right values. So delay it to first time image
            // rendering time.
            mImageMaxWidth = mGraphicOverlay?.width
        }
        return mImageMaxWidth as Int
    }

    // Returns max image height, always for portrait mode. Caller needs to swap width / height for
    // landscape mode.
    private fun getImageMaxHeight(): Int {
        if (mImageMaxHeight == null) {
            // Calculate the max width in portrait mode. This is done lazily since we need to
            // wait for
            // a UI layout pass to get the right values. So delay it to first time image
            // rendering time.
            mImageMaxHeight = mGraphicOverlay?.height
        }
        return mImageMaxHeight as Int
    }

    @RequiresApi(Build.VERSION_CODES.KITKAT)
    @SuppressLint("UnsafeExperimentalUsageError")
    override fun onChangeTextResult(imageProxy: ImageProxy) {

        mSelectedImage = BitmapUtils.getBitmap(imageProxy)

        if(mSelectedImage?.width!!.toFloat()/mSelectedImage?.height!!.toFloat()<1){
            mSelectedImage = Bitmap.createBitmap(
                    mSelectedImage!!,
                    ((mSelectedImage?.width!!.toFloat()-(getImageMaxWidth().toFloat()/getImageMaxHeight().toFloat()*mSelectedImage?.height!!.toFloat()))/2).toInt(),
                    0,
                    ((getImageMaxWidth().toFloat()/getImageMaxHeight().toFloat()*mSelectedImage?.height!!.toFloat())).toInt(),
                    (mSelectedImage?.height!!.toFloat()).toInt()
            )
        }
        else{
            mSelectedImage = Bitmap.createBitmap(
                    mSelectedImage!!,
                    0,
                    ((mSelectedImage?.height!!.toFloat()-(mSelectedImage?.width!!.toFloat()*getImageMaxHeight().toFloat()/getImageMaxWidth().toFloat()))/2).toInt(),
                    (mSelectedImage?.width!!.toFloat()).toInt(),
                    (mSelectedImage?.width!!.toFloat()*getImageMaxHeight().toFloat()/getImageMaxWidth().toFloat()).toInt()
            )
        }


        resizedBitmap = Bitmap.createScaledBitmap(
                mSelectedImage!!,
                getImageMaxWidth(),
                getImageMaxHeight(),
                true
        )

        val matrix = Matrix()
        matrix.postRotate(270f)
        mSelectedImage = Bitmap.createBitmap(resizedBitmap!!, 0, 0, resizedBitmap!!.width, resizedBitmap!!.height, matrix, true)
        val image = InputImage.fromBitmap(mSelectedImage!!, 0)
        val recognizer = TextRecognition.getClient()
        recognizer.process(image)
                .addOnSuccessListener { texts ->
                    processTextRecognitionResult(texts)
                }
                .addOnFailureListener { e -> // Task failed with an exception
                    e.printStackTrace()
                }
                .addOnCompleteListener { imageProxy.close() }

    }
    var line1Result: String = ""
    var line2Result: String = ""
    var line3Result: String = ""
    var textMRZResult: String = ""

    private fun findTextMRZ(text: String): String {
        var line1: String = text.substring(0, 30)
        var line2: String = text.substring(30, 60)
        var line3: String = text.substring(60, 90)
        line1 = line1.substring(0, 5)+line1.substring(5).replace("O", "0")
        line2 = line2.substring(0, 29)+line2.substring(29).replace("O", "0")
        line3 = line3.replace("K<", "<<")
        line3 = line3.replace("S<", "<<")
        line3 = line3.replace("s<", "<<")
        line3 = line3.replace("k<", "<<")
        val pattern1 = "I[A-Z]{4}[0-9]{22}+<{2}[0-9]{1}".toRegex()
        val pattern2 = "[A-Z0-9]{2,30}+<{2,20}[0-9]{1}".toRegex()
        val pattern3 = "\\w+<<(\\w+<)+<{3,15}".toRegex()

        if (pattern1.matches(line1)) {
            line1Result = line1
        }
        if (pattern2.matches(line2)) {
            line2Result = line2
        }
        if (pattern3.matches(line3)) {
            line3Result = line3
        }
        return line1Result + line2Result + line3Result
    }


}
