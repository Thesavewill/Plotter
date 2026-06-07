package com.example.plotter.ui.panels

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.plotter.ui.PlotterContract
import com.example.plotter.ui.theme.AppColors

private data class KeyStyle(val bg: androidx.compose.ui.graphics.Color, val text: androidx.compose.ui.graphics.Color)

@Composable
fun KeyboardPanel(
    onIntent: (PlotterContract.Intent) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth().heightIn(max = 280.dp),
        colors = CardDefaults.cardColors(containerColor = AppColors.KeyboardPanelBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
            val gap = 6.dp
            val buttonWidth = (maxWidth - gap * 4) / 5f
            val buttonHeight = buttonWidth * 2 / 3

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(gap)) {
                Column(modifier = Modifier.weight(3f), verticalArrangement = Arrangement.spacedBy(gap)) {
                    val numberRows = listOf(listOf("7", "8", "9"), listOf("4", "5", "6"), listOf("1", "2", "3"), listOf("-", "0", "+"))
                    numberRows.forEach { rowKeys ->
                        Row(modifier = Modifier.height(buttonHeight), horizontalArrangement = Arrangement.spacedBy(gap)) {
                            rowKeys.forEach { key ->
                                val isOperator = key in listOf("+", "-", "*", "/")
                                KeyButton(
                                    text = key,
                                    onClick = { onIntent(PlotterContract.Intent.InsertSymbol(key)) },
                                    width = buttonWidth,
                                    height = buttonHeight,
                                    containerColor = if (isOperator) AppColors.KeyboardOperatorBg else AppColors.KeyboardDigitBg,
                                    contentColor = if (isOperator) AppColors.KeyboardOperatorText else AppColors.KeyboardDigitText
                                )
                            }
                        }
                    }
                }

                Column(
                    modifier = Modifier.weight(1f).background(AppColors.KeyboardColumnBg, RoundedCornerShape(12.dp)).padding(4.dp)
                ) {
                    val trigKeys = listOf("sin()", "cos()", "tg()", "ctg()", "sh()", "ch()", "th()")
                    LazyColumn(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(gap), contentPadding = PaddingValues(vertical = 2.dp)) {
                        items(trigKeys) { key ->
                            KeyButton(
                                text = key,
                                onClick = { onIntent(PlotterContract.Intent.InsertSymbol(key)) },
                                width = buttonWidth,
                                height = buttonHeight,
                                containerColor = AppColors.KeyboardFunctionBg,
                                contentColor = AppColors.KeyboardFunctionText,
                                fontSize = 10.sp
                            )
                        }
                    }
                }

                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(gap)) {
                    val operatorKeys = listOf(
                        "*" to KeyStyle(AppColors.KeyboardOperatorBg, AppColors.KeyboardOperatorText),
                        "/" to KeyStyle(AppColors.KeyboardOperatorBg, AppColors.KeyboardOperatorText),
                        "x" to KeyStyle(AppColors.KeyboardVariableBg, AppColors.KeyboardVariableText),
                        "DEL" to KeyStyle(AppColors.KeyboardDeleteBg, AppColors.KeyboardDeleteText)
                    )
                    operatorKeys.forEach { (key, style) ->
                        KeyButton(
                            text = key,
                            onClick = { if (key == "DEL") onIntent(PlotterContract.Intent.DeleteSymbol) else onIntent(PlotterContract.Intent.InsertSymbol(key)) },
                            width = buttonWidth,
                            height = buttonHeight,
                            containerColor = style.bg,
                            contentColor = style.text,
                            fontSize = if (key == "DEL") 11.sp else 14.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun KeyButton(
    text: String, onClick: () -> Unit, width: Dp, height: Dp,
    containerColor: androidx.compose.ui.graphics.Color, contentColor: androidx.compose.ui.graphics.Color,
    fontSize: androidx.compose.ui.unit.TextUnit = 13.sp
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.width(width).height(height).clip(RoundedCornerShape(8.dp)),
        shape = RoundedCornerShape(8.dp),
        color = containerColor,
        contentColor = contentColor,
        tonalElevation = 2.dp
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(text = text, style = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = fontSize, textAlign = TextAlign.Center, color = contentColor), maxLines = 1, softWrap = false)
        }
    }
}