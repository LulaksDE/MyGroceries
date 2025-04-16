package com.lulakssoft.mygroceries.dataservice

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.lulakssoft.mygroceries.database.product.Household

class FirestoreManager {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    // Household-Synchronisierung
    fun syncHousehold(household: Household) {
        val householdMap =
            hashMapOf(
                "id" to household.id,
                "name" to household.householdName,
                "createdBy" to household.createdByUserId,
                "createdAt" to household.createdAt.toString(),
                "isPrivate" to household.isPrivate,
            )

        db
            .collection("households")
            .document(household.id.toString())
            .set(householdMap)
    }

    fun getHouseholdsForUser(
        userId: String,
        callback: (List<Household>) -> Unit,
    ) {
        db
            .collection("household_members")
            .whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener { members ->
                val householdIds = members.documents.map { it.getString("householdId") }

                if (householdIds.isEmpty()) {
                    callback(emptyList())
                    return@addOnSuccessListener
                }

                db
                    .collection("households")
                    .whereIn("id", householdIds)
                    .get()
                    .addOnSuccessListener { households ->
                        val result =
                            households.documents.map { doc ->
                                Household(
                                    id = doc.getLong("id")?.toInt() ?: 0,
                                    householdName = doc.getString("name") ?: "",
                                    createdByUserId = doc.getString("createdBy") ?: "",
                                    // Parse other fields as needed
                                )
                            }
                        callback(result)
                    }
            }
    }
}
