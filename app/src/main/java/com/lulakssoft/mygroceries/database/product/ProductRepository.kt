package com.lulakssoft.mygroceries.database.product

import android.util.Log
import com.lulakssoft.mygroceries.dataservice.FirestoreManager

class ProductRepository(
    val productDao: ProductDao,
) {
    val allProducts = productDao.selectAllProductsSortedByName()
    private val firestoreManager = FirestoreManager()

    suspend fun insertProduct(product: Product) {
        Log.d("ProductRepository", "Called insertProduct with product: ${product.productName}")
        productDao.insertProduct(product)

        firestoreManager.addProductToHousehold(product.firestoreId, product)
    }

    suspend fun insertOrUpdateProduct(product: Product) {
        val existingProduct = productDao.selectProductByUuid(product.productUuid)
        if (existingProduct == null) {
            productDao.insertProduct(product)
            Log.d("ProductRepository", "Inserted new product to local database: ${product.productName}")
        } else {
            productDao.updateProduct(
                existingProduct.id,
                product.productName,
                product.productBrand,
                product.productBarcode,
                product.productQuantity,
                product.productBestBeforeDate,
                product.productEntryDate,
                product.productImageUrl,
            )
        }
    }

    suspend fun getProductsByHouseholdId(householdId: Int) = productDao.selectProductsByHouseholdId(householdId)

    suspend fun getProductsByFirestoreId(firestoreId: String) = productDao.selectProductsByFirestoreId(firestoreId)

    suspend fun deleteProduct(product: Product) {
        productDao.delete(product)
    }

    suspend fun deleteAll() {
        productDao.deleteAll()
    }
}
