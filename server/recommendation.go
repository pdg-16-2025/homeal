package main

import (
	"encoding/json"
	"fmt"
	"net/http"
	"database/sql"

	_ "github.com/mattn/go-sqlite3"
)

func (h *Handler) handleRecommendations(w http.ResponseWriter, r *http.Request) {
	recoType := RecommendationType(r.URL.Query().Get("type"))
	if recoType == "" {
		http.Error(w, "Missing 'type' parameter", http.StatusBadRequest)
		return
	}

	// TODO: Implement data json structure parsing based on recoType
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
		rows, err := h.db.Query("SELECT id, name, total_time, images FROM Recipe ORDER BY RANDOM() LIMIT ?", number)
		if err != nil {
			http.Error(w, fmt.Sprintf("Database query error: %v", err), http.StatusInternalServerError)
			return
		}
		defer rows.Close()
		recommendations := []ShortRecipe{}
		for rows.Next() {
			var recipe ShortRecipe
			var totalTime sql.NullInt64
			if err := rows.Scan(&recipe.Id, &recipe.Name, &totalTime, &recipe.ImageURL); err != nil {
				http.Error(w, fmt.Sprintf("Row scan error: %v", err), http.StatusInternalServerError)
				return
			}
			if totalTime.Valid {
				recipe.TotalTime = int(totalTime.Int64)
			} else {
				recipe.TotalTime = 0
			}
			recommendations = append(recommendations, recipe)
		}

		w.Header().Set("Content-Type", "application/json")
		if err := json.NewEncoder(w).Encode(recommendations); err != nil {
			http.Error(w, fmt.Sprintf("JSON encoding error: %v", err), http.StatusInternalServerError)
			return
		}

	// TODO: Not implemented yet
	case PREFERENCES:
		http.Error(w, "Recommendation type 'preferences' not implemented yet", http.StatusNotImplemented)

	// TODO: Not implemented yet
	case INGREDIENTS:
		http.Error(w, "Recommendation type 'ingredients' not implemented yet", http.StatusNotImplemented)
	}
}
