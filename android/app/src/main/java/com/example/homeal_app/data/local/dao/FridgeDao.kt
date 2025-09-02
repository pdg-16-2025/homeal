// FridgeDao.kt
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
     * Remove ingredient by local ID
     */
    @Query("DELETE FROM fridge_ingredients WHERE id = :id")
    suspend fun removeIngredientById(id: Int)

    /**
     * Update ingredient quantity and unit
     */
    @Query("UPDATE fridge_ingredients SET quantity = :quantity, unit = :unit WHERE name = :name")
    suspend fun updateIngredientQuantity(name: String, quantity: Int, unit: String)

    /**
     * Check if ingredient already exists in fridge (by name)
     */
    @Query("SELECT COUNT(*) FROM fridge_ingredients WHERE name = :name")
    suspend fun ingredientExists(name: String): Int

    /**
     * Get ingredient by name
     */
    @Query("SELECT * FROM fridge_ingredients WHERE name = :name LIMIT 1")
    suspend fun getIngredientByName(name: String): FridgeIngredient?

    /**
     * Get ingredient by server ID (useful for avoiding duplicates from server)
     */
    @Query("SELECT * FROM fridge_ingredients WHERE ingredientId = :ingredientId AND ingredientId > 0 LIMIT 1")
    suspend fun getIngredientByServerId(ingredientId: Int): FridgeIngredient?

    /**
     * Clear all ingredients from fridge (for testing/reset purposes)
     */
    @Query("DELETE FROM fridge_ingredients")
    suspend fun clearAllIngredients()

    /**
     * Get total count of ingredients in fridge
     */
    @Query("SELECT COUNT(*) FROM fridge_ingredients")
    suspend fun getIngredientCount(): Int

    /**
     * Get ingredients by partial name match (for search functionality)
     */
    @Query("SELECT * FROM fridge_ingredients WHERE name LIKE '%' || :searchQuery || '%' ORDER BY name ASC")
    fun searchIngredients(searchQuery: String): Flow<List<FridgeIngredient>>

    /**
     * Get ingredients added in the last N days
     */
    @Query("SELECT * FROM fridge_ingredients WHERE addedDate >= :sinceDate ORDER BY addedDate DESC")
    fun getRecentlyAdded(sinceDate: Long): Flow<List<FridgeIngredient>>

    /**
     * Update ingredient name (useful for corrections)
     */
    @Query("UPDATE fridge_ingredients SET name = :newName WHERE id = :id")
    suspend fun updateIngredientName(id: Int, newName: String)

    /**
     * Get ingredients grouped by unit
     */
    @Query("SELECT * FROM fridge_ingredients ORDER BY unit ASC, name ASC")
    fun getIngredientsGroupedByUnit(): Flow<List<FridgeIngredient>>
}