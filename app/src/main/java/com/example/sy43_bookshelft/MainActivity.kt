package com.example.sy43_bookshelft

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.app.ActivityCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.sy43_bookshelft.csv.QuotationObj
import com.example.sy43_bookshelft.csv.checkCsvFile
import com.example.sy43_bookshelft.csv.createCsvFile
import com.example.sy43_bookshelft.ui.theme.MainTheme
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class MainActivity : ComponentActivity() {
    private lateinit var cameraExecutor: ExecutorService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (checkCsvFile(this)) {
            QuotationObj.loadQuotations(this)
            println("CSV file loaded")
        } else {
            println("CSV file not found.")
            createCsvFile(this)
            QuotationObj.loadQuotations(this)
            println("CSV file created and loaded")
        }
        cameraExecutor = Executors.newSingleThreadExecutor()

        ActivityCompat.requestPermissions(
            this, arrayOf(Manifest.permission.CAMERA),
            REQUEST_CODE_PERMISSIONS
        )

        setContent {
            MainTheme {
                Surface(color = MaterialTheme.colors.background, modifier = Modifier.fillMaxSize()) {
                    val navController = rememberNavController()
                    NavHost(navController = navController, startDestination = "home") {
                        composable("home") { HomeScreen(navController) }
                        composable("scanner") { ScannerScreen(navController, cameraExecutor) }
                        composable("list") { ListScreen(navController) }
                        composable("classification") { ClassificationScreen(navController) }
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }

    companion object {
        private const val REQUEST_CODE_PERMISSIONS = 10
    }
}