package com.example.homeal_app.data.local.dao

import androidx.room.*
import com.example.homeal_app.model.FridgeIngredient
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for fridge ingredients stored locally
 * Handles CRUD operations for the user's fridge inventory
 */
@Dao
interface FridgeDao {
    /**
     * Get all ingredients currently in the user's fridge
     * Returns a Flow for automatic UI updates when data changes
     */
    @Query("SELECT * FROM fridge_ingredients ORDER BY addedDate DESC")
    fun getAllFridgeIngredients(): Flow<List<FridgeIngredient>>
    
    /**
     * Add a new ingredient to the fridge
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addIngredient(ingredient: FridgeIngredient)
    
    /**
     * Remove an ingredient from the fridge
     */
    @Delete
    suspend fun removeIngredient(ingredient: FridgeIngredient)
    
    /**
     * Remove ingredient by its server ID (useful when removing duplicates)
     */
    @Query("DELETE FROM fridge_ingredients WHERE ingredientId = :ingredientId")
    suspend fun removeIngredientById(ingredientId: Int)
    
    /**
     * Get ingredients that are expiring soon (within next 3 days)
     */
    @Query("SELECT * FROM fridge_ingredients WHERE expirationDate BETWEEN :now AND :threeDaysFromNow")
    fun getExpiringSoon(now: Long, threeDaysFromNow: Long): Flow<List<FridgeIngredient>>
}