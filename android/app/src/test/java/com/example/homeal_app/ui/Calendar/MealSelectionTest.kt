package com.example.homeal_app.ui.Calendar

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.homeal_app.model.Recipe
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
class MealSelectionTest {

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
    fun `Recipe model should work correctly`() = runTest {
        // Given
        val recipe = Recipe(
            id = 1,
            name = "Pasta Carbonara",
            totalTime = 30,
            imageUrl = "https://example.com/pasta.jpg"
        )
        
        // Then
        assert(recipe.id == 1)
        assert(recipe.name == "Pasta Carbonara")
        assert(recipe.totalTime == 30)
        assert(recipe.imageUrl == "https://example.com/pasta.jpg")
    }

    @Test
    fun `Recipe with default values should work`() = runTest {
        // Given
        val recipe = Recipe()
        
        // Then
        assert(recipe.id == 0)
        assert(recipe.name == "")
        assert(recipe.totalTime == 0)
        assert(recipe.imageUrl == "")
    }

    @Test
    fun `MealData should work correctly`() = runTest {
        // Given
        val mealData = MealData(
            id = 1,
            name = "Caesar Salad",
            description = "Fresh romaine lettuce with parmesan",
            prepTime = "15 min",
            difficulty = "Easy",
            category = "Salad",
            ingredients = listOf("Lettuce", "Parmesan", "Croutons")
        )
        
        // Then
        assert(mealData.id == 1)
        assert(mealData.name == "Caesar Salad")
        assert(mealData.description == "Fresh romaine lettuce with parmesan")
        assert(mealData.prepTime == "15 min")
        assert(mealData.difficulty == "Easy")
        assert(mealData.category == "Salad")
        assert(mealData.ingredients.size == 3)
        assert(mealData.ingredients.contains("Lettuce"))
    }

    @Test
    fun `MealData with default values should work`() = runTest {
        // Given
        val mealData = MealData(
            name = "Simple Meal"
        )
        
        // Then
        assert(mealData.id == 0)
        assert(mealData.name == "Simple Meal")
        assert(mealData.description == "")
        assert(mealData.prepTime == "")
        assert(mealData.difficulty == "")
        assert(mealData.category == "")
        assert(mealData.ingredients.isEmpty())
    }
}
