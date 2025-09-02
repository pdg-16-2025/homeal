package com.example.homeal_app.ui.Shopping

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

data class ShoppingIngredient(
    val id: String,
    val name: String,
    val quantity: String,
    val isDone: Boolean = false
)

class ShoppingViewModel : ViewModel() {

    private val _ingredients = MutableLiveData<List<ShoppingIngredient>>().apply {
        value = listOf(
            ShoppingIngredient("1", "Tomatoes", "2 kg", false),
            ShoppingIngredient("2", "Onions", "500g", false),
            ShoppingIngredient("3", "Garlic", "1 bulb", true),
            ShoppingIngredient("4", "Olive Oil", "250ml", false),
            ShoppingIngredient("5", "Pasta", "500g", false)
        )
    }
    val ingredients: LiveData<List<ShoppingIngredient>> = _ingredients

    fun toggleIngredientDone(ingredientId: String) {
        val currentList = _ingredients.value ?: return
        val updatedList = currentList.map { ingredient ->
            if (ingredient.id == ingredientId) {
                ingredient.copy(isDone = !ingredient.isDone)
            } else {
                ingredient
            }
        }
        _ingredients.value = updatedList
    }

    fun addIngredient(name: String, quantity: String) {
        if (name.isBlank() || quantity.isBlank()) return

        val currentList = _ingredients.value ?: emptyList()
        val newIngredient = ShoppingIngredient(
            id = (currentList.size + 1).toString(),
            name = name.trim(),
            quantity = quantity.trim(),
            isDone = false
        )
        _ingredients.value = currentList + newIngredient
    }

    fun removeIngredient(ingredientId: String) {
        val currentList = _ingredients.value ?: return
        val updatedList = currentList.filterNot { it.id == ingredientId }
        _ingredients.value = updatedList
    }

    fun removeAllMarkedIngredients() {
        val currentList = _ingredients.value ?: return
        val updatedList = currentList.filterNot { it.isDone }
        _ingredients.value = updatedList
    }
}
