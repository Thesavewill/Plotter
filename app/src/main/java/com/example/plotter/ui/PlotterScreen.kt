package com.example.plotter.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.plotter.ui.canvas.CanvasPlot
import com.example.plotter.ui.panels.ColorPickerDialog
import com.example.plotter.ui.panels.FunctionInputPanel
import com.example.plotter.ui.panels.KeyboardPanel

@Composable
fun PlotterScreen(
    state: PlotterContract.State,
    onIntent: (PlotterContract.Intent) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(modifier = modifier.fillMaxSize()) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            CanvasPlot(
                state = state.canvas,
                functions = state.functions,
                onIntent = onIntent,
                modifier = Modifier.fillMaxSize()
            )

            Column(modifier = Modifier.fillMaxSize()) {
                Spacer(modifier = Modifier.weight(1f))

                FunctionInputPanel(
                    functions = state.functions,
                    selectedId = state.selectedFunctionId,
                    colorPickerState = state.colorPicker,
                    onIntent = onIntent,
                    modifier = Modifier
                        .weight(0.35f)
                        .fillMaxWidth()
                )

                KeyboardPanel(
                    onIntent = onIntent,
                    modifier = Modifier
                        .weight(0.65f)
                        .fillMaxWidth()
                )
            }

            // Диалог цвета
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