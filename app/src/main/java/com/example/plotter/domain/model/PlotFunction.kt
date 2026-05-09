package com.example.plotter.domain.model

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.TextFieldValue
import java.util.UUID

data class PlotFunction(
    val id: String = UUID.randomUUID().toString(),
    val expression: TextFieldValue = TextFieldValue(""),
    val color: Color = Color.Blue
)