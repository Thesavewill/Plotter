package com.example.plotter.viewmodel

import android.content.Context
import android.graphics.Bitmap
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.plotter.domain.evaluator.ExpressionEvaluator
import com.example.plotter.domain.model.CanvasTransform
import com.example.plotter.domain.model.PlotFunction
import com.example.plotter.domain.recognition.ImageRecognizer
import com.example.plotter.ui.PlotterContract
import com.example.plotter.ui.PlotterContract.Intent
import com.example.plotter.ui.PlotterContract.State
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.update

class PlotterViewModel(
    private val appContext: Context,
    private val imageRecognizer: ImageRecognizer = ImageRecognizer
) : ViewModel() {

    private val _state = MutableStateFlow(State())
    val state: StateFlow<State> = _state.asStateFlow()

    private val _imageRecognitionEvents = Channel<ImageRecognitionEvent>(Channel.BUFFERED)
    val imageRecognitionEvents: Flow<ImageRecognitionEvent> = _imageRecognitionEvents.receiveAsFlow()

    private var recognitionJob: Job? = null

    fun handleIntent(intent: Intent) {
        when (intent) {
            is Intent.Pan -> updateCanvas { it.copy(offsetX = it.offsetX + intent.dx, offsetY = it.offsetY + intent.dy) }
            is Intent.Zoom -> handleZoom(intent.factor, intent.centerX, intent.centerY)
            is Intent.CanvasInitialized -> handleCanvasInit(intent.width, intent.height)
            is Intent.AddFunction -> addFunction(intent.position)
            is Intent.RemoveFunction -> removeFunction(intent.id)
            is Intent.UpdateExpression -> updateExpression(intent.id, intent.value)
            is Intent.SelectFunction -> _state.update { it.copy(selectedFunctionId = intent.id) }
            is Intent.ChangeColor -> changeColor(intent.id, intent.color)
            is Intent.OpenColorPicker -> _state.update {
                it.copy(colorPicker = PlotterContract.ColorPickerState(isVisible = true, targetFunctionId = intent.functionId))
            }
            Intent.CloseColorPicker -> _state.update {
                it.copy(colorPicker = PlotterContract.ColorPickerState())
            }
            Intent.OpenImageSourceDialog -> {
                viewModelScope.launch {
                    _imageRecognitionEvents.send(ImageRecognitionEvent.RequestPermission)
                }
            }
            is Intent.InsertSymbol -> insertSymbol(intent.symbol)
            Intent.DeleteSymbol -> deleteSymbol()
            Intent.RequestImageCapture -> {
                viewModelScope.launch {
                    _imageRecognitionEvents.send(ImageRecognitionEvent.RequestPermission)
                }
            }
            is Intent.ProcessImageUri -> {
                processImageRecognition {
                    imageRecognizer.recognizeFromUri(appContext, android.net.Uri.parse(intent.uri))
                }
            }
            is Intent.ProcessImageBitmap -> {
                processImageRecognition {
                    imageRecognizer.recognizeFromBitmap(intent.bitmap)
                }
            }
        }
    }

    private fun handleZoom(factor: Float, centerX: Float, centerY: Float) {
        _state.update { state ->
            val canvas = state.canvas
            var newScale = canvas.scale * factor
            var newGridSize = canvas.gridSize * factor
            var newCountScale = canvas.countScale

            while (newScale >= 2f) {
                newCountScale++
                newScale /= 2f
                newGridSize /= 2f
            }
            while (newScale <= 0.5f) {
                newCountScale--
                newScale *= 2f
                newGridSize *= 2f
            }

            val newOffsetX = centerX - (centerX - canvas.offsetX) * factor
            val newOffsetY = centerY - (centerY - canvas.offsetY) * factor

            state.copy(
                canvas = canvas.copy(
                    offsetX = newOffsetX,
                    offsetY = newOffsetY,
                    gridSize = newGridSize,
                    scale = newScale,
                    countScale = newCountScale
                )
            )
        }
    }

    private fun handleCanvasInit(width: Float, height: Float) {
        _state.update { state ->
            if (state.canvas.isInitialized) return@update state
            state.copy(
                canvas = state.canvas.copy(
                    offsetX = width / 2f,
                    offsetY = height / 4f,
                    gridSize = width / 20f,
                    canvasWidth = width,
                    canvasHeight = height,
                    isInitialized = true
                )
            )
        }
    }

    private fun addFunction(position: Int) {
        _state.update { state ->
            val newFunc = PlotFunction()
            val newList = if (position < 0 || position >= state.functions.size) {
                state.functions + newFunc
            } else {
                state.functions.toMutableList().apply { add(position, newFunc) }
            }
            state.copy(functions = newList, selectedFunctionId = newFunc.id)
        }
    }

    private fun removeFunction(id: String) {
        _state.update { state ->
            val newList = state.functions.filter { it.id != id }
            state.copy(
                functions = if (newList.isEmpty()) listOf(PlotFunction()) else newList,
                selectedFunctionId = if (state.selectedFunctionId == id) null else state.selectedFunctionId
            )
        }
    }

    private fun updateExpression(id: String, value: TextFieldValue) {
        _state.update { state ->
            state.copy(
                functions = state.functions.map {
                    if (it.id == id) it.copy(expression = value) else it
                }
            )
        }
    }

    private fun changeColor(id: String, color: Color) {
        _state.update { state ->
            state.copy(
                functions = state.functions.map {
                    if (it.id == id) it.copy(color = color) else it
                }
            )
        }
    }

    private fun insertSymbol(symbol: String) {
        val selectedId = _state.value.selectedFunctionId ?: return
        _state.update { state ->
            state.copy(
                functions = state.functions.map { func ->
                    if (func.id != selectedId) return@map func
                    val value = func.expression
                    val text = value.text
                    val selection = value.selection
                    val newText = text.replaceRange(selection.min, selection.max, symbol)
                    val offset = when (symbol) {
                        "sin()", "cos()", "ctg()" -> selection.min + 4
                        "tg()" -> selection.min + 3
                        else -> selection.min + symbol.length
                    }
                    func.copy(expression = TextFieldValue(newText, TextRange(offset)))
                }
            )
        }
    }

    private fun deleteSymbol() {
        val selectedId = _state.value.selectedFunctionId ?: return
        _state.update { state ->
            state.copy(
                functions = state.functions.map { func ->
                    if (func.id != selectedId) return@map func
                    val value = func.expression
                    val text = value.text
                    val selection = value.selection
                    val (newText, newSelection) = if (!selection.collapsed) {
                        text.removeRange(selection.min, selection.max) to selection.min
                    } else if (selection.min > 0) {
                        text.removeRange(selection.min - 1, selection.min) to selection.min - 1
                    } else {
                        text to selection.min
                    }
                    func.copy(expression = TextFieldValue(newText, TextRange(newSelection)))
                }
            )
        }
    }

    private fun updateCanvas(update: (CanvasTransform) -> CanvasTransform) {
        _state.update { it.copy(canvas = update(it.canvas)) }
    }

    private fun processImageRecognition(recognize: suspend () -> String?) {
        recognitionJob?.cancel()
        recognitionJob = viewModelScope.launch {
            _state.update { it.copy(isProcessingImage = true) }
            try {
                val rawText = withContext(Dispatchers.IO) { recognize() }
                if (rawText != null && rawText.isNotBlank()) {
                    val equation = ImageRecognizer.preprocessEquation(rawText)
                    if (isValidEquation(equation)) {
                        addFunctionWithExpression(equation)
                        _imageRecognitionEvents.send(ImageRecognitionEvent.ShowSuccess)
                    } else {
                        _imageRecognitionEvents.send(ImageRecognitionEvent.ShowError("Распознанный текст не похож на уравнение"))
                    }
                } else {
                    _imageRecognitionEvents.send(ImageRecognitionEvent.ShowError("Не удалось распознать текст на изображении"))
                }
            } catch (e: Exception) {
                _imageRecognitionEvents.send(ImageRecognitionEvent.ShowError("Ошибка распознавания: ${e.localizedMessage}"))
            } finally {
                _state.update { it.copy(isProcessingImage = false) }
                _imageRecognitionEvents.send(ImageRecognitionEvent.DismissLoading)
            }
        }
    }

    private fun addFunctionWithExpression(expression: String) {
        _state.update { state ->
            val newFunc = PlotFunction(
                expression = TextFieldValue(expression),
                color = generateDistinctColor(state.functions.map { it.color })
            )
            state.copy(
                functions = state.functions + newFunc,
                selectedFunctionId = newFunc.id
            )
        }
    }

    private fun isValidEquation(expr: String): Boolean {
        if (expr.length > 200) return false
        return expr.matches(Regex("^[a-zA-Z0-9+\\-*/^().,\\s]+$"))
    }

    private fun generateDistinctColor(usedColors: List<Color>): Color {
        val candidates = listOf(
            Color.Red, Color.Blue, Color.Green, Color.Magenta,
            Color.Cyan, Color(0xFFFF9800), Color(0xFF9C27B0), Color(0xFF4CAF50)
        )
        val usedArgb = usedColors.map { it.toArgb() }.toSet()
        return candidates.firstOrNull { it.toArgb() !in usedArgb } ?: Color.Blue
    }

    override fun onCleared() {
        super.onCleared()
        recognitionJob?.cancel()
        ExpressionEvaluator.clearCache()
    }

    companion object {
        class Factory(private val context: Context) : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(PlotterViewModel::class.java)) {
                    return PlotterViewModel(context.applicationContext) as T
                }
                throw IllegalArgumentException("Unknown ViewModel class")
            }
        }
    }
}

sealed class ImageRecognitionEvent {
    data object RequestPermission : ImageRecognitionEvent()
    data class ShowError(val message: String) : ImageRecognitionEvent()
    data object ShowSuccess : ImageRecognitionEvent()
    data object DismissLoading : ImageRecognitionEvent()
}