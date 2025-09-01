package com.example.homeal_app.model

class PlannedMeal(
    val id: Long = 0,
    val recipeId: Int,
    val name: String,
    val mealDate: String,
    val mealType: String = "Lunch"
)


