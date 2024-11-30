package com.lulakssoft.mygroceries.database.grocery

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "grocery_table")
data class Grocery(
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    val groceryName: String,
)