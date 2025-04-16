package com.lulakssoft.mygroceries.database.product

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.lulakssoft.mygroceries.database.household.HouseholdInvitation
import com.lulakssoft.mygroceries.database.household.HouseholdMember

@Database(
    entities = [Product::class, Household::class, HouseholdMember::class, HouseholdInvitation::class],
    version = 1, // Neue Version ohne Migration
    exportSchema = false,
)
@TypeConverters(Converters::class, ImageBitmapConverters::class)
abstract class DatabaseApp : RoomDatabase() {
    abstract val productDao: ProductDao
    abstract val householdDao: HouseholdDao
    abstract val householdMemberDao: HouseholdMemberDao
    abstract val householdInvitationDao: HouseholdInvitationDao

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
                                "my_groceries_database",
                            ).build()
                }
                return instance
            }
        }
    }
}
