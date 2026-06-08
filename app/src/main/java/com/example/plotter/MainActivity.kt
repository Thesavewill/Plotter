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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.plotter.domain.recognition.HandwritingRecognizer
import com.example.plotter.ui.PlotterContract
import com.example.plotter.ui.PlotterScreen
import com.example.plotter.ui.theme.PlotterTheme
import com.example.plotter.viewmodel.ImageRecognitionEvent
import com.example.plotter.viewmodel.PlotterViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val viewModel: PlotterViewModel by viewModels {
        PlotterViewModel.Companion.Factory(
            applicationContext
        )
    }

    private val cameraLauncher = registerForActivityResult(
        ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        bitmap?.let {
            viewModel.handleIntent(PlotterContract.Intent.ProcessImageBitmap(it))
        }
    }

    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            viewModel.handleIntent(PlotterContract.Intent.ProcessImageUri(it.toString()))
        }
    }

    private val cameraPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            cameraLauncher.launch(null)
        } else {
            Toast.makeText(this, "Нет доступа к камере", Toast.LENGTH_SHORT).show()
        }
    }

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
                    onSignInRequested = { signInWithGoogle() }
                )
            }
        }

        lifecycleScope.launch {
            try {
                if (!HandwritingRecognizer.isModelDownloaded()) {
                    HandwritingRecognizer.downloadModel()
                }
            } catch (e: Exception) {
                android.util.Log.e("Handwriting", "Failed to preload model: ${e.localizedMessage}")
            }
        }
    }

    private fun showImageSourceDialog(context: android.content.Context) {
        val options = arrayOf("📷 Сделать фото", "🖼 Выбрать из галереи")
        AlertDialog.Builder(context)
            .setTitle("Откуда добавить уравнение?")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> requestCameraPermission()
                    1 -> requestStoragePermission()
                }
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

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

    private val googleSignInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)

        if (task.isSuccessful) {
            val account = task.result
            account.idToken?.let { token ->
                viewModel.handleIntent(PlotterContract.Intent.ProcessGoogleSignIn(token))
            } ?: run {
                Toast.makeText(this, "Ошибка: нет токена", Toast.LENGTH_LONG).show()
            }
        } else {
            val e = task.exception
            val message = when (e) {
                is com.google.android.gms.common.api.ApiException -> {
                    when (e.statusCode) {
                        10 -> "Неверный Web Client ID или SHA-1"
                        12500 -> "Пользователь отменил вход"
                        else -> "Ошибка ${e.statusCode}: ${e.localizedMessage}"
                    }
                }
                else -> "Неизвестная ошибка: ${e?.localizedMessage}"
            }
            Toast.makeText(this, message, Toast.LENGTH_LONG).show()
        }
    }

    private fun signInWithGoogle() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("885514888340-6pauo0d34odk6ts9ga79os8fko9vm6ki.apps.googleusercontent.com")
            .requestEmail()
            .build()
        val googleSignInClient = GoogleSignIn.getClient(this, gso)
        googleSignInLauncher.launch(googleSignInClient.signInIntent)
    }
}