package com.lulakssoft.mygroceries.database.product

class ProductRepository(
    val productDao: ProductDao,
) {
    val allProducts = productDao.selectAllProductsSortedByName()

    suspend fun insertProduct(product: Product) {
        productDao.insertProduct(product)
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
