package com.example.homeal_app.model

data class Ingredient(
    val id: Int = 0,
    val name: String,
    val quantity: Int = 1,
    val unit: String = "pcs"
)