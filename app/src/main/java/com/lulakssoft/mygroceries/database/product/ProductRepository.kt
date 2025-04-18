package com.lulakssoft.mygroceries.database.product

import kotlinx.coroutines.flow.Flow

class ProductRepository(
    val productDao: ProductDao,
    val householdDao: HouseholdDao,
    val memberDao: HouseholdMemberDao,
) {
    val allProducts = productDao.selectAllProductsSortedByName()
    val allHouseholds = householdDao.selectAllHouseholdsSortedByName()

    suspend fun insertProduct(product: Product) {
        productDao.insertProduct(product)
    }

    suspend fun insertHousehold(household: Household) {
        householdDao.insertHousehold(household)
    }

    suspend fun insertHouseholdAndGetId(household: Household): Long = householdDao.insertHouseholdAndGetId(household)

    suspend fun getHouseholdById(householdId: Int): Household? = householdDao.getHouseholdById(householdId)

    suspend fun getHouseholdsByUserId(userId: String): Flow<List<Household>> = householdDao.getHouseholdsByUserId(userId)

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
