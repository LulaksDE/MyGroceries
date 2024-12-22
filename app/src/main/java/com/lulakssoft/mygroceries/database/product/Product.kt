package com.lulakssoft.mygroceries.database.product

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "product_table")
data class Product(
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    val productName: String,
    val productImage: ByteArray,
)
