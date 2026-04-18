package com.example.plotter

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.plotter.ui.theme.PlotterTheme
import net.objecthunter.exp4j.ExpressionBuilder
import net.objecthunter.exp4j.function.Function
import kotlin.math.pow
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

// Тестовая функция
val mySin = object : Function("mySin", 1) {
    override fun apply(vararg args: Double): Double {
        val x = args[0]
        return x - x.pow(3.0) / 6 + x.pow(5.0) / 120
    }
}
// Список всех кастомных функций exp4j
val customFunctions = listOf(mySin)

fun DrawScope.Fuction1(offsetX: Float, offsetY: Float, gridSize: Float, countScale: Int, expressionString: String) {
    if (expressionString.isBlank()) return

    val unitSize = gridSize * 2f.pow(countScale.toFloat())
    val path = Path()
    var started = false

    val expression = try {
        ExpressionBuilder(expressionString)
            .variable("x")
            .functions(customFunctions)
            .build()
    } catch (e: Exception) {
        null
    }

    if (expression == null) return

    val step = 1
    for (px in 0..size.width.toInt() step step) {
        val x = (px - offsetX) / unitSize

        val y = try {
            expression.setVariable("x", x.toDouble()).evaluate().toFloat()
        } catch (e: Exception) {
            Float.NaN
        }

        val py = offsetY - y * unitSize

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
        color = Color.Blue,
        style = Stroke(width = 4f)
    )
}
fun DrawScope.Neravenstvo(offsetX: Float, offsetY: Float, gridSize: Float, countScale: Int, text: String){

}

// Определения вида функции
fun DrawScope.CheckFunc(offsetX: Float, offsetY: Float, gridSize: Float, countScale: Int, text: String){
    if (("<" in text) or ("=" in text) or (">" in text)){
        Neravenstvo(offsetX, offsetY, gridSize, countScale, text)
    }
    else {
        Fuction1(offsetX, offsetY, gridSize, countScale, text)
    }
}

@Composable
fun Greeting(modifier: Modifier = Modifier) {
    val lineColor1 = colorResource(id = R.color.light_gray)   // ЗАРАНЕЕ ОПРЕДЕЛЯЕМ ЦВЕТА ДЛЯ Canvas
    val lineColor2 = colorResource(id = R.color.gray)         // ЗАРАНЕЕ ОПРЕДЕЛЯЕМ ЦВЕТА ДЛЯ Canvas
    val lineColor3 = colorResource(id = R.color.black)        // ЗАРАНЕЕ ОПРЕДЕЛЯЕМ ЦВЕТА ДЛЯ Canvas

    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }
    var gridSize by remember { mutableFloatStateOf(0f) }
    var scale by remember { mutableStateOf(1f) }
    var countScale by remember { mutableStateOf(0) }

    var textState by remember { mutableStateOf("") }

    if (scale >= 2f) {
        countScale++
        scale /= 2f
        gridSize /= 2f
    }
    if (scale <= 0.5f) {
        countScale--
        scale *= 2f
        gridSize *= 2f
    }
    
    BoxWithConstraints(
        modifier = Modifier
        .pointerInput(Unit) {                               //ДВИЖЕНИЕ СЕТКИ
            detectDragGestures { change, dragAmount ->
                change.consume()
                offsetX += dragAmount.x
                offsetY += dragAmount.y
            }
        }
        .pointerInput(Unit) {                                //ЗУМ СЕТКИ
            detectTransformGestures { centroid, pan, zoom, rotation ->
                offsetX += pan.x
                offsetY += pan.y
                gridSize *= zoom
                scale *= zoom
            }
        }
    ) {
        //ЦЕНТРИРУЕМ СЕТКУ 1 РАЗ
        if (offsetX == 0f && offsetY == 0f) {
            offsetX = constraints.maxWidth.toFloat() / 2f
            offsetY = constraints.maxHeight.toFloat() / 4f
            gridSize = constraints.maxWidth.toFloat() / 20f
        }

        //========================СЕТКА==================================================
        Canvas(modifier = Modifier.fillMaxSize()) {
            val lineWidth = 2f

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
                    strokeWidth = lineWidth
                )
                minorY += gridSize
            }
            while (minorX < width) {                      //ВЕРТИКАЛЬНЫЕ МИНОРНЫЕ ЛИНИИ
                drawLine(
                    color = lineColor1,
                    start = Offset(minorX, 0f),
                    end = Offset(minorX, height),
                    strokeWidth = lineWidth
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
                        strokeWidth = lineWidth
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
                        strokeWidth = lineWidth
                    )
                }
                majorX += gridSize
            }

            //=======================Отрисовка графика=======================================
            CheckFunc(offsetX, offsetY, gridSize, countScale, textState)

            //----------------Ox и Oy--------------
            if (offsetY < height) {                     //Ось X
                drawLine(
                    color = lineColor3,
                    start = Offset(0f, offsetY),
                    end = Offset(width, offsetY),
                    strokeWidth = lineWidth * 1.5f
                )
            }
            if (offsetX < width) {                      //Ось Y
                drawLine(
                    color = lineColor3,
                    start = Offset(offsetX, 0f),
                    end = Offset(offsetX, height),
                    strokeWidth = lineWidth * 1.5f
                )
            }
        }

        //========================ИНТЕРФЕЙС ВВОДА ФУНКЦИИ=================================
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = maxHeight / 2)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(75.dp)
                    .background(Color.White)
                    .border(2.dp, lineColor3)
                    .padding(8.dp)
            ) {
                LazyColumn {
                    item {
                        OutlinedTextField(
                            value = textState,
                            onValueChange = { textState = it },
                            modifier = Modifier.fillMaxSize(),
                            singleLine = true,
                            maxLines = 1,
                            textStyle = androidx.compose.ui.text.TextStyle(fontSize = 18.sp)
                        )
                    }
                    item {
                        OutlinedTextField(
                            value = textState,
                            onValueChange = { textState = it },
                            modifier = Modifier.fillMaxSize(),
                            singleLine = true,
                            maxLines = 1,
                            textStyle = androidx.compose.ui.text.TextStyle(fontSize = 18.sp)
                        )
                    }
                }
            }
            Box(
                Modifier
                    .fillMaxSize()
                    .background(Color.White)
                    .border(2.dp, lineColor3)
            )
        }

        //========================ОТЛАДКА=================================
        Column(modifier = Modifier.padding(top = 30.dp, start = 10.dp)) {
            Text(
                text = "$scale",
                modifier = Modifier
            )
            Text(
                text = "$gridSize",
                modifier = Modifier
            )
            Text(
                text = "$countScale",
                modifier = Modifier
            )
        }
    }
}

@Preview(showSystemUi = true, showBackground = true,
    device = "spec:width=720px,height=1680px,dpi=440"
)
@Composable
fun Preview(){
    Greeting()
}