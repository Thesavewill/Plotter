package com.example.plotter.domain.model

data class CanvasTransform(
    val offsetX: Float = 0f,
    val offsetY: Float = 0f,
    val gridSize: Float = 100f,
    val scale: Float = 1f,
    val countScale: Int = 0,
    val canvasWidth: Float = 0f,
    val canvasHeight: Float = 0f,
    val isInitialized: Boolean = false
)