package main

import (
	"encoding/json"
	"fmt"
	"net/http"

	_ "github.com/mattn/go-sqlite3"
)

func (h *Handler) handleScan(w http.ResponseWriter, r *http.Request) {
	code := r.URL.Query().Get("code")
	if code == "" {
		http.Error(w, "Missing 'code' parameter", http.StatusBadRequest)
		return
	}
	rows, err := h.db.Query("SELECT Ingredient.id, Ingredient.name FROM Product JOIN Ingredient ON Product.ingredient_id = Ingredient.id WHERE Product.code = ?", code)
	if err != nil {
		http.Error(w, fmt.Sprintf("Database query error: %v", err), http.StatusInternalServerError)
		return
	}
	defer rows.Close()

	if rows.Next() {
		var ingredient Ingredient
		if err := rows.Scan(&ingredient.Id, &ingredient.Name); err != nil {
			http.Error(w, fmt.Sprintf("Row scan error: %v", err), http.StatusInternalServerError)
			return
		}
		w.Header().Set("Content-Type", "application/json")
		if err := json.NewEncoder(w).Encode(ingredient); err != nil {
			http.Error(w, fmt.Sprintf("JSON encoding error: %v", err), http.StatusInternalServerError)
			return
		}
	} else {
		http.Error(w, "No ingredient found for the given code", http.StatusNotFound)
	}
}
