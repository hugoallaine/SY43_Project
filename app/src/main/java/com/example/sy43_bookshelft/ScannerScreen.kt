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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.LifecycleOwner
import androidx.navigation.NavHostController
import com.google.common.util.concurrent.ListenableFuture
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.io.File
import java.util.concurrent.ExecutorService
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import android.content.Context
import androidx.compose.ui.graphics.Color.Companion.Black
import com.example.sy43_bookshelft.csv.Quotation
import com.example.sy43_bookshelft.csv.QuotationObj.quotationList
import com.example.sy43_bookshelft.csv.writeCsv
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.Date

//Check Order of the books in the parameter text depend on classification choose and return a list of string with a boolean if the order is correct
fun CheckOrder(text: String, classification: String, regexMap: Map<String, Regex>): List<Pair<String, Boolean>> {

    val callNumbers = text.split("\n").map { it.trim() }.filter { it.isNotEmpty() }
    if (callNumbers.isEmpty()) return emptyList()

    val result = mutableListOf<Pair<String, Boolean>>()
    //For each book check with the next one
    for (i in 0 until callNumbers.size - 1) {
        val current = callNumbers[i]
        val next = callNumbers[i + 1]
        result.add(Pair(current, isInCorrectOrder(current, next, classification, regexMap)))
    }

    result.add(Pair(callNumbers.last(), true))
    return result
}

// Check if the order of the call numbers is correct
fun isInCorrectOrder(callNumber1: String, callNumber2: String, classification: String, regexMap: Map<String, Regex>): Boolean {
    println("Regex used: ${regexMap[classification]}")
    val pattern = regexMap[classification] ?: return false

    // Remove spaces from the call numbers
    val cleanedCallNumber1 = callNumber1.replace(" ", "")
    val cleanedCallNumber2 = callNumber2.replace(" ", "")

    val match1 = pattern.find(cleanedCallNumber1)
    val match2 = pattern.find(cleanedCallNumber2)

    if (match1 == null || match2 == null) {
        return false
    }


    // Extract all groups and compare them
    val groupCount = maxOf(match1.groupValues.size, match2.groupValues.size)

    for (i in 1 until groupCount) {
        val group1 = match1.groupValues.getOrNull(i) ?: ""
        val group2 = match2.groupValues.getOrNull(i) ?: ""

        // Determine the type of group and compare accordingly
        val comparisonResult = compareGroups(group1, group2)
        if (comparisonResult != 0) {
            return comparisonResult < 0
        }
    }
    return true
}

// Compare two groups based on their type and content
fun compareGroups(group1: String, group2: String): Int {
    return when {
        group1.isEmpty() && group2.isEmpty() -> 0
        group1.isEmpty() -> -1
        group2.isEmpty() -> 1
        group1.all { it.isDigit() } && group2.all { it.isDigit() } -> group1.toInt().compareTo(group2.toInt())
        group1.all { it.isLetter() } && group2.all { it.isLetter() } -> group1.compareTo(group2)
        else -> group1.compareTo(group2)
    }
}

// Load regex from internal file or raw resources if their is no internal file
fun loadRegexFromInternalFile(context: Context): Map<String, Regex> {
    val regexMap = mutableMapOf<String, Regex>()
    val file = File(context.filesDir, "regex.txt")

    if (file.exists()) {
        BufferedReader(InputStreamReader(file.inputStream())).use { reader ->
            reader.forEachLine { line ->
                val parts = line.split(":")
                if (parts.size == 2) {
                    val name = parts[0]
                    val regex = parts[1].toRegex()
                    regexMap[name] = regex
                }
            }
        }
    } else {
        // Load from raw resources if the file doesn't exist
        val inputStream = context.resources.openRawResource(R.raw.regex)
        BufferedReader(InputStreamReader(inputStream)).use { reader ->
            reader.forEachLine { line ->
                val parts = line.split(":")
                if (parts.size == 2) {
                    val name = parts[0]
                    val regex = parts[1].toRegex()
                    regexMap[name] = regex
                }
            }
        }
    }

    return regexMap
}

