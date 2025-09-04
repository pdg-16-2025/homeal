// FridgeRepository.kt
package com.example.homeal_app.data.repository

import com.example.homeal_app.data.local.dao.FridgeDao
import com.example.homeal_app.data.remote.ApiService
import com.example.homeal_app.model.FridgeIngredient
import com.example.homeal_app.model.Ingredient
import kotlinx.coroutines.flow.Flow
import android.util.Log

/**
 * Repository that handles data operations for the fridge feature
 * Manages both local fridge storage and remote ingredient search
 */
class FridgeRepository(
    private val fridgeDao: FridgeDao,
    private val apiService: ApiService
) {

    /**
     * Get all ingredients currently stored in the user's fridge
     * Returns Flow for automatic UI updates when data changes
     */
    fun getAllIngredients(): Flow<List<FridgeIngredient>> {
        return fridgeDao.getAllFridgeIngredients()
    }

    /**
     * Add ingredient to fridge (manual entry with just name)
     */
    suspend fun addIngredient(name: String, quantity: Int, unit: String) {
        try {
            val fridgeIngredient = FridgeIngredient(
                ingredientId = 0, // 0 for manually added ingredients
                name = name,
                quantity = quantity,
                unit = unit
            )
            fridgeDao.addIngredient(fridgeIngredient)
            Log.d("FridgeRepository", "Added manual ingredient: $name")
        } catch (e: Exception) {
            Log.e("FridgeRepository", "Error adding ingredient: $name", e)
            throw e
        }
    }

    /**
     * Add ingredient from server suggestion
     */
    suspend fun addIngredientFromSuggestion(
        serverIngredient: Ingredient,
        quantity: Int = 1,
        unit: String = "pcs"
    ) {
        try {
            // Check if ingredient already exists
            if (ingredientExists(serverIngredient.name)) {
                Log.d("FridgeRepository", "Ingredient already exists: ${serverIngredient.name}")
                return
            }

            val fridgeIngredient = FridgeIngredient(
                ingredientId = serverIngredient.id, // Server ID
                name = serverIngredient.name,
                quantity = quantity,
                unit = unit
            )
            fridgeDao.addIngredient(fridgeIngredient)
            Log.d("FridgeRepository", "Added server ingredient: ${serverIngredient.name}")
        } catch (e: Exception) {
            Log.e("FridgeRepository", "Error adding server ingredient: ${serverIngredient.name}", e)
            throw e
        }
    }

    /**
     * Remove ingredient from fridge
     */
    suspend fun removeIngredient(ingredient: FridgeIngredient) {
        try {
            fridgeDao.removeIngredient(ingredient)
            Log.d("FridgeRepository", "Removed ingredient: ${ingredient.name}")
        } catch (e: Exception) {
            Log.e("FridgeRepository", "Error removing ingredient: ${ingredient.name}", e)
            throw e
        }
    }

    /**
     * Update ingredient quantity and unit
     */
    suspend fun updateIngredientQuantity(name: String, quantity: Int, unit: String) {
        try {
            fridgeDao.updateIngredientQuantity(name, quantity, unit)
            Log.d("FridgeRepository", "Updated ingredient: $name to $quantity $unit")
        } catch (e: Exception) {
            Log.e("FridgeRepository", "Error updating ingredient: $name", e)
            throw e
        }
    }

    /**
     * Search available ingredients from server for suggestions
     */
    suspend fun searchAvailableIngredients(query: String): List<Ingredient> {
        return try {
            if (query.isBlank()) {
                emptyList()
            } else {
                val results = apiService.searchIngredients(search = query, limit = 20)
                Log.d("FridgeRepository", "Found ${results.size} ingredients for query: $query")
                results
            }
        } catch (e: Exception) {
            Log.e("FridgeRepository", "Error searching ingredients: $query", e)
            emptyList()
        }
    }

    /**
     * Check if ingredient already exists in fridge (by name)
     */
    private suspend fun ingredientExists(name: String): Boolean {
        return try {
            fridgeDao.ingredientExists(name) > 0
        } catch (e: Exception) {
            Log.e("FridgeRepository", "Error checking if ingredient exists: $name", e)
            false
        }
    }

    /**
     * Get ingredient by name (used for updating existing ingredients)
     */
    suspend fun getIngredientByName(name: String): FridgeIngredient? {
        return try {
            fridgeDao.getIngredientByName(name)
        } catch (e: Exception) {
            Log.e("FridgeRepository", "Error getting ingredient by name: $name", e)
            null
        }
    }

    /**
     * Clear all ingredients from fridge (for testing purposes)
     */
    suspend fun clearAllIngredients() {
        try {
            fridgeDao.clearAllIngredients()
            Log.d("FridgeRepository", "Cleared all fridge ingredients")
        } catch (e: Exception) {
            Log.e("FridgeRepository", "Error clearing all ingredients", e)
            throw e
        }
    }

    /**
     * Get count of ingredients in fridge
     */
    suspend fun getIngredientCount(): Int {
        return try {
            fridgeDao.getIngredientCount()
        } catch (e: Exception) {
            Log.e("FridgeRepository", "Error getting ingredient count", e)
            0
        }
    }

    /**
     * Remove recipe ingredients from fridge when recipe is cooked
     * Ignores ingredients that are not found in the fridge
     */
    suspend fun removeRecipeIngredients(recipeIngredients: List<com.example.homeal_app.model.RecipeIngredient>) {
        try {
            for (ingredient in recipeIngredients) {
                val fridgeIngredient = fridgeDao.getIngredientByName(ingredient.name)
                if (fridgeIngredient != null) {
                    // Remove the ingredient from fridge
                    fridgeDao.removeIngredient(fridgeIngredient)
                    Log.d("FridgeRepository", "Removed ingredient from fridge: ${ingredient.name}")
                } else {
                    Log.d("FridgeRepository", "Ingredient not found in fridge, skipping: ${ingredient.name}")
                }
            }
        } catch (e: Exception) {
            Log.e("FridgeRepository", "Error removing recipe ingredients from fridge", e)
            throw e
        }
    }
}
