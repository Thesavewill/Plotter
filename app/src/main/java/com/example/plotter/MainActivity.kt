package com.example.plotter

import android.graphics.Paint
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
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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

//Класс для добавления графиков
class FunctionItem(
    var text: MutableState<TextFieldValue> = mutableStateOf(TextFieldValue("")),
    var color: MutableState<Color> = mutableStateOf(Color.Blue)
)

// Тестовая функция
val mySin = object : Function("mySin", 1) {
    override fun apply(vararg args: Double): Double {
        val x = args[0]
        return x - x.pow(3.0) / 6 + x.pow(5.0) / 120
    }
}
// Список всех кастомных функций exp4j
val customFunctions = listOf(mySin)

fun DrawScope.Fuction1(offsetX: Float, offsetY: Float, gridSize: Float, countScale: Int, expressionString: String, color: Color) {
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
        val x = (px - offsetX) / unitSize / 4

        val y = try {
            expression.setVariable("x", x.toDouble()).evaluate().toFloat()
        } catch (e: Exception) {
            Float.NaN
        }

        val py = offsetY - y * unitSize * 4

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
        color = color,
        style = Stroke(width = 4f)
    )
}

//fun DrawScope.Neravenstvo(offsetX: Float, offsetY: Float, gridSize: Float, countScale: Int, text: String, color: Color)

