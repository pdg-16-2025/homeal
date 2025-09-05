package com.example.homeal_app.ui.Shopping

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.homeal_app.data.repository.ShoppingRepository
import com.example.homeal_app.model.ShoppingItem
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

@ExperimentalCoroutinesApi
class ShoppingViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    @MockK
    private lateinit var repository: ShoppingRepository

    private lateinit var viewModel: ShoppingViewModel

    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        Dispatchers.setMain(testDispatcher)
        
        coEvery { repository.getAllItemsFlow() } returns flowOf(emptyList())
        
        viewModel = ShoppingViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `addItem should call repository addItem`() = runTest {
        // Given
        val item = ShoppingItem(
            id = 1,
            name = "Milk",
            quantity = 2.0,
            unit = "liters",
            isCompleted = false,
            category = "Dairy"
        )
        
        coEvery { repository.addItem(any()) } returns Unit
        
        // When
        viewModel.addItem(item)
        
        // Then
        verify { repository.addItem(item) }
    }

    @Test
    fun `deleteItem should call repository deleteItem`() = runTest {
        // Given
        val item = ShoppingItem(
            id = 1,
            name = "Milk",
            quantity = 2.0,
            unit = "liters",
            isCompleted = false,
            category = "Dairy"
        )
        
        coEvery { repository.deleteItem(any()) } returns Unit
        
        // When
        viewModel.deleteItem(item)
        
        // Then
        verify { repository.deleteItem(item) }
    }

    @Test
    fun `updateItem should call repository updateItem`() = runTest {
        // Given
        val item = ShoppingItem(
            id = 1,
            name = "Milk",
            quantity = 2.0,
            unit = "liters",
            isCompleted = true,
            category = "Dairy"
        )
        
        coEvery { repository.updateItem(any()) } returns Unit
        
        // When
        viewModel.updateItem(item)
        
        // Then
        verify { repository.updateItem(item) }
    }
}