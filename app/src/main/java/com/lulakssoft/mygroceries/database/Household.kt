package com.lulakssoft.mygroceries.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "household_table")
data class Household(
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    val householdName: String,
)
