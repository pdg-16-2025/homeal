package com.example.homeal_app.model

data class RecipeIngredient(
    val id: Int,
    val recipe_id: Int,
    val ingredient_id: Int,
    val quantity: Double,
    val unit: String
)