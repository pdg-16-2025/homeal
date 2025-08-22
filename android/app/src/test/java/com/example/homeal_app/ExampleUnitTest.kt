package com.example.homeal_app

import com.google.common.truth.Truth.assertThat
import org.junit.Test

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    
    @Test
    fun addition_isCorrect() {
        val result = 2 + 2
        assertThat(result).isEqualTo(4)
    }
    
    @Test
    fun string_concatenation_works() {
        val firstName = "Homeal"
        val lastName = "App"
        val fullName = "$firstName $lastName"
        assertThat(fullName).isEqualTo("Homeal App")
    }
    
    @Test
    fun list_operations_work_correctly() {
        val numbers = listOf(1, 2, 3, 4, 5)
        val evenNumbers = numbers.filter { it % 2 == 0 }
        
        assertThat(evenNumbers).hasSize(2)
        assertThat(evenNumbers).containsExactly(2, 4)
    }
}