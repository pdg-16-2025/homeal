package com.example.homeal_app.ui.Fridge

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.homeal_app.data.repository.FridgeRepository
import com.example.homeal_app.model.FridgeIngredient
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
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
class FridgeViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    @MockK
    private lateinit var repository: FridgeRepository

    private lateinit var viewModel: FridgeViewModel

    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        Dispatchers.setMain(testDispatcher)
        
        coEvery { repository.getAllIngredientsFlow() } returns flowOf(emptyList())
        
        viewModel = FridgeViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `addIngredient should call repository addIngredient`() = runTest {
        // Given
        val ingredient = FridgeIngredient(
            id = 1,
            name = "Tomato",
            quantity = 5.0,
            unit = "pieces",
            expirationDate = LocalDate.now().plusDays(7),
            category = "Vegetables"
        )
        
        coEvery { repository.addIngredient(any()) } returns Unit
        
        // When
        viewModel.addIngredient(ingredient)
        
        // Then
        verify { repository.addIngredient(ingredient) }
    }

    @Test
    fun `deleteIngredient should call repository deleteIngredient`() = runTest {
        // Given
        val ingredient = FridgeIngredient(
            id = 1,
            name = "Tomato",
            quantity = 5.0,
            unit = "pieces",
            expirationDate = LocalDate.now().plusDays(7),
            category = "Vegetables"
        )
        
        coEvery { repository.deleteIngredient(any()) } returns Unit
        
        // When
        viewModel.deleteIngredient(ingredient)
        
        // Then
        verify { repository.deleteIngredient(ingredient) }
    }

    @Test
    fun `updateIngredient should call repository updateIngredient`() = runTest {
        // Given
        val ingredient = FridgeIngredient(
            id = 1,
            name = "Tomato",
            quantity = 3.0,
            unit = "pieces",
            expirationDate = LocalDate.now().plusDays(5),
            category = "Vegetables"
        )
        
        coEvery { repository.updateIngredient(any()) } returns Unit
        
        // When
        viewModel.updateIngredient(ingredient)
        
        // Then
        verify { repository.updateIngredient(ingredient) }
    }
}