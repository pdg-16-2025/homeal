package com.example.homeal_app.ui.Fridge

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.homeal_app.data.repository.FridgeRepository
import com.example.homeal_app.data.local.database.AppDatabase
import com.example.homeal_app.data.remote.NetworkModule
import com.example.homeal_app.model.FridgeIngredient
import com.example.homeal_app.model.Ingredient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class FridgeViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = FridgeRepository(
        AppDatabase.getDatabase(application).fridgeDao(),
        NetworkModule.apiService
    )

    // Fridge ingredients from local database
    val ingredients: LiveData<List<FridgeIngredient>> =
        repository.getAllIngredients().asLiveData()

    // Search functionality
    private val _searchQuery = MutableLiveData("")
    val searchQuery: LiveData<String> = _searchQuery

    private val _availableIngredients = MutableLiveData<List<Ingredient>>()
    val availableIngredients: LiveData<List<Ingredient>> = _availableIngredients

    // Dialog state
    private val _showAddDialog = MutableStateFlow(false)
    val showAddDialog: StateFlow<Boolean> = _showAddDialog

    /**
     * Show add ingredient dialog
     */
    fun showDialog() {
        _showAddDialog.value = true
        _searchQuery.value = ""
        _availableIngredients.value = emptyList()
    }

    /**
     * Hide add ingredient dialog
     */
    fun hideDialog() {
        _showAddDialog.value = false
        _searchQuery.value = ""
        _availableIngredients.value = emptyList()
    }

    /**
     * Update search query and get suggestions from server
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
     * Add ingredient manually (typed by user)
     */
    fun addIngredient(name: String, quantity: Int = 1, unit: String = "pcs") {
        viewModelScope.launch {
            try {
                repository.addIngredient(name, quantity, unit)
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    /**
     * Add ingredient from server suggestion
     */
    fun addIngredientFromSuggestion(ingredient: Ingredient) {
        viewModelScope.launch {
            try {
                repository.addIngredient(ingredient.name, 1, "pcs")
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    /**
     * Remove ingredient from fridge
     */
    fun removeIngredient(ingredient: FridgeIngredient) {
        viewModelScope.launch {
            try {
                repository.removeIngredient(ingredient)
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    /**
     * Update ingredient quantity and unit
     */
    fun updateIngredientQuantity(name: String, quantity: Int, unit: String) {
        viewModelScope.launch {
            try {
                repository.updateIngredientQuantity(name, quantity, unit)
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    // DEPRECATED: Remove this method
    @Deprecated("Use availableIngredients LiveData instead")
    fun getFilteredAvailableIngredients(): List<String> {
        return _availableIngredients.value?.map { it.name } ?: emptyList()
    }
}