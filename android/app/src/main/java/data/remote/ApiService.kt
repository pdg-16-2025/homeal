package com.example.homeal_app.data.remote

import com.example.homeal_app.model.Ingredient
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
     * Get ingredient information by barcode scanning
     * @param code Barcode string from scanner
     */
    @GET("scan")
    suspend fun scanBarcode(@Query("code") code: String): Ingredient
}