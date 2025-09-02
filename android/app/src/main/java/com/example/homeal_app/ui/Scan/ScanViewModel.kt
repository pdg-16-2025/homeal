package com.example.homeal_app.ui.Scan

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

// Data classes for dialog states and product information
data class Product(
    val id: String,
    val name: String,
    val category: String? = null
)

sealed class DialogState {
    object None : DialogState()
    data class ProductFound(val product: Product, val barcode: String) : DialogState()
    data class ProductNotFound(val barcode: String) : DialogState()
    object Loading : DialogState()
}

class ScanViewModel : ViewModel() {

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
            // TODO: Implement actual server query
            // This should make an HTTP request to your API endpoint
            // Example: GET /api/products/barcode/{barcode}

            try {
                // Simulate server response - replace with actual API call
                val product = simulateServerResponse(barcode)

                if (product != null) {
                    _dialogState.value = DialogState.ProductFound(product, barcode)
                } else {
                    _dialogState.value = DialogState.ProductNotFound(barcode)
                }
            } catch (e: Exception) {
                // Handle network errors
                _dialogState.value = DialogState.ProductNotFound(barcode)
            }
        }
    }

    private suspend fun simulateServerResponse(barcode: String): Product? {
        // TODO: Replace this with actual server query
        // Example implementation:
        // val response = apiService.getProductByBarcode(barcode)
        // return response.product

        // Simulate network delay
        kotlinx.coroutines.delay(1000)

        // Simulate found/not found based on barcode
        return if (barcode.length > 10) {
            Product("1", "Sample Product", "Food")
        } else {
            null
        }
    }

    fun onProductConfirmed(product: Product, quantity: Int) {
        // TODO: Add product to owned list and remove from shopping list if present
        // Example:
        // - Add to Room database owned_ingredients table
        // - Remove from shopping_list table if exists
        // - Update UI state

        closeDialog()
    }

    fun onManualProductSelected(product: Product, quantity: Int) {
        // TODO: Same as onProductConfirmed but also store barcode mapping
        // Example:
        // - Store barcode -> product mapping for future scans
        // - Add to owned list and remove from shopping list

        closeDialog()
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
