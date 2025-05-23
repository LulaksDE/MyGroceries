package com.lulakssoft.mygroceries.database.product

import androidx.compose.ui.graphics.ImageBitmap
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.lulakssoft.mygroceries.database.household.Household
import java.time.LocalDate
import java.time.LocalDateTime

@Entity(
    tableName = "product_table",
    foreignKeys = [
        ForeignKey(
            entity = Household::class,
            parentColumns = ["id"],
            childColumns = ["householdId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("householdId")],
)
data class Product(
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    val householdId: Int,
    val firestoreId: String,
    val productUuid: String,
    val creatorId: String,
    val productName: String,
    val productBrand: String,
    val productBarcode: String,
    val productQuantity: Int,
    val productBestBeforeDate: LocalDate,
    val productEntryDate: LocalDateTime,
    val productImage: ImageBitmap,
    val productImageUrl: String? = null,
    val isSynced: Boolean = false,
)
