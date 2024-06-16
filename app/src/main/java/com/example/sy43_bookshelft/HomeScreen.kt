package com.example.sy43_bookshelft

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController

@Composable
fun HomeScreen(navController: NavHostController) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(
            onClick = { navController.navigate("scanner") },
            modifier = Modifier
                .padding(16.dp)
        ) {
            Text(text = stringResource(id = R.string.scan_btn_home))
        }
        Button(
            onClick = { navController.navigate("list") },
            modifier = Modifier
                .padding(16.dp)
        ) {
            Text(text = stringResource(id = R.string.list_btn_home))
        }
    }
}