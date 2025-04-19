package com.lulakssoft.mygroceries.database.product

class ProductRepository(
    val productDao: ProductDao,
) {
    val allProducts = productDao.selectAllProductsSortedByName()

    suspend fun insertProduct(product: Product) {
        productDao.insertProduct(product)
    }

    suspend fun deleteProduct(product: Product) {
        productDao.delete(product)
    }

    suspend fun deleteAll() {
        productDao.deleteAll()
    }
}
