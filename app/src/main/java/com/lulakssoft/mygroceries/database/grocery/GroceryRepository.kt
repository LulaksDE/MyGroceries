package com.lulakssoft.mygroceries.database.grocery

class GroceryRepository(
    private val groceryDao: GroceryDao,
) {
    val allGroceries = groceryDao.selectAllGroceriesSortedByName()

    suspend fun insert(grocery: Grocery) {
        groceryDao.insert(grocery)
    }

    suspend fun delete(grocery: Grocery) {
        groceryDao.delete(grocery)
    }

    suspend fun deleteAll() {
        groceryDao.deleteAll()
    }
}