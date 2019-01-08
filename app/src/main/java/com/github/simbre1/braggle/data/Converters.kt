package com.github.simbre1.braggle.data

import androidx.room.TypeConverter
import java.util.*

class Converters {
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }

    @TypeConverter
    fun fromWordlist(value: String?): TreeSet<String> {
        return value?.let { TreeSet(it.split(";")) } ?: TreeSet()
    }

    @TypeConverter
    fun wordlistToString(wordlist: TreeSet<String>?): String? {
        return wordlist?.joinToString(";")
    }
}