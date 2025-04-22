package com.lulakssoft.mygroceries.sync

import android.util.Log
import com.lulakssoft.mygroceries.database.household.Household
import com.lulakssoft.mygroceries.database.household.HouseholdMember
import com.lulakssoft.mygroceries.database.household.HouseholdRepository
import com.lulakssoft.mygroceries.database.household.MemberRole
import com.lulakssoft.mygroceries.dataservice.FirestoreHousehold
import com.lulakssoft.mygroceries.dataservice.FirestoreHouseholdMember
import com.lulakssoft.mygroceries.dataservice.FirestoreHouseholdRepository
import java.time.LocalDateTime

class HouseholdSyncService(
    private val localRepository: HouseholdRepository,
    private val firestoreRepository: FirestoreHouseholdRepository,
) {
    private val TAG = "HouseholdSyncService"

    suspend fun syncUserHouseholds(userId: String) {
        try {
            Log.d(TAG, "Starting household sync for user: $userId")

            // 1. Hole Remote-Daten
            val remoteHouseholds = firestoreRepository.getUserHouseholds(userId)
            Log.d(TAG, "Fetched ${remoteHouseholds.size} remote households")

            // 2. Für jeden Remote-Haushalt
            for (remoteHousehold in remoteHouseholds) {
                // Konvertiere zu lokalem Household-Objekt
                val household = convertToLocalHousehold(remoteHousehold)

                // Speichere in lokaler DB
                val householdId = localRepository.insertOrUpdateHousehold(household)

                // Hole und synchronisiere Mitglieder
                syncHouseholdMembers(remoteHousehold.firestoreId, householdId.toInt(), userId)
            }

            Log.d(TAG, "Household sync completed successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error during household sync", e)
        }
    }

    private suspend fun syncHouseholdMembers(
        firestoreId: String,
        localHouseholdId: Int,
        currentUserId: String,
    ) {
        try {
            val remoteMembers = firestoreRepository.getHouseholdMembers(firestoreId)

            for (remoteMember in remoteMembers) {
                val member = convertToLocalMember(remoteMember, localHouseholdId, firestoreId)
                localRepository.insertOrUpdateMember(member)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error syncing household members", e)
        }
    }

    private fun convertToLocalDateTime(date: String): LocalDateTime =
        try {
            LocalDateTime.parse(date)
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing date: $date", e)
            LocalDateTime.now()
        }

    private fun convertToLocalHousehold(remoteHousehold: FirestoreHousehold): Household {
        val createdAt = convertToLocalDateTime(remoteHousehold.createdAt)
        return Household(
            id = 0, // Wird von Room automatisch zugewiesen
            householdName = remoteHousehold.name,
            createdByUserId = remoteHousehold.createdByUserId,
            createdAt = createdAt,
            isPrivate = remoteHousehold.isPrivate,
            firestoreId = remoteHousehold.firestoreId,
        )
    }

    private fun convertToLocalMember(
        remoteMember: FirestoreHouseholdMember,
        localHouseholdId: Int,
        firestoreId: String,
    ): HouseholdMember {
        val role =
            try {
                MemberRole.valueOf(remoteMember.role)
            } catch (e: Exception) {
                MemberRole.MEMBER
            }

        return HouseholdMember(
            id = 0, // Wird von Room automatisch zugewiesen
            householdId = localHouseholdId,
            firestoreId = firestoreId,
            userId = remoteMember.userId,
            userName = remoteMember.userName,
            role = role,
            joinedAt = remoteMember.joinedAt,
        )
    }
}
