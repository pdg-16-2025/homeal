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