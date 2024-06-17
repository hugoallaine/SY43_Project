package com.example.sy43_bookshelft.csv

import android.content.Context

object QuotationObj {
    lateinit var quotationList: List<Quotation>

    fun loadQuotations(context: Context, fileName: String) {
        quotationList = readCsv(context, fileName)
    }
}