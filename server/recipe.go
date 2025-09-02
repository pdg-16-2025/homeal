package main

import (
	"database/sql"
	"encoding/json"
	"fmt"
	"net/http"

	_ "github.com/mattn/go-sqlite3"
)

func (h *Handler) handleRecipe(w http.ResponseWriter, r *http.Request) {
	id := r.URL.Query().Get("id")
	if id == "" {
		http.Error(w, "Missing 'id' parameter", http.StatusBadRequest)
		return
	}
	row := h.db.QueryRow("SELECT id, name, author_id, cook_time, prep_time, total_time, images, category, keywords, aggregated_rating, calories, fat_content, saturated_fat_content, cholesterol_content, sodium_content, carbohydrate_content, fiber_content, sugar_content, protein_content, recipe_servings, recipe_yield, recipe_instructions FROM Recipe WHERE id = ?", id)
	var recipe Recipe
	var cookTime, prepTime, totalTime, recipeServings sql.NullInt64
	var recipeYield, keywords, images sql.NullString
	if err := row.Scan(&recipe.Id, &recipe.Name, &recipe.AuthorId, &cookTime, &prepTime, &totalTime, &images, &recipe.Category, &keywords, &recipe.AggregatedRating, &recipe.Calories, &recipe.FatContent, &recipe.SaturatedFatContent, &recipe.CholesterolContent, &recipe.SodiumContent, &recipe.CarbohydrateContent, &recipe.FiberContent, &recipe.SugarContent, &recipe.ProteinContent, &recipeServings, &recipeYield, &recipe.RecipeInstructions); err != nil {
		if err == sql.ErrNoRows {
			http.Error(w, "Recipe not found", http.StatusNotFound)
		} else {
			http.Error(w, fmt.Sprintf("Row scan error: %v", err), http.StatusInternalServerError)
		}
		return
	}

	if cookTime.Valid {
		recipe.CookTime = int(cookTime.Int64)
	} else {
		recipe.CookTime = -1
	}
	if prepTime.Valid {
		recipe.PrepTime = int(prepTime.Int64)
	} else {
		recipe.PrepTime = -1
	}
	if totalTime.Valid {
		recipe.TotalTime = int(totalTime.Int64)
	} else {
		recipe.TotalTime = -1
	}
	if recipeServings.Valid {
		recipe.RecipeServings = int(recipeServings.Int64)
	} else {
		recipe.RecipeServings = -1
	}
	if recipeYield.Valid {
		recipe.RecipeYield = recipeYield.String
	} else {
		recipe.RecipeYield = ""
	}
	if keywords.Valid {
		recipe.Keywords = keywords.String
	} else {
		recipe.Keywords = ""
	}
	if images.Valid {
		recipe.Images = images.String
	} else {
		recipe.Images = ""
	}
	// Fetch ingredients
	rows, err := h.db.Query("SELECT Ingredient.id, Ingredient.name FROM RecipeIngredient JOIN Ingredient ON RecipeIngredient.ingredient_id = Ingredient.id WHERE RecipeIngredient.recipe_id = ?", recipe.Id)
	if err != nil {
		http.Error(w, fmt.Sprintf("Database query error: %v", err), http.StatusInternalServerError)
		return
	}
	defer rows.Close()
	ingredients := []Ingredient{}
	for rows.Next() {
		var ingredient Ingredient
		if err := rows.Scan(&ingredient.Id, &ingredient.Name); err != nil {
			http.Error(w, fmt.Sprintf("Row scan error: %v", err), http.StatusInternalServerError)
			return
		}
		ingredients = append(ingredients, ingredient)
	}
	recipe.Ingredients = ingredients

	w.Header().Set("Content-Type", "application/json")
	if err := json.NewEncoder(w).Encode(recipe); err != nil {
		http.Error(w, fmt.Sprintf("JSON encoding error: %v", err), http.StatusInternalServerError)
		return
	}
}
