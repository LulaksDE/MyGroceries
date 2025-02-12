package com.lulakssoft.mygroceries.database.product

import androidx.room.TypeConverter
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class Converters {
    private val dateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME
    private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE

    @TypeConverter
    fun fromLocalDateTime(value: LocalDateTime): String = value.format(dateTimeFormatter)

    @TypeConverter
    fun fromLocalDate(value: LocalDate): String = value.format(dateFormatter)

    @TypeConverter
    fun toLocalDateTime(value: String): LocalDateTime =
        value.let {
            LocalDateTime.parse(it, dateTimeFormatter)
        }

    @TypeConverter
    fun toLocalDate(value: String): LocalDate =
        value.let {
            LocalDate.parse(it, dateFormatter)
        }
}
