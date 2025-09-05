package com.example.homeal_app.ui.Calendar

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.homeal_app.model.PlannedMeal
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
import java.time.LocalDate

@ExperimentalCoroutinesApi
class CalendarViewModelTest {

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
    fun `PlannedMeal model should work correctly`() = runTest {
        // Given
        val plannedMeal = PlannedMeal(
            id = 1,
            recipeId = 123,
            recipeName = "Pasta Carbonara",
            mealDate = "2024-01-15",
            mealType = "Lunch"
        )
        
        // Then
        assert(plannedMeal.id == 1)
        assert(plannedMeal.recipeId == 123)
        assert(plannedMeal.recipeName == "Pasta Carbonara")
        assert(plannedMeal.mealDate == "2024-01-15")
        assert(plannedMeal.mealType == "Lunch")
        assert(plannedMeal.addedDate > 0)
    }

    @Test
    fun `PlannedMeal with default values should work`() = runTest {
        // Given
        val plannedMeal = PlannedMeal(
            recipeId = 456,
            recipeName = "Chicken Curry",
            mealDate = "2024-01-16",
            mealType = "Dinner"
        )
        
        // Then
        assert(plannedMeal.id == 0)
        assert(plannedMeal.recipeId == 456)
        assert(plannedMeal.recipeName == "Chicken Curry")
        assert(plannedMeal.mealDate == "2024-01-16")
        assert(plannedMeal.mealType == "Dinner")
        assert(plannedMeal.addedDate > 0)
    }

    @Test
    fun `LocalDate operations should work correctly`() = runTest {
        // Given
        val date = LocalDate.of(2024, 1, 15)
        
        // When
        val nextWeek = date.plusWeeks(1)
        val previousWeek = date.minusWeeks(1)
        val nextDay = date.plusDays(1)
        
        // Then
        assert(nextWeek == LocalDate.of(2024, 1, 22))
        assert(previousWeek == LocalDate.of(2024, 1, 8))
        assert(nextDay == LocalDate.of(2024, 1, 16))
    }

    @Test
    fun `Date string formatting should work correctly`() = runTest {
        // Given
        val date = LocalDate.of(2024, 1, 15)
        
        // When
        val dateString = date.toString()
        
        // Then
        assert(dateString == "2024-01-15")
    }
}
