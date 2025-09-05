package main

import (
	"database/sql"
	"encoding/json"
	"net/http"
	"net/http/httptest"
	"net/url"
	"strings"
	"testing"

	_ "github.com/mattn/go-sqlite3"
)

// setupTestDB creates an in-memory SQLite database for testing
func setupTestDB(t *testing.T) *sql.DB {
	db, err := sql.Open("sqlite3", ":memory:")
	if err != nil {
		t.Fatalf("Failed to create test database: %v", err)
	}

	schema := `
	CREATE TABLE Ingredient (
		id INTEGER PRIMARY KEY,
		name TEXT NOT NULL
	);

	CREATE TABLE Product (
		code TEXT PRIMARY KEY,
		ingredient_id INTEGER,
		FOREIGN KEY (ingredient_id) REFERENCES Ingredient(id)
	);

	CREATE TABLE Recipe (
		id INTEGER PRIMARY KEY,
		name TEXT NOT NULL,
		author_id INTEGER,
		cook_time INTEGER,
		prep_time INTEGER,
		total_time INTEGER,
		images TEXT,
		category TEXT,
		keywords TEXT,
		aggregated_rating REAL,
		calories REAL,
		fat_content REAL,
		saturated_fat_content REAL,
		cholesterol_content REAL,
		sodium_content REAL,
		carbohydrate_content REAL,
		fiber_content REAL,
		sugar_content REAL,
		protein_content REAL,
		recipe_servings INTEGER,
		recipe_yield TEXT,
		recipe_instructions TEXT
	);

	CREATE TABLE RecipeIngredient (
		id INTEGER PRIMARY KEY,
		recipe_id INTEGER,
		ingredient_id INTEGER,
		quantity TEXT,
		unit TEXT,
		FOREIGN KEY (recipe_id) REFERENCES Recipe(id),
		FOREIGN KEY (ingredient_id) REFERENCES Ingredient(id)
	);
	`

	if _, err := db.Exec(schema); err != nil {
		t.Fatalf("Failed to create test schema: %v", err)
	}

	return db
}

