package com.example.plotter.domain.recognition

import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.common.model.RemoteModelManager
import com.google.mlkit.vision.digitalink.DigitalInkRecognition
import com.google.mlkit.vision.digitalink.DigitalInkRecognitionModel
import com.google.mlkit.vision.digitalink.DigitalInkRecognitionModelIdentifier
import com.google.mlkit.vision.digitalink.DigitalInkRecognizer
import com.google.mlkit.vision.digitalink.DigitalInkRecognizerOptions
import com.google.mlkit.vision.digitalink.Ink
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

/**
 * Распознаватель рукописного ввода через ML Kit Digital Ink.
 * Требует загрузки языковой модели (~20 МБ).
 */
object HandwritingRecognizer {
    private var recognizer: DigitalInkRecognizer? = null
    private val modelManager = RemoteModelManager.getInstance()

    private val modelIdentifier =
        DigitalInkRecognitionModelIdentifier.fromLanguageTag("en-US")
    private var model: DigitalInkRecognitionModel? = null

    /** Проверяет, загружена ли модель для текущего языка */
    suspend fun isModelDownloaded(): Boolean = withContext(Dispatchers.IO) {
        val identifier = modelIdentifier ?: return@withContext false
        val currentModel = model
            ?: DigitalInkRecognitionModel.builder(identifier).build()
        modelManager.isModelDownloaded(currentModel).await()
    }

    /** Скачивает языковую модель */
    suspend fun downloadModel(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val identifier = modelIdentifier
                ?: return@withContext Result.failure(Exception("No identifier"))
            val currentModel = DigitalInkRecognitionModel.builder(identifier).build()
            val conditions = DownloadConditions.Builder().build()
            modelManager.download(currentModel, conditions).await()
            model = currentModel
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /** Распознаёт текст из объекта Ink (набора штрихов) */
    suspend fun recognize(ink: Ink): String? = withContext(Dispatchers.IO) {
        try {
            if (model == null) {
                val identifier = modelIdentifier ?: return@withContext null
                model = DigitalInkRecognitionModel.builder(identifier).build()
            }
            val currentModel = model ?: return@withContext null
            val currentRecognizer = recognizer ?: DigitalInkRecognition.getClient(
                DigitalInkRecognizerOptions.builder(currentModel).build()
            ).also { recognizer = it }
            val result = currentRecognizer.recognize(ink).await()
            result.candidates.firstOrNull()?.text?.takeIf { it.isNotBlank() }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /** Очищает распознанный текст и приводит к формату уравнения */
    fun preprocessEquation(rawText: String): String {
        return rawText.lowercase()
            .replace(Regex("[^a-zA-Z0-9\\s\\+\\-\\*/^().,=]"), "")
            .replace("\\s+".toRegex(), "")
            .replace("х", "x")
            .replace("у", "y")
            .take(200)
    }

    /** Освобождает ресурсы распознавателя */
    fun close() {
        recognizer?.close()
        recognizer = null
    }
}