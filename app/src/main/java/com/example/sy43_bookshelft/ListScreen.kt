package com.example.sy43_bookshelft

import android.app.Application
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.sy43_bookshelft.db.QuotationDatabase
import com.example.sy43_bookshelft.db.Quotation
import com.example.sy43_bookshelft.db.QuotationRepository
import com.example.sy43_bookshelft.db.QuotationViewModel
import com.example.sy43_bookshelft.db.QuotationViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListScreen(navController: NavHostController) {
    val context = LocalContext.current.applicationContext as Application
    val quotationDao = QuotationDatabase.getDatabase(context).quotationDao()
    val repository = QuotationRepository(quotationDao)
    val factory = QuotationViewModelFactory(context, repository)
    val viewModel: QuotationViewModel = viewModel(factory = factory)

    val quotations by viewModel.quotations.collectAsState()

    Column {
        TopAppBar(
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                titleContentColor = MaterialTheme.colorScheme.primary,
            ),
            title = {
                Text(
                    text = stringResource(id = R.string.list_btn_home)
                )
            },
            navigationIcon = {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBack,
                        contentDescription = stringResource(id = R.string.back_btn)
                    )
                }
            },
        )

        var searchQuery by remember { mutableStateOf("") }

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { newQuery ->
                searchQuery = newQuery
                viewModel.searchQuotations(newQuery)
            },
            label = { Text("Search Quotations") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        )

        LazyColumn(
            modifier = Modifier.padding(16.dp)
        ) {
            items(quotations) { quotation ->
                QuotationItem(quotation)
            }
        }
    }
}

@Composable
fun QuotationItem(quotation: Quotation) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = quotation.value)
            Text(text = "Added at: ${quotation.added_at}")
        }
    }
}