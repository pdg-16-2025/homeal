package main

import (
	"database/sql"

	_ "github.com/mattn/go-sqlite3"
)

type Handler struct {
	db *sql.DB
}

type Ingredient struct {
	Id   int    `json:"id"`
	Name string `json:"name"`
}

type RecipeIngredient struct {
	Id       int    `json:"id"`
	Name     string `json:"name"`
	Quantity string `json:"quantity"`
	Unit     string `json:"unit"`
}

type ShortRecipe struct {
	Id        int    `json:"id"`
	Name      string `json:"name"`
	TotalTime int    `json:"total_time"`
	ImageURL  string `json:"image_url"`
}

type RecommendationType string

const (
	RANDOM      RecommendationType = "random"
	PREFERENCES RecommendationType = "preferences"
	INGREDIENTS RecommendationType = "ingredients"
	NUTRIMENTS  RecommendationType = "nutriments"
)

type Recipe struct {
	Id                  int          `json:"id"`
	Name                string       `json:"name"`
	AuthorId            int          `json:"author_id"`
	CookTime            int          `json:"cook_time"`
	PrepTime            int          `json:"prep_time"`
	TotalTime           int          `json:"total_time"`
	Images              string       `json:"images"`
	Category            string       `json:"category"`
	Keywords            string       `json:"keywords"`
	AggregatedRating    float64      `json:"aggregated_rating"`
	Calories            float64      `json:"calories"`
	FatContent          float64      `json:"fat_content"`
	SaturatedFatContent float64      `json:"saturated_fat_content"`
	CholesterolContent  float64      `json:"cholesterol_content"`
	SodiumContent       float64      `json:"sodium_content"`
	CarbohydrateContent float64      `json:"carbohydrate_content"`
	FiberContent        float64      `json:"fiber_content"`
	SugarContent        float64      `json:"sugar_content"`
	ProteinContent      float64      `json:"protein_content"`
	RecipeServings      int          `json:"recipe_servings"`
	RecipeYield         string       `json:"recipe_yield"`
	RecipeInstructions  string       `json:"recipe_instructions"`
	Ingredients         []Ingredient `json:"ingredients,omitempty"`
}