// seedTestData adds test data to the database
func seedTestData(t *testing.T, db *sql.DB) {
	ingredients := []struct {
		id   int
		name string
	}{
		{1, "Tomato"},
		{2, "Onion"},
		{3, "Garlic"},
		{4, "Basil"},
		{5, "Olive Oil"},
	}

	for _, ing := range ingredients {
		_, err := db.Exec("INSERT INTO Ingredient (id, name) VALUES (?, ?)", ing.id, ing.name)
		if err != nil {
			t.Fatalf("Failed to insert test ingredient: %v", err)
		}
	}

	products := []struct {
		code         string
		ingredientId int
	}{
		{"0007200000021", 1}, // Tomato
		{"1234567890123", 2}, // Onion
	}

	for _, prod := range products {
		_, err := db.Exec("INSERT INTO Product (code, ingredient_id) VALUES (?, ?)", prod.code, prod.ingredientId)
		if err != nil {
			t.Fatalf("Failed to insert test product: %v", err)
		}
	}

	recipes := []struct {
		id                  int
		name                string
		authorId            int
		cookTime            *int
		prepTime            *int
		totalTime           *int
		images              *string
		category            string
		keywords            *string
		aggregatedRating    float64
		calories            float64
		fatContent          float64
		saturatedFatContent float64
		cholesterolContent  float64
		sodiumContent       float64
		carbohydrateContent float64
		fiberContent        float64
		sugarContent        float64
		proteinContent      float64
		recipeServings      *int
		recipeYield         *string
		recipeInstructions  string
	}{
		{
			id: 210, name: "Tomato Pasta", authorId: 1,
			cookTime: intPtr(20), prepTime: intPtr(10), totalTime: intPtr(30),
			images: stringPtr("pasta.jpg"), category: "Main", keywords: stringPtr("pasta,tomato"),
			aggregatedRating: 4.5, calories: 350, fatContent: 12,
			saturatedFatContent: 3, cholesterolContent: 0, sodiumContent: 400,
			carbohydrateContent: 55, fiberContent: 3, sugarContent: 8,
			proteinContent: 12, recipeServings: intPtr(4), recipeYield: stringPtr("4 servings"),
			recipeInstructions: "Cook pasta, add tomato sauce",
		},
		{
			id: 139, name: "Garlic Bread", authorId: 1,
			cookTime: intPtr(15), prepTime: intPtr(5), totalTime: intPtr(20),
			images: stringPtr("bread.jpg"), category: "Side", keywords: stringPtr("bread,garlic"),
			aggregatedRating: 4.0, calories: 200, fatContent: 8,
			saturatedFatContent: 2, cholesterolContent: 0, sodiumContent: 300,
			carbohydrateContent: 25, fiberContent: 1, sugarContent: 2,
			proteinContent: 6, recipeServings: intPtr(2), recipeYield: stringPtr("2 servings"),
			recipeInstructions: "Toast bread with garlic butter",
		},
		{
			id: 300, name: "Simple Salad", authorId: 2,
			cookTime: nil, prepTime: intPtr(5), totalTime: intPtr(5),
			images: nil, category: "Salad", keywords: nil,
			aggregatedRating: 3.5, calories: 120, fatContent: 5,
			saturatedFatContent: 1, cholesterolContent: 0, sodiumContent: 150,
			carbohydrateContent: 15, fiberContent: 4, sugarContent: 8,
			proteinContent: 3, recipeServings: nil, recipeYield: nil,
			recipeInstructions: "Mix vegetables",
		},
	}

	for _, recipe := range recipes {
		_, err := db.Exec(`
			INSERT INTO Recipe (id, name, author_id, cook_time, prep_time, total_time, images, category, keywords,
			aggregated_rating, calories, fat_content, saturated_fat_content, cholesterol_content, sodium_content,
			carbohydrate_content, fiber_content, sugar_content, protein_content, recipe_servings, recipe_yield, recipe_instructions)
			VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)`,
			recipe.id, recipe.name, recipe.authorId, recipe.cookTime, recipe.prepTime, recipe.totalTime,
			recipe.images, recipe.category, recipe.keywords, recipe.aggregatedRating, recipe.calories,
			recipe.fatContent, recipe.saturatedFatContent, recipe.cholesterolContent, recipe.sodiumContent,
			recipe.carbohydrateContent, recipe.fiberContent, recipe.sugarContent, recipe.proteinContent,
			recipe.recipeServings, recipe.recipeYield, recipe.recipeInstructions)
		if err != nil {
			t.Fatalf("Failed to insert test recipe: %v", err)
		}
	}

	// Insert recipe ingredients
	recipeIngredients := []struct {
		id           int
		recipeId     int
		ingredientId int
		quantity     string
		unit         string
	}{
		{1, 210, 1, "2", "cups"},     // Tomato Pasta - Tomato
		{2, 210, 3, "3", "cloves"},   // Tomato Pasta - Garlic
		{3, 210, 5, "2", "tbsp"},     // Tomato Pasta - Olive Oil
		{4, 139, 3, "4", "cloves"},   // Garlic Bread - Garlic
		{5, 139, 5, "3", "tbsp"},     // Garlic Bread - Olive Oil
		{6, 300, 1, "1", "large"},    // Simple Salad - Tomato
		{7, 300, 2, "1/2", "medium"}, // Simple Salad - Onion
	}

	for _, ri := range recipeIngredients {
		_, err := db.Exec("INSERT INTO RecipeIngredient (id, recipe_id, ingredient_id, quantity, unit) VALUES (?, ?, ?, ?, ?)",
			ri.id, ri.recipeId, ri.ingredientId, ri.quantity, ri.unit)
		if err != nil {
			t.Fatalf("Failed to insert test recipe ingredient: %v", err)
		}
	}
}

// Helper functions
func intPtr(i int) *int {
	return &i
}

func stringPtr(s string) *string {
	return &s
}

