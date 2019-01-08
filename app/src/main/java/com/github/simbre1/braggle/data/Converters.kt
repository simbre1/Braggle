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
    fun stringToWordlist(value: String?): TreeSet<String> {
        if (value == null || value.isBlank()) {
            return TreeSet()
        } else {
            return TreeSet(value.split(";"))
        }
    }

    @TypeConverter
    fun wordlistToString(wordlist: TreeSet<String>?): String? {
        if (wordlist == null || wordlist.isEmpty()) {
            return ""
        } else {
            return wordlist.joinToString(";")
        }
    }
}