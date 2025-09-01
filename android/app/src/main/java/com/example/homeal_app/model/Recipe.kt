package com.example.homeal_app.model

data class Recipe (
    val id: Int,
    val name: String,
    val url: String?,
    val author_id: Int?,
    val cook_time: Int?,
    val prep_time: Int?,
    val total_time: Int?,
    val description: String?,
    val images: String?,
    val category: String?,
    val keywords: String?,
    val aggregated_rating: Double?,
    val review_count: Int?,
    val calories: Double?,
    val fat_content: Double?,
    val saturated_fat_content: Double?,
    val cholesterol_content: Double?,
    val sodium_content: Double?,
    val carbohydrate_content: Double?,
    val fiber_content: Double?,
    val sugar_content: Double?,
    val protein_content: Double?,
    val recipe_servings: Int?,
    val recipe_yield: String?,
    val recipe_instructions: String?
)