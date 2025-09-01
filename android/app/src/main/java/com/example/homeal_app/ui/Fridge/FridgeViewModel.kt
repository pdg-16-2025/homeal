package com.example.homeal_app.ui.Fridge

import androidx.lifecycle.ViewModel
import com.example.homeal_app.model.Ingredient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class FridgeViewModel : ViewModel() {

    private val _ingredients = MutableStateFlow<List<Ingredient>>(emptyList())
    val ingredients : StateFlow<List<Ingredient>> = _ingredients.asStateFlow()

    private val _availableIngredients = MutableStateFlow<List<String>>(emptyList())
    val availableIngredients: StateFlow<List<String>> = _availableIngredients.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _showAddDialog = MutableStateFlow(false)
    val showAddDialog: StateFlow<Boolean> = _showAddDialog.asStateFlow()

    init {
        loadIngredients()
        loadAvailableIngredients()
    }

    private fun loadIngredients(){
        //TODO: load from DB
    }

    private fun loadAvailableIngredients() {
        // TODO: load from database
        // Example list
        _availableIngredients.value = listOf(
            "Apple", "Banana", "Bread", "Butter", "Carrot", "Cheese"
        )
    }

    fun updateSearchQuery( query: String){
        _searchQuery.value = query
    }

    fun showDialog(){
        _showAddDialog.value = true
        _searchQuery.value = ""
    }

    fun hideDialog() {
        _showAddDialog.value = false
        _searchQuery.value = ""
    }

    fun getFilteredAvailableIngredients(): List<String> {
        val query = _searchQuery.value.lowercase()
        if (query.isBlank()){
            return _availableIngredients.value.take(3)
        } else {
            return _availableIngredients.value.filter {
                it.lowercase().contains(query)
            }.take(5)
        }
    }

    fun addIngredient(ingredientName: String) {
        if (ingredientName.isNotBlank() && !_ingredients.value.any{it.name == ingredientName}) {
            _ingredients.value = _ingredients.value + Ingredient(name = ingredientName)
            hideDialog()
        }
    }

    fun addIngredientByBarcode(barcode: String) {

    }

    fun updateIngredientQuantity(name: String, newQuantity: Int, newUnit: String) {
        _ingredients.value = _ingredients.value.map {
            if (it.name == name) it.copy(quantity = newQuantity, unit = newUnit) else it
        }
    }

    fun removeIngredient(ingredient: Ingredient) {
        _ingredients.value = _ingredients.value - ingredient
    }

    fun removeIngredientsForRecipe(recipeId : Int) {

    }

}