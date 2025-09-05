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
