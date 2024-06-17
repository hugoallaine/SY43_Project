package com.example.sy43_bookshelft.db

import androidx.room.Database
import androidx.room.RoomDatabase
import android.content.Context
import androidx.room.Room

@Database(entities = [Quotation::class], version = 1, exportSchema = false)
abstract class QuotationDatabase : RoomDatabase() {

    abstract fun quotationDao(): QuotationDao

    companion object {
        @Volatile
        private var INSTANCE: QuotationDatabase? = null

        fun getDatabase(context: Context): QuotationDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    QuotationDatabase::class.java,
                    "quotation_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}