package com.example.plotter

import android.Manifest
import android.app.AlertDialog
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.ContextCompat
import com.example.plotter.domain.model.CanvasTransform
import com.example.plotter.domain.model.PlotFunction
import com.example.plotter.ui.PlotterContract
import com.example.plotter.ui.PlotterScreen
import com.example.plotter.ui.theme.PlotterTheme
import com.example.plotter.viewmodel.ImageRecognitionEvent
import com.example.plotter.viewmodel.PlotterViewModel
import kotlinx.coroutines.flow.collectLatest

class MainActivity : ComponentActivity() {
    private val viewModel: PlotterViewModel by viewModels {
        PlotterViewModel.Companion.Factory(
            applicationContext
        )
    }

    //  Лаунчер камеры
    private val cameraLauncher = registerForActivityResult(
        ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        bitmap?.let {
            viewModel.handleIntent(PlotterContract.Intent.ProcessImageBitmap(it))
        }
    }

    // Лаунчер галереи
    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            viewModel.handleIntent(PlotterContract.Intent.ProcessImageUri(it.toString()))
        }
    }

    // Лаунчер прав на камеру
    private val cameraPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            cameraLauncher.launch(null)
        } else {
            Toast.makeText(this, "Нет доступа к камере", Toast.LENGTH_SHORT).show()
        }
    }

    // Лаунчер прав на галерею
    private val storagePermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            imagePickerLauncher.launch("image/*")
        } else {
            Toast.makeText(this, "Нет доступа к фото", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PlotterTheme {
                val state by viewModel.state.collectAsState()
                val context = LocalContext.current

                LaunchedEffect(Unit) {
                    viewModel.imageRecognitionEvents.collectLatest { event ->
                        when (event) {
                            // Показ выбора при нажатии кнопки "С фото"
                            ImageRecognitionEvent.RequestPermission -> {
                                showImageSourceDialog(context)
                            }
                            is ImageRecognitionEvent.ShowError -> {
                                Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                            }
                            ImageRecognitionEvent.ShowSuccess -> {
                                Toast.makeText(context, "Уравнение добавлено", Toast.LENGTH_SHORT).show()
                            }
                            ImageRecognitionEvent.DismissLoading -> {}
                        }
                    }
                }

                PlotterScreen(
                    state = state,
                    onIntent = viewModel::handleIntent,
                    onImageCaptureRequested = {
                        viewModel.handleIntent(PlotterContract.Intent.OpenImageSourceDialog)
                    },
                    modifier = Modifier
                )
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

        when {
            ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED -> {
                imagePickerLauncher.launch("image/*")
            }
            else -> {
                storagePermissionLauncher.launch(permission)
            }
        }
    }
}