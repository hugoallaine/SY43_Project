package com.example.sy43_bookshelft

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import java.io.BufferedReader
import java.io.File
import java.io.FileOutputStream
import java.io.InputStreamReader

@Composable
fun ClassificationScreen(navController: NavHostController) {
    val context = LocalContext.current
    var classifications by remember { mutableStateOf(loadRegexesFromFile(context)) }
    var errorMessage by remember { mutableStateOf("") }
    var showModal by remember { mutableStateOf(false) }

    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top,
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text("Edit classifications", fontSize = 20.sp, modifier = Modifier.padding(bottom = 16.dp))

            LazyColumn(modifier = Modifier.weight(1f)) {
                items(classifications.toList()) { (name, regex) ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Text("$name :", modifier = Modifier.weight(1f))
                        Text(regex.pattern, modifier = Modifier.weight(2f))
                    }
                    // draw a line between each item
                    HorizontalDivider()
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (errorMessage.isNotEmpty()) {
                Text(errorMessage, color = Color.Red, modifier = Modifier.padding(top = 8.dp))
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                contentAlignment = Alignment.BottomEnd
            ) {
                Button(onClick = { showModal = true }) {
                    Text("Add a new classification")
                }
            }
        }

        if (showModal) {
            ClassificationModal(
                onClose = { showModal = false },
                onSave = { name, regex ->
                    try {
                        Regex(regex) // Validate regex
                        classifications = classifications.toMutableMap().apply {
                            put(name, Regex(regex))
                        }
                        saveRegexesToFile(context, classifications)
                        errorMessage = "Classification added successfully!"
                    } catch (e: Exception) {
                        errorMessage = "Wrong regex format"
                    }
                }
            )
        }
    }
}

@Composable
fun ClassificationModal(onClose: () -> Unit, onSave: (String, String) -> Unit) {
    var classificationName by remember { mutableStateOf("") }
    var regexParts by remember { mutableStateOf(List(6) { RegexPart("", "", "", false) }) }

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0x80000000))
            .padding(32.dp),
        shape = MaterialTheme.shapes.medium,
        tonalElevation = 8.dp
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top,
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                contentAlignment = Alignment.TopEnd
            ) {
                Button(onClick = onClose) {
                    Text("✕")
                }
            }

            Text("New classification", fontSize = 20.sp, modifier = Modifier.padding(bottom = 16.dp))

            BasicTextField(
                value = classificationName,
                onValueChange = { classificationName = it },
                modifier = Modifier
                    .border(1.dp, Color.Black)
                    .padding(8.dp)
                    .fillMaxWidth(),
                singleLine = true,
                decorationBox = { innerTextField ->
                    if (classificationName.isEmpty()) {
                        Text("Entrer the name of the classification", color = Color.Gray)
                    }
                    innerTextField()
                }
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text("Configure regex", fontSize = 16.sp, modifier = Modifier.padding(bottom = 8.dp))

            LazyColumn(
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(vertical = 4.dp)
            ) {
                items(regexParts.size) { index ->
                    val part = regexParts[index]
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Part ${index + 1}: ", modifier = Modifier.weight(1f))
                            DropdownWithLabel(
                                label = "Min",
                                options = (0..9).map { it.toString() },
                                selectedOption = part.min,
                                onOptionSelected = { selected ->
                                    regexParts = regexParts.toMutableList().apply {
                                        this[index] = this[index].copy(min = selected)
                                    }
                                }
                            )
                            DropdownWithLabel(
                                label = "Max",
                                options = (0..9).map { it.toString() },
                                selectedOption = part.max,
                                onOptionSelected = { selected ->
                                    regexParts = regexParts.toMutableList().apply {
                                        this[index] = this[index].copy(max = selected)
                                    }
                                }
                            )
                            DropdownWithLabel(
                                label = "Type",
                                options = listOf(
                                    "Letters [A-Z]",
                                    "Digits [0-9]",
                                    "Letters\n Or Digits \n[A-Z0-9]",
                                    "Comma\n or Dot"
                                ),
                                selectedOption = part.type,
                                onOptionSelected = { selected ->
                                    regexParts = regexParts.toMutableList().apply {
                                        this[index] = this[index].copy(type = selected)
                                    }
                                }
                            )
                            DropdownWithLabel(
                                label = "Optional",
                                options = listOf("Yes", "No"),
                                selectedOption = if (part.optional) "Yes" else "No",
                                onOptionSelected = { selected ->
                                    regexParts = regexParts.toMutableList().apply {
                                        this[index] =
                                            this[index].copy(optional = selected == "Yes")
                                    }
                                }
                            )
                        }
                    }

                }
                item {
                    Row(
                        horizontalArrangement = Arrangement.End,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Button(onClick = onClose, modifier = Modifier.padding(end = 8.dp)) {
                            Text("Cancel")
                        }
                        Button(onClick = {
                            if (classificationName.isNotEmpty() && regexParts.all { it.isValid() }) {
                                println("Save button clicked")
                                val regex = regexParts.joinToString("") { it.toRegexPart() }
                                onSave(classificationName, regex)
                                onClose()
                            }
                        }) {
                            Text("Save")
                        }
                    } // End of button row
                }} // End of LazyColumn


        }
    }
}

@Composable
fun DropdownWithLabel(
    label: String,
    options: List<String>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    Column(modifier = Modifier.padding(4.dp)) {
        Text(label, fontSize = 12.sp, color = Color.Gray)
        Box(
            modifier = Modifier
                .border(1.dp, Color.Black)
                .clickable { expanded = true }
                .padding(8.dp)
        ) {
            Text(selectedOption, color = if (selectedOption.isEmpty()) Color.Gray else Color.Black)
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                options.forEach { option ->
                    DropdownMenuItem(text =  {
                        Text(option)
                    }, onClick = {
                        onOptionSelected(option)
                        expanded = false
                    })
                }
            }
        }
    }
}

data class RegexPart(
    val min: String,
    val max: String,
    val type: String,
    val optional: Boolean
) {
    fun isValid(): Boolean {
        return min.isNotEmpty() && max.isNotEmpty() && type.isNotEmpty()
    }

    fun toRegexPart(): String {
        val typePattern = when (type) {
            "Lettres [A-Z]" -> "[A-Z]"
            "Chiffres [0-9]" -> "\\d"
            "Lettres ou Chiffres [A-Z0-9]" -> "[A-Z\\d]"
            else -> ""
        }
        val optionalPart = if (optional) "?" else ""
        return "$typePattern{$min,$max}$optionalPart"
    }
}

fun loadRegexesFromFile(context: Context): Map<String, Regex> {
    val regexMap = mutableMapOf<String, Regex>()
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
    return regexMap
}

fun saveRegexesToFile(context: Context, regexMap: Map<String, Regex>) {
    val file = File(context.filesDir, "regex.txt")
    FileOutputStream(file).use { output ->
        regexMap.forEach { (name, regex) ->
            output.write("$name:${regex.pattern}\n".toByteArray())
        }
    }
}
