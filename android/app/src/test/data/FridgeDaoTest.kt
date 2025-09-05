package com.example.homeal_app.data

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.homeal_app.data.local.database.AppDatabase
import com.example.homeal_app.data.local.dao.FridgeDao
import com.example.homeal_app.model.FridgeIngredient
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FridgeDaoTest {
    
    private lateinit var database: AppDatabase
    private lateinit var fridgeDao: FridgeDao
    
    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            InstrumentationRegistry.getInstrumentation().context,
            AppDatabase::class.java
        ).allowMainThreadQueries().build()
        
        fridgeDao = database.fridgeDao()
    }
    
    @After
    fun cleanup() {
        database.close()
    }
    
    @Test
    fun `addIngredient and getAllIngredients should work`() = runTest {
        // Given
        val ingredient = FridgeIngredient(
            name = "Test",
            quantity = 1,
            unit = "pcs"
        )
        
        // When
        fridgeDao.addIngredient(ingredient)
        val ingredients = fridgeDao.getAllFridgeIngredients().first()
        
        // Then
        assertEquals(1, ingredients.size)
        assertEquals("Test", ingredients[0].name)
    }
}