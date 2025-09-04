// CalendarRepository.kt
package com.example.homeal_app.data.repository

import com.example.homeal_app.data.local.dao.PlannedMealDao
import com.example.homeal_app.data.remote.ApiService
import com.example.homeal_app.model.PlannedMeal
import com.example.homeal_app.model.Recipe
import com.example.homeal_app.model.RecipeDetails
import com.example.homeal_app.model.RecipeIngredient
import kotlinx.coroutines.flow.Flow
import android.util.Log

/**
 * Repository that handles data operations for meal planning and recipes
 * Manages both local meal planning and remote recipe data
 */
class CalendarRepository(
    private val plannedMealDao: PlannedMealDao,
    private val apiService: ApiService
) {
    
    /**
     * Get planned meals for a specific date from local database
     */
    fun getMealsForDate(date: String): Flow<List<PlannedMeal>> {
        return plannedMealDao.getMealsForDate(date)
    }
    
    /**
     * Get planned meals for a week range from local database
     */
    fun getMealsForWeek(startDate: String, endDate: String): Flow<List<PlannedMeal>> {
        return plannedMealDao.getMealsForWeek(startDate, endDate)
    }
    
    /**
     * Add a meal to the calendar (local storage)
     */
    suspend fun addMealToCalendar(
        recipeId: Int,
        recipeName: String,
        date: String,
        mealType: String
    ) {
        Log.d("CalendarRepository", "addMealToCalendar called: recipeId=$recipeId, recipeName=$recipeName, date=$date, mealType=$mealType")
        val plannedMeal = PlannedMeal(
            recipeId = recipeId,
            recipeName = recipeName,
            mealDate = date,
            mealType = mealType
        )
        Log.d("CalendarRepository", "About to insert planned meal into database: $plannedMeal")
        plannedMealDao.addPlannedMeal(plannedMeal)
        Log.d("CalendarRepository", "Successfully inserted planned meal into database")
    }
    
    /**
     * Remove a meal from the calendar
     */
    suspend fun removeMealFromCalendar(meal: PlannedMeal) {
        plannedMealDao.removePlannedMeal(meal)
    }
    
    /**
     * Remove a meal by date and type (proper deletion)
     */
    suspend fun removeMealByDateAndType(date: String, mealType: String) {
        Log.d("CalendarRepository", "removeMealByDateAndType called: date=$date, mealType=$mealType")
        plannedMealDao.removeMealByDateAndType(date, mealType)
        Log.d("CalendarRepository", "Successfully removed meal by date and type")
    }

    /**
     * Replace a meal for a specific date and type
     */
    suspend fun replaceMeal(
        date: String,
        mealType: String,
        newRecipeId: Int,
        newRecipeName: String
    ) {
        Log.d("CalendarRepository", "replaceMeal called: date=$date, mealType=$mealType, newRecipeId=$newRecipeId, newRecipeName='$newRecipeName'")
        Log.d("CalendarRepository", "About to remove meal by date and type")
        plannedMealDao.removeMealByDateAndType(date, mealType)
        Log.d("CalendarRepository", "Successfully removed existing meal")
        Log.d("CalendarRepository", "About to add new meal")
        addMealToCalendar(newRecipeId, newRecipeName, date, mealType)
        Log.d("CalendarRepository", "Successfully added replacement meal")
    }
    
    /**
     * Search recipes from server for meal selection
     */
    suspend fun searchRecipes(query: String): List<Recipe> {
        return try {
            if (query.isBlank()) {
                // Get recommendations if no search query
                apiService.getRecommendations()
            } else {
                apiService.searchRecipes(search = query, limit = 20)
            }
        } catch (e: Exception) {
            Log.e("CalendarRepository", "Error searching recipes", e)
            emptyList()
        }
    }
    
    /**
     * Get detailed recipe information from server
     */
    suspend fun getRecipeDetails(recipeId: Int): RecipeDetails? {
        return try {
            apiService.getRecipeDetails(recipeId)
        } catch (e: Exception) {
            Log.e("CalendarRepository", "Error fetching recipe details", e)
            null
        }
    }
    
    /**
     * Get recipe recommendations from server
     */
    suspend fun getRecommendations(): List<Recipe> {
        return try {
            apiService.getRecommendations(
                type = "random",
                data = "{}",
                number = 15
            )
        } catch (e: Exception) {
            Log.e("CalendarRepository", "Error fetching recommendations", e)
            emptyList()
        }
    }

    /**
     * Get recipe ingredients with names from the recipe-ingredients endpoint
     * This endpoint returns ingredients with their names, quantities, and units
     */
    suspend fun getRecipeIngredients(recipeId: Int): List<RecipeIngredient> {
        return try {
            apiService.getRecipeIngredients(recipeId)
        } catch (e: Exception) {
            Log.e("CalendarRepository", "Error fetching recipe ingredients for recipe: $recipeId", e)
            emptyList()
        }
    }
}
