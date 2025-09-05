package com.example.homeal_app.repository

import com.example.homeal_app.data.local.dao.FridgeDao
import com.example.homeal_app.data.remote.ApiService
import com.example.homeal_app.data.repository.FridgeRepository
import com.example.homeal_app.model.Ingredient
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.Mock
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@ExperimentalCoroutinesApi
class FridgeRepositoryTest {
    
    @Mock
    private lateinit var fridgeDao: FridgeDao
    
    @Mock
    private lateinit var apiService: ApiService
    
    private lateinit var repository: FridgeRepository

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        repository = FridgeRepository(fridgeDao, apiService)
    }

    @Test
    fun `addIngredient should call dao addIngredient`() = runTest {
        // Given
        val name = "Tomato"
        val quantity = 2
        val unit = "pcs"
        
        // When
        repository.addIngredient(name, quantity, unit)
        
        // Then
        coVerify { fridgeDao.addIngredient(any()) }
    }

    @Test
    fun `searchAvailableIngredients should call apiService with correct query`() = runTest {
        // Given
        val query = "butter"
        val mockIngredients = listOf(Ingredient(id = 1, name = "Butter"))
        coEvery { apiService.searchIngredients(search = query, limit = 20) } returns mockIngredients
        
        // When
        val result = repository.searchAvailableIngredients(query)
        
        // Then
        assertEquals(mockIngredients, result)
        coVerify { apiService.searchIngredients(search = query, limit = 20) }
    }
}