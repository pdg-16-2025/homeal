@ExperimentalCoroutinesApi
class FridgeRepositoryTest {
    
    @Mock
    private lateinit var fridgeDao: FridgeDao
    
    @Mock
    private lateinit var apiService: ApiService
    
    private lateinit var repository: FridgeRepository

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
        coEvery { apiService.searchIngredients(query, 20) } returns mockIngredients
        
        // When
        val result = repository.searchAvailableIngredients(query)
        
        // Then
        assertEquals(mockIngredients, result)
        coVerify { apiService.searchIngredients(query, 20) }
    }
}