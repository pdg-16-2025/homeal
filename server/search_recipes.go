package main

import (
	"database/sql"
	"encoding/json"
	"net/http"
	"strconv"

	_ "github.com/mattn/go-sqlite3"
)

func (h *Handler) handleSearchRecipes(w http.ResponseWriter, r *http.Request) {
	search := r.URL.Query().Get("search")
	limit := r.URL.Query().Get("limit")
	if limit == "" {
		limit = "10"
	} else if _, err := strconv.Atoi(limit); err != nil {
		http.Error(w, "Invalid limit parameter", http.StatusBadRequest)
		return
	}

	// Build query based on whether search term is provided
	var query string
	var args []any

	if search != "" {
		query = "SELECT id, name, total_time, images FROM Recipe WHERE LOWER(name) LIKE LOWER(?) ORDER BY name LIMIT ?"
		args = []any{"%" + search + "%", limit}
	} else {
		query = "SELECT id, name, total_time, images FROM Recipe ORDER BY name LIMIT ?"
		args = []any{limit}
	}

	rows, err := h.db.Query(query, args...)
	if err != nil {
		http.Error(w, "Database query error", http.StatusInternalServerError)
		return
	}
	defer rows.Close()

	var recipes []ShortRecipe
	for rows.Next() {
		var recipe ShortRecipe
		var totalTime sql.NullInt64
		var imageURL sql.NullString
		if err := rows.Scan(&recipe.Id, &recipe.Name, &totalTime, &imageURL); err != nil {
			http.Error(w, "Row scan error", http.StatusInternalServerError)
			return
		}

		// Handle nullable total_time
		if totalTime.Valid {
			recipe.TotalTime = int(totalTime.Int64)
		} else {
			recipe.TotalTime = 0
		}

		// Handle nullable image_url
		if imageURL.Valid {
			recipe.ImageURL = imageURL.String
		} else {
			recipe.ImageURL = ""
		}

		recipes = append(recipes, recipe)
	}

	if err := rows.Err(); err != nil {
		http.Error(w, "Row iteration error", http.StatusInternalServerError)
		return
	}

	w.Header().Set("Content-Type", "application/json")
	if err := json.NewEncoder(w).Encode(recipes); err != nil {
		http.Error(w, "JSON encoding error", http.StatusInternalServerError)
		return
	}
}