@SuppressLint("UnrememberedMutableState")
@Composable
fun ScannerScreen(navController: NavHostController, cameraExecutor: ExecutorService) {
    var scannedText by mutableStateOf("") // Store the scanned text
    var errorMessage by mutableStateOf("") // Store the error message
    var orderedResults by mutableStateOf<List<Pair<String, Boolean>>>(emptyList()) // Store the ordered results
    var expanded by remember { mutableStateOf(false) } // Dropdown expanded state
    var selectedClassification by remember { mutableStateOf("LCC") } // Selected classification
    val context = LocalContext.current
    val classifications = loadRegexFromInternalFile(context).keys.toList() // List of available classifications
    var regexMap = loadRegexFromInternalFile(context) // Map of regex patterns

    val PREVIEW_ASPECT_RATIO = AspectRatio.RATIO_16_9 // Aspect ratio for the preview
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

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
                
                // Image capture
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

                        Text("Scanned Text: $scannedText")
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                            .offset(y = (-40).dp)  // Offset from the top
                            .background(Black.copy(alpha = 0.5f))
                            .border(2.dp, Color.White)
                            .align(Alignment.TopCenter),
                        contentAlignment = Alignment.Center
                    ) {

                    }

                    // Error message or success
                    if (errorMessage.isNotEmpty()) {
                        Text(
                            text = "Error: $errorMessage",
                            color = Color.Red,
                            fontSize = 10.sp,
                            textAlign = TextAlign.Center,
                        )
                    }
                    if (scannedText.isNotEmpty()) {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxHeight()
                                .rotate(270f)
                                .align(Alignment.Center)
                                .offset(x = (120).dp)
                        ) {
                            items(orderedResults) { (line, isCorrect) ->
                                Text(
                                    text = line,
                                    color = if (isCorrect) Color.Green else Color.Red,
                                    fontSize = 10.sp,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier
                                        .padding(8.dp)
                                )
                            }
                        }
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp)
                            .offset(y = 40.dp)  // Offset from the bottom
                            .background(Black.copy(alpha = 0.5f))
                            .border(2.dp, Color.White)
                            .align(Alignment.BottomCenter),
                        contentAlignment = Alignment.Center // Align button to the center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.wrapContentWidth()
                        ) {
                            Button(
                                onClick = { navController.popBackStack() }
                            ) {
                                Text(text = stringResource(id = R.string.back_btn))
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            // Dropdown for classification selection
                            Box(
                                modifier = Modifier
                                    .wrapContentWidth()
                                    .clickable { expanded = true }
                                    .background(Color.Gray)
                                    .padding(8.dp)
                            ) {
                                Text(text = selectedClassification)
                                DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                                    classifications.forEach { classification ->
                                        DropdownMenuItem(text = {Text(text = classification)}, onClick = {
                                            selectedClassification = classification
                                            expanded = false
                                        })
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            // Capture Image Button
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
                                                    if (scannedText == "") {
                                                        errorMessage = "No books detected!"
                                                    } else {
                                                        regexMap = loadRegexFromInternalFile(context)
                                                        orderedResults = CheckOrder(visionText.text, selectedClassification, regexMap)
                                                        if (orderedResults.any { !it.second }) {
                                                            errorMessage = "Books are not in order!"
                                                        } else {
                                                            errorMessage = ""
                                                            orderedResults.map {
                                                                quotationList.add(Quotation(it.first, Date()))
                                                            }
                                                            writeCsv(context, quotationList)
                                                        }
                                                    }
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
                                Text(stringResource(id = R.string.take_picture_btn))
                            }
                        }
                    }
                }
            }
        } else {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Text(stringResource(id = R.string.rotate_warning))
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { navController.popBackStack() }) {
                        Text(text = stringResource(id = R.string.back_btn))
                    }
                }
            }
        }
    }
}

// Camera Preview
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
                }

                override fun surfaceDestroyed(holder: SurfaceHolder) {
                }
            })
            previewSurfaceView
        },
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(16f / 9f)  // Set aspect ratio to 16:9
    )
}