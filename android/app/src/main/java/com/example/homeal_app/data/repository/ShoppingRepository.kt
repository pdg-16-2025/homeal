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
    private val apiService: ApiService,
    private val calendarRepository: CalendarRepository,
    private val fridgeRepository: FridgeRepository
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
     * Remove all marked (completed) ingredients and add them to fridge
     */
    suspend fun removeAllMarkedIngredients() {
        try {
            // Get all marked ingredients before removing them
            val markedIngredients = shoppingDao.getMarkedIngredients()

            // Add each marked ingredient to fridge (avoiding duplicates)
            for (shoppingIngredient in markedIngredients) {
                // Parse quantity to extract numeric value and unit
                val (quantity, unit) = parseQuantityAndUnit(shoppingIngredient.quantity)

                // Check if ingredient already exists in fridge
                val existingFridgeIngredient = fridgeRepository.getIngredientByName(shoppingIngredient.name)

                if (existingFridgeIngredient == null) {
                    // Add new ingredient to fridge
                    fridgeRepository.addIngredient(
                        name = shoppingIngredient.name,
                        quantity = quantity,
                        unit = unit
                    )
                    Log.d("ShoppingRepository", "Added to fridge: ${shoppingIngredient.name}")
                } else {
                    Log.d("ShoppingRepository", "Ingredient already in fridge, skipping: ${shoppingIngredient.name}")
                }
            }

            // Remove marked ingredients from shopping list
            shoppingDao.removeAllMarkedIngredients()

        } catch (e: Exception) {
            Log.e("ShoppingRepository", "Error moving marked ingredients to fridge", e)
            throw e
        }
    }

    /**
     * Parse quantity string to extract numeric quantity and unit
     * Returns Pair of (quantity: Int, unit: String)
     */
    private fun parseQuantityAndUnit(quantityStr: String): Pair<Int, String> {
        return try {
            val cleaned = quantityStr.trim().lowercase()

            // Extract numeric part
            val numericQuantity = extractNumericQuantity(cleaned)?.toInt() ?: 1

            // Extract unit part - everything after the first number
            val unitRegex = """^\d+\.?\d*\s*(.*)$""".toRegex()
            val unitMatch = unitRegex.find(cleaned)
            val unit = unitMatch?.groupValues?.get(1)?.trim()?.ifBlank { "pcs" } ?: "pcs"

            Pair(numericQuantity, unit)
        } catch (e: Exception) {
            Log.w("ShoppingRepository", "Failed to parse quantity: $quantityStr, using defaults", e)
            Pair(1, "pcs")
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

    /**
     * Add shopping ingredient entity directly (for internal use)
     */
    private suspend fun addShoppingIngredient(ingredient: ShoppingIngredient) {
        shoppingDao.addShoppingIngredient(ingredient)
    }

    /**
     * Get shopping ingredient by name to check for existing items
     */
    suspend fun getShoppingIngredientByName(name: String): ShoppingIngredient? {
        return shoppingDao.getShoppingIngredientByName(name)
    }

    /**
     * Generate shopping list from planned meals for the next X days
     * Combines ingredients from recipes and avoids duplicates with existing items
     */
    suspend fun generateShoppingListFromPlannedMeals(numberOfDays: Int) {
        try {
            // Calculate date range
            val today = java.time.LocalDate.now()
            val startDate = today.toString()
            val endDate = today.plusDays(numberOfDays.toLong() - 1).toString()

            // Get planned meals for the date range
            val plannedMeals = calendarRepository.getPlannedMealsForDateRange(startDate, endDate)

            // Get unique recipe IDs
            val recipeIds = plannedMeals.map { it.recipeId }.distinct()

            // Collect all ingredients from recipes
            val allIngredients = mutableListOf<com.example.homeal_app.model.RecipeIngredient>()

            for (recipeId in recipeIds) {
                val recipeIngredients = calendarRepository.getRecipeIngredients(recipeId)
                allIngredients.addAll(recipeIngredients)
            }

            // Combine ingredients with same name
            val combinedIngredients = mutableMapOf<String, MutableList<com.example.homeal_app.model.RecipeIngredient>>()

            for (ingredient in allIngredients) {
                val name = ingredient.name.lowercase().trim()
                if (combinedIngredients.containsKey(name)) {
                    combinedIngredients[name]?.add(ingredient)
                } else {
                    combinedIngredients[name] = mutableListOf(ingredient)
                }
            }

            // Add to shopping list (avoiding duplicates with existing items)
            for ((ingredientName, ingredientList) in combinedIngredients) {
                // Check if ingredient already exists in shopping list
                val existingIngredient = getShoppingIngredientByName(ingredientName)

                if (existingIngredient == null) {
                    // Combine quantities and units for the same ingredient
                    val totalQuantity = combineQuantities(ingredientList)

                    val shoppingIngredient = ShoppingIngredient(
                        name = ingredientName,
                        quantity = totalQuantity,
                        isDone = false
                    )

                    addShoppingIngredient(shoppingIngredient)
                }
                // If ingredient already exists, we skip it to avoid duplicates
            }

        } catch (e: Exception) {
            android.util.Log.e("ShoppingRepository", "Error generating shopping list", e)
            throw e
        }
    }

    /**
     * Combine quantities and units for ingredients with the same name
     * Returns a formatted string with combined quantities
     */
    private fun combineQuantities(ingredients: List<com.example.homeal_app.model.RecipeIngredient>): String {
        if (ingredients.isEmpty()) return "1 pcs"

        // Group by unit
        val groupedByUnit = ingredients.groupBy { it.unit.lowercase().trim() }

        val combinedParts = mutableListOf<String>()

        for ((unit, ingredientsWithSameUnit) in groupedByUnit) {
            // Try to sum numeric quantities
            var totalNumericQuantity = 0.0
            var hasNumericQuantities = true

            for (ingredient in ingredientsWithSameUnit) {
                val quantityStr = ingredient.quantity.trim()
                val numericQuantity = extractNumericQuantity(quantityStr)

                if (numericQuantity != null) {
                    totalNumericQuantity += numericQuantity
                } else {
                    hasNumericQuantities = false
                    break
                }
            }

            if (hasNumericQuantities && totalNumericQuantity > 0) {
                // Format the total quantity nicely
                val formattedQuantity = if (totalNumericQuantity == totalNumericQuantity.toInt().toDouble()) {
                    totalNumericQuantity.toInt().toString()
                } else {
                    String.format("%.1f", totalNumericQuantity)
                }
                combinedParts.add("$formattedQuantity ${unit.ifEmpty { "pcs" }}")
            } else {
                // If we can't parse quantities, just list them
                val quantities = ingredientsWithSameUnit.map { it.quantity }
                combinedParts.add("${quantities.joinToString(" + ")} ${unit.ifEmpty { "pcs" }}")
            }
        }

        return combinedParts.joinToString(", ")
    }

    /**
     * Extract numeric quantity from a quantity string
     * Handles formats like "2", "1.5", "2 cups", "1/2", etc.
     */
    private fun extractNumericQuantity(quantityStr: String): Double? {
        return try {
            // Remove common words and extra spaces
            val cleaned = quantityStr.lowercase()
                .replace("cups?".toRegex(), "")
                .replace("tbsp?".toRegex(), "")
                .replace("tsp?".toRegex(), "")
                .replace("pieces?".toRegex(), "")
                .replace("pcs?".toRegex(), "")
                .trim()

            // Handle fractions like "1/2"
            if (cleaned.contains("/")) {
                val parts = cleaned.split("/")
                if (parts.size == 2) {
                    val numerator = parts[0].trim().toDoubleOrNull()
                    val denominator = parts[1].trim().toDoubleOrNull()
                    if (numerator != null && denominator != null && denominator != 0.0) {
                        return numerator / denominator
                    }
                }
            }

            // Handle mixed numbers like "1 1/2"
            val mixedNumberRegex = """(\d+)\s+(\d+)/(\d+)""".toRegex()
            val mixedMatch = mixedNumberRegex.find(cleaned)
            if (mixedMatch != null) {
                val whole = mixedMatch.groupValues[1].toDouble()
                val numerator = mixedMatch.groupValues[2].toDouble()
                val denominator = mixedMatch.groupValues[3].toDouble()
                return whole + (numerator / denominator)
            }

            // Extract first number from string
            val numberRegex = """(\d+\.?\d*)""".toRegex()
            val match = numberRegex.find(cleaned)
            match?.value?.toDoubleOrNull()

        } catch (e: Exception) {
            null
        }
    }
}
