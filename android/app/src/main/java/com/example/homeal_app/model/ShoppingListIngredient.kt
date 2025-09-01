package com.example.homeal_app.model

data class ShoppingListIngredient(
    val id: Long = 0,
    val shoppingListId: Long,
    val ingredientName: String,
    val quantity: Double,
    val unit: String = "pcs"
)