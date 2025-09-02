package com.example.homeal_app.model

import com.google.gson.annotations.SerializedName

data class Ingredient(
    @SerializedName("Id")
    val id: Int = 0,
    @SerializedName("Name")
    val name: String = "",
    val quantity: Int = 1,
    val unit: String = "pcs"
)