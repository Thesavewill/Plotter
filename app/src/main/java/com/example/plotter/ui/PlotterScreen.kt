package com.example.plotter.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.plotter.domain.model.SavedGraph
import com.example.plotter.ui.canvas.CanvasPlot
import com.example.plotter.ui.components.AccountButton
import com.example.plotter.ui.components.InkCanvas
import com.example.plotter.ui.panels.ColorPickerDialog
import com.example.plotter.ui.panels.FunctionInputPanel
import com.example.plotter.ui.panels.KeyboardPanel
import com.example.plotter.ui.theme.AppColors
import com.google.mlkit.vision.digitalink.Ink

/** Главный экран приложения с графиком и панелями управления */
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
        modifier = modifier.fillMaxSize().padding(bottom = 32.dp),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        containerColor = AppColors.White
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            // График на заднем плане
            CanvasPlot(
                state = state.canvas,
                functions = state.functions,
                onIntent = onIntent,
                modifier = Modifier.fillMaxSize()
                    .padding(bottom = 40.dp)
            )

            // Кнопка аккаунта (правый верхний угол)
            AccountButton(
                userEmail = state.currentUserEmail,
                onIntent = onIntent,
                onSignInClick = onSignInRequested,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 48.dp, end = 16.dp)
            )

            // Панели ввода поверх графика
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
                    modifier = Modifier.weight(0.4f)
                )
                // Переключение между клавиатурой и рукописным вводом
                if (state.isHandwritingMode) {
                    InkCanvas(
                        onRecognize = { ink: Ink ->
                            onIntent(PlotterContract.Intent.ProcessHandwritingInk(ink))
                        },
                        onDismiss = {
                            onIntent(PlotterContract.Intent.CloseHandwritingMode)
                        },
                        modifier = Modifier.weight(0.5f)
                    )
                } else {
                    KeyboardPanel(
                        onIntent = onIntent,
                        modifier = Modifier.weight(0.5f)
                    )
                }
            }

            // Индикатор загрузки при обработке изображения
            if (state.isProcessingImage) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(AppColors.LoadingDim),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = AppColors.White)
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

            // Диалог списка сохранённых графиков
            if (state.showGraphsList) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(AppColors.DialogDim)
                        .clickable(
                            onClick = { onIntent(PlotterContract.Intent.CloseGraphsList) }
                        )
                ) {
                    Card(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .fillMaxWidth(0.92f)
                            .heightIn(min = 400.dp, max = 520.dp),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(20.dp)
                        ) {
                            // Заголовок
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "Мои графики",
                                        style = MaterialTheme.typography.headlineSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "${state.savedGraphs.size} сохранено",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                IconButton(
                                    onClick = {
                                        onIntent(PlotterContract.Intent.CloseGraphsList)
                                    },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = "Закрыть",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Контент: скелетон / пустое состояние / список
                            when {
                                state.isLoadingGraphs -> {
                                    LazyColumn(
                                        modifier = Modifier.weight(1f),
                                        verticalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        items(4) { SkeletonGraphItem() }
                                    }
                                }
                                state.savedGraphs.isEmpty() -> {
                                    EmptyGraphsList(
                                        onActionClick = {
                                            onIntent(PlotterContract.Intent.SaveGraph)
                                        }
                                    )
                                }
                                else -> {
                                    LazyColumn(
                                        modifier = Modifier.weight(1f),
                                        verticalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        items(state.savedGraphs, key = { it.id }) { graph ->
                                            SavedGraphItem(
                                                graph = graph,
                                                onLoadClick = {
                                                    onIntent(
                                                        PlotterContract.Intent.LoadGraph(graph.id)
                                                    )
                                                },
                                                onDeleteClick = {
                                                    onIntent(
                                                        PlotterContract.Intent.DeleteGraph(graph.id)
                                                    )
                                                },
                                                onRenameClick = {
                                                    onIntent(
                                                        PlotterContract.Intent.RequestRenameGraph(
                                                            graph.id, graph.name
                                                        )
                                                    )
                                                }
                                            )
                                        }
                                        item { Spacer(modifier = Modifier.height(8.dp)) }
                                    }
                                }
                            }

                            // Кнопки действий
                            Spacer(modifier = Modifier.height(16.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                OutlinedButton(
                                    onClick = {
                                        onIntent(PlotterContract.Intent.CloseGraphsList)
                                    },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Close,
                                        null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(Modifier.width(4.dp))
                                    Text("Закрыть")
                                }
                                Button(
                                    onClick = {
                                        onIntent(PlotterContract.Intent.SaveGraph)
                                    },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.primary
                                    )
                                ) {
                                    Icon(
                                        Icons.Default.Save,
                                        null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(Modifier.width(4.dp))
                                    Text("Сохранить текущий")
                                }
                            }
                        }
                    }
                }
            }

            // Диалог переименования графика
            if (state.showRenameDialog) {
                AlertDialog(
                    onDismissRequest = {
                        onIntent(PlotterContract.Intent.CloseRenameDialog)
                    },
                    title = { Text("Переименовать график") },
                    text = {
                        OutlinedTextField(
                            value = state.renameGraphName,
                            onValueChange = {
                                onIntent(PlotterContract.Intent.UpdateRenameName(it))
                            },
                            label = { Text("Новое имя") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                onIntent(PlotterContract.Intent.ConfirmRename)
                            }
                        ) {
                            Text("Сохранить")
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = {
                                onIntent(PlotterContract.Intent.CloseRenameDialog)
                            }
                        ) {
                            Text("Отмена")
                        }
                    }
                )
            }
        }
    }
}

