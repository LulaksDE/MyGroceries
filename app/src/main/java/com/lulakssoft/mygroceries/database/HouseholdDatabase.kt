package com.lulakssoft.mygroceries.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Household::class], version = 1, exportSchema = false)
abstract class HouseholdDatabase : RoomDatabase() {
    abstract val householdDao: HouseholdDao

    companion object {
        @Volatile
        private var INSTANCE: HouseholdDatabase? = null

        fun getInstance(context: Context): HouseholdDatabase {
            synchronized(this) {
                var instance = INSTANCE

                if (instance == null) {
                    instance =
                        Room
                            .databaseBuilder(
                                context.applicationContext,
                                HouseholdDatabase::class.java,
                                "household_database",
                            ).fallbackToDestructiveMigration()
                            .build()
                }

                return instance
            }
        }
    }
}
