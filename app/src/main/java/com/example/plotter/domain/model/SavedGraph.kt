package com.example.plotter.domain.model

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.TextFieldValue
import java.util.UUID

data class SavedGraph(
    val id: String = "",
    val name: String = "",
    val functions: List<SavedFunction> = emptyList(),
    val canvasTransform: CanvasTransformData = CanvasTransformData(),
    val createdAt: Long = 0,
    val updatedAt: Long = 0,
    val ownerId: String = ""
)

data class SavedFunction(
    val id: String = UUID.randomUUID().toString(),
    val expression: String = "",
    val colorLong: Long = Color.Blue.value.toLong()
)

data class CanvasTransformData(
    val offsetX: Float = 0f,
    val offsetY: Float = 0f,
    val gridSize: Float = 100f,
    val scale: Float = 1f,
    val countScale: Int = 0
)

// === Конвертеры ===
fun PlotFunction.toSavedFunction(): SavedFunction = SavedFunction(
    id = this.id,
    expression = this.expression.text,
    colorLong = this.color.value.toLong()
)

fun SavedFunction.toPlotFunction(): PlotFunction = PlotFunction(
    id = this.id,
    expression = TextFieldValue(this.expression),
    color = Color(this.colorLong.toULong())
)

fun CanvasTransform.toCanvasTransformData(): CanvasTransformData = CanvasTransformData(
    offsetX = this.offsetX, offsetY = this.offsetY,
    gridSize = this.gridSize, scale = this.scale, countScale = this.countScale
)

fun CanvasTransformData.toCanvasTransform(): CanvasTransform = CanvasTransform(
    offsetX = this.offsetX, offsetY = this.offsetY,
    gridSize = this.gridSize, scale = this.scale, countScale = this.countScale
)