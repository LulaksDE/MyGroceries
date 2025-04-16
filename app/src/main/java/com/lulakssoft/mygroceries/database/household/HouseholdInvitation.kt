package com.lulakssoft.mygroceries.database.household

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(tableName = "household_invitation_table")
data class HouseholdInvitation(
    @PrimaryKey
    val invitationCode: String,
    val householdId: Int,
    val createdByUserId: String,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val expiresAt: LocalDateTime = LocalDateTime.now().plusDays(7),
)
