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
	mux2.HandleFunc("/search-recipes", handler.handleSearchRecipes)

	if err := http.ListenAndServe(":3000", mux2); err != nil {
		fmt.Printf("Server API error: %v\n", err)
	}

}