// TestHandleScan tests the handleScan function
func TestHandleScan(t *testing.T) {
	db := setupTestDB(t)
	defer db.Close()
	seedTestData(t, db)

	handler := &Handler{db: db}

	tests := []struct {
		name           string
		code           string
		expectedStatus int
		expectedName   string
	}{
		{
			name:           "Valid product code",
			code:           "0007200000021",
			expectedStatus: http.StatusOK,
			expectedName:   "Tomato",
		},
		{
			name:           "Another valid product code",
			code:           "1234567890123",
			expectedStatus: http.StatusOK,
			expectedName:   "Onion",
		},
		{
			name:           "Non-existent product code",
			code:           "9999999999999",
			expectedStatus: http.StatusNotFound,
		},
		{
			name:           "Missing code parameter",
			code:           "",
			expectedStatus: http.StatusBadRequest,
		},
	}

	for _, tt := range tests {
		t.Run(tt.name, func(t *testing.T) {
			req := httptest.NewRequest("GET", "/scan?code="+tt.code, nil)
			w := httptest.NewRecorder()

			handler.handleScan(w, req)

			if w.Code != tt.expectedStatus {
				t.Errorf("Expected status %d, got %d", tt.expectedStatus, w.Code)
			}

			if tt.expectedStatus == http.StatusOK {
				var ingredient Ingredient
				if err := json.Unmarshal(w.Body.Bytes(), &ingredient); err != nil {
					t.Errorf("Failed to unmarshal response: %v", err)
				}
				if ingredient.Name != tt.expectedName {
					t.Errorf("Expected ingredient name %s, got %s", tt.expectedName, ingredient.Name)
				}
			}
		})
	}
}

// TestHandleRecipe tests the handleRecipe function
func TestHandleRecipe(t *testing.T) {
	db := setupTestDB(t)
	defer db.Close()
	seedTestData(t, db)

	handler := &Handler{db: db}

	tests := []struct {
		name                    string
		id                      string
		expectedStatus          int
		expectedName            string
		expectedIngredientCount int
	}{
		{
			name:                    "Valid recipe ID with ingredients",
			id:                      "210",
			expectedStatus:          http.StatusOK,
			expectedName:            "Tomato Pasta",
			expectedIngredientCount: 3,
		},
		{
			name:                    "Valid recipe ID with null values",
			id:                      "300",
			expectedStatus:          http.StatusOK,
			expectedName:            "Simple Salad",
			expectedIngredientCount: 2,
		},
		{
			name:           "Non-existent recipe ID",
			id:             "9999",
			expectedStatus: http.StatusNotFound,
		},
		{
			name:           "Missing ID parameter",
			id:             "",
			expectedStatus: http.StatusBadRequest,
		},
	}

	for _, tt := range tests {
		t.Run(tt.name, func(t *testing.T) {
			req := httptest.NewRequest("GET", "/recipe?id="+tt.id, nil)
			w := httptest.NewRecorder()

			handler.handleRecipe(w, req)

			if w.Code != tt.expectedStatus {
				t.Errorf("Expected status %d, got %d", tt.expectedStatus, w.Code)
			}

			if tt.expectedStatus == http.StatusOK {
				var recipe Recipe
				if err := json.Unmarshal(w.Body.Bytes(), &recipe); err != nil {
					t.Errorf("Failed to unmarshal response: %v", err)
				}
				if recipe.Name != tt.expectedName {
					t.Errorf("Expected recipe name %s, got %s", tt.expectedName, recipe.Name)
				}
				if len(recipe.Ingredients) != tt.expectedIngredientCount {
					t.Errorf("Expected %d ingredients, got %d", tt.expectedIngredientCount, len(recipe.Ingredients))
				}

				// Test null value handling for recipe 300
				if tt.id == "300" {
					if recipe.CookTime != -1 {
						t.Errorf("Expected cook_time to be -1 for null value, got %d", recipe.CookTime)
					}
					if recipe.RecipeServings != -1 {
						t.Errorf("Expected recipe_servings to be -1 for null value, got %d", recipe.RecipeServings)
					}
					if recipe.Images != "" {
						t.Errorf("Expected images to be empty for null value, got %s", recipe.Images)
					}
				}
			}
		})
	}
}

