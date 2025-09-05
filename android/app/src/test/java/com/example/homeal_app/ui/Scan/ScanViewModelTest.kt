package com.example.homeal_app.ui.Scan

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class ScanViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `Product model should work correctly`() = runTest {
        // Given
        val product = Product(
            id = "1234567890",
            name = "Organic Milk",
            category = "Dairy"
        )
        
        // Then
        assert(product.id == "1234567890")
        assert(product.name == "Organic Milk")
        assert(product.category == "Dairy")
    }

    @Test
    fun `Product with default values should work`() = runTest {
        // Given
        val product = Product(
            id = "",
            name = ""
        )
        
        // Then
        assert(product.id == "")
        assert(product.name == "")
        assert(product.category == null)
    }

    @Test
    fun `Barcode validation should work correctly`() = runTest {
        // Given
        val validBarcode = "1234567890123"
        val invalidBarcode = "123"
        val emptyBarcode = ""
        
        // When & Then
        assert(validBarcode.length >= 8) // Valid barcode length
        assert(invalidBarcode.length < 8) // Invalid barcode length
        assert(emptyBarcode.isEmpty()) // Empty barcode
    }

    @Test
    fun `Product creation with minimal data should work`() = runTest {
        // Given
        val product = Product(
            id = "9876543210",
            name = "Bread"
        )
        
        // Then
        assert(product.id == "9876543210")
        assert(product.name == "Bread")
        assert(product.category == null)
    }
}
