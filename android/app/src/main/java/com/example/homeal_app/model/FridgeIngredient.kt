package com.example.homeal_app.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entity representing an ingredient stored in the user's fridge (local database)
 * This is separate from the server's ingredient list
 */
@Entity(tableName = "fridge_ingredients")
data class FridgeIngredient(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val ingredientId: Int, // Reference to server ingredient ID
    val name: String,
    val quantity: Int,
    val unit: String,
    val addedDate: Long = System.currentTimeMillis(),
    val expirationDate: Long? = null,
    val barcode: String? = null
)