// MealSelectionViewModel.kt
package com.example.homeal_app.ui.Calendar

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.homeal_app.data.repository.CalendarRepository
import com.example.homeal_app.data.local.database.AppDatabase
import com.example.homeal_app.data.remote.NetworkModule
import com.example.homeal_app.model.Recipe
import kotlinx.coroutines.launch

// Keep your existing MealData for backward compatibility
data class MealData(
    val id: Int = 0, // Add ID for database compatibility
    val name: String,
    val description: String = "",
    val prepTime: String = "",
    val difficulty: String = "",
    val category: String = "",
    val ingredients: List<String> = emptyList()
)

class MealSelectionViewModel(application: Application) : AndroidViewModel(application) {

    // Initialize repository
    private val repository = CalendarRepository(
        AppDatabase.getDatabase(application).plannedMealDao(),
        NetworkModule.apiService
    )

    private val _searchQuery = MutableLiveData("")
    val searchQuery: LiveData<String> = _searchQuery

    private val _allMeals = MutableLiveData<List<MealData>>()
    val allMeals: LiveData<List<MealData>> = _allMeals

    private val _filteredMeals = MutableLiveData<List<MealData>>()
    val filteredMeals: LiveData<List<MealData>> = _filteredMeals

    private val _recommendations = MutableLiveData<List<MealData>>()
    val recommendations: LiveData<List<MealData>> = _recommendations

    init {
        loadMeals()
        loadRecommendations()
    }

    private fun loadMeals() {
        viewModelScope.launch {
            try {
                // Load from server
                val serverRecipes = repository.searchRecipes("")
                val meals = serverRecipes.map { recipe ->
                    MealData(
                        id = recipe.id,
                        name = recipe.name,
                        description = "Cooking time: ${recipe.totalTime} min",
                        prepTime = "${recipe.totalTime} min",
                        difficulty = "Medium", // Default for now
                        category = "Recipe"
                    )
                }
                _allMeals.value = meals
            } catch (e: Exception) {
                // Fallback to example data on error
                val meals = listOf(
                    MealData(
                        name = "Caesar Salad",
                        description = "Fresh romaine lettuce with parmesan and croutons",
                        prepTime = "15 min",
                        difficulty = "Easy",
                        category = "Salad"
                    ),
                    MealData(
                        name = "Margherita Pizza",
                        description = "Classic pizza with tomato, mozzarella, and basil",
                        prepTime = "30 min",
                        difficulty = "Medium",
                        category = "Pizza"
                    ),
                    MealData(
                        name = "Burger and Fries",
                        description = "Juicy beef burger with crispy french fries",
                        prepTime = "20 min",
                        difficulty = "Easy",
                        category = "Fast Food"
                    ),
                    MealData(
                        name = "Spaghetti Bolognese",
                        description = "Traditional meat sauce with spaghetti pasta",
                        prepTime = "45 min",
                        difficulty = "Medium",
                        category = "Pasta"
                    ),
                    MealData(
                        name = "Chicken Curry",
                        description = "Spicy chicken curry with rice and naan bread",
                        prepTime = "40 min",
                        difficulty = "Hard",
                        category = "Indian"
                    )
                )
                _allMeals.value = meals
            }
        }
    }

    private fun loadRecommendations() {
        viewModelScope.launch {
            try {
                val serverRecommendations = repository.getRecommendations()
                val recommendations = serverRecommendations.take(5).map { recipe ->
                    MealData(
                        id = recipe.id,
                        name = recipe.name,
                        description = "Recommended for you",
                        prepTime = "${recipe.totalTime} min",
                        difficulty = "Medium",
                        category = "Recommendation"
                    )
                }
                _recommendations.value = recommendations
            } catch (e: Exception) {
                // Use some meals as recommendations on error
                val fallbackRecommendations = _allMeals.value?.take(3) ?: emptyList()
                _recommendations.value = fallbackRecommendations
            }
        }
    }

    fun getRecommendations(mealType: String, dayOfWeek: String) {
        // Could be enhanced to use mealType and dayOfWeek for better recommendations
        loadRecommendations()
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
        
        if (query.isNotBlank()) {
            // Search on server
            viewModelScope.launch {
                try {
                    val serverResults = repository.searchRecipes(query)
                    val searchResults = serverResults.map { recipe ->
                        MealData(
                            id = recipe.id,
                            name = recipe.name,
                            description = "Cooking time: ${recipe.totalTime} min",
                            prepTime = "${recipe.totalTime} min",
                            difficulty = "Medium",
                            category = "Search Result"
                        )
                    }
                    _filteredMeals.value = searchResults
                } catch (e: Exception) {
                    // Fallback to local filtering
                    filterMealsLocally(query)
                }
            }
        } else {
            _filteredMeals.value = emptyList()
        }
    }

    private fun filterMealsLocally(query: String) {
        val allMeals = _allMeals.value ?: return

        if (query.isEmpty()) {
            _filteredMeals.value = emptyList()
            return
        }

        val filtered = allMeals.filter { meal ->
            meal.name.contains(query, ignoreCase = true) ||
                    meal.description.contains(query, ignoreCase = true) ||
                    meal.category.contains(query, ignoreCase = true) ||
                    meal.ingredients.any { it.contains(query, ignoreCase = true) }
        }

        _filteredMeals.value = filtered
    }

    fun addCustomMeal(meal: MealData) {
        val currentMeals = _allMeals.value?.toMutableList() ?: mutableListOf()
        currentMeals.add(meal)
        _allMeals.value = currentMeals

        loadRecommendations()
    }
}