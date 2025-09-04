package com.example.homeal_app.model

import com.google.gson.annotations.SerializedName

data class RecipeIngredient(
    @SerializedName("id")
    val id: Int,
    @SerializedName("name")
    val name: String = "",
    @SerializedName("quantity")
    val quantity: String = "1",
    @SerializedName("unit")
    val unit: String = ""
)
