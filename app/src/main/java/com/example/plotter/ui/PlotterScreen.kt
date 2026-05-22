package com.example.plotter.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.example.plotter.domain.model.CanvasTransform
import com.example.plotter.domain.model.PlotFunction
import com.example.plotter.domain.model.SavedGraph
import com.example.plotter.ui.canvas.CanvasPlot
import com.example.plotter.ui.components.AccountButton
import com.example.plotter.ui.panels.ColorPickerDialog
import com.example.plotter.ui.panels.FunctionInputPanel
import com.example.plotter.ui.panels.KeyboardPanel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlotterScreen(
    state: PlotterContract.State,
    onIntent: (PlotterContract.Intent) -> Unit,
    onImageCaptureRequested: () -> Unit,
    onSignInRequested: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .navigationBarsPadding()
        ) {
            // График на заднем плане
            CanvasPlot(
                state = state.canvas,
                functions = state.functions,
                onIntent = onIntent,
                modifier = Modifier.fillMaxSize()
            )

            // Кнопка аккаунта (правый верхний угол)
            AccountButton(
                userEmail = state.currentUserEmail,
                onIntent = onIntent,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(32.dp)
            )

            // Панели поверх графика
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .imePadding()
            ) {
                Spacer(modifier = Modifier.weight(1f))

                FunctionInputPanel(
                    functions = state.functions,
                    selectedId = state.selectedFunctionId,
                    colorPickerState = state.colorPicker,
                    onIntent = onIntent,
                    onImageCaptureRequested = onImageCaptureRequested,
                    modifier = Modifier.height(140.dp)
                )

                KeyboardPanel(
                    onIntent = onIntent,
                    modifier = Modifier.weight(1f)
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

            if (state.showGraphsList) {
                AlertDialog(
                    onDismissRequest = { onIntent(PlotterContract.Intent.CloseGraphsList) },
                    title = { Text("Сохранённые графики") },
                    text = {
                        if (state.isLoadingGraphs) {
                            Box(
                                modifier = Modifier.fillMaxWidth().height(100.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator()
                            }
                        } else if (state.savedGraphs.isEmpty()) {
                            Text(
                                text = "Нет сохранённых графиков",
                                modifier = Modifier.padding(vertical = 16.dp),
                                color = Color.Gray
                            )
                        } else {
                            LazyColumn(
                                modifier = Modifier.height(300.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(state.savedGraphs.size) { index ->
                                    val graph = state.savedGraphs[index]
                                    android.util.Log.d("UI_DEBUG", "Rendering graph: ${graph.name}")

                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Text(
                                                text = graph.name,
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = "${graph.functions.size} функций",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = Color.Gray
                                            )
                                            Row(
                                                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                                                horizontalArrangement = Arrangement.End
                                            ) {
                                                TextButton(
                                                    onClick = {
                                                        android.util.Log.d("UI_DEBUG", "Loading graph: ${graph.id}")
                                                        onIntent(PlotterContract.Intent.LoadGraph(graph.id))
                                                    }
                                                ) {
                                                    Text("Загрузить")
                                                }
                                                TextButton(
                                                    onClick = {
                                                        android.util.Log.d("UI_DEBUG", "Deleting graph: ${graph.id}")
                                                        onIntent(PlotterContract.Intent.DeleteGraph(graph.id))
                                                    }
                                                ) {
                                                    Text("Удалить", color = Color.Red)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = { onIntent(PlotterContract.Intent.CloseGraphsList) }) {
                            Text("Закрыть")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { onIntent(PlotterContract.Intent.SaveGraph) }) {
                            Text("Сохранить текущий", color = Color(0xFF4CAF50))
                        }
                    }
                )
            }
        }
    }
}