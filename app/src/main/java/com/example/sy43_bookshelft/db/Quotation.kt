package com.example.sy43_bookshelft.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "quotation")
data class Quotation(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val value: String,
    val added_at: Long
)