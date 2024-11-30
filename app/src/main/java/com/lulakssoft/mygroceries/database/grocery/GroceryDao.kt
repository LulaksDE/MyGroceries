package com.lulakssoft.mygroceries.database.grocery

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.lulakssoft.mygroceries.database.household.Household
import kotlinx.coroutines.flow.Flow

@Dao
interface GroceryDao {
    @Query("SELECT * FROM grocery_table ORDER BY groceryName ASC")
    fun selectAllGroceriesSortedByName(): Flow<List<Grocery>>

    @Insert
    suspend fun insert(grocery: Grocery)

    @Query("DELETE FROM grocery_table")
    suspend fun deleteAll()

    @Delete
    suspend fun delete(grocery: Grocery)
}