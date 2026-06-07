package com.example.plotter.ui.canvas

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import com.example.plotter.domain.evaluator.CustomFunctions
import com.example.plotter.domain.model.CanvasTransform
import com.example.plotter.domain.model.PlotFunction
import com.example.plotter.ui.PlotterContract
import net.objecthunter.exp4j.ExpressionBuilder
import kotlin.math.pow

@Composable
fun CanvasPlot(
    state: CanvasTransform,
    functions: List<PlotFunction>,
    onIntent: (PlotterContract.Intent) -> Unit,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTransformGestures { centroid, pan, zoom, _ ->
                    onIntent(PlotterContract.Intent.Pan(pan.x, pan.y))
                    if (zoom != 1f) {
                        onIntent(PlotterContract.Intent.Zoom(zoom, centroid.x, centroid.y))
                    }
                }
            }
    ) {
        LaunchedEffect(constraints.maxWidth, constraints.maxHeight) {
            if (!state.isInitialized) {
                onIntent(PlotterContract.Intent.CanvasInitialized(
                    width = constraints.maxWidth.toFloat(),
                    height = constraints.maxHeight.toFloat()
                ))
            }
        }

        Canvas(modifier = Modifier.fillMaxSize()) {
            GridDrawer.draw(this, state)
            functions.forEach { func ->
                if (func.expression.text.isNotBlank()) {
                    drawFunction(state, func)
                }
            }
        }
    }
}

private fun DrawScope.drawFunction(canvasState: CanvasTransform, function: PlotFunction) {
    if (canvasState.gridSize <= 0f) return

    val unitSize = canvasState.gridSize * 2.0.pow(canvasState.countScale.toDouble()).toFloat()
    val path = Path()
    var started = false

    val compiledExpression = try {
        ExpressionBuilder(function.expression.text)
            .variable("x")
            .functions(CustomFunctions.all)
            .build()
    } catch (e: Exception) {
        null
    }

    if (compiledExpression == null) return

    val step = 1
    for (px in 0..size.width.toInt() step step) {
        val x = (px - canvasState.offsetX) / unitSize / 4
        val y = try {
            compiledExpression.setVariable("x", x.toDouble()).evaluate().toFloat()
        } catch (e: Exception) {
            Float.NaN
        }

        val py = canvasState.offsetY - y * unitSize * 4

        if (py.isFinite()) {
            if (!started) {
                path.moveTo(px.toFloat(), py)
                started = true
            } else {
                if (py > -1000f && py < size.height + 1000f) {
                    path.lineTo(px.toFloat(), py)
                } else {
                    started = false
                }
            }
        } else {
            started = false
        }
    }

    drawPath(
        path = path,
        color = function.color,
        style = Stroke(width = 4f)
    )
}