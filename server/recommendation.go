package main

import (
	"encoding/json"
	"fmt"
	"net/http"
	"os/exec"
	"strconv"

	_ "github.com/mattn/go-sqlite3"
)

func (h *Handler) handleRecommendations(w http.ResponseWriter, r *http.Request) {
	recoType := RecommendationType(r.URL.Query().Get("type"))
	if recoType == "" {
		http.Error(w, "Missing 'type' parameter", http.StatusBadRequest)
		return
	}

	// Get data parameter
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
		// Get random recipes from database
		query := `
			SELECT id, name, total_time, images
			FROM Recipe
			ORDER BY RANDOM()
			LIMIT ?`

		rows, err := h.db.Query(query, number)
		if err != nil {
			http.Error(w, fmt.Sprintf("Database query error: %v", err), http.StatusInternalServerError)
			return
		}
		defer rows.Close()

		var recipes []ShortRecipe
		for rows.Next() {
			var recipe ShortRecipe
			var images *string
			err := rows.Scan(&recipe.Id, &recipe.Name, &recipe.TotalTime, &images)
			if err != nil {
				http.Error(w, fmt.Sprintf("Row scan error: %v", err), http.StatusInternalServerError)
				return
			}
			if images != nil {
				recipe.ImageURL = *images
			}
			recipes = append(recipes, recipe)
		}

		if err = rows.Err(); err != nil {
			http.Error(w, fmt.Sprintf("Rows iteration error: %v", err), http.StatusInternalServerError)
			return
		}

		w.Header().Set("Content-Type", "application/json")
		if err := json.NewEncoder(w).Encode(recipes); err != nil {
			http.Error(w, fmt.Sprintf("JSON encoding error: %v", err), http.StatusInternalServerError)
			return
		}

	case INGREDIENTS:
		http.Error(w, "Ingredients recommendations not implemented", http.StatusNotImplemented)
		return

	case NUTRIMENTS:
		cmd := exec.Command("python3", "recommendations/src/recommendation_api.py", "nutriments", data, strconv.Itoa(number))
		cmd.Dir = "." // Set working directory to server root
		output, err := cmd.Output()
		if err != nil {
			http.Error(w, fmt.Sprintf("Nutriments recommendation error: %v", err), http.StatusInternalServerError)
			return
		}

		// Extract only the list of ShortRecipe from the Python response
		var parsed struct {
			Recommendations []ShortRecipe `json:"recommendations"`
		}
		if err := json.Unmarshal(output, &parsed); err != nil {
			http.Error(w, fmt.Sprintf("Response parsing error: %v", err), http.StatusInternalServerError)
			return
		}

		w.Header().Set("Content-Type", "application/json")
		if err := json.NewEncoder(w).Encode(parsed.Recommendations); err != nil {
			http.Error(w, fmt.Sprintf("JSON encoding error: %v", err), http.StatusInternalServerError)
			return
		}

	case PREFERENCES:
		http.Error(w, "Preferences recommendations not implemented", http.StatusNotImplemented)
		return

	default:
		http.Error(w, fmt.Sprintf("Unknown recommendation type: %s", recoType), http.StatusBadRequest)
	}
}
