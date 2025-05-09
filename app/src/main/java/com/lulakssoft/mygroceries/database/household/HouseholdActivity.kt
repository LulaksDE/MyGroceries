package com.lulakssoft.mygroceries.database.household

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDateTime

enum class ActivityType {
    PRODUCT_ADDED,
    PRODUCT_REMOVED,
    PRODUCT_UPDATED,
    MEMBER_JOINED,
    MEMBER_LEFT,
    HOUSEHOLD_CREATED,
    HOUSEHOLD_UPDATED,
}

@Entity(
    tableName = "household_activity_table",
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
data class HouseholdActivity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val householdId: Int,
    val firestoreId: String,
    val activityId: String,
    val userId: String,
    val userName: String,
    val activityType: ActivityType,
    val details: String,
    val timestamp: LocalDateTime = LocalDateTime.now(),
)
