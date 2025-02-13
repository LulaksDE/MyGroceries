package com.lulakssoft.mygroceries.database.product

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {
    @Query("SELECT * FROM product_table ORDER BY productName ASC")
    fun selectAllProductsSortedByName(): Flow<List<Product>>

    @Query("SELECT * FROM product_table WHERE householdId = :householdId ORDER BY productName ASC")
    fun selectProductsByHouseholdId(householdId: Int): Flow<List<Product>>

    @Insert
    suspend fun insertProduct(product: Product)

    @Query("DELETE FROM product_table")
    suspend fun deleteAll()

    @Delete
    suspend fun delete(product: Product)
}

@Dao
interface HouseholdDao {
    @Query("SELECT * FROM household_table ORDER BY householdName ASC")
    fun selectAllHouseholdsSortedByName(): Flow<List<Household>>

    @Insert
    suspend fun insertHousehold(household: Household)

    @Query("DELETE FROM household_table")
    suspend fun deleteAll()

    @Delete
    suspend fun delete(household: Household)
}
