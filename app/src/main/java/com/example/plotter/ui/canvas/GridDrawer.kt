package com.example.plotter.ui.canvas

import android.graphics.Paint
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.nativeCanvas
import com.example.plotter.domain.model.CanvasTransform
import java.util.Locale
import kotlin.math.*

/**
 * Отрисовщик координатной сетки, осей и подписей.
 */
object GridDrawer {
    private val minorColor = 0xFFDDDDDD.toInt() // Светло-серый для мелкой сетки
    private val majorColor = 0xFF999999.toInt() // Серый для крупной сетки
    private val axisColor = 0xFF000000.toInt()  // Черный для осей
    private val textColor = 0xFF333333.toInt()  // Темно-серый для цифр

    /** Основная точка входа — рисует сетку и оси */
    fun draw(scope: DrawScope, canvasState: CanvasTransform) {
        val lineWidth = 1.5f
        val width = scope.size.width
        val height = scope.size.height
        scope.drawGridWithLabels(canvasState, width, height, lineWidth)
        scope.drawAxes(canvasState, width, height, lineWidth * 1.5f)
    }

    /** Отрисовка линий сетки и числовых подписей */
    private fun DrawScope.drawGridWithLabels(
        canvasState: CanvasTransform,
        width: Float,
        height: Float,
        lineWidth: Float
    ) {
        val textPaint = Paint().apply {
            color = textColor
            textSize = 26f
            isAntiAlias = true
        }

        val unitSize = canvasState.gridSize * 2.0.pow(canvasState.countScale.toDouble()).toFloat()
        val pixelsPerUnit = unitSize * 4f
        val targetSpacingPx = 90f
        val rawDataStep = targetSpacingPx / pixelsPerUnit

        val niceStep = calculateNiceStep(rawDataStep.toDouble())
        val majorPixelStep = (niceStep * pixelsPerUnit).toFloat()
        val minorLinesPerMajor = calculateMinorLinesCount(niceStep)
        val minorPixelStep = majorPixelStep / minorLinesPerMajor

        // Вертикальные линии (ось X)
        textPaint.textAlign = Paint.Align.CENTER
        val startIdxX = floor((-canvasState.offsetX) / minorPixelStep).toInt()
        val endIdxX = ceil((width - canvasState.offsetX) / minorPixelStep).toInt()

        for (i in startIdxX..endIdxX) {
            val px = (canvasState.offsetX + i * minorPixelStep)
            if (px < 0f || px > width) continue
            val isMajor = (i % minorLinesPerMajor == 0)
            if (isMajor) {
                // Крупная линия
                drawLine(
                    majorColor.toComposeColor(),
                    Offset(px, 0f),
                    Offset(px, height),
                    lineWidth
                )
                val xVal = (i / minorLinesPerMajor) * niceStep
                if (xVal != 0.0) {
                    // Цифры на оси X
                    drawContext.canvas.nativeCanvas.drawText(
                        formatLabel(xVal),
                        px - textPaint.textSize,
                        canvasState.offsetY + 30f,
                        textPaint
                    )
                }
            } else {
                // Мелкая линия
                drawLine(
                    minorColor.toComposeColor(),
                    Offset(px, 0f),
                    Offset(px, height),
                    lineWidth * 0.7f
                )
            }
        }

        // Горизонтальные линии (ось Y)
        textPaint.textAlign = Paint.Align.RIGHT
        val startIdxY = floor((canvasState.offsetY - height) / minorPixelStep).toInt()
        val endIdxY = ceil(canvasState.offsetY / minorPixelStep).toInt()

        for (i in startIdxY..endIdxY) {
            val py = (canvasState.offsetY - i * minorPixelStep)
            if (py < 0f || py > height) continue
            val isMajor = (i % minorLinesPerMajor == 0)
            if (isMajor) {
                // Крупная линия
                drawLine(
                    majorColor.toComposeColor(),
                    Offset(0f, py),
                    Offset(width, py),
                    lineWidth
                )
                val yVal = (i / minorLinesPerMajor) * niceStep
                // Цифры на оси Y
                drawContext.canvas.nativeCanvas.drawText(
                    formatLabel(yVal),
                    canvasState.offsetX - 8f,
                    py + textPaint.textSize,
                    textPaint
                )
            } else {
                // Мелкая линия
                drawLine(
                    minorColor.toComposeColor(),
                    Offset(0f, py),
                    Offset(width, py),
                    lineWidth * 0.7f
                )
            }
        }
    }

    /** Отрисовка осей X и Y с подписями */
    private fun DrawScope.drawAxes(
        canvasState: CanvasTransform,
        width: Float,
        height: Float,
        lineWidth: Float
    ) {
        val labelPaint = Paint().apply {
            color = axisColor
            textSize = 30f
            textAlign = Paint.Align.CENTER
            isFakeBoldText = true
            isAntiAlias = true
        }
        if (canvasState.offsetY in 0f..height) {
            drawLine(
                axisColor.toComposeColor(),
                Offset(0f, canvasState.offsetY),
                Offset(width, canvasState.offsetY),
                lineWidth
            )
            drawContext.canvas.nativeCanvas.drawText(
                "x", width - 20f,
                canvasState.offsetY - 8f, labelPaint
            )
        }
        if (canvasState.offsetX in 0f..width) {
            drawLine(
                axisColor.toComposeColor(),
                Offset(canvasState.offsetX, 0f),
                Offset(canvasState.offsetX, height),
                lineWidth
            )
            drawContext.canvas.nativeCanvas.drawText(
                "y", canvasState.offsetX + 18f, 22f,
                labelPaint
            )
        }
    }

    /** Конвертация Int цвета в Compose Color */
    private fun Int.toComposeColor(): androidx.compose.ui.graphics.Color {
        return androidx.compose.ui.graphics.Color(this)
    }

    /** Вычисляет "красивый" шаг сетки (1, 2, 5, 10 и т.д.) */
    private fun calculateNiceStep(rawStep: Double): Double {
        if (rawStep <= 0) return 1.0
        val exponent = floor(log10(rawStep)).toInt()
        val fraction = rawStep / 10.0.pow(exponent)
        val niceFraction = when {
            fraction <= 1.0 -> 1.0
            fraction <= 2.0 -> 2.0
            fraction <= 5.0 -> 5.0
            else -> 10.0
        }
        return niceFraction * 10.0.pow(exponent)
    }

    /** Вычисляет количество мелких линий между крупными */
    private fun calculateMinorLinesCount(niceStep: Double): Int {
        val exponent = floor(log10(niceStep)).toInt()
        val fraction = niceStep / 10.0.pow(exponent)
        return if (niceStep < 1.0 || fraction == 5.0) 5 else 4
    }

    /** Форматирует числовое значение для подписи оси */
    private fun formatLabel(value: Double): String {
        if (value == 0.0) return "0"
        val abs = abs(value)
        if (abs >= 1e5 || abs < 1e-3) {
            return String.format(Locale.US, "%.1e", value)
                .replace(Regex("\\.0*e"), "e")
                .replace(Regex("e[+-]?0*"), "e")
        }
        val maxDec = if (abs >= 10) 1 else if (abs >= 1) 2 else 4
        return String.format(Locale.US, "%.${maxDec}f", value)
            .replace(Regex("\\.?0*$"), "")
    }
}