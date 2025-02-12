package com.lulakssoft.mygroceries.database.product

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.lulakssoft.mygroceries.database.household.Household

@Database(entities = [Product::class, Household::class], version = 4, exportSchema = false)
@TypeConverters(Converters::class, ImageBitmapConverters::class)
abstract class ProductDatabase : RoomDatabase() {
    abstract val productDao: ProductDao

    companion object {
        @Volatile
        private var INSTANCE: ProductDatabase? = null

        fun getInstance(context: Context): ProductDatabase {
            synchronized(this) {
                var instance = INSTANCE

                if (instance == null) {
                    instance =
                        Room
                            .databaseBuilder(
                                context.applicationContext,
                                ProductDatabase::class.java,
                                "product_database",
                            ).fallbackToDestructiveMigration()
                            .build()
                }

                return instance
            }
        }
    }
}
