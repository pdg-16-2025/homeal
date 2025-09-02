package com.example.homeal_app.data.local.dao

import androidx.room.*
import com.example.homeal_app.model.ShoppingIngredient
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for shopping list ingredients
 * Handles CRUD operations for shopping list management
 */
@Dao
interface ShoppingDao {
    
    /**
     * Get all shopping ingredients ordered by completion status and name
     */
    @Query("SELECT * FROM shopping_ingredients ORDER BY isDone ASC, name ASC")
    fun getAllShoppingIngredients(): Flow<List<ShoppingIngredient>>
    
    /**
     * Add ingredient to shopping list
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addShoppingIngredient(ingredient: ShoppingIngredient)
    
    /**
     * Remove ingredient from shopping list
     */
    @Delete
    suspend fun removeShoppingIngredient(ingredient: ShoppingIngredient)
    
    /**
     * Remove ingredient by ID
     */
    @Query("DELETE FROM shopping_ingredients WHERE id = :ingredientId")
    suspend fun removeShoppingIngredientById(ingredientId: Int)
    
    /**
     * Toggle ingredient done status
     */
    @Query("UPDATE shopping_ingredients SET isDone = :isDone WHERE id = :ingredientId")
    suspend fun toggleIngredientDone(ingredientId: Int, isDone: Boolean)
    
    /**
     * Remove all marked (done) ingredients
     */
    @Query("DELETE FROM shopping_ingredients WHERE isDone = 1")
    suspend fun removeAllMarkedIngredients()
    
    /**
     * Check if ingredient already exists in shopping list
     */
    @Query("SELECT COUNT(*) FROM shopping_ingredients WHERE name = :name")
    suspend fun ingredientExists(name: String): Int
    
    /**
     * Get shopping ingredient by name
     */
    @Query("SELECT * FROM shopping_ingredients WHERE name = :name LIMIT 1")
    suspend fun getShoppingIngredientByName(name: String): ShoppingIngredient?
}