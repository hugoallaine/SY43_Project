// File: MainActivity.kt
package com.example.sy43_bookshelft

import android.Manifest
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Size
import android.view.SurfaceHolder
import android.view.SurfaceView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.camera.core.AspectRatio
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.core.SurfaceRequest
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.app.ActivityCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import com.example.sy43_bookshelft.ui.theme.SY43_bookshelftTheme
import com.google.common.util.concurrent.ListenableFuture
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.io.File
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class MainActivity : ComponentActivity() {
    private lateinit var cameraExecutor: ExecutorService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        cameraExecutor = Executors.newSingleThreadExecutor()

        ActivityCompat.requestPermissions(
            this, arrayOf(Manifest.permission.CAMERA),
            REQUEST_CODE_PERMISSIONS
        )

        setContent {
            SY43_bookshelftTheme {
                val viewModel: BookViewModel by viewModels()
                MainScreen(viewModel, cameraExecutor)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }

    companion object {
        private const val REQUEST_CODE_PERMISSIONS = 10
        const val DESIRED_WIDTH = 4000
        const val DESIRED_HEIGHT = 900
        const val PREVIEW_ASPECT_RATIO = AspectRatio.RATIO_16_9
    }
}

// ViewModel to manage the app's state
class BookViewModel : ViewModel() {
    var scannedText by mutableStateOf("")
    var errorMessage by mutableStateOf("")
}

@Composable
fun MainScreen(viewModel: BookViewModel, cameraExecutor: ExecutorService) {
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    Surface(modifier = Modifier.fillMaxSize()) {
        if (isLandscape) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxSize().padding(16.dp)
            ) {
                val context = LocalContext.current
                val lifecycleOwner = LocalLifecycleOwner.current
                val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }

                var imageCapture by remember { mutableStateOf<ImageCapture?>(null) }

                Box(modifier = Modifier.fillMaxSize()) {
                    Column(modifier = Modifier.align(Alignment.Center)) {
                        CameraPreview(
                            cameraProviderFuture = cameraProviderFuture,
                            lifecycleOwner = lifecycleOwner,
                            aspectRatio = MainActivity.PREVIEW_ASPECT_RATIO,
                            onImageCaptureReady = { imageCapture = it },
                            onError = { message -> viewModel.errorMessage = message }
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Text("Scanned Text: ${viewModel.scannedText}")
                    }

                    // Add black bars to the top and bottom with padding for the offset and borders
                    Box(modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .offset(y = (-40).dp)  // Offset from the top
                        .background(Color.Black)
                        .border(2.dp, Color.White)
                        .align(Alignment.TopCenter),
                        contentAlignment = Alignment.Center
                    ) {
                        if (viewModel.errorMessage.isNotEmpty()) {
                            Text(text = "Error: ${viewModel.errorMessage}", color = Color.Red)
                        } else if (viewModel.scannedText.isNotEmpty()) {
                            Row(
                                modifier = Modifier
                                    .padding(bottom = 0.dp),  // Adjust padding to move text lower
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                viewModel.scannedText.split("\n").forEach { line ->
                                    Text(
                                        text = line,
                                        color = Color.Green,
                                        modifier = Modifier
                                            .padding(5.dp)
                                            .rotate(270f)
                                    )
                                }
                            }
                        }
                    }


                    Box(modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .offset(y = 40.dp)  // Offset from the bottom
                        .background(Color.Black)
                        .border(2.dp, Color.White)
                        .align(Alignment.BottomCenter),
                        contentAlignment = Alignment.Center // Align button to the center
                    ) {
                        Button(onClick = {
                            val file = File(context.cacheDir, "image.jpg")
                            val outputOptions = ImageCapture.OutputFileOptions.Builder(file).build()
                            imageCapture?.takePicture(
                                outputOptions,
                                cameraExecutor,
                                object : ImageCapture.OnImageSavedCallback {
                                    override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                                        val bitmap = BitmapFactory.decodeFile(file.absolutePath)
                                        // Crop the bitmap to the area between the black bars
                                        val croppedBitmap = Bitmap.createBitmap(
                                            bitmap,
                                            0,
                                            bitmap.height / 3,
                                            bitmap.width,
                                            bitmap.height / 3
                                        )
                                        val image = InputImage.fromBitmap(croppedBitmap, 0)
                                        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
                                        recognizer.process(image)
                                            .addOnSuccessListener { visionText ->
                                                viewModel.scannedText = visionText.text
                                                viewModel.errorMessage = ""
                                                // Display the classifications
                                                CheckOrder(visionText.text)
                                            }
                                            .addOnFailureListener { e ->
                                                viewModel.errorMessage = "Failed to scan text: ${e.message}"
                                                viewModel.scannedText = ""
                                            }
                                    }

                                    override fun onError(exception: ImageCaptureException) {
                                        viewModel.errorMessage = "Image capture failed: ${exception.message}"
                                        viewModel.scannedText = ""
                                    }
                                }
                            )
                        }) {
                            Text("Capture Image (${MainActivity.DESIRED_WIDTH}x${MainActivity.DESIRED_HEIGHT})")
                        }
                    }
                }
            }
        } else {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                Text("Please rotate your device to landscape mode")
            }
        }
    }
}

@Composable
fun CameraPreview(
    cameraProviderFuture: ListenableFuture<ProcessCameraProvider>,
    lifecycleOwner: LifecycleOwner,
    aspectRatio: Int,
    onImageCaptureReady: (ImageCapture) -> Unit,
    onError: (String) -> Unit
) {
    val context = LocalContext.current
    val previewSurfaceView = remember { SurfaceView(context) }

    AndroidView(
        factory = {
            previewSurfaceView.holder.addCallback(object : SurfaceHolder.Callback {
                override fun surfaceCreated(holder: SurfaceHolder) {
                    cameraProviderFuture.addListener({
                        val cameraProvider = cameraProviderFuture.get()
                        val preview = Preview.Builder()
                            .setTargetAspectRatio(aspectRatio)
                            .build()
                        preview.setSurfaceProvider { request ->
                            val surface = holder.surface
                            request.provideSurface(surface, context.mainExecutor) { result ->
                                if (result.resultCode != SurfaceRequest.Result.RESULT_SURFACE_USED_SUCCESSFULLY) {
                                    onError("Surface was not used successfully: ${result.resultCode}")
                                }
                            }
                        }
                        val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                        val imageCapture = ImageCapture.Builder()
                            .setTargetResolution(Size(MainActivity.DESIRED_WIDTH, MainActivity.DESIRED_HEIGHT))
                            .build()

                        try {
                            cameraProvider.unbindAll()
                            cameraProvider.bindToLifecycle(
                                lifecycleOwner,
                                cameraSelector,
                                preview,
                                imageCapture
                            )
                            onImageCaptureReady(imageCapture)
                        } catch (exc: Exception) {
                            onError("Failed to bind camera: ${exc.message}")
                        }
                    }, context.mainExecutor)
                }

                override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
                    // Handle surface changes if necessary
                }

                override fun surfaceDestroyed(holder: SurfaceHolder) {
                    // Handle surface destruction if necessary
                }
            })
            previewSurfaceView
        },
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(16f / 9f)  // Set aspect ratio to 16:9
    )
}

fun CheckOrder(text: String) {
    // Simulate checking order of book spines based on Library of Congress Classification
    val callNumbers = text.split("\n").map { it.trim() }.filter { it.isNotEmpty() }
    if (callNumbers.isEmpty()) return

    // Here you can implement the logic to verify the order of the call numbers.
    // For now, just return the call numbers as they are.
}
