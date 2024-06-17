package com.example.sy43_bookshelft.db

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class QuotationViewModel(application: Application, private val repository: QuotationRepository) : AndroidViewModel(application) {

    private val _quotations = MutableStateFlow<List<Quotation>>(emptyList())
    val quotations: StateFlow<List<Quotation>> get() = _quotations

    init {
        getAllQuotations()
    }

    fun getAllQuotations() {
        viewModelScope.launch {
            _quotations.value = repository.getAllQuotations()
        }
    }

    fun searchQuotations(query: String) {
        viewModelScope.launch {
            _quotations.value = repository.searchQuotations(query)
        }
    }

    fun addQuotation(quotation: Quotation) {
        viewModelScope.launch {
            repository.addQuotation(quotation)
            getAllQuotations()
        }
    }

    fun deleteQuotation(quotation: Quotation) {
        viewModelScope.launch {
            repository.deleteQuotation(quotation)
            getAllQuotations()
        }
    }

    fun deleteAllQuotations() {
        viewModelScope.launch {
            repository.deleteAllQuotations()
            getAllQuotations()
        }
    }
}