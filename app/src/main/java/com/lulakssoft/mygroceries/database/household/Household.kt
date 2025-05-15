package com.lulakssoft.mygroceries.database.household

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(tableName = "household_table")
data class Household(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val householdName: String,
    val createdByUserId: String,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val isPrivate: Boolean = false,
    val firestoreId: String? = null,
    val synced: Boolean = false,
)
