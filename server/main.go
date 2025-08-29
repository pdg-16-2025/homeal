package main

import (
	"fmt"
	"net/http"
	"database/sql"

	_ "github.com/mattn/go-sqlite3"
)

// SQLite database
const dbPath = "./homeal.db"

func main() {
	go func() {
		mux1 := http.NewServeMux()
		fs := http.FileServer(http.Dir("./landing-page"))
		mux1.Handle("/", fs)
		if err := http.ListenAndServe(":80", mux1); err != nil {
			fmt.Printf("Server landing-page error: %v\n", err)
		}
	}()

	mux2 := http.NewServeMux()

	db, err := sql.Open("sqlite3", dbPath)
	if err != nil {
		fmt.Printf("Database connection error: %v\n", err)
		return
	}
	defer db.Close()

	handler := &Handler{db: db}

	// Useful for health checks
	mux2.HandleFunc("/", func(w http.ResponseWriter, r *http.Request) {
		fmt.Fprintf(w, "Hello, World!")
	})

	mux2.HandleFunc("/scan", handler.handleScan)
	mux2.HandleFunc("/recommendations", handler.handleRecommendations)
	mux2.HandleFunc("/recipe", handler.handleRecipe)
	mux2.HandleFunc("/ingredients", handler.handleIngredients)
	mux2.HandleFunc("/recipe-ingredients", handler.handleRecipeIngredients)

	if err := http.ListenAndServe(":3000", mux2); err != nil {
		fmt.Printf("Server API error: %v\n", err)
	}

}

/*
# REST-API Description

## GET /scan
**Arguments:**
- `code` (string, required): The code to be scanned.
**Returns:**
- `200 OK`: JSON object with scan results (Ingredient).
- `400 Bad Request`: If the `code` parameter is missing or invalid.
- `404 Not Found`: If no ingredient is found with the given code.
- `500 Internal Server Error`: If an unexpected error occurs.

## GET /recommendations
**Arguments:**
- `type` (string, required): The type of recommendations to create
- `data` (string, required): The data to base recommendations on
- `number` (integer, optional): The number of recommendations to return (default is 5)
**Returns:**
- `200 OK`: JSON array of recommended items.
- `400 Bad Request`: If required parameters are missing or invalid.
- `500 Internal Server Error`: If an unexpected error occurs.

## GET /recipe
**Arguments:**
- `id` (int, required): The ID of the recipe to retrieve.
**Returns:**
- `200 OK`: JSON object with recipe details (Recipe).
- `400 Bad Request`: If the `id` parameter is missing or invalid.
- `404 Not Found`: If no recipe is found with the given ID.
- `500 Internal Server Error`: If an unexpected error occurs.

## GET /ingredients
**Arguments:**
- `search` (string, optional): A search term to filter ingredients by name.
- `limit` (integer, optional): The maximum number of ingredients to return (default is 10).
**Returns:**
- `200 OK`: JSON array of ingredients.
- `400 Bad Request`: If parameters are invalid.
- `500 Internal Server Error`: If an unexpected error occurs.

## GET /recipe-ingredients
**Arguments:**
- `recipe_id` (int, required): The ID of the recipe to retrieve ingredients for.
**Returns:**
- `200 OK`: JSON array of ingredients for the specified recipe.
- `400 Bad Request`: If the `recipe_id` parameter is missing or invalid.
- `404 Not Found`: If no recipe is found with the given ID.
- `500 Internal Server Error`: If an unexpected error occurs.


-- SQLite
PRAGMA foreign_keys = ON;

CREATE TABLE Ingredient (
	id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
	name TEXT NOT NULL
);

CREATE TABLE Recipe (
	id INTEGER PRIMARY KEY NOT NULL,
	name TEXT NOT NULL,
	author_id INTEGER,
	cook_time INTEGER,
	prep_time INTEGER,
	total_time INTEGER,
	description TEXT,
	images TEXT,
	category TEXT,
	keywords TEXT,
	aggregated_rating REAL,
	review_count INTEGER,
	calories REAL,
	fat_content REAL,
	saturated_fat_content REAL,
	cholesterol_content REAL,
	sodium_content REAL,
	carbohydrate_content REAL,
	fiber_content REAL,
	sugar_content REAL,
	protein_content REAL,
	recipe_servings INTEGER,
	recipe_yield TEXT,
	recipe_instructions TEXT
);

CREATE TABLE Review (
	id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
	author_id INTEGER,
	recipe_id INTEGER NOT NULL,
	rating REAL CHECK (rating >= 1 AND rating <= 5),
	FOREIGN KEY (recipe_id) REFERENCES Recipe(id) ON DELETE CASCADE
);

CREATE TABLE Product (
	id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
	code TEXT NOT NULL,
	ingredient_id INTEGER,
	FOREIGN KEY (ingredient_id) REFERENCES Ingredient(id) ON DELETE SET NULL,
	UNIQUE (code)
);

CREATE TABLE RecipeIngredient (
	id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
	recipe_id INTEGER NOT NULL,
	ingredient_id INTEGER NOT NULL,
	quantity REAL NOT NULL CHECK (quantity > 0),
	unit TEXT NOT NULL,
	FOREIGN KEY (recipe_id) REFERENCES Recipe(id) ON DELETE CASCADE,
	FOREIGN KEY (ingredient_id) REFERENCES Ingredient(id) ON DELETE CASCADE,
	UNIQUE (recipe_id, ingredient_id)
);

CREATE INDEX idx_product_ingredient ON Product(ingredient_id);
CREATE INDEX idx_recipe_ingredient_recipe ON RecipeIngredient(recipe_id);
CREATE INDEX idx_recipe_ingredient_ingredient ON RecipeIngredient(ingredient_id);

CREATE INDEX idx_product_code ON Product(code);
CREATE INDEX idx_ingredient_name ON Ingredient(name);
*/
