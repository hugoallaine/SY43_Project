package com.example.sy43_bookshelft.db

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class QuotationViewModelFactory(
    private val application: Application,
    private val repository: QuotationRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(QuotationViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return QuotationViewModel(application, repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}