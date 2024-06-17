package com.example.sy43_bookshelft.db

import androidx.room.TypeConverter
import java.util.Date

class Converter {
    @TypeConverter
    fun timestampToDate(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }
}