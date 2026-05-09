package com.example.plotter

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import com.example.plotter.domain.model.CanvasTransform
import com.example.plotter.domain.model.PlotFunction
import com.example.plotter.ui.PlotterContract
import com.example.plotter.ui.PlotterScreen
import com.example.plotter.ui.theme.PlotterTheme
import com.example.plotter.viewmodel.PlotterViewModel
import androidx.compose.ui.text.TextRange

class MainActivity : ComponentActivity() {

    private val viewModel: PlotterViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PlotterTheme {
                val state by viewModel.state.collectAsState()
                PlotterScreen(
                    state = state,
                    onIntent = viewModel::handleIntent,
                    modifier = Modifier.Companion
                )
            }
        }
    }
}

@Preview(
    showSystemUi = true,
    showBackground = true,
    device = "spec:width=720px,height=1680px,dpi=440",
    name = "Plotter - Interactive"
)
@Composable
fun PlotterScreenPreview() {
    PlotterTheme {
        // ========== Локальное состояние ==========
        val previewState = remember {
            mutableStateOf(
                PlotterContract.State(
                    canvas = CanvasTransform(
                        offsetX = 360f,
                        offsetY = 420f,
                        gridSize = 36f,
                        scale = 1f,
                        countScale = 0,
                        canvasWidth = 720f,
                        canvasHeight = 1680f,
                        isInitialized = true
                    ),
                    functions = listOf(),
                    selectedFunctionId = null
                )
            )
        }

        // ========== Обработчик интентов ==========
        val handleIntent: (PlotterContract.Intent) -> Unit = let@{ intent ->
            when (intent) {
                // === Ввод текста ===
                is PlotterContract.Intent.UpdateExpression -> {
                    previewState.value = previewState.value.copy(
                        functions = previewState.value.functions.map { func ->
                            if (func.id == intent.id) func.copy(expression = intent.value) else func
                        }
                    )
                }

                // === Выбор функции ===
                is PlotterContract.Intent.SelectFunction -> {
                    previewState.value = previewState.value.copy(selectedFunctionId = intent.id)
                }

                // === Вставка символа с клавиатуры ===
                is PlotterContract.Intent.InsertSymbol -> {
                    val selectedId = previewState.value.selectedFunctionId ?: return@let
                    previewState.value = previewState.value.copy(
                        functions = previewState.value.functions.map { func ->
                            if (func.id != selectedId) return@map func
                            val value = func.expression
                            val text = value.text
                            val selection = value.selection
                            val newText = text.replaceRange(selection.min, selection.max, intent.symbol)
                            val offset = if (intent.symbol == "sin()") selection.min + 4 else selection.min + intent.symbol.length
                            func.copy(expression = TextFieldValue(newText, TextRange(offset)))
                        }
                    )
                }

                // === Удаление символа ===
                PlotterContract.Intent.DeleteSymbol -> {
                    val selectedId = previewState.value.selectedFunctionId ?: return@let
                    previewState.value = previewState.value.copy(
                        functions = previewState.value.functions.map { func ->
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

                // === Добавить функцию ===
                is PlotterContract.Intent.AddFunction -> {
                    val newFunc = PlotFunction()
                    previewState.value = previewState.value.copy(
                        functions = previewState.value.functions + newFunc,
                        selectedFunctionId = newFunc.id
                    )
                }

                // === Удалить функцию ===
                is PlotterContract.Intent.RemoveFunction -> {
                    val newList = previewState.value.functions.filter { it.id != intent.id }
                    previewState.value = previewState.value.copy(
                        functions = if (newList.isEmpty()) listOf(PlotFunction()) else newList,
                        selectedFunctionId = if (previewState.value.selectedFunctionId == intent.id) null else previewState.value.selectedFunctionId
                    )
                }

                // === Открыть выбор цвета ===
                is PlotterContract.Intent.OpenColorPicker -> {
                    previewState.value = previewState.value.copy(
                        colorPicker = PlotterContract.ColorPickerState(
                            isVisible = true,
                            targetFunctionId = intent.functionId
                        )
                    )
                }

                // === Закрыть выбор цвета ===
                PlotterContract.Intent.CloseColorPicker -> {
                    previewState.value = previewState.value.copy(
                        colorPicker = PlotterContract.ColorPickerState()
                    )
                }

                // === Изменить цвет ===
                is PlotterContract.Intent.ChangeColor -> {
                    previewState.value = previewState.value.copy(
                        functions = previewState.value.functions.map { func ->
                            if (func.id == intent.id) func.copy(color = intent.color) else func
                        }
                    )
                }

                // === Зум/пан ===
                is PlotterContract.Intent.Pan,
                is PlotterContract.Intent.Zoom,
                is PlotterContract.Intent.CanvasInitialized -> {
                }
            }
        }

        // ========== Рендер экрана с интерактивным состоянием ==========
        PlotterScreen(
            state = previewState.value,
            onIntent = handleIntent,
            modifier = Modifier.fillMaxSize()
        )
    }
}