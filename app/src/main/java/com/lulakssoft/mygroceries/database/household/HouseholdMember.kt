package com.lulakssoft.mygroceries.database.household

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.lulakssoft.mygroceries.database.product.Household
import java.time.LocalDateTime

enum class MemberRole {
    OWNER,
    ADMIN,
    MEMBER,
    READONLY,
}

@Entity(
    tableName = "household_member_table",
    foreignKeys = [
        ForeignKey(
            entity = Household::class,
            parentColumns = ["id"],
            childColumns = ["householdId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("householdId"), Index("userId")],
)
data class HouseholdMember(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val householdId: Int,
    val userId: String,
    val userName: String,
    val role: MemberRole,
    val joinedAt: LocalDateTime = LocalDateTime.now(),
)
