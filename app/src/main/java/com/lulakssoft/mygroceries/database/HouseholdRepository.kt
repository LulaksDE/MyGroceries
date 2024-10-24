package com.lulakssoft.mygroceries.database

class HouseholdRepository(
    private val householdDao: HouseholdDao,
) {
    val allHouseholds = householdDao.selectAllHouseholdsSortedByName()

    suspend fun insert(household: Household) {
        householdDao.insert(household)
    }

    suspend fun delete(household: Household) {
        householdDao.delete(household)
    }

    suspend fun deleteAll() {
        householdDao.deleteAll()
    }
}
