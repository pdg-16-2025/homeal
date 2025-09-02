package com.example.homeal_app.data.remote

import com.example.homeal_app.model.Ingredient
import com.example.homeal_app.model.Recipe
import com.example.homeal_app.model.RecipeDetails
import com.example.homeal_app.model.RecipeIngredient
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * API service to communicate with the Homeal server
 * Server URL: http://138.199.171.173/ (may change in future)
 */
interface ApiService {
    
    /**
     * Search for available ingredients on the server
     * Used for ingredient suggestions when manually adding items
     * @param search Search query to filter ingredients
     * @param limit Maximum number of results to return
     */
    @GET("ingredients")
    suspend fun searchIngredients(
        @Query("search") search: String? = null,
        @Query("limit") limit: Int = 10
    ): List<Ingredient>

    /**
     * Search for recipes based on ingredients or recipe name
     * @param search Search query to filter recipes
     * @param limit Maximum number of results to return
     */
    @GET("search-recipes")
    suspend fun searchRecipes(
        @Query("search") search: String? = null,
        @Query("limit") limit: Int = 20
    ): List<Recipe>
    
    /**
     * Get detailed information about a specific recipe
     * @param recipeId Unique identifier of the recipe
     */
    @GET("recipe")
    suspend fun getRecipeDetails(@Query("id") recipeId: Int): RecipeDetails
    
    /**
     * Get recipe recommendations based on user preferences
     * @param type Type of recommendations (e.g., "random")
     * @param data Additional data for recommendations in JSON format
     * @param number Number of recommendations to return
     */
    @GET("recommendations")
    suspend fun getRecommendations(
        @Query("type") type: String = "random",
        @Query("data") data: String = "{}",
        @Query("number") number: Int = 10
    ): List<Recipe>
    
    /**
     * Get ingredients required for a specific recipe
     * @param recipeId Unique identifier of the recipe
     */
    @GET("recipe-ingredients")
    suspend fun getRecipeIngredients(@Query("recipe_id") recipeId: Int): List<RecipeIngredient>
    
    /**
     * Get ingredient information by barcode scanning
     * @param code Barcode string from scanner
     */
    @GET("scan")
    suspend fun scanBarcode(@Query("code") code: String): Ingredient
}