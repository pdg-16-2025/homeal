package com.example.homeal_app.model

import androidx.room.ColumnInfo
import androidx.room.PrimaryKey
import androidx.room.Entity

@Entity(tableName = "planned_meals")
data class PlannedMeal(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val recipeId: Int,
    val recipeName: String,
    @ColumnInfo(name = "meal_date")
    val mealDate: String, // Format: "2025-09-02"
    val mealType: String, // "Lunch" or "Dinner"
    val addedDate: Long = System.currentTimeMillis()
)


