package com.lulakssoft.mygroceries.database.grocery

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Grocery::class], version = 1, exportSchema = false)
abstract class GroceryDatabase : RoomDatabase() {
    abstract val groceryDao: GroceryDao

    companion object {
        @Volatile
        private var INSTANCE: GroceryDatabase? = null

        fun getInstance(context: Context): GroceryDatabase {
            synchronized(this) {
                var instance = INSTANCE

                if (instance == null) {
                    instance =
                        Room
                            .databaseBuilder(
                                context.applicationContext,
                                GroceryDatabase::class.java,
                                "household_database",
                            ).fallbackToDestructiveMigration()
                            .build()
                }

                return instance
            }
        }
    }
}