// TestHandleIngredients tests the handleIngredients function
func TestHandleIngredients(t *testing.T) {
	db := setupTestDB(t)
	defer db.Close()
	seedTestData(t, db)

	handler := &Handler{db: db}

	tests := []struct {
		name           string
		search         string
		limit          string
		expectedStatus int
		expectedCount  int
		shouldContain  string
	}{
		{
			name:           "Search for tomato",
			search:         "tomato",
			limit:          "",
			expectedStatus: http.StatusOK,
			expectedCount:  1,
			shouldContain:  "Tomato",
		},
		{
			name:           "Search case insensitive",
			search:         "TOMATO",
			limit:          "",
			expectedStatus: http.StatusOK,
			expectedCount:  1,
			shouldContain:  "Tomato",
		},
		{
			name:           "Search partial match",
			search:         "o",
			limit:          "",
			expectedStatus: http.StatusOK,
			expectedCount:  3, // Tomato, Onion, Olive Oil
		},
		{
			name:           "No search term - get all with default limit",
			search:         "",
			limit:          "",
			expectedStatus: http.StatusOK,
			expectedCount:  5, // All ingredients
		},
		{
			name:           "Custom limit",
			search:         "",
			limit:          "2",
			expectedStatus: http.StatusOK,
			expectedCount:  2,
		},
		{
			name:           "Invalid limit",
			search:         "",
			limit:          "invalid",
			expectedStatus: http.StatusBadRequest,
		},
		{
			name:           "Search no results",
			search:         "nonexistent",
			limit:          "",
			expectedStatus: http.StatusOK,
			expectedCount:  0,
		},
	}

	for _, tt := range tests {
		t.Run(tt.name, func(t *testing.T) {
			params := url.Values{}
			if tt.search != "" {
				params.Set("search", tt.search)
			}
			if tt.limit != "" {
				params.Set("limit", tt.limit)
			}

			url := "/ingredients"
			if len(params) > 0 {
				url += "?" + params.Encode()
			}

			req := httptest.NewRequest("GET", url, nil)
			w := httptest.NewRecorder()

			handler.handleIngredients(w, req)

			if w.Code != tt.expectedStatus {
				t.Errorf("Expected status %d, got %d", tt.expectedStatus, w.Code)
			}

			if tt.expectedStatus == http.StatusOK {
				var ingredients []Ingredient
				if err := json.Unmarshal(w.Body.Bytes(), &ingredients); err != nil {
					t.Errorf("Failed to unmarshal response: %v", err)
				}
				if len(ingredients) != tt.expectedCount {
					t.Errorf("Expected %d ingredients, got %d", tt.expectedCount, len(ingredients))
				}
				if tt.shouldContain != "" {
					found := false
					for _, ing := range ingredients {
						if strings.Contains(ing.Name, tt.shouldContain) {
							found = true
							break
						}
					}
					if !found {
						t.Errorf("Expected to find ingredient containing %s", tt.shouldContain)
					}
				}
			}
		})
	}
}

