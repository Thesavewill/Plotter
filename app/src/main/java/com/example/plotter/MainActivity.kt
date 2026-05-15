package com.example.plotter

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import com.example.plotter.domain.model.CanvasTransform
import com.example.plotter.domain.model.PlotFunction
import com.example.plotter.ui.PlotterContract
import com.example.plotter.ui.PlotterScreen
import com.example.plotter.ui.theme.PlotterTheme
import com.example.plotter.viewmodel.PlotterViewModel
import androidx.compose.ui.text.TextRange

class MainActivity : ComponentActivity() {

    private val viewModel: PlotterViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PlotterTheme {
                val state by viewModel.state.collectAsState()
                PlotterScreen(
                    state = state,
                    onIntent = viewModel::handleIntent,
                    modifier = Modifier.Companion
                )
            }
        }
    }
}

    // Диалог выбора: Камера или Галерея
    private fun showImageSourceDialog(context: android.content.Context) {
        val options = arrayOf("📷 Сделать фото", "🖼 Выбрать из галереи")
        AlertDialog.Builder(context)
            .setTitle("Откуда добавить уравнение?")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> requestCameraPermission() // Камера
                    1 -> requestStoragePermission() // Галерея
                }
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    // Проверка и запрос прав для Камеры
    private fun requestCameraPermission() {
        when {
            ContextCompat.checkSelfPermission(
                this, Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> {
                cameraLauncher.launch(null)
            }
            else -> {
                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }

    // Проверка и запрос прав для Галереи
    private fun requestStoragePermission() {
        val permission = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_IMAGES
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }

                // === Изменить цвет ===
                is PlotterContract.Intent.ChangeColor -> {
                    previewState.value = previewState.value.copy(
                        functions = previewState.value.functions.map { func ->
                            if (func.id == intent.id) func.copy(color = intent.color) else func
                        }
                    )
                }

                // === Зум/пан ===
                is PlotterContract.Intent.Pan,
                is PlotterContract.Intent.Zoom,
                is PlotterContract.Intent.CanvasInitialized -> {
                }
            }
        }

        // ========== Рендер экрана с интерактивным состоянием ==========
        PlotterScreen(
            state = previewState.value,
            onIntent = handleIntent,
            modifier = Modifier.fillMaxSize()
        )
    }
}