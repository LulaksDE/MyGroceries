package com.lulakssoft.mygroceries.database.product

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [Product::class, Household::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class, ImageBitmapConverters::class)
abstract class DatabaseApp : RoomDatabase() {
    abstract val productDao: ProductDao
    abstract val householdDao: HouseholdDao

    companion object {
        @Volatile
        private var INSTANCE: DatabaseApp? = null

        fun getInstance(context: Context): DatabaseApp {
            synchronized(this) {
                var instance = INSTANCE

                if (instance == null) {
                    instance =
                        Room
                            .databaseBuilder(
                                context.applicationContext,
                                DatabaseApp::class.java,
                                "product_database",
                            ).fallbackToDestructiveMigration()
                            .build()
                }

                return instance
            }
        }
    }
}
