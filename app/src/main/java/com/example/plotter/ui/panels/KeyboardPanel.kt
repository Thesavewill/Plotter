package com.example.plotter.ui.panels

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.plotter.ui.PlotterContract

@Composable
fun KeyboardPanel(
    onIntent: (PlotterContract.Intent) -> Unit,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .background(Color(0xFFF5F5F5))
            .border(2.dp, Color.Black)) {
        val gap = 4.dp
        val buttonSize = (maxWidth - gap*4) / 5f

        Row(modifier = Modifier.fillMaxWidth().padding(gap/2)) {

            // === ЛЕВАЯ ЧАСТЬ: ЦИФРЫ (Вес 3) ===
            Column(
                modifier = Modifier
                    .weight(3f)
                    .padding(gap / 2),
                verticalArrangement = Arrangement.spacedBy(gap / 2)
            ) {
                val numberRows = listOf(
                    listOf("7", "8", "9"),
                    listOf("4", "5", "6"),
                    listOf("1", "2", "3"),
                    listOf("-", "0", "+")
                )

                numberRows.forEach { rowKeys ->
                    Row(
                        modifier = Modifier.height(buttonSize),
                        horizontalArrangement = Arrangement.spacedBy(gap / 2)
                    ) {
                        rowKeys.forEach { key ->
                            KeyButton(
                                text = key,
                                onClick = { onIntent(PlotterContract.Intent.InsertSymbol(key)) },
                                size = buttonSize,
                                containerColor = when (key) {
                                    "+", "-" -> Color(0xFFFFF9C4)
                                    else -> Color.White
                                },
                                contentColor = when (key) {
                                    "+", "-" -> Color(0xFF5D4037)
                                    else -> Color.Black
                                }
                            )
                        }
                    }
                }
            }

            // === СРЕДНЯЯ ЧАСТЬ: ФУНКЦИИ (Вес 1, Скролл) ===
            Column(
                modifier = Modifier
                    .weight(1f)
                    .background(Color.White.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                    .padding(gap/2)
            ) {
                val trigKeys = listOf("sin()", "cos()", "tg()", "ctg()", "sh()", "ch()", "th()")

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(gap / 2)
                ) {
                    items(trigKeys) { key ->
                        KeyButton(
                            text = key,
                            onClick = { onIntent(PlotterContract.Intent.InsertSymbol(key)) },
                            size = buttonSize,
                            containerColor = Color(0xFFE3F2FD),
                            contentColor = Color(0xFF1976D2),
                            fontSize = 12.sp
                        )
                    }
                }
            }

            // === ПРАВАЯ ЧАСТЬ: ОПЕРАЦИИ (Вес 1) ===
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(gap/2),
                verticalArrangement = Arrangement.spacedBy(gap / 2)
            ) {
                val operatorKeys = listOf(
                    Triple("*", Color(0xFFFFF9C4), Color(0xFF5D4037)),
                    Triple("/", Color(0xFFFFF9C4), Color(0xFF5D4037)),
                    Triple("x", Color(0xFF2196F3), Color.White),
                    Triple("DEL", Color(0xFFE57373), Color.White)
                )

                operatorKeys.forEach { (key, bg, fg) ->
                    KeyButton(
                        text = key,
                        onClick = {
                            if (key == "DEL") onIntent(PlotterContract.Intent.DeleteSymbol)
                            else onIntent(PlotterContract.Intent.InsertSymbol(key))
                        },
                        size = buttonSize,
                        containerColor = bg,
                        contentColor = fg,
                        fontSize = if (key == "DEL") 14.sp else 16.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun KeyButton(
    text: String,
    onClick: () -> Unit,
    size: Dp,
    containerColor: Color,
    contentColor: Color,
    fontSize: androidx.compose.ui.unit.TextUnit = 14.sp
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .size(size)
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(8.dp),
                ambientColor = Color.Black.copy(alpha = 0.2f),
                spotColor = Color.Black.copy(alpha = 0.4f)
            ),
        contentPadding = PaddingValues(0.dp),
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor
        ),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 0.dp,
            pressedElevation = 0.dp
        )
    ) {
        Text(
            text = text,
            style = TextStyle(
                fontWeight = FontWeight.Bold,
                fontSize = fontSize,
                textAlign = TextAlign.Center
            ),
            maxLines = 1,
            softWrap = false
        )
    }
}