package com.example.plotter.ui

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.TextFieldValue
import com.example.plotter.domain.model.CanvasTransform
import com.example.plotter.domain.model.PlotFunction

object PlotterContract {

    data class State(
        val canvas: CanvasTransform = CanvasTransform(),
        val functions: List<PlotFunction> = listOf(PlotFunction()),
        val selectedFunctionId: String? = null,
        val colorPicker: ColorPickerState = ColorPickerState(),
        val error: String? = null
    )

    data class ColorPickerState(
        val isVisible: Boolean = false,
        val targetFunctionId: String? = null
    )

    sealed interface Intent {
        data class Pan(val dx: Float, val dy: Float) : Intent
        data class Zoom(val factor: Float, val centerX: Float, val centerY: Float) : Intent
        data class CanvasInitialized(val width: Float, val height: Float) : Intent
        data class AddFunction(val position: Int = -1) : Intent
        data class RemoveFunction(val id: String) : Intent
        data class UpdateExpression(val id: String, val value: TextFieldValue) : Intent
        data class SelectFunction(val id: String?) : Intent
        data class ChangeColor(val id: String, val color: Color) : Intent
        data class OpenColorPicker(val functionId: String) : Intent
        data object CloseColorPicker : Intent
        data class InsertSymbol(val symbol: String) : Intent
        data object DeleteSymbol : Intent
    }
}