// TestHandleRecommendations tests the handleRecommendations function
func TestHandleRecommendations(t *testing.T) {
	db := setupTestDB(t)
	defer db.Close()
	seedTestData(t, db)

	handler := &Handler{db: db}

	tests := []struct {
		name           string
		recoType       string
		data           string
		number         string
		expectedStatus int
		expectedCount  int
	}{
		{
			name:           "Random recommendations with default number",
			recoType:       "random",
			data:           "{}",
			number:         "",
			expectedStatus: http.StatusOK,
			expectedCount:  3, // We have 3 recipes in test data, default limit is 5
		},
		{
			name:           "Random recommendations with custom number",
			recoType:       "random",
			data:           "{}",
			number:         "2",
			expectedStatus: http.StatusOK,
			expectedCount:  2,
		},
		{
			name:           "Preferences type - not implemented",
			recoType:       "preferences",
			data:           "{}",
			number:         "",
			expectedStatus: http.StatusNotImplemented,
		},
		{
			name:           "Ingredients type - not implemented",
			recoType:       "ingredients",
			data:           "{}",
			number:         "",
			expectedStatus: http.StatusNotImplemented,
		},
		{
			name:           "Missing type parameter",
			recoType:       "",
			data:           "{}",
			number:         "",
			expectedStatus: http.StatusBadRequest,
		},
		{
			name:           "Missing data parameter",
			recoType:       "random",
			data:           "",
			number:         "",
			expectedStatus: http.StatusBadRequest,
		},
	}

	for _, tt := range tests {
		t.Run(tt.name, func(t *testing.T) {
			params := url.Values{}
			if tt.recoType != "" {
				params.Set("type", tt.recoType)
			}
			if tt.data != "" {
				params.Set("data", tt.data)
			}
			if tt.number != "" {
				params.Set("number", tt.number)
			}

			url := "/recommendations"
			if len(params) > 0 {
				url += "?" + params.Encode()
			}

			req := httptest.NewRequest("GET", url, nil)
			w := httptest.NewRecorder()

			handler.handleRecommendations(w, req)

			if w.Code != tt.expectedStatus {
				t.Errorf("Expected status %d, got %d. Response body: %s", tt.expectedStatus, w.Code, w.Body.String())
			}

			if tt.expectedStatus == http.StatusOK {
				var recipes []ShortRecipe
				if err := json.Unmarshal(w.Body.Bytes(), &recipes); err != nil {
					t.Errorf("Failed to unmarshal response: %v. Response body: %s", err, w.Body.String())
				}
				if len(recipes) != tt.expectedCount {
					t.Errorf("Expected %d recipes, got %d", tt.expectedCount, len(recipes))
				}
				// Verify structure of ShortRecipe
				if len(recipes) > 0 {
					recipe := recipes[0]
					if recipe.Id == 0 || recipe.Name == "" {
						t.Errorf("Recipe should have valid ID and Name")
					}
				}
			}
		})
	}
}

// TestHandleSearchRecipes tests the handleSearchRecipes function
func TestHandleSearchRecipes(t *testing.T) {
	db := setupTestDB(t)
	defer db.Close()
	seedTestData(t, db)

	handler := &Handler{db: db}

	tests := []struct {
		name           string
		search         string
		limit          string
		expectedStatus int
		expectedCount  int
		shouldContain  string
	}{
		{
			name:           "Search for pasta",
			search:         "pasta",
			limit:          "",
			expectedStatus: http.StatusOK,
			expectedCount:  1,
			shouldContain:  "Tomato Pasta",
		},
		{
			name:           "Search case insensitive",
			search:         "PASTA",
			limit:          "",
			expectedStatus: http.StatusOK,
			expectedCount:  1,
			shouldContain:  "Tomato Pasta",
		},
		{
			name:           "Search partial match",
			search:         "a",
			limit:          "",
			expectedStatus: http.StatusOK,
			expectedCount:  3, // Tomato Pasta, Garlic Bread, Simple Salad
		},
		{
			name:           "No search term - get all with default limit",
			search:         "",
			limit:          "",
			expectedStatus: http.StatusOK,
			expectedCount:  3, // All recipes
		},
		{
			name:           "Custom limit",
			search:         "",
			limit:          "2",
			expectedStatus: http.StatusOK,
			expectedCount:  2,
		},
		{
			name:           "Invalid limit",
			search:         "",
			limit:          "invalid",
			expectedStatus: http.StatusBadRequest,
		},
		{
			name:           "Search no results",
			search:         "nonexistent",
			limit:          "",
			expectedStatus: http.StatusOK,
			expectedCount:  0,
		},
	}

	for _, tt := range tests {
		t.Run(tt.name, func(t *testing.T) {
			params := url.Values{}
			if tt.search != "" {
				params.Set("search", tt.search)
			}
			if tt.limit != "" {
				params.Set("limit", tt.limit)
			}

			url := "/search-recipes"
			if len(params) > 0 {
				url += "?" + params.Encode()
			}

			req := httptest.NewRequest("GET", url, nil)
			w := httptest.NewRecorder()

			handler.handleSearchRecipes(w, req)

			if w.Code != tt.expectedStatus {
				t.Errorf("Expected status %d, got %d. Response body: %s", tt.expectedStatus, w.Code, w.Body.String())
			}

			if tt.expectedStatus == http.StatusOK {
				var recipes []ShortRecipe
				if err := json.Unmarshal(w.Body.Bytes(), &recipes); err != nil {
					t.Errorf("Failed to unmarshal response: %v. Response body: %s", err, w.Body.String())
				}
				if len(recipes) != tt.expectedCount {
					t.Errorf("Expected %d recipes, got %d", tt.expectedCount, len(recipes))
				}
				if tt.shouldContain != "" {
					found := false
					for _, recipe := range recipes {
						if strings.Contains(recipe.Name, tt.shouldContain) {
							found = true
							break
						}
					}
					if !found {
						t.Errorf("Expected to find recipe containing %s", tt.shouldContain)
					}
				}
			}
		})
	}
}

