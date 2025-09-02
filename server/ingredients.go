package main

import (
	"encoding/json"
	"net/http"
	"strconv"

	_ "github.com/mattn/go-sqlite3"
)

func (h *Handler) handleIngredients(w http.ResponseWriter, r *http.Request) {
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
	var args []interface{}

	if search != "" {
		query = "SELECT id, name FROM Ingredient WHERE LOWER(name) LIKE LOWER(?) ORDER BY name LIMIT ?"
		args = []interface{}{"%" + search + "%", limit}
	} else {
		query = "SELECT id, name FROM Ingredient ORDER BY name LIMIT ?"
		args = []interface{}{limit}
	}

	rows, err := h.db.Query(query, args...)
	if err != nil {
		http.Error(w, "Database query error", http.StatusInternalServerError)
		return
	}
	defer rows.Close()

	var ingredients []Ingredient
	for rows.Next() {
		var ingredient Ingredient
		if err := rows.Scan(&ingredient.Id, &ingredient.Name); err != nil {
			http.Error(w, "Row scan error", http.StatusInternalServerError)
			return
		}
		ingredients = append(ingredients, ingredient)
	}

	if err := rows.Err(); err != nil {
		http.Error(w, "Row iteration error", http.StatusInternalServerError)
		return
	}

	w.Header().Set("Content-Type", "application/json")

	// Use proper JSON encoding to handle special characters
	if err := json.NewEncoder(w).Encode(ingredients); err != nil {
		http.Error(w, "JSON encoding error", http.StatusInternalServerError)
		return
	}
}

/*
localhost:3000/ingredients?search=tomato
## GET /ingredients
**Arguments:**
- `search` (string, optional): A search term to filter ingredients by name.
- `limit` (integer, optional): The maximum number of ingredients to return (default is 10).
**Returns:**
- `200 OK`: JSON array of ingredients.
- `400 Bad Request`: If parameters are invalid.
- `500 Internal Server Error`: If an unexpected error occurs.
*/
