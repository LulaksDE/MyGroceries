package com.lulakssoft.mygroceries.database.product

class ProductRepository(
    private val productDao: ProductDao,
) {
    val allProducts = productDao.selectAllProductsSortedByName()

    suspend fun insert(product: Product) {
        productDao.insert(product)
    }

    suspend fun delete(product: Product) {
        productDao.delete(product)
    }

    suspend fun deleteAll() {
        productDao.deleteAll()
    }
}
