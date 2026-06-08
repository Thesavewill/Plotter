package com.example.plotter.ui.panels

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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

/** Стиль кнопки (фон + цвет текста) */
private data class KeyStyle(
    val bg: androidx.compose.ui.graphics.Color,
    val text: androidx.compose.ui.graphics.Color
)

/**
 * Математическая клавиатура: цифры, функции, операторы.
 */
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

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(gap)
            ) {
                // === ЛЕВАЯ ЧАСТЬ: ФУНКЦИИ ===
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .background(AppColors.KeyboardColumnBg, RoundedCornerShape(12.dp))
                        .padding(4.dp),
                    verticalArrangement = Arrangement.spacedBy(gap)
                ) {
                    val trigKeys = listOf(
                        "sin()", "cos()", "tg()", "ctg()", "sh()", "ch()", "th()"
                    )
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(gap),
                        contentPadding = PaddingValues(vertical = 2.dp)
                    ) {
                        items(trigKeys) { key ->
                            KeyButton(
                                text = key,
                                onClick = {
                                    onIntent(PlotterContract.Intent.InsertSymbol(key))
                                },
                                width = buttonWidth,
                                height = buttonHeight,
                                containerColor = AppColors.KeyboardFunctionBg,
                                contentColor = AppColors.KeyboardFunctionText,
                                fontSize = 9.sp
                            )
                        }
                    }
                }

                // === ЦЕНТРАЛЬНАЯ ЧАСТЬ: ЦИФРЫ ===
                Column(
                    modifier = Modifier.weight(3f),
                    verticalArrangement = Arrangement.spacedBy(gap)
                ) {
                    val numberRows = listOf(
                        listOf("7", "8", "9"),
                        listOf("4", "5", "6"),
                        listOf("1", "2", "3")
                    )
                    numberRows.forEach { rowKeys ->
                        Row(
                            modifier = Modifier.height(buttonHeight),
                            horizontalArrangement = Arrangement.spacedBy(gap)
                        ) {
                            rowKeys.forEach { key ->
                                KeyButton(
                                    text = key,
                                    onClick = {
                                        onIntent(PlotterContract.Intent.InsertSymbol(key))
                                    },
                                    width = buttonWidth,
                                    height = buttonHeight,
                                    containerColor = AppColors.KeyboardDigitBg,
                                    contentColor = AppColors.KeyboardDigitText
                                )
                            }
                        }
                    }
                    // Последняя строка: скобки, 0 и x
                    Row(
                        modifier = Modifier.height(buttonHeight),
                        horizontalArrangement = Arrangement.spacedBy(gap)
                    ) {
                        // Комбинированная кнопка ( / )
                        CombinedKeyButtonVertical(
                            leftText = "(",
                            rightText = ")",
                            leftOnClick = {
                                onIntent(PlotterContract.Intent.InsertSymbol("("))
                            },
                            rightOnClick = {
                                onIntent(PlotterContract.Intent.InsertSymbol(")"))
                            },
                            width = buttonWidth,
                            height = buttonHeight,
                            containerColor = AppColors.KeyboardFunctionBg,
                            contentColor = AppColors.KeyboardFunctionText,
                            fontSize = 16.sp
                        )
                        // Кнопка 0
                        KeyButton(
                            text = "0",
                            onClick = {
                                onIntent(PlotterContract.Intent.InsertSymbol("0"))
                            },
                            width = buttonWidth,
                            height = buttonHeight,
                            containerColor = AppColors.KeyboardDigitBg,
                            contentColor = AppColors.KeyboardDigitText
                        )
                        // Кнопка x
                        KeyButton(
                            text = "x",
                            onClick = {
                                onIntent(PlotterContract.Intent.InsertSymbol("x"))
                            },
                            width = buttonWidth,
                            height = buttonHeight,
                            containerColor = AppColors.KeyboardVariableBg,
                            contentColor = AppColors.KeyboardVariableText,
                            fontSize = 14.sp
                        )
                    }
                }

                // === ПРАВАЯ ЧАСТЬ: ОПЕРАЦИИ ===
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(gap)
                ) {
                    // Кнопка степени
                    KeyButton(
                        text = "^",
                        onClick = { onIntent(PlotterContract.Intent.InsertSymbol("^")) },
                        width = buttonWidth,
                        height = buttonHeight,
                        containerColor = AppColors.KeyboardOperatorBg,
                        contentColor = AppColors.KeyboardOperatorText,
                        fontSize = 14.sp
                    )
                    // Комбинированная кнопка * и /
                    CombinedKeyButtonVertical(
                        leftText = "*",
                        rightText = "/",
                        leftOnClick = {
                            onIntent(PlotterContract.Intent.InsertSymbol("*"))
                        },
                        rightOnClick = {
                            onIntent(PlotterContract.Intent.InsertSymbol("/"))
                        },
                        width = buttonWidth,
                        height = buttonHeight,
                        containerColor = AppColors.KeyboardOperatorBg,
                        contentColor = AppColors.KeyboardOperatorText,
                        verticalPadding = 20.dp
                    )
                    // Комбинированная кнопка + и -
                    CombinedKeyButtonVertical(
                        leftText = "+",
                        rightText = "-",
                        leftOnClick = {
                            onIntent(PlotterContract.Intent.InsertSymbol("+"))
                        },
                        rightOnClick = {
                            onIntent(PlotterContract.Intent.InsertSymbol("-"))
                        },
                        width = buttonWidth,
                        height = buttonHeight,
                        containerColor = AppColors.KeyboardOperatorBg,
                        contentColor = AppColors.KeyboardOperatorText,
                        verticalPadding = 20.dp
                    )
                    // Кнопка DEL
                    KeyButton(
                        text = "DEL",
                        onClick = { onIntent(PlotterContract.Intent.DeleteSymbol) },
                        width = buttonWidth,
                        height = buttonHeight,
                        containerColor = AppColors.KeyboardDeleteBg,
                        contentColor = AppColors.KeyboardDeleteText,
                        fontSize = 10.sp
                    )
                }
            }
        }
    }
}

