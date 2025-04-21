package com.lulakssoft.mygroceries.database.product

import com.lulakssoft.mygroceries.dataservice.FirestoreManager

class ProductRepository(
    val productDao: ProductDao,
) {
    val allProducts = productDao.selectAllProductsSortedByName()
    private val firestoreManager = FirestoreManager()

    suspend fun insertProduct(product: Product) {
        productDao.insertProduct(product)

        firestoreManager.addProductToHousehold(product.firestoreId, product)
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
