package com.example.bitirmeproje.presentation

import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.util.Log
import android.util.Size
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import com.example.bitirmeproje.R
import com.example.bitirmeproje.business.OverlayView
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class CameraActivity : AppCompatActivity() {
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var previewView: PreviewView
    private lateinit var overlayView: OverlayView
    private lateinit var textView: TextView
    private lateinit var stopButton: ImageButton
    private lateinit var rePhotoBtn: ImageButton
    private lateinit var sendButton: ImageButton
    private var cameraProvider: ProcessCameraProvider? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)

        previewView = findViewById(R.id.previewView)
        overlayView = findViewById(R.id.overlayView)
        textView = findViewById(R.id.textView)
        stopButton = findViewById(R.id.stopButton)
        rePhotoBtn = findViewById(R.id.reButton)
        sendButton = findViewById(R.id.sendButton)

        cameraExecutor = Executors.newSingleThreadExecutor()

        stopButton.setOnClickListener {
            stopCameraPreview()
            rePhotoBtn.visibility = Button.VISIBLE
            sendButton.visibility = Button.VISIBLE
            stopButton.visibility = Button.INVISIBLE
        }
        rePhotoBtn.setOnClickListener {
            startCamera()
            rePhotoBtn.visibility = Button.INVISIBLE
            sendButton.visibility = Button.INVISIBLE
            stopButton.visibility = Button.VISIBLE
        }

        sendButton.setOnClickListener {
            val intent = Intent(this, AiActivity::class.java)
            intent.putExtra("capturedText", textView.text.toString())
            startActivity(intent)
            rePhotoBtn.visibility = Button.INVISIBLE
            sendButton.visibility = Button.INVISIBLE
            stopButton.visibility = Button.VISIBLE
        }

        startCamera()
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            cameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }

            val imageAnalyzer = ImageAnalysis.Builder()
                .setTargetResolution(Size(1280, 720))
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .also {
                    it.setAnalyzer(cameraExecutor, TextAnalyzer(overlayView.getRectangle(), previewView.width, previewView.height))
                }

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider?.unbindAll()
                cameraProvider?.bindToLifecycle(
                    this, cameraSelector, preview, imageAnalyzer
                )
            } catch (exc: Exception) {
                Log.e("Camera", "Binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(this))
    }

    private fun stopCameraPreview() {
        cameraProvider?.unbindAll() // Kamera önizlemesini durdur
    }

    private inner class TextAnalyzer(private val rect: Rect, private val previewWidth: Int, private val previewHeight: Int) : ImageAnalysis.Analyzer {
        private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

        override fun analyze(imageProxy: ImageProxy) {
            val mediaImage = imageProxy.image
            if (mediaImage != null) {
                val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)

                // Kamera görüntüsünün boyutları
                val imageWidth = mediaImage.width
                val imageHeight = mediaImage.height

                // Dönüşüm oranlarını hesapla
                val widthScale = imageWidth.toFloat() / previewWidth
                val heightScale = imageHeight.toFloat() / previewHeight

                // Ekran koordinatlarını kamera koordinatlarına dönüştür
                val transformedRect = Rect(
                    (rect.left * widthScale).toInt(),
                    (rect.top * heightScale).toInt(),
                    (rect.right * widthScale).toInt(),
                    (rect.bottom * heightScale).toInt()
                )

                recognizer.process(image)
                    .addOnSuccessListener { visionText ->
                        processText(visionText, transformedRect)
                    }
                    .addOnFailureListener { e ->
                        Log.e("TextRecognition", "Text recognition failed", e)
                    }
                    .addOnCompleteListener {
                        imageProxy.close()
                    }
            } else {
                imageProxy.close()
            }
        }

        private fun processText(visionText: Text, rect: Rect) {
            val recognizedTexts = mutableListOf<String>()
            for (block in visionText.textBlocks) {
                for (line in block.lines) {
                    val boundingBox = line.boundingBox
                    if (boundingBox != null && rect.contains(boundingBox)) {
                        recognizedTexts.add(line.text)
                        Log.d("TextRecognition", "Recognized text: ${line.text}")
                    }
                }
            }
            // Tüm tanınan metinleri birleştir ve TextView'a yazdır
            runOnUiThread {
                textView.text = recognizedTexts.joinToString("\n")
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }
}
