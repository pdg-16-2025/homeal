package com.example.homeal_app.data

import com.example.homeal_app.model.FridgeIngredient
import com.example.homeal_app.model.ShoppingIngredient
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FridgeIngredientTest {
    
    @Test
    fun `create FridgeIngredient with default values`() {
        // Given & When
        val ingredient = FridgeIngredient(
            name = "Tomato",
            quantity = 2,
            unit = "pcs"
        )
        
        // Then
        assertEquals("Tomato", ingredient.name)
        assertEquals(2, ingredient.quantity)
        assertEquals("pcs", ingredient.unit)
        assertEquals(0, ingredient.id)
        assertEquals(0, ingredient.ingredientId)
        assertTrue(ingredient.addedDate > 0)
    }
}

// ShoppingIngredientTest.kt
class ShoppingIngredientTest {
    
    @Test
    fun `create ShoppingIngredient with default isDone false`() {
        // Given & When
        val ingredient = ShoppingIngredient(
            name = "Milk",
            quantity = "1 L"
        )
        
        // Then
        assertFalse(ingredient.isDone)
        assertEquals("Milk", ingredient.name)
        assertEquals("1 L", ingredient.quantity)
    }
}