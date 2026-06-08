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
import com.example.plotter.domain.model.CanvasTransform
import com.example.plotter.domain.model.PlotFunction
import com.example.plotter.domain.model.SavedGraph
import com.example.plotter.ui.canvas.CanvasPlot
import com.example.plotter.ui.components.AccountButton
import com.example.plotter.ui.panels.ColorPickerDialog
import com.example.plotter.ui.panels.FunctionInputPanel
import com.example.plotter.ui.panels.KeyboardPanel
import com.example.plotter.ui.theme.AppColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlotterScreen(
    state: PlotterContract.State,
    onIntent: (PlotterContract.Intent) -> Unit,
    onImageCaptureRequested: () -> Unit,
    onSignInRequested: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(modifier = modifier.fillMaxSize(), contentWindowInsets = WindowInsets(0, 0, 0, 0), containerColor = AppColors.White) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding).fillMaxSize().navigationBarsPadding()) {
            CanvasPlot(state = state.canvas, functions = state.functions, onIntent = onIntent, modifier = Modifier.fillMaxSize())

            AccountButton(userEmail = state.currentUserEmail, onIntent = onIntent, onSignInClick = onSignInRequested, modifier = Modifier.align(Alignment.TopEnd).padding(top = 48.dp, end = 16.dp))

            Column(modifier = Modifier.fillMaxSize().imePadding()) {
                Spacer(modifier = Modifier.weight(1f))
                FunctionInputPanel(functions = state.functions, selectedId = state.selectedFunctionId, colorPickerState = state.colorPicker, onIntent = onIntent, onImageCaptureRequested = onImageCaptureRequested, modifier = Modifier.weight(0.4f))
                KeyboardPanel(onIntent = onIntent, modifier = Modifier.weight(0.5f))
            }

            if (state.isProcessingImage) {
                Box(modifier = Modifier.fillMaxSize().background(AppColors.LoadingDim), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = AppColors.White) }
            }

            if (state.colorPicker.isVisible) {
                ColorPickerDialog(onColorSelected = { color -> state.colorPicker.targetFunctionId?.let { id -> onIntent(PlotterContract.Intent.ChangeColor(id, color)) }; onIntent(PlotterContract.Intent.CloseColorPicker) }, onDismiss = { onIntent(PlotterContract.Intent.CloseColorPicker) })
            }

            if (state.showGraphsList) {
                Box(modifier = Modifier.fillMaxSize().background(AppColors.DialogDim).clickable(onClick = { onIntent(PlotterContract.Intent.CloseGraphsList) })) {
                    Card(modifier = Modifier.align(Alignment.Center).fillMaxWidth(0.92f).heightIn(min = 400.dp, max = 520.dp), shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)) {
                        Column(modifier = Modifier.fillMaxSize().padding(20.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Column {
                                    Text(text = "Мои графики", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                    Text(text = "${state.savedGraphs.size} сохранено", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                IconButton(onClick = { onIntent(PlotterContract.Intent.CloseGraphsList) }, modifier = Modifier.size(36.dp)) { Icon(Icons.Default.Close, contentDescription = "Закрыть", tint = MaterialTheme.colorScheme.onSurfaceVariant) }
                            }
                            Spacer(modifier = Modifier.height(16.dp))

                            when {
                                state.isLoadingGraphs -> {
                                    LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(12.dp)) { items(4) { SkeletonGraphItem() } }
                                }
                                state.savedGraphs.isEmpty() -> {
                                    EmptyGraphsList(onActionClick = { onIntent(PlotterContract.Intent.SaveGraph) })
                                }
                                else -> {
                                    LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                        items(state.savedGraphs, key = { it.id }) { graph ->
                                            SavedGraphItem(graph = graph, onLoadClick = { onIntent(PlotterContract.Intent.LoadGraph(graph.id)) }, onDeleteClick = { onIntent(PlotterContract.Intent.DeleteGraph(graph.id)) })
                                        }
                                        item { Spacer(modifier = Modifier.height(8.dp)) }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                OutlinedButton(onClick = { onIntent(PlotterContract.Intent.CloseGraphsList) }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp)) { Icon(Icons.Default.Close, null, modifier = Modifier.size(18.dp)); Spacer(Modifier.width(4.dp)); Text("Закрыть") }
                                Button(onClick = { onIntent(PlotterContract.Intent.SaveGraph) }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)) { Icon(Icons.Default.Save, null, modifier = Modifier.size(18.dp)); Spacer(Modifier.width(4.dp)); Text("Сохранить текущий") }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SavedGraphItem(graph: SavedGraph, onLoadClick: () -> Unit, onDeleteClick: () -> Unit) {
    val formattedDate = remember(graph.updatedAt) { java.text.SimpleDateFormat("dd.MM.yyyy HH:mm", java.util.Locale.getDefault()).format(java.util.Date(graph.updatedAt)) }
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant), onClick = onLoadClick) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = graph.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Schedule, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = formattedDate, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
                    }
                }
                Surface(shape = RoundedCornerShape(8.dp), color = MaterialTheme.colorScheme.primaryContainer, modifier = Modifier.padding(start = 12.dp)) { Text(text = "${graph.functions.size}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)) }
            }
            if (graph.functions.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                    graph.functions.take(5).forEach { func -> Box(modifier = Modifier.size(16.dp).clip(CircleShape).background(Color(func.colorLong.toInt()))) }
                    if (graph.functions.size > 5) Text(text = "+${graph.functions.size - 5}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Row(modifier = Modifier.fillMaxWidth().padding(top = 12.dp), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = onLoadClick, modifier = Modifier.height(36.dp)) { Icon(Icons.Default.CloudDownload, null, modifier = Modifier.size(16.dp)); Spacer(Modifier.width(4.dp)); Text("Загрузить") }
                IconButton(onClick = onDeleteClick, modifier = Modifier.size(36.dp)) { Icon(Icons.Default.Delete, contentDescription = "Удалить", tint = MaterialTheme.colorScheme.error) }
            }
        }
    }
}

@Composable
private fun SkeletonGraphItem() {
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))) {
        Column(modifier = Modifier.padding(16.dp)) {
            Box(modifier = Modifier.fillMaxWidth(0.7f).height(20.dp).background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f), RoundedCornerShape(4.dp)))
            Spacer(modifier = Modifier.height(8.dp))
            Box(modifier = Modifier.fillMaxWidth(0.4f).height(14.dp).background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.15f), RoundedCornerShape(4.dp)))
            Spacer(modifier = Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) { repeat(4) { Box(modifier = Modifier.size(16.dp).clip(CircleShape).background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f))) } }
        }
    }
}

@Composable
private fun EmptyGraphsList(onActionClick: () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Icon(Icons.Default.Folder, contentDescription = null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f))
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "Нет сохранённых графиков", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "Сохраните текущий график, чтобы он появился здесь", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f), textAlign = TextAlign.Center, modifier = Modifier.padding(horizontal = 24.dp))
        Spacer(modifier = Modifier.height(20.dp))
        Button(onClick = onActionClick, shape = RoundedCornerShape(12.dp)) { Icon(Icons.Default.Save, null, modifier = Modifier.size(18.dp)); Spacer(Modifier.width(4.dp)); Text("Сохранить график") }
    }
}