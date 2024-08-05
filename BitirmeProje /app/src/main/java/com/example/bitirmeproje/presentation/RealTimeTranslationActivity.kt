package com.example.bitirmeproje.presentation

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Rect
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.bitirmeproje.R
import com.google.cloud.translate.Translate
import com.google.cloud.translate.TranslateOptions
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@androidx.camera.core.ExperimentalGetImage
class RealTimeTranslationActivity : AppCompatActivity() {
    private lateinit var previewView: PreviewView
    private lateinit var overlay: ConstraintLayout
    private lateinit var cameraExecutor: ExecutorService
    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    private lateinit var translate: Translate
    private val translatedTexts = mutableMapOf<String, TextView>()

    companion object {
        private const val REQUEST_CAMERA_PERMISSION = 101
        private const val ANALYSIS_LATENCY = 2000L
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_real_time_translation)

        previewView = findViewById(R.id.previewView)
        overlay = findViewById(R.id.overlay)

        if (allPermissionsGranted()) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), REQUEST_CAMERA_PERMISSION)
        }

        cameraExecutor = Executors.newSingleThreadExecutor()

        // Initialize Google Translate API
        val translateOptions = TranslateOptions.newBuilder().setApiKey("AIzaSyBQyoONU_OS9NCAzxAT5xDCu8OkWEalRTk").build()
        translate = translateOptions.service
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }

    private fun allPermissionsGranted() = arrayOf(Manifest.permission.CAMERA).all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }

            val imageAnalyzer = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .setTargetResolution(android.util.Size(1280, 720))
                .build().also { imageAnalysis ->
                    imageAnalysis.setAnalyzer(cameraExecutor, { imageProxy ->
                        Handler(Looper.getMainLooper()).postDelayed({
                            processImageProxy(imageProxy)
                        }, ANALYSIS_LATENCY)
                    })
                }

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalyzer)
            } catch (exc: Exception) {
                Log.e("CameraX", "Use case binding failed", exc)
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun processImageProxy(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage != null) {
            val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
            recognizer.process(image)
                .addOnSuccessListener { texts ->
                    val detectedTextBlocks = texts.textBlocks.map { it.text to it.boundingBox }.toMap()
                    updateTextViews(detectedTextBlocks)
                    for ((text, boundingBox) in detectedTextBlocks) {
                        if (boundingBox != null && !translatedTexts.containsKey(text)) {
                            translateText(text, boundingBox)
                        }
                    }
                    imageProxy.close()
                }
                .addOnFailureListener { e ->
                    Log.e("CameraX", "Text recognition failed", e)
                    imageProxy.close()
                }
        } else {
            imageProxy.close()
        }
    }

    private fun translateText(text: String, boundingBox: Rect) {
        cameraExecutor.execute {
            try {
                val translation = translate.translate(
                    text,
                    Translate.TranslateOption.targetLanguage("tr"),
                    Translate.TranslateOption.model("nmt")
                )
                val translatedText = translation.translatedText
                runOnUiThread {
                    displayText(translatedText, boundingBox, text)
                }
            } catch (e: Exception) {
                Log.e("Translate", "Translation failed", e)
                runOnUiThread {
                    displayText(text, boundingBox, text) // Orijinal metni göster
                }
            }
        }
    }

    private fun displayText(text: String, boundingBox: Rect, originalText: String) {
        val textView = TextView(this).apply {
            this.text = text
            setTextColor(ContextCompat.getColor(context, android.R.color.white))
            setBackgroundColor(ContextCompat.getColor(context, android.R.color.darker_gray))
            textSize = 20f

            // Ekran genişliğini ve yüksekliğini alın
            val displayMetrics = resources.displayMetrics
            val screenWidth = displayMetrics.widthPixels
            val screenHeight = displayMetrics.heightPixels

            // TextView'in boyutlarını ayarlayın ve ekranın dışına taşmamasını sağlayın
            val left = boundingBox.left.coerceAtLeast(0).toFloat()
            val top = boundingBox.top.coerceAtLeast(0).toFloat()
            val right = boundingBox.right.coerceAtMost(screenWidth).toFloat()
            val bottom = boundingBox.bottom.coerceAtMost(screenHeight).toFloat()

            x = left
            y = top

            // TextView'in genişliğini ayarlayın
            val layoutParams = ConstraintLayout.LayoutParams((right - left).toInt(), ConstraintLayout.LayoutParams.WRAP_CONTENT)
            this.layoutParams = layoutParams
        }
        overlay.addView(textView)
        translatedTexts[originalText] = textView
    }


    private fun updateTextViews(detectedTextBlocks: Map<String, Rect?>) {
        val iterator = translatedTexts.iterator()
        while (iterator.hasNext()) {
            val (text, textView) = iterator.next()
            if (!detectedTextBlocks.containsKey(text)) {
                overlay.removeView(textView)
                iterator.remove()
            }
        }
    }
}
