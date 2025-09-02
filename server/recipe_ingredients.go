package main

import (
	"encoding/json"
	"fmt"
	"net/http"

	_ "github.com/mattn/go-sqlite3"
)

func (h *Handler) handleRecipeIngredients(w http.ResponseWriter, r *http.Request) {
	id := r.URL.Query().Get("recipe_id")
	if id == "" {
		http.Error(w, "Missing 'recipe_id' parameter", http.StatusBadRequest)
		return
	}

	query := `
		SELECT ri.id, i.name, ri.quantity, ri.unit
		FROM RecipeIngredient ri
		JOIN Ingredient i ON ri.ingredient_id = i.id
		WHERE ri.recipe_id = ?
		ORDER BY i.name
	`

	rows, err := h.db.Query(query, id)
	if err != nil {
		http.Error(w, fmt.Sprintf("Database query error: %v", err), http.StatusInternalServerError)
		return
	}
	defer rows.Close()

	var recipeIngredients []RecipeIngredient
	for rows.Next() {
		var ri RecipeIngredient
		if err := rows.Scan(&ri.Id, &ri.Name, &ri.Quantity, &ri.Unit); err != nil {
			http.Error(w, fmt.Sprintf("Row scan error: %v", err), http.StatusInternalServerError)
			return
		}
		recipeIngredients = append(recipeIngredients, ri)
	}

	if err := rows.Err(); err != nil {
		http.Error(w, fmt.Sprintf("Row iteration error: %v", err), http.StatusInternalServerError)
		return
	}

	w.Header().Set("Content-Type", "application/json")
	if err := json.NewEncoder(w).Encode(recipeIngredients); err != nil {
		http.Error(w, fmt.Sprintf("JSON encoding error: %v", err), http.StatusInternalServerError)
		return
	}
}
