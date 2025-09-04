package main

import (
	"encoding/json"
	"net/http"
	"strconv"

	_ "github.com/mattn/go-sqlite3"
)

// handleIngredients handles the /ingredients endpoint
func (h *Handler) handleIngredients(w http.ResponseWriter, r *http.Request) {
	// Parse query parameters
	search := r.URL.Query().Get("search")
	limitStr := r.URL.Query().Get("limit")

	// Default limit
	limit := 10
	if limitStr != "" {
		if l, err := strconv.Atoi(limitStr); err == nil && l > 0 {
			limit = l
		} else {
			http.Error(w, "Invalid limit parameter", http.StatusBadRequest)
			return
		}
	}

	// Build SQL query
	var query string
	var args []interface{}

	if search != "" {
		query = "SELECT id, name FROM Ingredient WHERE name LIKE ? LIMIT ?"
		args = []interface{}{"%" + search + "%", limit}
	} else {
		query = "SELECT id, name FROM Ingredient LIMIT ?"
		args = []interface{}{limit}
	}

	// Execute query
	rows, err := h.db.Query(query, args...)
	if err != nil {
		http.Error(w, "Database error", http.StatusInternalServerError)
		return
	}
	defer rows.Close()

	// Parse results
	var ingredients []map[string]interface{}
	for rows.Next() {
		var id int
		var name string
		if err := rows.Scan(&id, &name); err != nil {
			continue
		}

		ingredients = append(ingredients, map[string]interface{}{
			"Id":   id,
			"Name": name,
		})
	}

	// Return JSON response
	w.Header().Set("Content-Type", "application/json")
	json.NewEncoder(w).Encode(ingredients)
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
