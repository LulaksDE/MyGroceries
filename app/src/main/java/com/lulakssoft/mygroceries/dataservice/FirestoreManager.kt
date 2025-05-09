package com.lulakssoft.mygroceries.dataservice

import android.content.ContentValues.TAG
import android.util.Log
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.lulakssoft.mygroceries.database.household.ActivityType
import com.lulakssoft.mygroceries.database.household.Household
import com.lulakssoft.mygroceries.database.household.HouseholdInvitation
import com.lulakssoft.mygroceries.database.product.Product
import kotlinx.coroutines.tasks.await
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID

class FirestoreManager {
    private val firestore = FirebaseFirestore.getInstance()
    private val householdCollection = firestore.collection("households")
    private val invitationCollection = firestore.collection("invitations")

    private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE
    private val dateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME

    // Synchronize household with Firebase Firestore
    suspend fun syncNewHousehold(
        household: Household,
        userName: String,
    ) {
        if (household.firestoreId == null) {
            Log.e("FirestoreManager", "Firestore ID is null, cannot sync household")
            return
        }
        try {
            val householdData =
                hashMapOf(
                    "householdName" to household.householdName,
                    "createdByUserId" to household.createdByUserId,
                    "createdAt" to household.createdAt.toString(),
                    "isPrivate" to household.isPrivate,
                    "firestoreId" to household.firestoreId,
                )

            householdCollection
                .document(household.firestoreId)
                .set(householdData)
                .await()

            syncNewMember(household.firestoreId, household.createdByUserId, userName, "OWNER")
            logActivity(
                household.firestoreId,
                household.createdByUserId,
                userName,
                ActivityType.HOUSEHOLD_CREATED,
                "Household created: ${household.householdName}",
            )
            Log.d("FirestoreManager", "Household synchronized to Firestore")
        } catch (e: Exception) {
            Log.e("FirestoreManager", "Failed to sync household: ${e.message}")
        }
    }

    // Get household from Firestore by ID
    suspend fun getHouseholdById(id: String): Map<String, Any>? =
        try {
            val document = householdCollection.document(id).get().await()
            if (document.exists()) {
                Log.d("FirestoreManager", "Household found in Firestore: $id")
                Log.d("FirestoreManager", "Household data: ${document.data}")
                document.data
            } else {
                Log.d("FirestoreManager", "Household not found in Firestore: $id")
                null
            }
        } catch (e: Exception) {
            Log.e("FirestoreManager", "Failed to get household from Firestore: ${e.message}")
            null
        }

    // Synchronize invitation with Firebase Firestore
    suspend fun syncInvitation(invitation: HouseholdInvitation) {
        val invitationData =
            hashMapOf(
                "invitationCode" to invitation.invitationCode,
                "firestoreId" to invitation.firestoreId,
                "createdByUserId" to invitation.createdByUserId,
                "createdAt" to invitation.createdAt.toString(),
                "isActive" to true,
            )

        try {
            invitationCollection
                .document(invitation.invitationCode)
                .set(invitationData)
                .await()
            Log.d("FirestoreManager", "Invitation synchronized to Firestore")
        } catch (e: Exception) {
            Log.e("FirestoreManager", "Failed to sync invitation: ${e.message}")
        }
    }

    // Get invitation from Firestore by code
    suspend fun getInvitationByCode(code: String): Map<String, Any>? =
        try {
            val document = invitationCollection.document(code).get().await()
            if (document.exists()) {
                Log.d("FirestoreManager", "Invitation found in Firestore: $code")
                Log.d("FirestoreManager", "Invitation data: ${document.data}")
                document.data
            } else {
                Log.d("FirestoreManager", "Invitation not found in Firestore: $code")
                null
            }
        } catch (e: Exception) {
            Log.e("FirestoreManager", "Failed to get invitation from Firestore: ${e.message}")
            null
        }

    // Mark invitation as used/inactive
    suspend fun deactivateInvitation(code: String) {
        try {
            invitationCollection
                .document(code)
                .update("isActive", false)
                .await()
            Log.d("FirestoreManager", "Invitation deactivated in Firestore: $code")
        } catch (e: Exception) {
            Log.e("FirestoreManager", "Failed to deactivate invitation in Firestore: ${e.message}")
        }
    }

