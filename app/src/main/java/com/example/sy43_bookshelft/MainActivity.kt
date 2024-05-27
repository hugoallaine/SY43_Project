package com.example.sy43_bookshelft

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicText
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import com.example.sy43_bookshelft.ui.theme.SY43_bookshelftTheme
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions

class MainActivity : ComponentActivity() {
    private var capturedImage: Bitmap? by mutableStateOf(null)
    private var recognizedLCCCodes: List<String> by mutableStateOf(emptyList())
    private var recognizedText: String by mutableStateOf("")

    private val takePictureLauncher = registerForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
        capturedImage = bitmap
        bitmap?.let { processImage(it) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), 1)

        setContent {
            SY43_bookshelftTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    Column(
                        modifier = Modifier.fillMaxSize().padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Button(onClick = { takePictureLauncher.launch(null) }) {
                            Text("Take Picture")
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        capturedImage?.let {
                            Image(bitmap = it.asImageBitmap(), contentDescription = null, modifier = Modifier.size(200.dp))
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        if (recognizedLCCCodes.isEmpty()) {
                            BasicText(text = "No LCC codes recognized. Full recognized text:")
                            BasicText(text = recognizedText)
                        } else {
                            recognizedLCCCodes.forEach { lccCode ->
                                BasicText(text = lccCode)
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            val orderCheck = checkOrder(recognizedLCCCodes)
                            BasicText(text = if (orderCheck) "Books are in correct order" else "Books are not in correct order")
                        }
                    }
                }
            }
        }
    }

    private fun processImage(bitmap: Bitmap) {
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

    private fun checkOrder(codes: List<String>): Boolean {
        // Example implementation: Check if the LCC codes are in the correct order
        return codes == codes.sorted()
    }
}
