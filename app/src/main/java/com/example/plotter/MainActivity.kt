package com.example.plotter

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.tooling.preview.Preview
import com.example.plotter.ui.theme.PlotterTheme
import kotlin.math.abs
import kotlin.math.roundToInt

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PlotterTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Greeting(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun Greeting(gridSize: Float = 70f, modifier: Modifier = Modifier) {
    val lineColor1 = colorResource(id = R.color.light_gray)   // ЗАРАНЕЕ ОПРЕДЕЛЯЕМ ЦВЕТА ДЛЯ Canvas
    val lineColor2 = colorResource(id = R.color.gray)         // ЗАРАНЕЕ ОПРЕДЕЛЯЕМ ЦВЕТА ДЛЯ Canvas
    val lineColor3 = colorResource(id = R.color.black)        // ЗАРАНЕЕ ОПРЕДЕЛЯЕМ ЦВЕТА ДЛЯ Canvas

    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }

    var gridSize: Float = 50f
    Box(modifier = Modifier
        .pointerInput( Unit){                               //ДВИЖЕНИЕ СЕТКИ
                detectDragGestures{change, dragAmount -> change.consume()
                offsetX += dragAmount.x
                offsetY += dragAmount.y
            }
        }
            .pointerInput(Unit) {                           //ЗУМ СЕТКИ
                detectTransformGestures { centroid, pan, zoom, rotation ->
                offsetX += pan.x
                offsetY += pan.y
                gridSize *= zoom
            }
        }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val gridWidth = 4f

            val width = size.width
            val height = size.height

            var minorX = offsetX % gridSize
            var minorY = offsetY % gridSize

            var majorX = offsetX % gridSize
            var majorY = offsetY % gridSize

            //----------------Минорные линии сетки--------------
            while (minorY < height) {                     //ГОРИЗОНТАЛЬНЫЕ МИНОРНЫЕ ЛИНИИ
                drawLine(
                    color = lineColor1,
                    start = Offset(0f, minorY),
                    end = Offset(width, minorY),
                    strokeWidth = gridWidth
                )
                minorY += gridSize
            }
            while (minorX < width) {                     //ВЕРТИКАЛЬНЫЕ МИНОРНЫЕ ЛИНИИ
                drawLine(
                    color = lineColor1,
                    start = Offset(minorX, 0f),
                    end = Offset(minorX, height),
                    strokeWidth = gridWidth
                )
                minorX += gridSize
            }

            //----------------Мажорные линии сетки--------------
            while (majorY < height) {                       //ГОРИЗОНТАЛЬНЫЕ МАЖОРНЫЕ ЛИНИИ
                if (((majorY - offsetY) / gridSize).roundToInt() % 4f == 0f) {
                    drawLine(
                        color = lineColor2,
                        start = Offset(0f, majorY),
                        end = Offset(width, majorY),
                        strokeWidth = gridWidth
                    )
                }
                majorY += gridSize
            }
            while (majorX < width) {                        //ВЕРТИКАЛЬНЫЕ МАЖОРНЫЕ ЛИНИИ
                if (((majorX - offsetX) / gridSize).roundToInt() % 4f == 0f) {
                    drawLine(
                        color = lineColor2,
                        start = Offset(majorX, 0f),
                        end = Offset(majorX, height),
                        strokeWidth = gridWidth
                    )
                }
                majorX += gridSize
            }

            //----------------Ox и Oy--------------
            if (offsetY < height) {                     //Ось X
                drawLine(
                    color = lineColor3,
                    start = Offset(0f, offsetY),
                    end = Offset(width, offsetY),
                    strokeWidth = gridWidth * 1.5f
                )
            }
            if (offsetX < width) {                      //Ось Y
                drawLine(
                    color = lineColor3,
                    start = Offset(offsetX, 0f),
                    end = Offset(offsetX, height),
                    strokeWidth = gridWidth * 1.5f
                )
            }
        }
    }
}