package com.lulakssoft.mygroceries.dataservice

import android.content.ContentValues.TAG
import android.util.Log
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.lulakssoft.mygroceries.database.household.Household
import com.lulakssoft.mygroceries.database.household.HouseholdInvitation
import com.lulakssoft.mygroceries.database.product.Product
import kotlinx.coroutines.tasks.await
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

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
            Log.d("FirestoreManager", "New member synchronized to Firestore")
        } catch (e: Exception) {
            Log.e("FirestoreManager", "Failed to sync new member: ${e.message}")
        }
    }

    suspend fun addProductToHousehold(
        firestoreId: String,
        product: Product,
    ) {
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

            Log.d(TAG, "Product added successfully to firestore")
        } catch (e: Exception) {
            Log.e(TAG, "Error while adding product to firestore", e)
            throw e
        }
    }

    suspend fun deleteProductFromHousehold(
        firestoreId: String,
        productUuid: String,
    ) {
        try {
            firestore
                .collection("households")
                .document(firestoreId)
                .collection("products")
                .document(productUuid)
                .delete()
                .await()
            Log.d(TAG, "Product deleted successfully from firestore")
        } catch (e: Exception) {
            Log.e(TAG, "Error while deleting product from firestore", e)
            throw e
        }
    }
}
