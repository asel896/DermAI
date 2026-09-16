package com.example.dermai

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
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
    uri: Uri,
    onTextFound: (String) -> Unit,
    onError: (Exception) -> Unit
) {
    try {

        val bitmap: Bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            ImageDecoder.decodeBitmap(ImageDecoder.createSource(context.contentResolver, uri))
        } else {
            @Suppress("DEPRECATION")
            MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
        }


        val scaledBitmap = if (bitmap.width > 2048 || bitmap.height > 2048) {
            val scale = 2048f / Math.max(bitmap.width, bitmap.height)
            Bitmap.createScaledBitmap(
                bitmap,
                (bitmap.width * scale).toInt(),
                (bitmap.height * scale).toInt(),
                true
            )
        } else {
            bitmap
        }


        val image = InputImage.fromBitmap(scaledBitmap, 0)
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