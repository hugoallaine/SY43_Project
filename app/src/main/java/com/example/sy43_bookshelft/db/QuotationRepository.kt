package com.example.sy43_bookshelft.db

import javax.inject.Inject

class QuotationRepository(private val quotationDao: QuotationDao) {
    suspend fun getAllQuotations(): List<Quotation> {
        return quotationDao.getAllQuotations()
    }

    suspend fun addQuotation(quotation: Quotation) {
        quotationDao.addQuotation(quotation)
    }

    suspend fun deleteQuotation(quotation: Quotation) {
        quotationDao.deleteQuotation(quotation)
    }

    suspend fun deleteAllQuotations() {
        quotationDao.deleteAllQuotations()
    }

    suspend fun searchQuotations(query: String): List<Quotation> {
        return quotationDao.searchQuotations("%$query%")
    }
}