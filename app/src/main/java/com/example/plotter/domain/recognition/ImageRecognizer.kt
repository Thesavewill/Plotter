package com.example.plotter.domain.recognition

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import com.google.android.gms.tasks.Task
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

object ImageRecognizer {

    suspend fun recognizeFromUri(context: Context, imageUri: Uri): String? = withContext(Dispatchers.IO) {
        try {
            val inputImage = InputImage.fromFilePath(context, imageUri)
            val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

            val result = recognizer.process(inputImage).await()
            recognizer.close()
            result.text.takeIf { it.isNotBlank() }

        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun recognizeFromBitmap(bitmap: Bitmap): String? = withContext(Dispatchers.IO) {
        try {
            val inputImage = InputImage.fromBitmap(bitmap, 0)
            val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

            val result = recognizer.process(inputImage).await()
            recognizer.close()
            result.text.takeIf { it.isNotBlank() }

        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun preprocessEquation(rawText: String): String {
        return rawText
            .replace(Regex("[^a-zA-Z0-9\\s\\+\\-\\*/^().,=]"), "")
            .replace("\\s+".toRegex(), "")
            .take(200)
    }

    private suspend fun <T> Task<T>.await(): T {
        return suspendCoroutine { cont ->
            addOnSuccessListener { cont.resume(it) }
            addOnFailureListener { cont.resumeWithException(it) }
        }
    }
}