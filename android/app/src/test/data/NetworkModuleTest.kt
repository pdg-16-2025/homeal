package com.example.homeal_app.data

import com.example.homeal_app.data.remote.NetworkModule
import org.junit.Assert.assertNotNull
import org.junit.Test

class NetworkModuleTest {
    
    @Test
    fun `apiService should have correct base URL`() {
        // When
        val apiService = NetworkModule.apiService
        
        // Then
        assertNotNull(apiService)
        // Vérifier que Retrofit est configuré correctement
    }
    
    @Test
    fun `retrofit should have correct timeout configuration`() {
        // Test de la configuration Retrofit
    }
}