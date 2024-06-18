package com.example.sy43_bookshelft

import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController

@Composable
fun HomeScreen(navController: NavHostController) {
    val configuration = LocalConfiguration.current

    when (configuration.orientation) {
        Configuration.ORIENTATION_LANDSCAPE -> {
            LandscapeLayout(navController)
        }
        else -> {
            PortraitLayout(navController)
        }
    }
}

@Composable
fun PortraitLayout(navController: NavHostController) {
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
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = stringResource(id = R.string.app_name), fontSize = 24.sp)
        Spacer(modifier = Modifier.height(16.dp))
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

@Composable
fun LandscapeLayout(navController: NavHostController) {
    Row(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFBCCAC8)),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.logo),
            contentDescription = "logo",
            modifier = Modifier.size(300.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = stringResource(id = R.string.app_name), fontSize = 24.sp)
        Column(
            modifier = Modifier
                .padding(start = 16.dp),
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
            Button(
                onClick = { navController.navigate("classification") },
                modifier = Modifier
                    .padding(16.dp)
            ) {
                Text(text = stringResource(id = R.string.classification_btn_home))
            }
        }
    }
}