/** Карточка сохранённого графика в списке */
@Composable
private fun SavedGraphItem(
    graph: SavedGraph,
    onLoadClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onRenameClick: () -> Unit
) {
    val formattedDate = remember(graph.updatedAt) {
        java.text.SimpleDateFormat("dd.MM.yyyy HH:mm", java.util.Locale.getDefault())
            .format(java.util.Date(graph.updatedAt))
    }
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        onClick = onLoadClick
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    // Кликабельное название для переименования
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable(onClick = onRenameClick)
                    ) {
                        Text(
                            text = graph.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Переименовать",
                            modifier = Modifier.size(18.dp).padding(start = 4.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Schedule,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = formattedDate,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                }
                // Бейдж с количеством функций
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.padding(start = 12.dp)
                ) {
                    Text(
                        text = "${graph.functions.size}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
            // Превью цветов функций
            if (graph.functions.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    graph.functions.take(5).forEach { func ->
                        Box(
                            modifier = Modifier
                                .size(16.dp)
                                .clip(CircleShape)
                                .background(Color(func.colorLong.toInt()))
                        )
                    }
                    if (graph.functions.size > 5) {
                        Text(
                            text = "+${graph.functions.size - 5}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            // Кнопки действий
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(
                    onClick = onLoadClick,
                    modifier = Modifier.height(36.dp)
                ) {
                    Icon(
                        Icons.Default.CloudDownload,
                        null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text("Загрузить")
                }
                IconButton(
                    onClick = onDeleteClick,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Удалить",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

/** Скелетон-заглушка для состояния загрузки */
@Composable
private fun SkeletonGraphItem() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .height(20.dp)
                    .background(
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f),
                        RoundedCornerShape(4.dp)
                    )
            )
            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.4f)
                    .height(14.dp)
                    .background(
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.15f),
                        RoundedCornerShape(4.dp)
                    )
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                repeat(4) {
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .clip(CircleShape)
                            .background(
                                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f)
                            )
                    )
                }
            }
        }
    }
}

/** Пустое состояние списка графиков */
@Composable
private fun EmptyGraphsList(onActionClick: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.Folder,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Нет сохранённых графиков",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Сохраните текущий график, чтобы он появился здесь",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 24.dp)
        )
        Spacer(modifier = Modifier.height(20.dp))
        Button(
            onClick = onActionClick,
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.Save, null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(4.dp))
            Text("Сохранить график")
        }
    }
}