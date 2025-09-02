// PlannedMealDao.kt
package com.example.homeal_app.data.local.dao

import androidx.room.*
import com.example.homeal_app.model.PlannedMeal
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for planned meals stored in calendar
 * Handles CRUD operations for meal planning
 */
@Dao
interface PlannedMealDao {
    
    /**
     * Get all planned meals for a specific date
     */
    @Query("SELECT * FROM planned_meals WHERE meal_date = :date ORDER BY mealType")
    fun getMealsForDate(date: String): Flow<List<PlannedMeal>>
    
    /**
     * Get all planned meals for the current week
     */
    @Query("SELECT * FROM planned_meals WHERE meal_date BETWEEN :startDate AND :endDate ORDER BY meal_date, mealType")
    fun getMealsForWeek(startDate: String, endDate: String): Flow<List<PlannedMeal>>
    
    /**
     * Add a planned meal to the calendar
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addPlannedMeal(meal: PlannedMeal)
    
    /**
     * Remove a planned meal from the calendar
     */
    @Delete
    suspend fun removePlannedMeal(meal: PlannedMeal)
    
    /**
     * Remove meal by date and type (useful for replacing meals)
     */
    @Query("DELETE FROM planned_meals WHERE meal_date = :date AND mealType = :mealType")
    suspend fun removeMealByDateAndType(date: String, mealType: String)
    
    /**
     * Get all planned meals (for statistics or overview)
     */
    @Query("SELECT * FROM planned_meals ORDER BY meal_date DESC")
    fun getAllPlannedMeals(): Flow<List<PlannedMeal>>
}