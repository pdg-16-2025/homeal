package main

import (
	"encoding/json"
	"fmt"
	"net/http"
	"os/exec"
	"strconv"
)

func (h *Handler) handleRecommendations(w http.ResponseWriter, r *http.Request) {
	recoType := RecommendationType(r.URL.Query().Get("type"))
	if recoType == "" {
		http.Error(w, "Missing 'type' parameter", http.StatusBadRequest)
		return
	}

	// Get data parameter (optional for RANDOM type)
	data := r.URL.Query().Get("data")
	if data == "" {
		if recoType == RANDOM {
			// For random recommendations, provide empty JSON object
			data = "{}"
		} else {
			http.Error(w, "Missing 'data' parameter", http.StatusBadRequest)
			return
		}
	}

	number := 5
	if n := r.URL.Query().Get("number"); n != "" {
		fmt.Sscanf(n, "%d", &number)
	}

	switch recoType {
	case RANDOM:
		cmd := exec.Command("python3", "recommendations/src/recommendation_api.py", "random", data, strconv.Itoa(number))
		cmd.Dir = "." // Set working directory to server root
		output, err := cmd.Output()
		if err != nil {
			http.Error(w, fmt.Sprintf("Random recommendation error: %v", err), http.StatusInternalServerError)
			return
		}

		// Extract only the list of ShortRecipe from the Python response
		var parsed struct {
			Recommendations []ShortRecipe `json:"recommendations"`
		}
		if err := json.Unmarshal(output, &parsed); err != nil {
			http.Error(w, fmt.Sprintf("Response parsing error: %v", err), http.StatusInternalServerError)
			return
		}

		w.Header().Set("Content-Type", "application/json")
		if err := json.NewEncoder(w).Encode(parsed.Recommendations); err != nil {
			http.Error(w, fmt.Sprintf("JSON encoding error: %v", err), http.StatusInternalServerError)
			return
		}

	case INGREDIENTS:
		cmd := exec.Command("python3", "recommendations/src/recommendation_api.py", "ingredients", data, strconv.Itoa(number))
		cmd.Dir = "." // Set working directory to server root
		output, err := cmd.Output()
		if err != nil {
			http.Error(w, fmt.Sprintf("Ingredients recommendation error: %v", err), http.StatusInternalServerError)
			return
		}

		// Extract only the list of ShortRecipe from the Python response
		var parsed struct {
			Recommendations []ShortRecipe `json:"recommendations"`
		}
		if err := json.Unmarshal(output, &parsed); err != nil {
			http.Error(w, fmt.Sprintf("Response parsing error: %v", err), http.StatusInternalServerError)
			return
		}

		w.Header().Set("Content-Type", "application/json")
		if err := json.NewEncoder(w).Encode(parsed.Recommendations); err != nil {
			http.Error(w, fmt.Sprintf("JSON encoding error: %v", err), http.StatusInternalServerError)
			return
		}

	case NUTRIMENTS:
		cmd := exec.Command("python3", "recommendations/src/recommendation_api.py", "nutriments", data, strconv.Itoa(number))
		cmd.Dir = "." // Set working directory to server root
		output, err := cmd.Output()
		if err != nil {
			http.Error(w, fmt.Sprintf("Nutriments recommendation error: %v", err), http.StatusInternalServerError)
			return
		}

		// Extract only the list of ShortRecipe from the Python response
		var parsed struct {
			Recommendations []ShortRecipe `json:"recommendations"`
		}
		if err := json.Unmarshal(output, &parsed); err != nil {
			http.Error(w, fmt.Sprintf("Response parsing error: %v", err), http.StatusInternalServerError)
			return
		}

		w.Header().Set("Content-Type", "application/json")
		if err := json.NewEncoder(w).Encode(parsed.Recommendations); err != nil {
			http.Error(w, fmt.Sprintf("JSON encoding error: %v", err), http.StatusInternalServerError)
			return
		}

	case PREFERENCES:
		cmd := exec.Command("python3", "recommendations/src/recommendation_api.py", "preferences", data, strconv.Itoa(number))
		cmd.Dir = "." // Set working directory to server root
		output, err := cmd.Output()
		if err != nil {
			http.Error(w, fmt.Sprintf("Preferences recommendation error: %v", err), http.StatusInternalServerError)
			return
		}

		// Extract only the list of ShortRecipe from the Python response
		var parsed struct {
			Recommendations []ShortRecipe `json:"recommendations"`
		}
		if err := json.Unmarshal(output, &parsed); err != nil {
			http.Error(w, fmt.Sprintf("Response parsing error: %v", err), http.StatusInternalServerError)
			return
		}

		w.Header().Set("Content-Type", "application/json")
		if err := json.NewEncoder(w).Encode(parsed.Recommendations); err != nil {
			http.Error(w, fmt.Sprintf("JSON encoding error: %v", err), http.StatusInternalServerError)
			return
		}

	default:
		http.Error(w, fmt.Sprintf("Unknown recommendation type: %s", recoType), http.StatusBadRequest)
	}
}
