package com.example.homeal_app.data.repository

import com.example.homeal_app.data.local.dao.ShoppingDao
import com.example.homeal_app.data.remote.ApiService
import com.example.homeal_app.model.ShoppingIngredient
import com.example.homeal_app.model.Ingredient
import kotlinx.coroutines.flow.Flow
import android.util.Log

/**
 * Repository that handles data operations for shopping list
 * Manages local shopping list and remote ingredient search
 */
class ShoppingRepository(
    private val shoppingDao: ShoppingDao,
    private val apiService: ApiService
) {
    
    /**
     * Get all shopping ingredients from local database
     */
    fun getAllShoppingIngredients(): Flow<List<ShoppingIngredient>> {
        return shoppingDao.getAllShoppingIngredients()
    }
    
    /**
     * Add ingredient to shopping list (local storage)
     */
    suspend fun addShoppingIngredient(name: String, quantity: String) {
        val ingredient = ShoppingIngredient(
            name = name,
            quantity = quantity,
            isDone = false
        )
        shoppingDao.addShoppingIngredient(ingredient)
    }
    
    /**
     * Remove ingredient from shopping list by ID
     */
    suspend fun removeShoppingIngredient(ingredientId: Int) {
        shoppingDao.removeShoppingIngredientById(ingredientId)
    }
    
    /**
     * Toggle ingredient done status (for visual strikethrough)
     */
    suspend fun toggleIngredientDone(ingredientId: Int, isDone: Boolean) {
        shoppingDao.toggleIngredientDone(ingredientId, isDone)
    }
    
    /**
     * Remove all marked (completed) ingredients
     */
    suspend fun removeAllMarkedIngredients() {
        shoppingDao.removeAllMarkedIngredients()
    }
    
    /**
     * Search available ingredients from server for suggestions
     */
    suspend fun searchAvailableIngredients(query: String): List<Ingredient> {
        return try {
            if (query.isBlank()) {
                emptyList()
            } else {
                apiService.searchIngredients(search = query, limit = 20)
            }
        } catch (e: Exception) {
            Log.e("ShoppingRepository", "Error searching ingredients", e)
            emptyList()
        }
    }
    
    /**
     * Check if ingredient already exists in shopping list
     */
    suspend fun ingredientExists(name: String): Boolean {
        return shoppingDao.ingredientExists(name) > 0
    }
    
    /**
     * Add ingredient from server suggestion to shopping list
     */
    suspend fun addIngredientFromSuggestion(ingredient: Ingredient, quantity: String = "1 pcs") {
        if (!ingredientExists(ingredient.name)) {
            addShoppingIngredient(ingredient.name, quantity)
        }
    }
}