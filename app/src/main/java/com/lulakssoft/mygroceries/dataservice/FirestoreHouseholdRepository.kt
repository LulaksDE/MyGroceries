package com.lulakssoft.mygroceries.dataservice

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.time.LocalDateTime

class FirestoreHouseholdRepository(
    private val context: Context,
) {
    private val firestore = FirebaseFirestore.getInstance()
    private val TAG = "FirestoreRepository"

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    suspend fun getUserHouseholds(userId: String): List<FirestoreHousehold> {
        if (!isNetworkAvailable()) {
            Log.e(TAG, "No internet connection available")
            return emptyList()
        }
        try {
            Log.d(TAG, "Fetching household memberships in firestore for user: $userId")
            val memberships =
                firestore
                    .collection("users")
                    .document(userId)
                    .collection("memberships")
                    .get()
                    .await()

            Log.d(TAG, "Found ${memberships.size()} memberships for user: $userId")
            val households = mutableListOf<FirestoreHousehold>()
            for (membership in memberships) {
                val householdId = membership.id
                val householdDoc =
                    firestore
                        .collection("households")
                        .document(householdId)
                        .get()
                        .await()

                if (householdDoc.exists()) {
                    Log.d(TAG, "Found household document for ID: $householdDoc")
                    try {
                        val household =
                            FirestoreHousehold(
                                firestoreId = householdId,
                                name = householdDoc.getString("householdName") ?: "",
                                isPrivate = householdDoc.getBoolean("isPrivate") ?: false,
                                createdByUserId = householdDoc.getString("createdByUserId") ?: "",
                                createdAt = householdDoc.getString("createdAt") ?: "",
                            )
                        households.add(household)
                    } catch (e: Exception) {
                        Log.e(TAG, "Error parsing household document: ${householdDoc.id}", e)
                    }
                }
            }
            return households
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching user households", e)
            return emptyList()
        }
    }

    suspend fun getHouseholdMembers(householdId: String): List<FirestoreHouseholdMember> {
        try {
            Log.d(TAG, "Fetching household members in firestore for household: $householdId")
            val membersSnapshot =
                firestore
                    .collection("households")
                    .document(householdId)
                    .collection("members")
                    .get()
                    .await()

            Log.d(TAG, "Fetched ${membersSnapshot.size()} members for household: $householdId")
            val members = mutableListOf<FirestoreHouseholdMember>()
            for (memberSnapshot in membersSnapshot) {
                Log.d(TAG, "Fetched member snapshot: $memberSnapshot")
                val memberData = memberSnapshot.data

                @Suppress("UNCHECKED_CAST")
                val joinedAtMap = memberData["joinedAt"] as? Map<String, Any>
                val joinedAt =
                    if (joinedAtMap != null) {
                        parseLocalDateTimeFromMap(joinedAtMap)
                    } else {
                        LocalDateTime.now() // Fallback
                    }
                val member =
                    FirestoreHouseholdMember(
                        householdId = householdId,
                        userId = memberData["userId"] as String,
                        userName = memberData["userName"] as String,
                        role = memberData["role"] as String,
                        joinedAt = joinedAt,
                        id = memberSnapshot.id,
                    )
                members.add(member)
                Log.d(TAG, "Fetched household member: $member")
            }
            return members
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching household members", e)
            return emptyList()
        }
    }

    suspend fun getHouseholdProducts(householdId: String): List<FirestoreProduct> {
        if (!isNetworkAvailable()) {
            Log.e(TAG, "No internet connection available")
            return emptyList()
        }
        try {
            Log.d(TAG, "Fetching household products in firestore for household: $householdId")
            val productsSnapshot =
                firestore
                    .collection("households")
                    .document(householdId)
                    .collection("products")
                    .get()
                    .await()

            Log.d(TAG, "Fetched ${productsSnapshot.size()} products for household: $householdId")
            val products = mutableListOf<FirestoreProduct>()
            for (productSnapshot in productsSnapshot) {
                val productData = productSnapshot.data
                val product =
                    FirestoreProduct(
                        productUuid = productSnapshot.id,
                        createdByUserId = productData["createdByUserId"] as String,
                        imageUrl = productData["imageUrl"] as String,
                        productBarcode = productData["productBarcode"] as String,
                        productBestBeforeDate = productData["productBestBeforeDate"] as String,
                        productBrand = productData["productBrand"] as String,
                        productEntryDate = productData["productEntryDate"] as String,
                        productName = productData["productName"] as String,
                        productQuantity = (productData["productQuantity"] as Long).toInt(),
                    )
                products.add(product)
                Log.d(TAG, "Fetched household product: $product")
            }
            return products
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching household products", e)
            return emptyList()
        }
    }

    private fun parseLocalDateTimeFromMap(dateMap: Map<String, Any>): LocalDateTime =
        try {
            val year = (dateMap["year"] as Long).toInt()
            val month = (dateMap["monthValue"] as Long).toInt()
            val day = (dateMap["dayOfMonth"] as Long).toInt()
            val hour = (dateMap["hour"] as Long).toInt()
            val minute = (dateMap["minute"] as Long).toInt()
            val second = (dateMap["second"] as Long).toInt()
            val nano = if (dateMap.containsKey("nano")) (dateMap["nano"] as Long).toInt() else 0

            LocalDateTime.of(year, month, day, hour, minute, second, nano)
        } catch (e: Exception) {
            Log.e("DateParsing", "Error while parsing date values: $dateMap", e)
            LocalDateTime.now() // Fallback
        }
}

data class FirestoreHousehold(
    var firestoreId: String = "",
    val name: String = "",
    val isPrivate: Boolean = false,
    val createdByUserId: String = "",
    val createdAt: String = "",
)

data class FirestoreHouseholdMember(
    val householdId: String = "",
    val userId: String = "",
    val userName: String = "",
    val role: String = "MEMBER",
    val joinedAt: LocalDateTime,
    val id: String = "",
)

data class FirestoreProduct(
    val productUuid: String = "",
    val createdByUserId: String = "",
    val imageUrl: String = "",
    val productBarcode: String = "",
    val productBestBeforeDate: String = "",
    val productBrand: String = "",
    val productEntryDate: String = "",
    val productName: String = "",
    val productQuantity: Int = 0,
)