// Определения вида функции
fun DrawScope.CheckFunc(offsetX: Float, offsetY: Float, gridSize: Float, countScale: Int, item: FunctionItem){
    if (("<" in item.text.value.text) || ("=" in item.text.value.text) || (">" in item.text.value.text)){
        //Neravenstvo(offsetX, offsetY, gridSize, countScale, text)
    } else {
        Fuction1(offsetX, offsetY, gridSize, countScale, item.text.value.text, item.color.value)
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
    var scale by remember { mutableFloatStateOf(1f) }
    var countScale by remember { mutableIntStateOf(0) }

    val functionsList = remember { mutableStateListOf(FunctionItem()) }

    var showColorPicker by remember { mutableStateOf(false) }
    var selectedItemForColor by remember { mutableStateOf<FunctionItem?>(null) }

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
            .pointerInput(Unit) {
                detectTransformGestures { centroid, pan, zoom, _ ->
                    offsetX += pan.x
                    offsetY += pan.y
                    if (zoom != 1f) {
                        offsetX -= (centroid.x - offsetX) * (zoom - 1f)
                        offsetY -= (centroid.y - offsetY) * (zoom - 1f)
                    }
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


            val textPaint = Paint().apply {
                color = android.graphics.Color.GRAY
                textSize = 30f
                textAlign = Paint.Align.LEFT
            }

            // Коэффициент масштаба для значений
            val unitFactor = 2f.pow(countScale.toFloat())

            //----------------Мажорные линии сетки--------------
            while (majorY < height) {                       //ГОРИЗОНТАЛЬНЫЕ МАЖОРНЫЕ ЛИНИИ
                val lineIndex = ((majorY - offsetY) / gridSize).roundToInt()
                if (lineIndex % 4 == 0) {
                    drawLine(
                        color = lineColor2,
                        start = Offset(0f, majorY),
                        end = Offset(width, majorY),
                        strokeWidth = lineWidth
                    )

                    //Числовая разметка
                    val yValue = -(lineIndex.toFloat() / unitFactor) / 4
                    val textToShow = if (countScale <= 0) {
                        yValue.toInt()
                    } else {
                        yValue.toFloat()
                    }
                    if (yValue != 0f) {
                        drawContext.canvas.nativeCanvas.drawText(
                            textToShow.toString(),
                            offsetX + 10f,
                            majorY - 5f,
                            textPaint
                        )
                    }
                }
                majorY += gridSize
            }
            while (majorX < width) {                        //ВЕРТИКАЛЬНЫЕ МАЖОРНЫЕ ЛИНИИ
                val lineIndex = ((majorX - offsetX) / gridSize).roundToInt()
                if (lineIndex % 4 == 0) {
                    drawLine(
                        color = lineColor2,
                        start = Offset(majorX, 0f),
                        end = Offset(majorX, height),
                        strokeWidth = lineWidth
                    )

                    //Числовая разметка
                    val xValue = lineIndex.toFloat() / unitFactor / 4
                    val textToShow = if (countScale <= 0 || xValue.toDouble() == 0.0) {
                        xValue.toInt()
                    } else {
                        xValue.toFloat()
                    }
                    drawContext.canvas.nativeCanvas.drawText(
                        textToShow.toString(),
                        majorX + 10f,
                        offsetY - 10f,
                        textPaint
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

            //----------------------------Отрисовка графика-----------------------------------
            functionsList.forEach { item ->
                CheckFunc(offsetX, offsetY, gridSize, countScale, item)
            }
        }

        //========================ИНТЕРФЕЙС ВВОДА ФУНКЦИИ=================================
        Column(modifier = Modifier.fillMaxSize().padding(top = maxHeight / 2)) {
            //-------------------------------Верхняя панель---------------------------------------
            Box(modifier = Modifier.fillMaxWidth().height(115.dp).background(Color.White).border(2.dp, lineColor3).padding(8.dp)) {
                LazyColumn {
                    items(functionsList.size) { index ->
                        val item = functionsList[index]
                        Row(Modifier.padding(bottom = 8.dp)){
                            Column {
                                Button(                                         //Кнопка удаления
                                    onClick = { functionsList.removeAt(index) },
                                    modifier = Modifier
                                        .padding(end = 5.dp)
                                        .size(20.dp),
                                    shape = RoundedCornerShape(3.dp),
                                    contentPadding = PaddingValues(0.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Delete",
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Button(                                           //Кнопка выбора цвета
                                    onClick = {
                                        selectedItemForColor = item
                                        showColorPicker = true
                                    },
                                    modifier = Modifier
                                        .padding(top = 5.dp, end = 8.dp)
                                        .size(20.dp),
                                    shape = RoundedCornerShape(3.dp),
                                    contentPadding = PaddingValues(0.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = item.color.value)
                                ) {}
                            }
                            BasicTextField(
                                value = item.text.value,
                                onValueChange = {
                                    item.text.value = it
                                    selectedItem = item },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(45.dp)
                                    .background(Color.White.copy(alpha = 0.7f), RoundedCornerShape(4.dp))
                                    .border(1.dp, Color.Gray, RoundedCornerShape(4.dp))
                                    .onFocusChanged { focusState ->
                                        if (focusState.isFocused) {
                                            selectedItem = item
                                        }
                                    },
                                singleLine = true,
                                decorationBox = { innerTextField ->
                                    Box(
                                        modifier = Modifier.padding(horizontal = 8.dp),
                                        contentAlignment = Alignment.CenterStart
                                    ) {
                                        if (item.text.value.isEmpty()) {
                                            Text("Поле ${index + 1}", color = Color.Gray)
                                        }
                                        innerTextField()
                                    }
                                }
                            )
                        }
                    }
                    item {
                        Button(
                            onClick = {
                                val newItem = FunctionItem()
                                functionsList.add(newItem)
                                selectedItem = newItem },
                                modifier = Modifier.fillMaxWidth().height(42.dp),
                            shape = RoundedCornerShape(5.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                            ),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = "Добавить график",
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }
                }
            }
            //------------------------Нижняя панель--------------------------
            Box(
                Modifier
                    .fillMaxSize()
                    .background(Color.White)
                    .border(2.dp, lineColor3)
            ) {
                Column {
                    // Функция вставки текста
                    val insertText: (String) -> Unit = { symbol ->
                        selectedItem?.let { item ->
                            val value = item.text.value
                            val text = value.text
                            val selection = value.selection
                            val newText = text.replaceRange(selection.min, selection.max, symbol)

                            // Если вставляем sin(), ставим курсор внутрь скобок
                            val offset = if (symbol == "sin()") selection.min + 4 else selection.min + symbol.length

                            item.text.value = TextFieldValue(
                                text = newText,
                                selection = TextRange(offset)
                            )
                        }
                    }

                    // Функция для удаления
                    val deleteText: () -> Unit = {
                        selectedItem?.let { item ->
                            val value = item.text.value
                            val text = value.text
                            val selection = value.selection

                            if (!selection.collapsed) {
                                val newText = text.removeRange(selection.min, selection.max)
                                item.text.value = TextFieldValue(newText, TextRange(selection.min))
                            } else if (selection.min > 0) {
                                val newText = text.removeRange(selection.min - 1, selection.min)
                                item.text.value = TextFieldValue(newText, TextRange(selection.min - 1))
                            }
                        }
                    }

                    val keys = listOf("1", "2", "3", "*", "4", "5", "6", "+", "7", "8", "9", "-", "0", "sin()", "x", "⌫")

                    LazyVerticalGrid(
                        columns = GridCells.Fixed(4),
                        contentPadding = PaddingValues(5.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        items(keys.size) { index ->
                            val key = keys[index]

                            Button(
                                onClick = {
                                    if (key == "⌫") deleteText() else insertText(key)
                                },
                                modifier = Modifier
                                    .padding(4.dp)
                                    .height(32.dp),
                                contentPadding = PaddingValues(0.dp),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = when(key) {
                                        "x" -> Color.Blue
                                        "⌫" -> Color(0xFFE57373)
                                        else -> Color.White
                                    },
                                    contentColor = if (key == "x" || key == "⌫") Color.White else Color.Black
                                ),
                                elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp),
                            ) {
                                Text(
                                    text = key,
                                    style = TextStyle(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        textAlign = TextAlign.Center
                                    ),
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }
                        }
                    }
                }
            }
        }
        /*
        //=======================================ОТЛАДКА=====================================
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
        */

        //===============================ВЫБОР ЦВЕТА ГРАФИКА=================================
        //Список цветов на выбор
        val colorChoices = listOf(
            Color.Red, Color.Blue, Color.Green, Color.Yellow,
            Color.Magenta, Color.Cyan, Color.Black, Color.Gray,
            Color(0xFFFFA500), Color(0xFF800080), Color(0xFF008080), Color(0xFFFFC0CB)
        )

        //Поле выбора цвета
        if (showColorPicker && selectedItemForColor != null) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.BottomCenter
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth().height(200.dp)
                        .background(Color.White, RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                        .border(1.dp, Color.Black, RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                        .padding(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Цвет графика", fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                        Text(
                            "Закрыть",
                            color = Color.Blue,
                            modifier = Modifier.clickable { showColorPicker = false }
                        )
                    }
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(5),
                        modifier = Modifier.height(150.dp)
                    ) {
                        items(colorChoices.size) { index ->
                            Box(
                                modifier = Modifier
                                    .padding(4.dp).aspectRatio(1f)
                                    .background(colorChoices[index], RoundedCornerShape(8.dp))
                                    .clickable { selectedItemForColor?.color?.value = colorChoices[index] }
                            )
                        }
                    }
                }
            }
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