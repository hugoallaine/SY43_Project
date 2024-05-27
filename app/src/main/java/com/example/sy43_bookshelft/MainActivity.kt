package com.example.sy43_bookshelft

import android.Manifest
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicText
import androidx.compose.material3.Button
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModel
import com.example.sy43_bookshelft.ui.theme.SY43_bookshelftTheme
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.io.ByteArrayOutputStream
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext


class MainActivity : ComponentActivity() {

    private val mainViewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), 1)

        setContent {
            SY43_bookshelftTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    if (savedInstanceState != null) {
                        mainViewModel.restoreState(savedInstanceState)
                    }

                    MainScreen(mainViewModel)
                }
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mainViewModel.saveState(outState)
    }
}

@Composable
fun MainScreen(viewModel: MainViewModel) {
    val context = LocalContext.current
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    val cameraProvider = cameraProviderFuture.get()
    val preview = Preview.Builder().build()
    val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

    var imageCapture by remember { mutableStateOf<Bitmap?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        val takePictureLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
            bitmap?.let {
                val croppedBitmap = cropToFrame(it)
                viewModel.capturedImage = croppedBitmap
                viewModel.processImage(croppedBitmap)
            }
        }

        // Camera Preview
        androidx.camera.view.PreviewView(context).also {
            it.scaleType = androidx.camera.view.PreviewView.ScaleType.FILL_CENTER
            preview.setSurfaceProvider(it.surfaceProvider)
            it.scaleType = androidx.camera.view.PreviewView.ScaleType.FILL_CENTER
        }

        cameraProvider.bindToLifecycle(LocalContext.current as ComponentActivity, cameraSelector, preview)

        // UI Overlay
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Button(onClick = { takePictureLauncher.launch(null) }) {
                Text("Take Picture")
            }

            Spacer(modifier = Modifier.height(16.dp))

            viewModel.capturedImage?.let {
                Image(bitmap = it.asImageBitmap(), contentDescription = null, modifier = Modifier.size(200.dp))
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (viewModel.recognizedLCCCodes.isEmpty()) {
                BasicText(text = "No LCC codes recognized. Full recognized text:")
                BasicText(text = viewModel.recognizedText)
            } else {
                viewModel.recognizedLCCCodes.forEach { lccCode ->
                    BasicText(text = lccCode)
                }

                Spacer(modifier = Modifier.height(16.dp))

                val orderCheck = checkOrder(viewModel.recognizedLCCCodes)
                BasicText(text = if (orderCheck) "Books are in correct order" else "Books are not in correct order")
            }
        }

        // Drawing the frame
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasWidth = size.width
            val canvasHeight = size.height
            val frameWidth = canvasWidth * 0.8f
            val frameHeight = canvasHeight * 0.3f
            val left = (canvasWidth - frameWidth) / 2
            val top = (canvasHeight - frameHeight) / 2

            drawRect(
                color = Color.Red,
                topLeft = Offset(left, top),
                size = Size(frameWidth, frameHeight),
                style = Stroke(width = 5f)
            )
        }
    }
}

class MainViewModel : ViewModel() {
    var capturedImage: Bitmap? by mutableStateOf(null)
    var recognizedLCCCodes: List<String> by mutableStateOf(emptyList())
    var recognizedText: String by mutableStateOf("")

    fun processImage(bitmap: Bitmap) {
        val image = InputImage.fromBitmap(bitmap, 0)
        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
        recognizer.process(image)
            .addOnSuccessListener { visionText ->
                recognizedLCCCodes = parseLCCCodes(visionText.text)
                recognizedText = visionText.text
            }
            .addOnFailureListener { e ->
                recognizedLCCCodes = listOf("Text recognition failed: ${e.message}")
            }
    }

    private fun parseLCCCodes(text: String): List<String> {
        val regex = Regex("""[A-Z]{1,2}\s?\d{1,4}(\.\d+)?\s?[A-Z]{3}""")
        return regex.findAll(text)
            .map { it.value }
            .toList()
    }

    fun saveState(outState: Bundle) {
        capturedImage?.let {
            val stream = ByteArrayOutputStream()
            it.compress(Bitmap.CompressFormat.PNG, 100, stream)
            outState.putByteArray("capturedImage", stream.toByteArray())
        }
        outState.putStringArray("recognizedLCCCodes", recognizedLCCCodes.toTypedArray())
        outState.putString("recognizedText", recognizedText)
    }

    fun restoreState(savedInstanceState: Bundle) {
        savedInstanceState.getByteArray("capturedImage")?.let {
            capturedImage = BitmapFactory.decodeByteArray(it, 0, it.size)
        }
        recognizedLCCCodes = savedInstanceState.getStringArray("recognizedLCCCodes")?.toList() ?: emptyList()
        recognizedText = savedInstanceState.getString("recognizedText", "")
    }
}

private fun checkOrder(codes: List<String>): Boolean {
    return codes == codes.sorted()
}

private fun cropToFrame(bitmap: Bitmap): Bitmap {
    val frameWidth = bitmap.width * 0.8f
    val frameHeight = bitmap.height * 0.3f
    val left = ((bitmap.width - frameWidth) / 2).toInt()
    val top = ((bitmap.height - frameHeight) / 2).toInt()
    return Bitmap.createBitmap(bitmap, left, top, frameWidth.toInt(), frameHeight.toInt())
}
