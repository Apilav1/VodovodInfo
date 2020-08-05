package com.pilove.vodovodinfo.room

import androidx.room.TypeConverter
import java.sql.Date
import java.text.SimpleDateFormat

class Converters {

    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date): Long? {
        return date.time
    }
}