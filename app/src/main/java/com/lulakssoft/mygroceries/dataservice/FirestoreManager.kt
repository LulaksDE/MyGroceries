package com.lulakssoft.mygroceries.dataservice

import com.google.firebase.firestore.FirebaseFirestore
import com.lulakssoft.mygroceries.database.household.HouseholdInvitation
import com.lulakssoft.mygroceries.database.product.Household
import kotlinx.coroutines.tasks.await
import java.util.logging.Logger

class FirestoreManager {
    private val firestore = FirebaseFirestore.getInstance()
    private val householdCollection = firestore.collection("households")
    private val invitationCollection = firestore.collection("invitations")
    private val logger = Logger.getLogger(FirestoreManager::class.java.name)

    // Synchronize household with Firebase Firestore
    suspend fun syncHousehold(household: Household) {
        val householdData =
            hashMapOf(
                "id" to household.id,
                "householdName" to household.householdName,
                "createdByUserId" to household.createdByUserId,
                "createdAt" to household.createdAt.toString(),
            )

        try {
            householdCollection
                .document(household.id.toString())
                .set(householdData)
                .await()
            logger.info("Household ${household.id} synchronized to Firestore")
        } catch (e: Exception) {
            logger.severe("Failed to sync household: ${e.message}")
        }
    }

    // Synchronize invitation with Firebase Firestore
    suspend fun syncInvitation(invitation: HouseholdInvitation) {
        val invitationData =
            hashMapOf(
                "invitationCode" to invitation.invitationCode,
                "householdId" to invitation.householdId,
                "createdByUserId" to invitation.createdByUserId,
                "createdAt" to invitation.createdAt.toString(),
                "isActive" to true,
            )

        try {
            invitationCollection
                .document(invitation.invitationCode)
                .set(invitationData)
                .await()
            logger.info("Invitation code ${invitation.invitationCode} synchronized to Firestore")
        } catch (e: Exception) {
            logger.severe("Failed to sync invitation: ${e.message}")
        }
    }

    // Get invitation from Firestore by code
    suspend fun getInvitationByCode(code: String): Map<String, Any>? =
        try {
            val document = invitationCollection.document(code).get().await()
            if (document.exists()) {
                logger.info("Invitation found in Firestore: $code")
                document.data
            } else {
                logger.info("Invitation not found in Firestore: $code")
                null
            }
        } catch (e: Exception) {
            logger.severe("Error getting invitation: ${e.message}")
            null
        }

    // Mark invitation as used/inactive
    suspend fun deactivateInvitation(code: String) {
        try {
            invitationCollection
                .document(code)
                .update("isActive", false)
                .await()
            logger.info("Invitation deactivated: $code")
        } catch (e: Exception) {
            logger.severe("Failed to deactivate invitation: ${e.message}")
        }
    }

    // Synchronize member addition with Firestore
    suspend fun syncNewMember(
        householdId: Int,
        userId: String,
        role: String,
    ) {
        val memberData =
            hashMapOf(
                "householdId" to householdId,
                "userId" to userId,
                "role" to role,
                "joinedAt" to System.currentTimeMillis(),
            )

        try {
            householdCollection
                .document(householdId.toString())
                .collection("members")
                .document(userId)
                .set(memberData)
                .await()
            logger.info("Member $userId added to household $householdId in Firestore")
        } catch (e: Exception) {
            logger.severe("Failed to sync new member: ${e.message}")
        }
    }
}
