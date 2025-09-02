package com.example.homeal_app.ui.Shopping

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.homeal_app.data.repository.ShoppingRepository
import com.example.homeal_app.data.local.database.AppDatabase
import com.example.homeal_app.data.remote.NetworkModule
import com.example.homeal_app.model.ShoppingIngredient
import com.example.homeal_app.model.Ingredient
import kotlinx.coroutines.launch

class ShoppingViewModel(application: Application) : AndroidViewModel(application) {

    // Initialize repository
    private val repository = ShoppingRepository(
        AppDatabase.getDatabase(application).shoppingDao(),
        NetworkModule.apiService
    )

    // Shopping list from database
    val ingredients: LiveData<List<ShoppingIngredient>> =
        repository.getAllShoppingIngredients().asLiveData()

    // Search functionality
    private val _searchQuery = MutableLiveData("")
    val searchQuery: LiveData<String> = _searchQuery

    private val _availableIngredients = MutableLiveData<List<Ingredient>>()
    val availableIngredients: LiveData<List<Ingredient>> = _availableIngredients

    /**
     * Update search query and fetch suggestions from server
     */
    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
        if (query.isNotBlank()) {
            viewModelScope.launch {
                try {
                    val serverIngredients = repository.searchAvailableIngredients(query)
                    _availableIngredients.value = serverIngredients
                } catch (e: Exception) {
                    // Keep empty list on error
                    _availableIngredients.value = emptyList()
                }
            }
        } else {
            _availableIngredients.value = emptyList()
        }
    }

    /**
     * Add ingredient to shopping list (manual entry)
     */
    fun addIngredient(name: String, quantity: String) {
        viewModelScope.launch {
            try {
                repository.addShoppingIngredient(name, quantity)
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    /**
     * Add ingredient from server suggestion
     */
    fun addIngredientFromSuggestion(ingredient: Ingredient, quantity: String = "1 pcs") {
        viewModelScope.launch {
            try {
                repository.addIngredientFromSuggestion(ingredient, quantity)
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    /**
     * Remove ingredient from shopping list
     */
    fun removeIngredient(ingredientId: Int) {
        viewModelScope.launch {
            try {
                repository.removeShoppingIngredient(ingredientId)
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    /**
     * Toggle ingredient done status (for strikethrough effect)
     */
    fun toggleIngredientDone(ingredientId: Int) {
        viewModelScope.launch {
            try {
                val currentIngredient = ingredients.value?.find { it.id == ingredientId }
                currentIngredient?.let {
                    repository.toggleIngredientDone(ingredientId, !it.isDone)
                }
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    /**
     * Remove all marked (completed) ingredients
     */
    fun removeAllMarkedIngredients() {
        viewModelScope.launch {
            try {
                repository.removeAllMarkedIngredients()
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
}
