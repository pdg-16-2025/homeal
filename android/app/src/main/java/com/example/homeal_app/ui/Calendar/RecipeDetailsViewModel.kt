package com.example.homeal_app.ui.Calendar

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.homeal_app.data.repository.CalendarRepository
import com.example.homeal_app.data.local.database.AppDatabase
import com.example.homeal_app.data.remote.NetworkModule
import com.example.homeal_app.model.RecipeDetails
import kotlinx.coroutines.launch

class RecipeDetailsViewModel(application: Application) : AndroidViewModel(application) {

    // Initialize repository
    private val repository = CalendarRepository(
        AppDatabase.getDatabase(application).plannedMealDao(),
        NetworkModule.apiService
    )

    private val _recipeDetails = MutableLiveData<RecipeDetails?>()
    val recipeDetails: LiveData<RecipeDetails?> = _recipeDetails

    private val _recipeIngredients = MutableLiveData<List<com.example.homeal_app.model.RecipeIngredient>>()
    val recipeIngredients: LiveData<List<com.example.homeal_app.model.RecipeIngredient>> = _recipeIngredients

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    fun loadRecipeDetails(recipeId: Int) {
        android.util.Log.d("RecipeDetailsViewModel", "loadRecipeDetails called with recipeId: $recipeId")

        _isLoading.value = true
        _error.value = null

        viewModelScope.launch {
            try {
                android.util.Log.d("RecipeDetailsViewModel", "About to call repository.getRecipeDetails")
                val details = repository.getRecipeDetails(recipeId)

                if (details != null) {
                    android.util.Log.d("RecipeDetailsViewModel", "Successfully loaded recipe: ${details.name}")
                    _recipeDetails.value = details

                    // Fetch ingredients with names using the proper endpoint
                    android.util.Log.d("RecipeDetailsViewModel", "Fetching ingredients with names for recipe: $recipeId")
                    val ingredientsWithNames = repository.getRecipeIngredients(recipeId)
                    _recipeIngredients.value = ingredientsWithNames
                    android.util.Log.d("RecipeDetailsViewModel", "Ingredients with names loaded: ${ingredientsWithNames.size} ingredients")
                } else {
                    android.util.Log.w("RecipeDetailsViewModel", "Recipe details returned null")
                    _error.value = "Recipe not found"
                }
            } catch (e: Exception) {
                android.util.Log.e("RecipeDetailsViewModel", "Error loading recipe details: ${e.message}", e)
                _error.value = "Error loading recipe: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearError() {
        _error.value = null
    }
}

