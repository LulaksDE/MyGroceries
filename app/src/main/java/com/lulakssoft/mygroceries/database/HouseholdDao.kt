package com.lulakssoft.mygroceries.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface HouseholdDao {
    @Query("SELECT * FROM household_table ORDER BY householdName ASC")
    fun selectAllHouseholdsSortedByName(): Flow<List<Household>>

    @Insert
    suspend fun insert(household: Household)

    @Query("DELETE FROM household_table")
    suspend fun deleteAll()

    @Delete
    suspend fun delete(household: Household)
}
