package com.example.sy43_bookshelft

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.sy43_bookshelft.csv.Quotation
import com.example.sy43_bookshelft.csv.QuotationObj.quotationList
import com.example.sy43_bookshelft.csv.writeCsv
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


/**
 * Composable function that represents the list screen of the app.
 *
 * @param navController The navigation controller used for navigating between screens.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListScreen(navController: NavHostController) {
    var expanded by remember { mutableStateOf(false) }
    var showDialog by remember { mutableStateOf(false) }
    var showDialogDelete by remember { mutableStateOf(false) }
    var quotationValue by remember { mutableStateOf("") }
    val context = LocalContext.current

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDialog = false
                        quotationList.add(Quotation(quotationValue, Date()))
                        writeCsv(context = context, quotationList)
                        navController.popBackStack()
                        navController.navigate("list")
                    }
                ) {
                    Text(stringResource(id = R.string.add))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDialog = false }
                ) {
                    Text(stringResource(id = R.string.cancel))
                }
            },
            title = { Text(stringResource(id = R.string.add_quotation)) },
            text = {
                Column {
                    Text(stringResource(id = R.string.form_quotation_value))
                    Spacer(modifier = Modifier.height(8.dp))
                    TextField(
                        value = quotationValue,
                        onValueChange = { quotationValue = it },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        )
    } else if (showDialogDelete) {
        AlertDialog(
            onDismissRequest = { showDialogDelete = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDialogDelete = false
                        quotationList.clear()
                        writeCsv(context = context, quotationList)
                        navController.popBackStack()
                        navController.navigate("list")
                    }
                ) {
                    Text(stringResource(id = R.string.confirm))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDialog = false }
                ) {
                    Text(stringResource(id = R.string.cancel))
                }
            },
            title = { Text(stringResource(id = R.string.alert_delete_database)) },
        )
    }

    Surface(modifier = Modifier.fillMaxSize()) {
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
                actions = {
                    IconButton(onClick = { expanded = true }) {
                        Icon(
                            imageVector = Icons.Filled.MoreVert,
                            contentDescription = stringResource(id = R.string.more_options)
                        )
                    }
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text(stringResource(id = R.string.add_quotation)) },
                            onClick = {
                                expanded = false
                                showDialog = true
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(id = R.string.delete_database),color = Color.Red) },
                            onClick = {
                                expanded = false
                                showDialogDelete = true
                            }
                        )
                    }
                }
            )
            if(quotationList.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = stringResource(id = R.string.empty_list),
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center,
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.padding(16.dp)
                ) {
                    items(quotationList) { quotation ->
                        QuotationItem(navController, quotation)
                    }
                }
            }
        }
    }
}

/**
 * Composable function that represents a single quotation item.
 *
 * @param navController The navigation controller used for navigating between screens.
 * @param quotation The quotation object to display.
 */
@Composable
fun QuotationItem(navController: NavHostController, quotation: Quotation) {
    val context = LocalContext.current
    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(1.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = quotation.value, style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = dateFormat.format(quotation.date), style = MaterialTheme.typography.bodyMedium)
            }
            IconButton(
                onClick = {
                    quotationList.remove(quotation)
                    writeCsv(context, quotationList)
                    navController.popBackStack()
                    navController.navigate("list")
                },
                modifier = Modifier
                    .padding(start = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = "Delete",
                    tint = Color.Red
                )
            }
        }
    }
}