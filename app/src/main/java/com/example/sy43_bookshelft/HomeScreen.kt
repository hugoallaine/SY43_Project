package com.example.sy43_bookshelft

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController

@Composable
fun HomeScreen(navController: NavHostController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFBCCAC8)),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = R.drawable.logo),
            contentDescription = "logo",
            modifier = Modifier.size(300.dp)
        )
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
        Button(
            onClick = { navController.navigate("classification") },
            modifier = Modifier
                .padding(16.dp)
        ) {
            Text(text = stringResource(id = R.string.classification_btn_home))
        }
    }
}