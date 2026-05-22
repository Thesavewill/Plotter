package com.example.plotter.ui

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.TextFieldValue
import com.example.plotter.domain.model.CanvasTransform
import com.example.plotter.domain.model.PlotFunction
import com.example.plotter.domain.model.SavedGraph

object PlotterContract {

    data class State(
        val canvas: CanvasTransform = CanvasTransform(),
        val functions: List<PlotFunction> = listOf(PlotFunction()),
        val selectedFunctionId: String? = null,
        val colorPicker: ColorPickerState = ColorPickerState(),
        val error: String? = null,
        val isProcessingImage: Boolean = false,
        val savedGraphs: List<SavedGraph> = emptyList(),
        val isLoadingGraphs: Boolean = false,
        val currentUserEmail: String? = null,
        val showSaveDialog: Boolean = false,
        val showGraphsList: Boolean = false,
        val graphName: String = ""
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
        data object RequestImageCapture : Intent
        data class ProcessImageUri(val uri: String) : Intent
        data class ProcessImageBitmap(val bitmap: android.graphics.Bitmap) : Intent
        data object OpenImageSourceDialog : Intent
        data object SaveGraph : Intent
        data class LoadGraph(val graphId: String) : Intent
        data object ShowSavedGraphs : Intent
        data class DeleteGraph(val graphId: String) : Intent
        data object ToggleSignIn : Intent
        data object SignOut : Intent
        data class ProcessGoogleSignIn(val idToken: String) : Intent
        data class UpdateGraphName(val name: String) : Intent
        data object CloseSaveDialog : Intent
        data object CloseGraphsList : Intent
    }
}