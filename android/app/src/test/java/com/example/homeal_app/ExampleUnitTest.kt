package com.example.homeal_app

import org.junit.Test
import org.junit.Assert.*
import org.junit.Before
import org.junit.After

class ExampleUnitTest {

    @Before
    fun setUp() {
        // Configuration avant chaque test
    }

    @After
    fun tearDown() {
        // Nettoyage après chaque test
    }

    @Test
    fun `addition should work correctly`() {
        val result = 2 + 2
        assertEquals(4, result)
    }

    @Test
    fun `string operations should work`() {
        val text = "Hello World"
        assertTrue(text.contains("World"))
        assertEquals(11, text.length)
    }

    @Test
    fun `list operations should work`() {
        val list = mutableListOf<String>()
        list.add("test")
        list.add("android")
        
        assertEquals(2, list.size)
        assertTrue(list.contains("test"))
        assertTrue(list.contains("android"))
    }

    @Test
    fun `numbers should be positive`() {
        val numbers = listOf(1, 2, 3, 4, 5)
        numbers.forEach { number ->
            assertTrue("Number $number should be positive", number > 0)
        }
    }

    @Test
    fun `calculator operations should work`() {
        // Test multiplication
        val multiplyResult = 3 * 4
        assertEquals(12, multiplyResult)
        
        // Test division
        val divideResult = 10 / 2
        assertEquals(5, divideResult)
        
        // Test subtraction
        val subtractResult = 10 - 3
        assertEquals(7, subtractResult)
    }
}