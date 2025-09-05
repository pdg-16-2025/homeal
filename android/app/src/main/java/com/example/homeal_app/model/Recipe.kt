package com.example.homeal_app.model

import com.google.gson.annotations.SerializedName

data class Recipe (
    @SerializedName("id")
    val id: Int = 0,
    @SerializedName("name")
    val name: String = "",
    @SerializedName("total_time")
    val totalTime: Int = 0,
    @SerializedName("image_url")
    val imageUrl: String = ""
)