package com.example.homeal_app.data.repository

import com.example.homeal_app.data.local.dao.FridgeDao
import com.example.homeal_app.data.remote.ApiService
import com.example.homeal_app.model.Ingredient
import com.example.homeal_app.model.FridgeIngredient
import kotlinx.coroutines.flow.Flow

/**
 * Repository that handles data operations for the fridge feature
 * Acts as a single source of truth, managing both local and remote data sources
 */
class FridgeRepository(
    private val fridgeDao: FridgeDao,
    private val apiService: ApiService
) {
    
    /**
     * Get all ingredients currently stored in the user's fridge
     * Data comes from local database and updates automatically via Flow
     */
    fun getFridgeIngredients(): Flow<List<FridgeIngredient>> {
        return fridgeDao.getAllFridgeIngredients()
    }
    
    /**
     * Add an ingredient to the user's fridge (local storage)
     * @param ingredient The ingredient from server database
     * @param quantity Amount of ingredient
     * @param unit Unit of measurement (e.g., "kg", "pieces", "liters")
     * @param expirationDate Optional expiration date
     */
    suspend fun addToFridge(
        ingredient: Ingredient,
        quantity: Int,
        unit: String,
        expirationDate: Long? = null,
        barcode: String? = null
    ) {
        val fridgeIngredient = FridgeIngredient(
            ingredientId = ingredient.id,
            name = ingredient.name,
            quantity = quantity,
            unit = unit,
            expirationDate = expirationDate,
            barcode = barcode
        )
        fridgeDao.addIngredient(fridgeIngredient)
    }
    
    /**
     * Remove an ingredient from the fridge
     */
    suspend fun removeFromFridge(fridgeIngredient: FridgeIngredient) {
        fridgeDao.removeIngredient(fridgeIngredient)
    }
    
    /**
     * Search for available ingredients on the server
     * Used to provide suggestions when user manually adds ingredients
     * @param query Search term entered by user
     */
    suspend fun searchAvailableIngredients(query: String): List<Ingredient> {
        return if (query.isBlank()) {
            emptyList()
        } else {
            try {
                apiService.searchIngredients(search = query, limit = 20)
            } catch (e: Exception) {
                // Log error or handle network issues
                emptyList()
            }
        }
    }
    
    /**
     * Scan barcode to get ingredient information from server
     */
    suspend fun scanIngredient(barcode: String): Ingredient? {
        return try {
            apiService.scanBarcode(barcode)
        } catch (e: Exception) {
            // Handle scanning errors (invalid barcode, network issues, etc.)
            null
        }
    }
    
    /**
     * Get ingredients that are expiring soon
     */
    fun getExpiringSoonIngredients(): Flow<List<FridgeIngredient>> {
        val now = System.currentTimeMillis()
        val threeDaysFromNow = now + (3 * 24 * 60 * 60 * 1000L) // 3 days in milliseconds
        return fridgeDao.getExpiringSoon(now, threeDaysFromNow)
    }
}