    // Synchronize member addition with Firestore
    suspend fun syncNewMember(
        firestoreId: String,
        userId: String,
        userName: String,
        role: String,
    ) {
        val memberData =
            hashMapOf(
                "userId" to userId,
                "userName" to userName,
                "role" to role,
                "joinedAt" to LocalDateTime.now(),
            )

        try {
            householdCollection
                .document(firestoreId)
                .collection("members")
                .document(userId)
                .set(memberData)
                .await()

            firestore
                .collection("users")
                .document(userId)
                .collection("memberships")
                .document(firestoreId)
                .set(
                    mapOf(
                        "role" to role,
                        "joinedAt" to FieldValue.serverTimestamp(),
                    ),
                )

            logActivity(
                firestoreId,
                userId,
                userName,
                ActivityType.MEMBER_JOINED,
                "Member added: $userName with role: $role",
            )

            Log.d("FirestoreManager", "New member synchronized to Firestore")
        } catch (e: Exception) {
            Log.e("FirestoreManager", "Failed to sync new member: ${e.message}")
        }
    }

    suspend fun addProductToHousehold(
        firestoreId: String,
        product: Product,
    ): Boolean {
        try {
            val productData =
                hashMapOf(
                    "productId" to product.productUuid,
                    "productName" to product.productName,
                    "productBrand" to product.productBrand,
                    "productBarcode" to product.productBarcode,
                    "productQuantity" to product.productQuantity,
                    "productBestBeforeDate" to product.productBestBeforeDate.format(dateFormatter),
                    "productEntryDate" to LocalDateTime.now().format(dateTimeFormatter),
                    "createdByUserId" to product.creatorId,
                    "imageUrl" to product.productImageUrl,
                )

            val productRef =
                firestore
                    .collection("households")
                    .document(firestoreId)
                    .collection("products")
                    .document(product.productUuid)
                    .set(productData)
                    .await()
            Log.d(TAG, "Product added successfully to firestore with ID: ${product.productUuid}")

            logActivity(
                firestoreId,
                product.creatorId,
                getUserName(product.firestoreId, product.creatorId),
                ActivityType.PRODUCT_ADDED,
                "Product added: ${product.productName}",
            )

            return true
        } catch (e: Exception) {
            Log.e(TAG, "Error while adding product to firestore", e)
            throw e
        }
    }

    suspend fun deleteProductFromHousehold(product: Product) {
        try {
            firestore
                .collection("households")
                .document(product.firestoreId)
                .collection("products")
                .document(product.productUuid)
                .delete()
                .await()
            Log.d(TAG, "Product deleted successfully from firestore")

            logActivity(
                product.firestoreId,
                product.productUuid,
                getUserName(product.firestoreId, product.creatorId),
                ActivityType.PRODUCT_REMOVED,
                "Product deleted: ${product.productName}",
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error while deleting product from firestore", e)
            throw e
        }
    }

    suspend fun logActivity(
        firestoreId: String,
        userId: String,
        userName: String,
        activityType: ActivityType,
        details: String,
    ): String {
        val activityId = UUID.randomUUID().toString()
        try {
            val activityData =
                hashMapOf(
                    "activityId" to activityId,
                    "userId" to userId,
                    "userName" to userName,
                    "activityType" to activityType,
                    "details" to details,
                    "timestamp" to LocalDateTime.now().format(dateTimeFormatter),
                )

            firestore
                .collection("households")
                .document(firestoreId)
                .collection("activities")
                .document(activityId)
                .set(activityData)
                .await()

            Log.d(TAG, "Activity logged to Firestore: $activityType by $userName")
            return activityId
        } catch (e: Exception) {
            Log.e(TAG, "Failed to log activity to Firestore", e)
            throw e
        }
    }

    private suspend fun getUserName(
        firestoreId: String,
        userId: String,
    ): String =
        try {
            val userDoc =
                firestore
                    .collection("households")
                    .document(firestoreId)
                    .collection("members")
                    .document(userId)
                    .get()
                    .await()
            userDoc.getString("userName") ?: "Unknown User"
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get username for userId: $userId", e)
            "Unknown User"
        }
}
