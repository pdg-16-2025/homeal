package com.example.homeal_app.ui.Fridge

import androidx.lifecycle.viewModelScope
import com.example.homeal_app.model.Ingredient
import com.example.homeal_app.model.FridgeIngredient
import com.example.homeal_app.data.repository.FridgeRepository
import com.example.homeal_app.data.local.database.AppDatabase
import com.example.homeal_app.data.remote.NetworkModule
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import android.app.Application
import androidx.lifecycle.AndroidViewModel

class FridgeViewModel(application: Application) : AndroidViewModel(application) {

    // Initialize repository with database and API service
    private val repository = FridgeRepository(
        AppDatabase.getDatabase(application).fridgeDao(),
        NetworkModule.apiService
    )

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
        // Load from local database and convert FridgeIngredient to Ingredient for UI compatibility
        viewModelScope.launch {
            repository.getFridgeIngredients().collect { fridgeIngredients ->
                _ingredients.value = fridgeIngredients.map { fridgeIngredient ->
                    Ingredient(
                        id = fridgeIngredient.ingredientId,
                        name = fridgeIngredient.name,
                        quantity = fridgeIngredient.quantity,
                        unit = fridgeIngredient.unit
                    )
                }
            }
        }
    }

    private fun loadAvailableIngredients() {
        // TODO: load from database
        // Example list
        _availableIngredients.value = listOf(
            "Apple", "Banana", "Bread", "Butter", "Carrot", "Cheese"
        )
    }

    fun updateSearchQuery(query: String){
        _searchQuery.value = query
        // Search server ingredients when query changes
        if (query.isNotBlank()) {
            viewModelScope.launch {
                try {
                    val serverIngredients = repository.searchAvailableIngredients(query)
                    _availableIngredients.value = serverIngredients.map { it.name }
                } catch (e: Exception) {
                    // Keep fallback list on error
                }
            }
        }
    }

    fun showDialog(){
        _showAddDialog.value = true
        _searchQuery.value = ""
        // Reset to fallback suggestions when opening dialog
        _availableIngredients.value = listOf(
            "Apple", "Banana", "Bread", "Butter", "Carrot", "Cheese"
        )
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
            viewModelScope.launch {
                try {
                    val ingredient = Ingredient(
                        id = 0, // Will be set by server or generated
                        name = ingredientName
                    )
                    repository.addToFridge(ingredient, 1, "pcs")
                    hideDialog()
                } catch (e: Exception) {
                    // Handle error
                }
            }
        }
    }

    fun addIngredientByBarcode(barcode: String) {
        viewModelScope.launch {
            try {
                val ingredient = repository.scanIngredient(barcode)
                ingredient?.let {
                    repository.addToFridge(it, 1, "pcs", barcode = barcode)
                }
            } catch (e: Exception) {
                // Handle scanning error
            }
        }
    }

    fun updateIngredientQuantity(name: String, newQuantity: Int, newUnit: String) {
        _ingredients.value = _ingredients.value.map {
            if (it.name == name) it.copy(quantity = newQuantity, unit = newUnit) else it
        }
        
        // TODO: Update in database as well
        // This would require finding the FridgeIngredient and updating it
    }

    fun removeIngredient(ingredient: Ingredient) {
        viewModelScope.launch {
            try {
                val fridgeIngredient = FridgeIngredient(
                    ingredientId = ingredient.id,
                    name = ingredient.name,
                    quantity = ingredient.quantity,
                    unit = ingredient.unit
                )
                repository.removeFromFridge(fridgeIngredient)
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

}