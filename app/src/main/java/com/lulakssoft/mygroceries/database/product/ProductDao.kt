package com.lulakssoft.mygroceries.database.product

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import java.time.LocalDate
import java.time.LocalDateTime

@Dao
interface ProductDao {
    @Query("SELECT * FROM product_table WHERE householdId = :householdId ORDER BY productName ASC")
    suspend fun selectProductsByHouseholdId(householdId: Int): List<Product>

    @Query("SELECT * FROM product_table WHERE firestoreId = :firestoreId ORDER BY productName ASC")
    suspend fun selectProductsByFirestoreId(firestoreId: String): List<Product>

    @Query("SELECT * FROM product_table WHERE productUuid = :productUuid ORDER BY productName ASC")
    suspend fun selectProductByUuid(productUuid: String): Product?

    @Insert
    suspend fun insertProduct(product: Product)

    @Query(
        "UPDATE product_table SET productName = :productName, productBrand = :productBrand, productBarcode = :productBarcode, productQuantity = :productQuantity, productBestBeforeDate = :productBestBeforeDate, productEntryDate = :productEntryDate, productImageUrl = :productImageUrl, isSynced = :isSynced WHERE productUuid = :uuid",
    )
    suspend fun updateProduct(
        uuid: String,
        productName: String,
        productBrand: String,
        productBarcode: String,
        productQuantity: Int,
        productBestBeforeDate: LocalDate,
        productEntryDate: LocalDateTime,
        productImageUrl: String? = null,
        isSynced: Boolean,
    )

    @Query("DELETE FROM product_table")
    suspend fun deleteAll()

    @Delete
    suspend fun delete(product: Product)

    @Query(
        "SELECT * FROM product_table WHERE productBestBeforeDate <= :date AND productBestBeforeDate >= :today ORDER BY productBestBeforeDate ASC",
    )
    suspend fun getProductsExpiringBefore(
        date: LocalDate,
        today: LocalDate = LocalDate.now(),
    ): List<Product>
}
