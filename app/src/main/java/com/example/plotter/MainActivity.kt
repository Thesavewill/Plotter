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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.tooling.preview.Preview
import com.example.plotter.ui.theme.PlotterTheme

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
    var lineColor1 = colorResource(id = R.color.gray)
    var lineColor2 = colorResource(id = R.color.black)

    var offsetX by remember { mutableStateOf(0f) }
    var offsetY by remember { mutableStateOf(0f) }

    Box(modifier = Modifier.pointerInput( Unit){
        detectDragGestures{change, dragAmount ->
            change.consume()
            offsetX += dragAmount.x
            offsetY += dragAmount.y
        }
    }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val gridWidth = 5f

            val width = size.width
            val height = size.height

            var x = offsetX % gridSize
            var y = offsetY % gridSize

            while (y <= height) {                     //ГОРИЗОНТАЛЬ
                drawLine(
                    color = lineColor1,
                    start = Offset(0f, y),
                    end = Offset(width, y),
                    strokeWidth = gridWidth
                )
                y += gridSize
            }

            while (x <= width) {                      //ВЕРТИКАЛЬ
                drawLine(
                    color = lineColor1,
                    start = Offset(x, 0f),
                    end = Offset(x, height),
                    strokeWidth = gridWidth
                )
                x += gridSize
            }

            val axisX = offsetX                       //Ось X
            if (axisX in 0f..width) {
                drawLine(
                    color = lineColor2,
                    start = Offset(axisX, 0f),
                    end = Offset(axisX, height),
                    strokeWidth = gridWidth * 1.5f
                )
            }
            val axisY = offsetY                       //Ось Y
            if (axisY in 0f..height) {
                drawLine(
                    color = lineColor2,
                    start = Offset(0f, axisY),
                    end = Offset(width, axisY),
                    strokeWidth = gridWidth * 1.5f
                )
            }
        }
    }
}