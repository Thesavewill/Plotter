package com.example.plotter.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.google.mlkit.vision.digitalink.Ink

/**
 * Поле для рисования штрихов пальцем.
 * Собирает штрихи и конвертирует их в Ink для ML Kit.
 */
@Composable
fun InkCanvas(
    onRecognize: (Ink) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val strokeColor = Color.Black
    val strokeWidth = 6f
    val canvasBackgroundColor = Color.White

    var strokes by remember { mutableStateOf<List<List<Offset>>>(emptyList()) }
    var currentStroke by remember { mutableStateOf<List<Offset>>(emptyList()) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(320.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = canvasBackgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
        ) {
            // Canvas для рисования
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(canvasBackgroundColor, RoundedCornerShape(8.dp))
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDragStart = { offset ->
                                currentStroke = listOf(offset)
                            },
                            onDrag = { change, _ ->
                                currentStroke = currentStroke + change.position
                            },
                            onDragEnd = {
                                if (currentStroke.size > 1) {
                                    strokes = strokes + listOf(currentStroke)
                                }
                                currentStroke = emptyList()
                            },
                            onDragCancel = { currentStroke = emptyList() }
                        )
                    }
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    // Рисуем завершённые штрихи
                    strokes.forEach { stroke ->
                        if (stroke.size > 1) {
                            drawPath(
                                path = Path().apply {
                                    moveTo(stroke[0].x, stroke[0].y)
                                    stroke.drop(1).forEach { point ->
                                        lineTo(point.x, point.y)
                                    }
                                },
                                color = strokeColor,
                                style = Stroke(
                                    width = strokeWidth,
                                    cap = StrokeCap.Round,
                                    join = StrokeJoin.Round
                                )
                            )
                        }
                    }
                    // Рисуем текущий штрих
                    if (currentStroke.size > 1) {
                        drawPath(
                            path = Path().apply {
                                moveTo(currentStroke[0].x, currentStroke[0].y)
                                currentStroke.drop(1).forEach { point ->
                                    lineTo(point.x, point.y)
                                }
                            },
                            color = strokeColor,
                            style = Stroke(
                                width = strokeWidth,
                                cap = StrokeCap.Round,
                                join = StrokeJoin.Round
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Кнопки управления
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        strokes = emptyList()
                        currentStroke = emptyList()
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(Icons.Default.Clear, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Очистить")
                }
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(0.8f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Отмена")
                }
                Button(
                    onClick = {
                        val ink = strokesToInk(strokes)
                        if (ink.strokes.isNotEmpty()) {
                            onRecognize(ink)
                        }
                    },
                    modifier = Modifier.weight(1.1f),
                    shape = RoundedCornerShape(12.dp),
                    enabled = strokes.isNotEmpty()
                ) {
                    Icon(Icons.Default.Check, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Распознать")
                }
            }
        }
    }
}

/** Конвертация списка штрихов Compose в ML Kit Ink */
private fun strokesToInk(strokes: List<List<Offset>>): Ink {
    val inkBuilder = Ink.builder()
    strokes.forEach { stroke ->
        if (stroke.size > 1) {
            val strokeBuilder = Ink.Stroke.builder()
            stroke.forEach { point ->
                strokeBuilder.addPoint(Ink.Point.create(point.x, point.y))
            }
            inkBuilder.addStroke(strokeBuilder.build())
        }
    }
    return inkBuilder.build()
}