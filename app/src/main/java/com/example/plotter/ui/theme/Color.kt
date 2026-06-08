package com.example.plotter.ui.theme

import android.inputmethodservice.Keyboard
import androidx.compose.ui.graphics.Color

data class GridColors(
    val minor: Color,       // Мелкие линии сетки
    val major: Color,       // Крупные линии сетки
    val axis: Color,        // Оси X и Y
    val label: Color,       // Цифры на осях
    val canvasLabel: Color  // Буквы "x" и "y"
)

object AppColors {
    // === Базовые ===
    val Transparent = Color.Transparent
    val White = Color(0xFFFFFFFF)
    val Black = Color(0xFF000000)
    val Gray = Color(0xFF9E9E9E)
    val Red = Color(0xFFD32F2F)
    val Green = Color(0xFF388E3C)
    val Blue = Color(0xFF1976D2)
    val Cyan = Color(0xFF00ACC1)
    val Magenta = Color(0xFFAD1457)

    // === Оверлеи ===
    val LoadingDim = Black.copy(alpha = 0.3f)
    val DialogDim = Black.copy(alpha = 0.4f)

    // === Клавиатура ===
    val KeyboardPanelBg = Color(0xFFECE2E2)
    val KeyboardColumnBg = Color(0xFFF1F3F4)
    val KeyboardDigitBg = Color(0xFFFCFDFF)
    val KeyboardDigitText = Color(0xFF2D2D2D)
    val KeyboardOperatorBg = Color(0xFFFCF1A9)
    val KeyboardOperatorText = Color(0xFF5D4037)
    val KeyboardFunctionBg = Color(0xFFCFEAFF)
    val KeyboardFunctionText = Color(0xFF1565C0)
    val KeyboardVariableBg = Color(0xFFC6CEF6)
    val KeyboardVariableText = Color(0xFF1B4E9A)
    val KeyboardDeleteBg = Color(0xFFFAC3CC)
    val KeyboardDeleteText = Color(0xFFC62828)

    // === Графики ===
    val GraphDefaultBlue = Color(0xFF1976D2)
    val GraphDistinctColors = listOf(
        Red, Blue, Green, Magenta, Cyan,
        Color(0xFFFF9800), Color(0xFF9C27B0), Color(0xFF4CAF50)
    )

    // === Палитра выбора цвета ===
    val ColorPickerPalette = listOf(
        Color(0xFFE91E63), Color(0xFF9C27B0), Color(0xFF673AB7), Color(0xFF3F51B5),
        Color(0xFF1976D2), Color(0xFF03A9F4), Color(0xFF00BCD4), Color(0xFF009688),
        Color(0xFF388E3C), Color(0xFF8BC34A), Color(0xFFCDDC39), Color(0xFFFBC02D),
        Color(0xFFFFC107), Color(0xFFFF9800), Color(0xFFFF5722), Color(0xFFD32F2F),
        Color(0xFFEF5350), Color(0xFFEC407A), Color(0xFFAB47BC), Color(0xFF7E57C2),
        Color(0xFF5C6BC0), Color(0xFF42A5F5), Color(0xFF26C6DA), Color(0xFF26A69A),
        Color(0xFF66BB6A), Color(0xFF9CCC65), Color(0xFFD4E157), Color(0xFFFFEE58),
        Color(0xFFFFCA28), Color(0xFFFFA726), Color(0xFFFF7043), Color(0xFF8D6E63),
        Color(0xFF424242), Color(0xFF616161), Color(0xFF9E9E9E), Color(0xFFBDBDBD),
        Color(0xFFE0E0E0), Color(0xFF000000), Color(0xFFFFFFFF)
    )

    // === Цвета сетки для СВЕТЛОЙ темы ===
    val LightGrid = GridColors(
        minor = Color(0xFFDDDDDD),      // Чуть темнее
        major = Color(0xFF999999),      // Серый посередине
        axis = Color(0xFF000000),       // Чёрный
        label = Color(0xFF666666),      // Тёмно-серый для цифр
        canvasLabel = Color(0xFF000000) // Чёрный для x/y
    )

    // === Цвета сетки для ТЁМНОЙ темы ===
    val DarkGrid = GridColors(
        minor = Color(0xFF424242),      // Тёмно-серый
        major = Color(0xFF757575),      // Серый посветлее
        axis = Color(0xFFE0E0E0),       // Светло-серый
        label = Color(0xFFBDBDBD),      // Светло-серый для цифр
        canvasLabel = Color(0xFFE0E0E0) // Светло-серый для x/y
    )

    // === Material Theme ===
    val Purple80 = Color(0xFFD0BCFF)
    val PurpleGrey80 = Color(0xFFCCC2DC)
    val Pink80 = Color(0xFFEFB8C8)
    val Purple40 = Color(0xFF6650a4)
    val PurpleGrey40 = Color(0xFF625b71)
    val Pink40 = Color(0xFF7D5260)
}