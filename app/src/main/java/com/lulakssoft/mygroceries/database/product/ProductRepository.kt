package com.lulakssoft.mygroceries.database.product

class ProductRepository(
    private val productDao: ProductDao,
    private val householdDao: HouseholdDao,
) {
    val allProducts = productDao.selectAllProductsSortedByName()
    val allHouseholds = householdDao.selectAllHouseholdsSortedByName()

    suspend fun insertProduct(product: Product) {
        productDao.insertProduct(product)
    }

    suspend fun insertHousehold(household: Household) {
        householdDao.insertHousehold(household)
    }

    suspend fun deleteProduct(product: Product) {
        productDao.delete(product)
    }

    suspend fun deleteHousehold(household: Household) {
        householdDao.delete(household)
    }

    suspend fun deleteAll() {
        productDao.deleteAll()
    }
}
