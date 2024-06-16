package com.example.sy43_bookshelft

import android.annotation.SuppressLint
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.view.SurfaceHolder
import android.view.SurfaceView
import androidx.camera.core.AspectRatio
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.core.SurfaceRequest
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.toSize
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.LifecycleOwner
import androidx.navigation.NavHostController
import com.google.common.util.concurrent.ListenableFuture
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.io.File
import java.util.concurrent.ExecutorService

@SuppressLint("UnrememberedMutableState")
@Composable
fun ScannerScreen(navController: NavHostController, cameraExecutor: ExecutorService) {
    var scannedText by mutableStateOf("")
    var errorMessage by mutableStateOf("")
    val PREVIEW_ASPECT_RATIO = AspectRatio.RATIO_16_9
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    var boxSize by remember { mutableStateOf(Size.Zero) }
    val screenHeight = configuration.screenHeightDp.dp
    val screenWidth = configuration.screenWidthDp.dp

    Surface(modifier = Modifier.fillMaxSize()) {
        if (isLandscape) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
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
                            aspectRatio = PREVIEW_ASPECT_RATIO,
                            onImageCaptureReady = { imageCapture = it },
                            onError = { message -> errorMessage = message }
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Text("Scanned Text: ${scannedText}")
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                            .offset(y = (-40).dp)  // Offset from the top
                            .background(Color.Black)
                            .border(2.dp, Color.White)
                            .align(Alignment.TopCenter),
                        contentAlignment = Alignment.Center
                    ) {
                        if (errorMessage.isNotEmpty()) {
                            Text(
                                text = "Error: ${errorMessage}",
                                color = Color.Red,
                                fontSize = 10.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.rotate(270f)
                            )
                        } else if (scannedText.isNotEmpty()) {
                            Box(
                                modifier = Modifier
                                    .rotate(270f)
                                    .onGloballyPositioned { layoutCoordinates ->
                                        boxSize = layoutCoordinates.size.toSize()
                                    }
                                    .width(screenHeight)
                                    .height(screenWidth)
                                    .background(Color.White),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = scannedText.split("\n").joinToString("\n"),
                                    color = Color.Green,
                                    fontSize = 10.sp,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }

                    Box(
                        modifier = Modifier
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
                                        val croppedBitmap = Bitmap.createBitmap(
                                            bitmap,
                                            0,
                                            bitmap.height / 3,
                                            bitmap.width,
                                            bitmap.height / 3
                                        )
                                        val image = InputImage.fromBitmap(croppedBitmap, 0)
                                        val recognizer = TextRecognition.getClient(
                                            TextRecognizerOptions.DEFAULT_OPTIONS)
                                        recognizer.process(image)
                                            .addOnSuccessListener { visionText ->
                                                scannedText = visionText.text
                                                errorMessage = ""
                                                CheckOrder(visionText.text)
                                            }
                                            .addOnFailureListener { e ->
                                                errorMessage = "Failed to scan text: ${e.message}"
                                                scannedText = ""
                                            }
                                    }

                                    override fun onError(exception: ImageCaptureException) {
                                        errorMessage = "Image capture failed: ${exception.message}"
                                        scannedText = ""
                                    }
                                }
                            )
                        }) {
                            Text("Capture Image")
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
    val DESIRED_WIDTH = 4000
    val DESIRED_HEIGHT = 900

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
                            .setTargetResolution(android.util.Size(DESIRED_WIDTH, DESIRED_HEIGHT))
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