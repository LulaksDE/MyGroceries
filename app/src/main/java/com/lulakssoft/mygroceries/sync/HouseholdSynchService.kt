package com.lulakssoft.mygroceries.sync

import android.graphics.BitmapFactory
import android.util.Log
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import com.lulakssoft.mygroceries.database.household.Household
import com.lulakssoft.mygroceries.database.household.HouseholdMember
import com.lulakssoft.mygroceries.database.household.HouseholdRepository
import com.lulakssoft.mygroceries.database.household.MemberRole
import com.lulakssoft.mygroceries.database.product.Product
import com.lulakssoft.mygroceries.database.product.ProductRepository
import com.lulakssoft.mygroceries.dataservice.DataService
import com.lulakssoft.mygroceries.dataservice.FirestoreHousehold
import com.lulakssoft.mygroceries.dataservice.FirestoreHouseholdMember
import com.lulakssoft.mygroceries.dataservice.FirestoreHouseholdRepository
import com.lulakssoft.mygroceries.dataservice.FirestoreManager
import com.lulakssoft.mygroceries.dataservice.FirestoreProduct
import java.time.LocalDate
import java.time.LocalDateTime

class HouseholdSyncService(
    private val localHouseholdRepository: HouseholdRepository,
    private val localProductRepository: ProductRepository,
    private val firestoreRepository: FirestoreHouseholdRepository,
) {
    private val TAG = "HouseholdSyncService"
    private val dataService = DataService()

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
                val householdId = localHouseholdRepository.insertOrUpdateHousehold(household)

                try {
                    // Hole und synchronisiere Mitglieder
                    syncHouseholdMembers(remoteHousehold.firestoreId, householdId.toInt(), userId)
                } catch (e: Exception) {
                    Log.e(TAG, "Error syncing household members for household ID: ${remoteHousehold.firestoreId}", e)
                }
                try {
                    // Hole und synchronisiere Produkte
                    syncHouseholdProducts(remoteHousehold.firestoreId, householdId.toInt())
                } catch (e: Exception) {
                    Log.e(TAG, "Error syncing household products for household ID: ${remoteHousehold.firestoreId}", e)
                }
            }
            Log.d(TAG, "Household sync completed")
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
                localHouseholdRepository.insertOrUpdateMember(member)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error syncing household members", e)
        }
    }

    suspend fun syncHouseholdProducts(
        firestoreId: String,
        localHouseholdId: Int,
    ) {
        try {
            val localProducts = localProductRepository.getProductsByHouseholdId(localHouseholdId)
            for (product in localProducts) {
                if (!product.isSynced) {
                    Log.d(TAG, "Adding missing product to Firestore: ${product.productName}")
                    try {
                        FirestoreManager().addProductToHousehold(firestoreId, product)
                    } catch (e: Exception) {
                        Log.e(TAG, "Error adding product to Firestore: ${product.productName}", e)
                    }
                }
            }

            val remoteProducts = firestoreRepository.getHouseholdProducts(firestoreId)

            // Lösche lokale Produkte, die nicht mehr in Firestore vorhanden sind
            val remoteProductIds = remoteProducts.map { it.productUuid }
            val productsToDelete = localProducts.filter { it.productUuid !in remoteProductIds }
            for (product in productsToDelete) {
                localProductRepository.deleteProduct(product)
            }
            for (remoteProduct in remoteProducts) {
                val productImage = dataService.getProductImage(remoteProduct.imageUrl)
                val bitmap = BitmapFactory.decodeByteArray(productImage, 0, productImage.size)
                val imageBitmap = bitmap.asImageBitmap()
                val product = convertToLocalProduct(remoteProduct, localHouseholdId, firestoreId, imageBitmap)
                localProductRepository.insertOrUpdateProduct(product)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error syncing household products", e)
        }
    }

    private fun convertToLocalDateTime(date: String): LocalDateTime =
        try {
            LocalDateTime.parse(date)
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing date: $date", e)
            LocalDateTime.now()
        }

    private fun convertToLocalDate(date: String): LocalDate =
        try {
            LocalDate.parse(date)
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing date: $date", e)
            LocalDate.now()
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

    private fun convertToLocalProduct(
        remoteProduct: FirestoreProduct,
        localHouseholdId: Int,
        firestoreId: String,
        productImage: ImageBitmap,
    ): Product {
        val productBestBeforeDate = convertToLocalDate(remoteProduct.productBestBeforeDate)
        val productEntryDate = convertToLocalDateTime(remoteProduct.productEntryDate)
        return Product(
            id = 0, // Wird von Room automatisch zugewiesen
            householdId = localHouseholdId,
            firestoreId = firestoreId,
            productUuid = remoteProduct.productUuid,
            creatorId = remoteProduct.createdByUserId,
            productImageUrl = remoteProduct.imageUrl,
            productBarcode = remoteProduct.productBarcode,
            productBestBeforeDate = productBestBeforeDate,
            productBrand = remoteProduct.productBrand,
            productEntryDate = productEntryDate,
            productName = remoteProduct.productName,
            productQuantity = remoteProduct.productQuantity,
            productImage = productImage,
            isSynced = true,
        )
    }
}
