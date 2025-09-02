package com.example.homeal_app.ui.Scan

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.homeal_app.data.repository.ScanRepository
import com.example.homeal_app.data.local.database.AppDatabase
import com.example.homeal_app.data.remote.NetworkModule
import com.example.homeal_app.model.Ingredient
import kotlinx.coroutines.launch

// Keep existing data classes
data class Product(
    val id: String,
    val name: String,
    val category: String? = null
) {
    // Convert from Ingredient
    companion object {
        fun fromIngredient(ingredient: Ingredient): Product {
            return Product(
                id = ingredient.id.toString(),
                name = ingredient.name,
                category = "Food" // Default category
            )
        }
    }
}

sealed class DialogState {
    object None : DialogState()
    data class ProductFound(val product: Product, val barcode: String, val ingredient: Ingredient) : DialogState()
    data class ProductNotFound(val barcode: String) : DialogState()
    object Loading : DialogState()
    data class Success(val message: String) : DialogState()
}

class ScanViewModel(application: Application) : AndroidViewModel(application) {

    // Initialize repository
    private val repository = ScanRepository(
        AppDatabase.getDatabase(application).fridgeDao(),
        AppDatabase.getDatabase(application).shoppingDao(),
        NetworkModule.apiService
    )

    private val _scannedText = MutableLiveData<String>().apply {
        value = ""
    }
    val scannedText: LiveData<String> = _scannedText

    private val _isScanning = MutableLiveData<Boolean>().apply {
        value = true
    }
    val isScanning: LiveData<Boolean> = _isScanning

    private val _dialogState = MutableLiveData<DialogState>().apply {
        value = DialogState.None
    }
    val dialogState: LiveData<DialogState> = _dialogState

    fun onBarcodeScanned(barcode: String) {
        _scannedText.value = barcode
        _isScanning.value = false // Stop scanning after successful scan

        // Show loading dialog and query server
        _dialogState.value = DialogState.Loading
        queryProductByBarcode(barcode)
    }

    private fun queryProductByBarcode(barcode: String) {
        viewModelScope.launch {
            try {
                val ingredient = repository.scanBarcode(barcode)

                if (ingredient != null) {
                    val product = Product.fromIngredient(ingredient)
                    _dialogState.value = DialogState.ProductFound(product, barcode, ingredient)
                } else {
                    _dialogState.value = DialogState.ProductNotFound(barcode)
                }
            } catch (e: Exception) {
                // Handle network errors
                _dialogState.value = DialogState.ProductNotFound(barcode)
            }
        }
    }

    fun onProductConfirmed(product: Product, quantity: Int) {
        // Find the original ingredient from the dialog state
        val currentState = _dialogState.value
        if (currentState is DialogState.ProductFound) {
            val ingredient = currentState.ingredient
            
            viewModelScope.launch {
                try {
                    val result = repository.processScannedIngredient(ingredient, quantity)
                    
                    if (result.success) {
                        val message = buildString {
                            append("✅ Added ${ingredient.name} to fridge!")
                            if (result.markedInShoppingList) {
                                append("\n✅ Marked as done in shopping list!")
                            }
                        }
                        _dialogState.value = DialogState.Success(message)
                    } else {
                        _dialogState.value = DialogState.ProductNotFound(currentState.barcode)
                    }
                } catch (e: Exception) {
                    _dialogState.value = DialogState.ProductNotFound(currentState.barcode)
                }
            }
        } else {
            closeDialog()
        }
    }

    fun onManualProductSelected(product: Product, quantity: Int) {
        // Create ingredient from manually selected product
        val ingredient = Ingredient(
            id = product.id.toIntOrNull() ?: 0,
            name = product.name
        )
        
        viewModelScope.launch {
            try {
                val result = repository.processScannedIngredient(ingredient, quantity)
                
                if (result.success) {
                    val message = buildString {
                        append("✅ Added ${ingredient.name} to fridge!")
                        if (result.markedInShoppingList) {
                            append("\n✅ Marked as done in shopping list!")
                        }
                    }
                    _dialogState.value = DialogState.Success(message)
                } else {
                    closeDialog()
                }
            } catch (e: Exception) {
                closeDialog()
            }
        }
    }

    fun closeDialog() {
        _dialogState.value = DialogState.None
        clearScan() // Resume scanning
    }

    fun clearScan() {
        _scannedText.value = ""
        _isScanning.value = true // Resume scanning
    }

    fun startScanning() {
        _isScanning.value = true
    }

    fun stopScanning() {
        _isScanning.value = false
    }
}
