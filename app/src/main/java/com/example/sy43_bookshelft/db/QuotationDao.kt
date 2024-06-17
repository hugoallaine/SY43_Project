package com.example.sy43_bookshelft.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface QuotationDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addQuotation(quotation: Quotation)

    @Query("SELECT * FROM quotation")
    suspend fun getAllQuotations(): List<Quotation>

    @Query("SELECT * FROM quotation WHERE value LIKE :searchQuery")
    suspend fun searchQuotations(searchQuery: String): List<Quotation>

    @Delete
    suspend fun deleteQuotation(quotation: Quotation)

    @Query("DELETE FROM quotation")
    suspend fun deleteAllQuotations()
}