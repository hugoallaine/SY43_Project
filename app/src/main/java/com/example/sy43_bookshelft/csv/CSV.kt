package com.example.sy43_bookshelft.csv

import android.content.Context
import com.example.sy43_bookshelft.csv.QuotationObj.loadQuotations
import java.io.BufferedWriter
import java.io.OutputStreamWriter
import java.text.SimpleDateFormat
import java.util.*

val csvFile = "database.csv"

/**
 * Check if the CSV file exists
 *
 * @param context The context of the application
 * @return True if the file exists, false otherwise
 */
fun checkCsvFile(context: Context): Boolean {
    println("Checking CSV file...")
    val file = context.getFileStreamPath(csvFile)
    return file.exists()
}

/**
 * Create the CSV file at first launch
 *
 * @param context The context of the application
 */
fun createCsvFile(context: Context) {
    println("Creating CSV file...")
    val file = context.getFileStreamPath(csvFile)
    file.createNewFile()
    val writer = OutputStreamWriter(file.outputStream())
    writer.write("Value,Date\n")
    writer.close()
}

/**
 * Read the CSV file and return a list of Quotation objects
 *
 * @param context The context of the application
 * @return A list of Quotation objects
 */
fun readCsv(context: Context): MutableList<Quotation> {
    println("Reading CSV file...")
    val quotationList = mutableListOf<Quotation>()
    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    context.openFileInput(csvFile).bufferedReader().use { reader ->
        reader.readLine()
        var line = reader.readLine()
        while (line != null) {
            val parts = line.split(",")
            if (parts.size == 2) {
                val value = parts[0]
                val date = dateFormat.parse(parts[1])
                if (date != null) {
                    quotationList.add(Quotation(value, date))
                }
            }
            line = reader.readLine()
        }
    }
    return quotationList
}

/**
 * Write the list of Quotation objects to the CSV file
 *
 * @param context The context of the application
 * @param data The list of Quotation objects to be written
 * @return True if the file was written successfully, false otherwise
 */
fun writeCsv(context: Context, data: MutableList<Quotation>) {
    println("Writing CSV file...")
    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    val outputStream = context.openFileOutput(csvFile, Context.MODE_PRIVATE)
    val writer = BufferedWriter(OutputStreamWriter(outputStream))
    writer.use { writer ->
        writer.write("Value,Date\n")
        data.forEach { quotation ->
            val line = "${quotation.value},${dateFormat.format(quotation.date)}\n"
            writer.write(line)
        }
    }
    loadQuotations(context)
}