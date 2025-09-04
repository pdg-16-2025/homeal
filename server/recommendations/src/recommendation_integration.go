package main

import (
	"encoding/json"
	"fmt"
	"net/http"
	"os/exec"
	"strconv"

	_ "github.com/mattn/go-sqlite3"
)

// Updated recommendation handler that integrates with Python recommendation systems
func (h *Handler) handleRecommendationsWithPython(w http.ResponseWriter, r *http.Request) {
	recoType := RecommendationType(r.URL.Query().Get("type"))
	if recoType == "" {
		http.Error(w, "Missing 'type' parameter", http.StatusBadRequest)
		return
	}

	data := r.URL.Query().Get("data")
	if data == "" {
		http.Error(w, "Missing 'data' parameter", http.StatusBadRequest)
		return
	}

	number := 5
	if n := r.URL.Query().Get("number"); n != "" {
		fmt.Sscanf(n, "%d", &number)
	}

	switch recoType {
	case RANDOM:
		// Keep existing random implementation
		h.handleRandomRecommendations(w, r, number)

	case INGREDIENTS:
		// Call Python leftover/ingredients recommendation system
		cmd := exec.Command("python3", "recommendations/src/recommendation_api.py", "ingredients", data, strconv.Itoa(number))
		output, err := cmd.Output()
		if err != nil {
			http.Error(w, fmt.Sprintf("Ingredients recommendation error: %v", err), http.StatusInternalServerError)
			return
		}

		w.Header().Set("Content-Type", "application/json")
		w.Write(output)

	case PREFERENCES:
		// Call Python preference-based recommendation system
		cmd := exec.Command("python3", "recommendations/src/recommendation_api.py", "preferences", data, strconv.Itoa(number))
		output, err := cmd.Output()
		if err != nil {
			http.Error(w, fmt.Sprintf("Preferences recommendation error: %v", err), http.StatusInternalServerError)
			return
		}

		w.Header().Set("Content-Type", "application/json")
		w.Write(output)

	case "nutriments": // Add new type for nutritional recommendations
		cmd := exec.Command("python3", "recommendations/src/recommendation_api.py", "nutriments", data, strconv.Itoa(number))
		output, err := cmd.Output()
		if err != nil {
			http.Error(w, fmt.Sprintf("Nutriments recommendation error: %v", err), http.StatusInternalServerError)
			return
		}

		w.Header().Set("Content-Type", "application/json")
		w.Write(output)

	default:
		http.Error(w, "Unknown recommendation type", http.StatusBadRequest)
	}
}

// Helper function for existing random recommendations
func (h *Handler) handleRandomRecommendations(w http.ResponseWriter, r *http.Request, number int) {
	rows, err := h.db.Query("SELECT id, name, total_time, images FROM Recipe ORDER BY RANDOM() LIMIT ?", number)
	if err != nil {
		http.Error(w, fmt.Sprintf("Database query error: %v", err), http.StatusInternalServerError)
		return
	}
	defer rows.Close()

	recommendations := []ShortRecipe{}
	for rows.Next() {
		var recipe ShortRecipe
		var totalTime, imageURL interface{}

		if err := rows.Scan(&recipe.Id, &recipe.Name, &totalTime, &imageURL); err != nil {
			http.Error(w, fmt.Sprintf("Row scan error: %v", err), http.StatusInternalServerError)
			return
		}

		if totalTime != nil {
			recipe.TotalTime = int(totalTime.(int64))
		}
		if imageURL != nil {
			recipe.ImageURL = imageURL.(string)
		}

		recommendations = append(recommendations, recipe)
	}

	w.Header().Set("Content-Type", "application/json")
	if err := json.NewEncoder(w).Encode(recommendations); err != nil {
		http.Error(w, fmt.Sprintf("JSON encoding error: %v", err), http.StatusInternalServerError)
		return
	}
}

/*
INTEGRATION STEPS:

1. Update types.go - Add NUTRIMENTS constant:
   const (
       RANDOM      RecommendationType = "random"
       PREFERENCES RecommendationType = "preferences"
       INGREDIENTS RecommendationType = "ingredients"
       NUTRIMENTS  RecommendationType = "nutriments"  // Add this line
   )

2. Replace recommendation.go function:
   Replace handleRecommendations with handleRecommendationsWithPython

3. Ensure Python files are in recommendations/src/ directory

4. Test endpoints:

   # Random (existing)
   curl "http://localhost:8080/recommendations?type=random&number=3"

   # Ingredients recommendation
   curl "http://localhost:8080/recommendations?type=ingredients&data={\"ingredients\":[{\"name\":\"tomato\",\"quantity\":2,\"unit\":\"pieces\",\"expiration_date\":\"2024-01-15\"}]}&number=3"

   # Nutriments recommendation
   curl "http://localhost:8080/recommendations?type=nutriments&data={\"age\":30,\"gender\":\"male\",\"weight\":75,\"height\":180,\"activity_level\":\"moderately_active\"}&number=5"

   # Preferences recommendation (requires UserRecipeRating table)
   curl "http://localhost:8080/recommendations?type=preferences&data={\"user_id\":123,\"ratings\":[{\"recipe_id\":1,\"rating\":4.5}]}&number=3"

DATABASE REQUIREMENTS:

1. Create UserRecipeRating table:
   CREATE TABLE UserRecipeRating (
       id INTEGER PRIMARY KEY AUTOINCREMENT,
       user_id INTEGER NOT NULL,
       recipe_id INTEGER NOT NULL,
       rating REAL NOT NULL CHECK(rating >= 0 AND rating <= 5),
       created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
       FOREIGN KEY (recipe_id) REFERENCES Recipe(id),
       UNIQUE(user_id, recipe_id)
   );

2. Create RecipeIngredient table:
   CREATE TABLE RecipeIngredient (
       id INTEGER PRIMARY KEY AUTOINCREMENT,
       recipe_id INTEGER NOT NULL,
       ingredient_name TEXT NOT NULL,
       quantity TEXT,
       unit TEXT,
       FOREIGN KEY (recipe_id) REFERENCES Recipe(id)
   );

3. Ensure Recipe table has nutritional fields populated:
   - calories, protein_content, carbohydrate_content, fat_content
   - fiber_content, sodium_content, saturated_fat_content, sugar_content

TESTING:

Run unit tests:
   cd recommendations/test/
   python3 run_tests.py

Run specific test:
   python3 run_tests.py leftover
   python3 run_tests.py nutriment
   python3 run_tests.py preference
   python3 run_tests.py api

DEPLOYMENT NOTES:

- Ensure Python 3.7+ is available in production
- Ensure recommendations/src/ directory is accessible from Go server
- Consider adding logging for Python subprocess calls
- Monitor Python script execution times for performance
*/
