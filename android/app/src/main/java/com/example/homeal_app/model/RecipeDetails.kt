package com.example.homeal_app.model

import com.google.gson.annotations.SerializedName

data class RecipeDetails(
    @SerializedName("id")
    val id: Int = 0,
    @SerializedName("name")
    val name: String = "",
    @SerializedName("author_id")
    val authorId: Int? = null,
    @SerializedName("cook_time")
    val cookTime: Int = -1,
    @SerializedName("prep_time")
    val prepTime: Int = -1,
    @SerializedName("total_time")
    val totalTime: Int = -1,
    @SerializedName("description")
    val description: String? = null,
    @SerializedName("images")
    val images: String = "",
    @SerializedName("category")
    val category: String? = null,
    @SerializedName("keywords")
    val keywords: String? = null,
    @SerializedName("aggregated_rating")
    val rating: Float = 0f,
    @SerializedName("review_count")
    val reviewCount: Int = 0,
    @SerializedName("calories")
    val calories: Float = 0f,
    @SerializedName("fat_content")
    val fatContent: Float = 0f,
    @SerializedName("protein_content")
    val proteinContent: Float = 0f,
    @SerializedName("carbohydrate_content")
    val carbohydrateContent: Float = 0f,
    @SerializedName("recipe_servings")
    val servings: Int = 0,
    @SerializedName("recipe_instructions")
    val instructions: String = "",
    @SerializedName("ingredients")
    val ingredients: List<RecipeIngredient> = emptyList()
)