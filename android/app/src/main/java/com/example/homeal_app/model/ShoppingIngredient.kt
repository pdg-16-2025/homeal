package com.example.homeal_app.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "shopping_ingredients")
data class ShoppingIngredient(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val quantity: String,
    val isDone: Boolean = false, // Pour barrer visuellement
    val addedDate: Long = System.currentTimeMillis()
)