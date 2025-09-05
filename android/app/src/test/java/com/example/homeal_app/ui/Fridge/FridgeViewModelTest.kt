package com.example.homeal_app.ui.Fridge

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.homeal_app.model.FridgeIngredient
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
class FridgeViewModelTest {

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
    fun `FridgeIngredient model should work correctly`() = runTest {
        // Given
        val ingredient = FridgeIngredient(
            id = 1,
            name = "Tomato",
            quantity = 5,
            unit = "pieces"
        )
        
        // Then
        assert(ingredient.id == 1)
        assert(ingredient.name == "Tomato")
        assert(ingredient.quantity == 5)
        assert(ingredient.unit == "pieces")
        assert(ingredient.addedDate > 0)
    }

    @Test
    fun `FridgeIngredient with default values should work`() = runTest {
        // Given
        val ingredient = FridgeIngredient(
            name = "Apple",
            quantity = 3,
            unit = "pcs"
        )
        
        // Then
        assert(ingredient.id == 0)
        assert(ingredient.name == "Apple")
        assert(ingredient.quantity == 3)
        assert(ingredient.unit == "pcs")
        assert(ingredient.ingredientId == 0)
        assert(ingredient.addedDate > 0)
    }

    @Test
    fun `FridgeIngredient with expiration date should work`() = runTest {
        // Given
        val currentTime = System.currentTimeMillis()
        val expirationTime = currentTime + (7 * 24 * 60 * 60 * 1000) // 7 days from now
        
        val ingredient = FridgeIngredient(
            name = "Milk",
            quantity = 1,
            unit = "liter",
            expirationDate = expirationTime
        )
        
        // Then
        assert(ingredient.name == "Milk")
        assert(ingredient.quantity == 1)
        assert(ingredient.unit == "liter")
        assert(ingredient.expirationDate == expirationTime)
    }
}
