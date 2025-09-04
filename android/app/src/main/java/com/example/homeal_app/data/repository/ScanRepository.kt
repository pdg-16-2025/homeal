package com.example.homeal_app.data.repository

import com.example.homeal_app.data.local.dao.FridgeDao
import com.example.homeal_app.data.local.dao.ShoppingDao
import com.example.homeal_app.data.remote.ApiService
import com.example.homeal_app.model.FridgeIngredient
import com.example.homeal_app.model.Ingredient
import android.util.Log

/**
 * Repository that handles barcode scanning operations
 * Integrates server lookup with local database updates
 */
class ScanRepository(
    private val fridgeDao: FridgeDao,
    private val shoppingDao: ShoppingDao,
    private val apiService: ApiService
) {
    
    /**
     * Scan barcode and lookup product from server
     */
    suspend fun scanBarcode(barcode: String): Ingredient? {
        return try {
            val result = apiService.scanBarcode(barcode)
            if (result.found && result.ingredient != null) {
                result.ingredient
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e("ScanRepository", "Error scanning barcode: $barcode", e)
            null
        }
    }
    
    /**
     * Add scanned ingredient to fridge
     */
    suspend fun addToFridge(ingredient: Ingredient, quantity: Int = 1, unit: String = "pcs") {
        try {
            val fridgeIngredient = FridgeIngredient(
                ingredientId = ingredient.id,
                name = ingredient.name,
                quantity = quantity,
                unit = unit
            )
            fridgeDao.addIngredient(fridgeIngredient)
        } catch (e: Exception) {
            Log.e("ScanRepository", "Error adding to fridge: ${ingredient.name}", e)
        }
    }
    
    /**
     * Mark ingredient as done in shopping list if it exists
     */
    suspend fun markInShoppingListIfExists(ingredientName: String): Boolean {
        return try {
            val existingItem = shoppingDao.getShoppingIngredientByName(ingredientName)
            if (existingItem != null) {
                shoppingDao.toggleIngredientDone(existingItem.id, true)
                true // Found and marked
            } else {
                false // Not found in shopping list
            }
        } catch (e: Exception) {
            Log.e("ScanRepository", "Error checking shopping list: $ingredientName", e)
            false
        }
    }
    
    /**
     * Complete scan process: add to fridge and mark in shopping list
     */
    suspend fun processScannedIngredient(ingredient: Ingredient, quantity: Int): ScanProcessResult {
        return try {
            // Add to fridge
            addToFridge(ingredient, quantity)
            
            // Check and mark in shopping list
            val wasInShoppingList = markInShoppingListIfExists(ingredient.name)
            
            ScanProcessResult(
                success = true,
                addedToFridge = true,
                markedInShoppingList = wasInShoppingList
            )
        } catch (e: Exception) {
            Log.e("ScanRepository", "Error processing scanned ingredient", e)
            ScanProcessResult(
                success = false,
                addedToFridge = false,
                markedInShoppingList = false,
                error = e.message
            )
        }
    }
}

/**
 * Result of scanning and processing an ingredient
 */
data class ScanProcessResult(
    val success: Boolean,
    val addedToFridge: Boolean,
    val markedInShoppingList: Boolean,
    val error: String? = null
)