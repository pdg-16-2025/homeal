package com.example.homeal_app.ui.Calendar

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import com.example.homeal_app.data.repository.CalendarRepository
import com.example.homeal_app.model.PlannedMeal
import com.example.homeal_app.model.Recipe
import com.example.homeal_app.model.MealType
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
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

    @MockK
    private lateinit var repository: CalendarRepository

    private lateinit var viewModel: CalendarViewModel

    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        Dispatchers.setMain(testDispatcher)
        
        // Mock repository methods
        coEvery { repository.getAllPlannedMealsFlow() } returns flowOf(emptyList())
        
        viewModel = CalendarViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `addPlannedMeal should call repository addPlannedMeal`() = runTest {
        // Given
        val plannedMeal = PlannedMeal(
            id = 1,
            recipeId = 1,
            mealDate = "2024-01-15",
            mealType = MealType.BREAKFAST
        )
        
        coEvery { repository.addPlannedMeal(any()) } returns Unit
        
        // When
        viewModel.addPlannedMeal(plannedMeal)
        
        // Then
        verify { repository.addPlannedMeal(plannedMeal) }
    }

    @Test
    fun `removePlannedMeal should call repository removePlannedMeal`() = runTest {
        // Given
        val plannedMeal = PlannedMeal(
            id = 1,
            recipeId = 1,
            mealDate = "2024-01-15",
            mealType = MealType.BREAKFAST
        )
        
        coEvery { repository.removePlannedMeal(any()) } returns Unit
        
        // When
        viewModel.removePlannedMeal(plannedMeal)
        
        // Then
        verify { repository.removePlannedMeal(plannedMeal) }
    }

    @Test
    fun `getMealsForDate should return meals for specific date`() = runTest {
        // Given
        val date = LocalDate.of(2024, 1, 15)
        val dateString = "2024-01-15"
        val plannedMeals = listOf(
            PlannedMeal(1, 1, dateString, MealType.BREAKFAST),
            PlannedMeal(2, 2, dateString, MealType.LUNCH),
            PlannedMeal(3, 3, "2024-01-16", MealType.BREAKFAST)
        )
        
        coEvery { repository.getAllPlannedMealsFlow() } returns flowOf(plannedMeals)
        
        // When
        val result = viewModel.getMealsForDate(dateString)
        
        // Then
        assert(result.size == 2)
        assert(result.all { it.mealDate == dateString })
    }

    @Test
    fun `getMealsForDateAndType should return meals for specific date and type`() = runTest {
        // Given
        val dateString = "2024-01-15"
        val mealType = MealType.BREAKFAST
        val plannedMeals = listOf(
            PlannedMeal(1, 1, dateString, MealType.BREAKFAST),
            PlannedMeal(2, 2, dateString, MealType.LUNCH)
        )
        
        coEvery { repository.getAllPlannedMealsFlow() } returns flowOf(plannedMeals)
        
        // When
        val result = viewModel.getMealsForDateAndType(dateString, mealType)
        
        // Then
        assert(result.size == 1)
        assert(result.first().mealType == mealType)
        assert(result.first().mealDate == dateString)
    }
}