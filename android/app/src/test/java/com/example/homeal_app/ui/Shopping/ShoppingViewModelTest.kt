package com.example.homeal_app.ui.Shopping

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.homeal_app.model.ShoppingIngredient
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
class ShoppingViewModelTest {

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
    fun `ShoppingIngredient model should work correctly`() = runTest {
        // Given
        val shoppingIngredient = ShoppingIngredient(
            id = 1,
            name = "Milk",
            quantity = "2 liters",
            isDone = false
        )
        
        // Then
        assert(shoppingIngredient.id == 1)
        assert(shoppingIngredient.name == "Milk")
        assert(shoppingIngredient.quantity == "2 liters")
        assert(!shoppingIngredient.isDone)
        assert(shoppingIngredient.addedDate > 0)
    }

    @Test
    fun `ShoppingIngredient with default values should work`() = runTest {
        // Given
        val shoppingIngredient = ShoppingIngredient(
            name = "Bread",
            quantity = "1 loaf"
        )
        
        // Then
        assert(shoppingIngredient.id == 0)
        assert(shoppingIngredient.name == "Bread")
        assert(shoppingIngredient.quantity == "1 loaf")
        assert(!shoppingIngredient.isDone)
        assert(shoppingIngredient.addedDate > 0)
    }

    @Test
    fun `ShoppingIngredient marked as done should work`() = runTest {
        // Given
        val shoppingIngredient = ShoppingIngredient(
            name = "Eggs",
            quantity = "12 pieces",
            isDone = true
        )
        
        // Then
        assert(shoppingIngredient.name == "Eggs")
        assert(shoppingIngredient.quantity == "12 pieces")
        assert(shoppingIngredient.isDone)
    }

    @Test
    fun `ShoppingIngredient with custom ID should work`() = runTest {
        // Given
        val shoppingIngredient = ShoppingIngredient(
            id = 5,
            name = "Medicine",
            quantity = "1 bottle"
        )
        
        // Then
        assert(shoppingIngredient.id == 5)
        assert(shoppingIngredient.name == "Medicine")
        assert(shoppingIngredient.quantity == "1 bottle")
        assert(!shoppingIngredient.isDone)
    }

    @Test
    fun `ShoppingIngredient with custom date should work`() = runTest {
        // Given
        val customDate = 1640995200000L // January 1, 2022
        val shoppingIngredient = ShoppingIngredient(
            name = "Special Cheese",
            quantity = "200g",
            addedDate = customDate
        )
        
        // Then
        assert(shoppingIngredient.name == "Special Cheese")
        assert(shoppingIngredient.quantity == "200g")
        assert(shoppingIngredient.addedDate == customDate)
    }
}
