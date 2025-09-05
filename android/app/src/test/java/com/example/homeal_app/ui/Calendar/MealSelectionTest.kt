package com.example.homeal_app.ui.Calendar

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import com.example.homeal_app.data.repository.CalendarRepository
import com.example.homeal_app.model.Recipe
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.verify
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

    @MockK
    private lateinit var repository: CalendarRepository

    private lateinit var viewModel: MealSelectionViewModel

    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        Dispatchers.setMain(testDispatcher)
        
        viewModel = MealSelectionViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `searchRecipes should return search results`() = runTest {
        // Given
        val query = "pasta"
        val recipes = listOf(
            Recipe(id = 1, title = "Pasta Carbonara", description = "Delicious pasta", ingredients = emptyList()),
            Recipe(id = 2, title = "Pasta Bolognese", description = "Traditional pasta", ingredients = emptyList())
        )
        
        coEvery { repository.searchRecipes(query) } returns recipes
        
        // When
        val result = viewModel.searchRecipes(query)
        
        // Then
        assert(result == recipes)
        verify { repository.searchRecipes(query) }
    }

    @Test
    fun `getRecommendations should return recommended recipes`() = runTest {
        // Given
        val recipes = listOf(
            Recipe(id = 1, title = "Recommended Recipe", description = "Great recipe", ingredients = emptyList())
        )
        
        coEvery { repository.getRecommendations() } returns recipes
        
        // When
        val result = viewModel.getRecommendations()
        
        // Then
        assert(result == recipes)
        verify { repository.getRecommendations() }
    }

    @Test
    fun `searchRecipes with empty query should return empty list`() = runTest {
        // Given
        val query = ""
        
        coEvery { repository.searchRecipes(query) } returns emptyList()
        
        // When
        val result = viewModel.searchRecipes(query)
        
        // Then
        assert(result.isEmpty())
    }
}