/** Обычная кнопка клавиатуры */
@Composable
private fun KeyButton(
    text: String,
    onClick: () -> Unit,
    width: Dp,
    height: Dp,
    containerColor: androidx.compose.ui.graphics.Color,
    contentColor: androidx.compose.ui.graphics.Color,
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
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                style = TextStyle(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = fontSize,
                    textAlign = TextAlign.Center,
                    color = contentColor
                ),
                maxLines = 1,
                softWrap = false
            )
        }
    }
}

/** Комбинированная кнопка с вертикальным разделением */
@Composable
private fun CombinedKeyButtonVertical(
    leftText: String,
    rightText: String,
    leftOnClick: () -> Unit,
    rightOnClick: () -> Unit,
    width: Dp,
    height: Dp,
    containerColor: androidx.compose.ui.graphics.Color,
    contentColor: androidx.compose.ui.graphics.Color,
    fontSize: androidx.compose.ui.unit.TextUnit = 12.sp,
    leftFontSize: androidx.compose.ui.unit.TextUnit = fontSize,
    rightFontSize: androidx.compose.ui.unit.TextUnit = fontSize,
    verticalPadding: Dp = 18.dp,
    horizontalPadding: Dp = 2.dp
) {
    Surface(
        modifier = Modifier.width(width).height(height).clip(RoundedCornerShape(8.dp)),
        shape = RoundedCornerShape(8.dp),
        color = containerColor,
        tonalElevation = 2.dp
    ) {
        Row(modifier = Modifier.fillMaxSize()) {
            // Левая половина
            Box(
                modifier = Modifier
                    .weight(2f)
                    .clickable(onClick = leftOnClick),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = leftText,
                    style = TextStyle(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = leftFontSize,
                        textAlign = TextAlign.Center,
                        color = contentColor
                    ),
                    maxLines = 1,
                    softWrap = false,
                    modifier = Modifier.padding(
                        horizontal = horizontalPadding,
                        vertical = verticalPadding
                    )
                )
            }
            // Вертикальная разделительная линия
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(1.dp)
                    .background(contentColor.copy(alpha = 0.3f))
            )
            // Правая половина
            Box(
                modifier = Modifier
                    .weight(2f)
                    .clickable(onClick = rightOnClick),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = rightText,
                    style = TextStyle(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = rightFontSize,
                        textAlign = TextAlign.Center,
                        color = contentColor
                    ),
                    maxLines = 1,
                    softWrap = false,
                    modifier = Modifier.padding(
                        horizontal = horizontalPadding,
                        vertical = verticalPadding
                    )
                )
            }
        }
    }
}