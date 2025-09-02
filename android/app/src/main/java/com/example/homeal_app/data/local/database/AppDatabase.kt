package com.example.homeal_app.data.local.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import android.content.Context
import com.example.homeal_app.model.FridgeIngredient
import com.example.homeal_app.data.local.dao.FridgeDao
import com.example.homeal_app.model.PlannedMeal
import com.example.homeal_app.data.local.dao.PlannedMealDao

/**
 * Main database configuration for the app
 * Contains all local entities and provides DAOs
 */
@Database(
    entities = [FridgeIngredient::class, PlannedMeal::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    
    abstract fun fridgeDao(): FridgeDao
    abstract fun plannedMealDao(): PlannedMealDao
    
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "homeal_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}