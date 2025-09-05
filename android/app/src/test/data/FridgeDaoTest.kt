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