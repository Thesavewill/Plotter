package com.example.plotter.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.plotter.ui.canvas.CanvasPlot
import com.example.plotter.ui.panels.ColorPickerDialog
import com.example.plotter.ui.panels.FunctionInputPanel
import com.example.plotter.ui.panels.KeyboardPanel

@Composable
fun PlotterScreen(
    state: PlotterContract.State,
    onIntent: (PlotterContract.Intent) -> Unit,
    onImageCaptureRequested: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(modifier = modifier.fillMaxSize()) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
            // График на заднем плане
            CanvasPlot(
                state = state.canvas,
                functions = state.functions,
                onIntent = onIntent,
                modifier = Modifier.fillMaxSize()
            )

            // Панели поверх графика
            Column(modifier = Modifier.fillMaxSize()) {
                Spacer(modifier = Modifier.weight(6.5f))

                FunctionInputPanel(
                    functions = state.functions,
                    selectedId = state.selectedFunctionId,
                    colorPickerState = state.colorPicker,
                    onIntent = onIntent,
                    onImageCaptureRequested = onImageCaptureRequested,
                    modifier = Modifier.weight(2.5f)
                )

                KeyboardPanel(
                    onIntent = onIntent,
                    modifier = Modifier.weight(6f)
                )
            }

            // Индикатор загрузки при обработке изображения
            if (state.isProcessingImage) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color.White)
                }
            }

            // Диалог выбора цвета
            if (state.colorPicker.isVisible) {
                ColorPickerDialog(
                    onColorSelected = { color ->
                        state.colorPicker.targetFunctionId?.let { id ->
                            onIntent(PlotterContract.Intent.ChangeColor(id, color))
                        }
                        onIntent(PlotterContract.Intent.CloseColorPicker)
                    },
                    onDismiss = { onIntent(PlotterContract.Intent.CloseColorPicker) }
                )
            }
        }
    }
}