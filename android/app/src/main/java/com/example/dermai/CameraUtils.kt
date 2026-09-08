package com.example.dermai

import android.content.Context
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions

@androidx.annotation.OptIn(androidx.camera.core.ExperimentalGetImage::class)
fun captureAndRecognizeText(
    imageCapture: ImageCapture,
    context: Context,
    onTextFound: (String) -> Unit,
    onError: (Exception) -> Unit
) {
    imageCapture.takePicture(
        ContextCompat.getMainExecutor(context),
        object : ImageCapture.OnImageCapturedCallback() {
            override fun onCaptureSuccess(imageProxy: ImageProxy) {
                val mediaImage = imageProxy.image
                if (mediaImage != null) {
                    val image = InputImage.fromMediaImage(
                        mediaImage,
                        imageProxy.imageInfo.rotationDegrees
                    )
                    val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

                    recognizer.process(image)
                        .addOnSuccessListener { visionText ->
                            onTextFound(visionText.text)
                            imageProxy.close()
                        }
                        .addOnFailureListener { e ->
                            onError(e)
                            imageProxy.close()
                        }
                } else {
                    imageProxy.close()
                }
            }

            override fun onError(exception: ImageCaptureException) {
                onError(exception)
            }
        }
    )
}


fun recognizeTextFromUri(
    context: Context,
    uri: android.net.Uri,
    onTextFound: (String) -> Unit,
    onError: (Exception) -> Unit
) {
    try {
        val image = InputImage.fromFilePath(context, uri)
        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
        recognizer.process(image)
            .addOnSuccessListener { visionText ->
                onTextFound(visionText.text)
            }
            .addOnFailureListener { e ->
                onError(e)
            }
    } catch (e: Exception) {
        onError(e)
    }
}