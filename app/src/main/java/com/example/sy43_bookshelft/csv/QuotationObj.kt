package com.example.sy43_bookshelft.csv

import android.content.Context

object QuotationObj {
    lateinit var quotationList: MutableList<Quotation>

    fun loadQuotations(context: Context) {
        quotationList = readCsv(context)
    }
}