// TestHandleRecipeIngredients tests the handleRecipeIngredients function
func TestHandleRecipeIngredients(t *testing.T) {
	db := setupTestDB(t)
	defer db.Close()
	seedTestData(t, db)

	handler := &Handler{db: db}

	tests := []struct {
		name           string
		recipeId       string
		expectedStatus int
		expectedCount  int
		shouldContain  []string
	}{
		{
			name:           "Valid recipe with multiple ingredients",
			recipeId:       "210",
			expectedStatus: http.StatusOK,
			expectedCount:  3,
			shouldContain:  []string{"Tomato", "Garlic", "Olive Oil"},
		},
		{
			name:           "Valid recipe with fewer ingredients",
			recipeId:       "139",
			expectedStatus: http.StatusOK,
			expectedCount:  2,
			shouldContain:  []string{"Garlic", "Olive Oil"},
		},
		{
			name:           "Recipe with no ingredients in our test data",
			recipeId:       "9999",
			expectedStatus: http.StatusOK,
			expectedCount:  0,
		},
		{
			name:           "Missing recipe_id parameter",
			recipeId:       "",
			expectedStatus: http.StatusBadRequest,
		},
	}

	for _, tt := range tests {
		t.Run(tt.name, func(t *testing.T) {
			url := "/recipe-ingredients"
			if tt.recipeId != "" {
				url += "?recipe_id=" + tt.recipeId
			}

			req := httptest.NewRequest("GET", url, nil)
			w := httptest.NewRecorder()

			handler.handleRecipeIngredients(w, req)

			if w.Code != tt.expectedStatus {
				t.Errorf("Expected status %d, got %d", tt.expectedStatus, w.Code)
			}

			if tt.expectedStatus == http.StatusOK {
				var recipeIngredients []RecipeIngredient
				if err := json.Unmarshal(w.Body.Bytes(), &recipeIngredients); err != nil {
					t.Errorf("Failed to unmarshal response: %v", err)
				}
				if len(recipeIngredients) != tt.expectedCount {
					t.Errorf("Expected %d recipe ingredients, got %d", tt.expectedCount, len(recipeIngredients))
				}

				// Check that expected ingredients are present
				for _, expectedName := range tt.shouldContain {
					found := false
					for _, ri := range recipeIngredients {
						if ri.Name == expectedName {
							found = true
							// Verify that quantity and unit are present
							if ri.Quantity == "" || ri.Unit == "" {
								t.Errorf("Recipe ingredient %s should have quantity and unit", expectedName)
							}
							break
						}
					}
					if !found {
						t.Errorf("Expected to find ingredient %s in recipe ingredients", expectedName)
					}
				}
			}
		})
	}
}
