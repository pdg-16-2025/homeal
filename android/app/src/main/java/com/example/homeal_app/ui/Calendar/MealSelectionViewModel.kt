// MealSelectionViewModel.kt
package com.example.homeal_app.ui.Calendar

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

data class MealData(
    val name: String,
    val description: String = "",
    val prepTime: String = "",
    val difficulty: String = "",
    val category: String = "",
    val ingredients: List<String> = emptyList()
)

class MealSelectionViewModel : ViewModel() {

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
        // TODO: Replace with DB
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

    private fun loadRecommendations() {
    }

    fun getPersonalizedRecommendations(mealType: String, dayOfWeek: String) {
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
        filterMeals(query)
    }

    private fun filterMeals(query: String) {
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