#!/usr/bin/env python3
"""
Main recommendation API handler.
Routes requests to appropriate recommendation systems and provides a unified interface.
"""

import json
import sys
import os
from typing import Dict, Any, List

# Add current directory to path for imports
current_dir = os.path.dirname(os.path.abspath(__file__))
sys.path.insert(0, current_dir)

try:
    from leftover_recommendation import get_leftover_recommendations
    from nutriment_recommendation import get_nutriment_recommendations  
    from preference_recommendation import get_preference_recommendations, get_intelligent_mock_users
    from recipe_filtering import RecipeFilter, DietaryFilter, parse_dietary_filter_from_data
except ImportError as e:
    print(f"Import error: {e}", file=sys.stderr)
    print(f"Current directory: {current_dir}", file=sys.stderr)
    print(f"Python path: {sys.path}", file=sys.stderr)
    sys.exit(1)


class RecommendationAPI:
    def __init__(self, db_path: str = None):
        if db_path is None:
            # Try to find the database relative to this script's location
            import os
            script_dir = os.path.dirname(os.path.abspath(__file__))
            self.db_path = os.path.join(script_dir, "../../homeal.db")
        else:
            self.db_path = db_path
        
        # Initialize the filtering system
        self.filter_system = RecipeFilter(self.db_path)
    
    def get_recommendations(self, recommendation_type: str, data: str, number: int = 5) -> Dict[str, Any]:
        """
        Main entry point for getting recommendations with filtering.
        
        Args:
            recommendation_type: Type of recommendation ("ingredients", "nutriments", "preferences")
            data: JSON string with input data for the specific recommendation type
            number: Number of recommendations to return
            
        Returns:
            Dictionary with recommendations and metadata
        """
        try:
            # Parse input data
            data_dict = json.loads(data)
            
            # Extract dietary filtering preferences from request
            dietary_filter = parse_dietary_filter_from_data(data_dict)
            
            # Get base recommendations
            if recommendation_type == "ingredients":
                recommendations = get_leftover_recommendations(self.db_path, data, number * 3)  # Get more to filter
                message = "Recipes optimized for your leftover ingredients"
            
            elif recommendation_type == "nutriments":
                recommendations = get_nutriment_recommendations(self.db_path, data, number * 3)  # Get more to filter
                message = "Recipes tailored to your nutritional needs"
            
            elif recommendation_type == "preferences":
                recommendations = get_preference_recommendations(self.db_path, data, number * 3)  # Get more to filter
                message = "Recipes recommended based on similar users' preferences"
            
            elif recommendation_type == "random":
                # Random recommendations don't have algorithm-specific logic, use filter system directly
                recommendations = []
                message = "Random recipe recommendations"
            
            else:
                return {
                    "error": f"Unknown recommendation type: {recommendation_type}",
                    "valid_types": ["ingredients", "nutriments", "preferences", "random"]
                }
            
            # Apply dietary filtering if we have recommendations
            if recommendations:
                # Convert to format expected by filter system
                recipe_dicts = []
                for rec in recommendations:
                    # Get additional recipe data for filtering
                    recipe_dict = {
                        'id': rec['id'],
                        'name': rec['name'],
                        'total_time': rec.get('total_time', 0),
                        'image_url': rec.get('image_url', ''),
                        'keywords': '',  # Will be loaded by filter system
                        'calories': rec.get('calories'),
                        'aggregated_rating': rec.get('avg_rating')
                    }
                    # Copy all other fields from the recommendation
                    for key, value in rec.items():
                        if key not in recipe_dict:
                            recipe_dict[key] = value
                    recipe_dicts.append(recipe_dict)
                
                # Apply filtering
                filtered_recommendations = self.filter_system.filter_recipes(recipe_dicts, dietary_filter)
                
                # Ensure we have enough recipes
                if len(filtered_recommendations) < number:
                    # Get additional filtered recipes from database
                    additional_recipes = self.filter_system.get_filtered_recipes(dietary_filter, number * 2)
                    
                    # Convert additional recipes to recommendation format
                    existing_ids = {rec['id'] for rec in filtered_recommendations}
                    for recipe in additional_recipes:
                        if recipe['id'] not in existing_ids and len(filtered_recommendations) < number:
                            # Convert to recommendation format
                            filtered_recommendations.append({
                                'id': recipe['id'],
                                'name': recipe['name'],
                                'total_time': recipe['total_time'],
                                'image_url': recipe['image_url']
                            })
                
                # Ensure minimum number of recipes
                final_recommendations = self.filter_system.ensure_minimum_recipes(
                    filtered_recommendations, dietary_filter, min(number, 3)
                )[:number]
                
            else:
                # No base recommendations, get filtered recipes directly
                final_recommendations = self.filter_system.get_filtered_recipes(dietary_filter, number)
                final_recommendations = self.filter_system.ensure_minimum_recipes(
                    final_recommendations, dietary_filter, min(number, 3)
                )
            
            # Add filtering info to message
            filter_info = []
            if dietary_filter.regime:
                filter_info.append(f"{dietary_filter.regime}")
            if dietary_filter.blacklisted_ingredients:
                filter_info.append(f"avoiding {', '.join(dietary_filter.blacklisted_ingredients)}")
            if dietary_filter.allergies:
                filter_info.append(f"allergen-free ({', '.join(dietary_filter.allergies)})")
            
            if filter_info:
                message += f" | Filtered for: {', '.join(filter_info)}"
            
            return {
                "type": recommendation_type,
                "recommendations": final_recommendations,
                "message": message,
                "filters_applied": {
                    "dietary_regime": dietary_filter.regime,
                    "blacklisted_ingredients": dietary_filter.blacklisted_ingredients,
                    "allergies": dietary_filter.allergies,
                    "max_calories": dietary_filter.max_calories
                }
            }
                
        except json.JSONDecodeError as e:
            return {
                "error": f"Invalid JSON data: {str(e)}",
                "type": recommendation_type
            }
        except ValueError as e:
            return {
                "error": f"Invalid input data: {str(e)}",
                "type": recommendation_type
            }
        except Exception as e:
            return {
                "error": f"Internal error: {str(e)}",
                "type": recommendation_type
            }


def main():
    """
    Command line interface for testing the recommendation API.
    Usage: python recommendation_api.py <type> <data_json> [number]
    """
    if len(sys.argv) < 3:
        print("Usage: python recommendation_api.py <type> <data_json> [number]")
        print("Types: ingredients, nutriments, preferences")
        sys.exit(1)
    
    recommendation_type = sys.argv[1]
    data_json = sys.argv[2]
    number = int(sys.argv[3]) if len(sys.argv) > 3 else 5
    
    api = RecommendationAPI()
    result = api.get_recommendations(recommendation_type, data_json, number)
    
    print(json.dumps(result, indent=2))


if __name__ == "__main__":
    main()


# Example usage and test data
def get_example_data():
    """Returns example data for testing each recommendation type."""
    
    ingredients_example = {
        "ingredients": [
            {"name": "tomato", "quantity": 2, "unit": "pieces", "expiration_date": "2024-01-15"},
            {"name": "cheese", "quantity": 200, "unit": "g", "expiration_date": "2024-01-20"},
            {"name": "pasta", "quantity": 500, "unit": "g", "expiration_date": "2024-01-25"}
        ]
    }
    
    nutriments_example = {
        "age": 30,
        "gender": "male", 
        "weight": 75.0,
        "height": 180.0,
        "activity_level": "moderately_active",
        "meal_type": "lunch"
    }
    
    # Get intelligent mock user data based on real review patterns
    try:
        mock_users = get_intelligent_mock_users("../homeal.db")
        preferences_example = mock_users[0] if mock_users else {
            "user_id": 90001,
            "ratings": [
                {"recipe_id": 117, "rating": 5.0},
                {"recipe_id": 374, "rating": 4.5},
                {"recipe_id": 479, "rating": 4.0}
            ]
        }
    except:
        preferences_example = {
            "user_id": 90001,
            "ratings": [
                {"recipe_id": 117, "rating": 5.0},
                {"recipe_id": 374, "rating": 4.5},
                {"recipe_id": 479, "rating": 4.0}
            ]
        }
    
    return {
        "ingredients": ingredients_example,
        "nutriments": nutriments_example,
        "preferences": preferences_example
    }


# Integration notes for Go server:
"""
To integrate with the existing Go server (recommendation.go), you can:

1. Call this Python script as a subprocess from Go:
   cmd := exec.Command("python3", "recommendations/src/recommendation_api.py", recoType, data, strconv.Itoa(number))
   output, err := cmd.Output()

2. Or set up a simple HTTP server wrapper:
   Add Flask/FastAPI wrapper around this code to create a microservice

3. Update the Go recommendation.go file to handle the new types:
   Replace the TODO sections for INGREDIENTS and PREFERENCES cases with subprocess calls

Example Go integration:
```go
case INGREDIENTS:
    cmd := exec.Command("python3", "recommendations/src/recommendation_api.py", "ingredients", data, strconv.Itoa(number))
    output, err := cmd.Output()
    if err != nil {
        http.Error(w, fmt.Sprintf("Recommendation error: %v", err), http.StatusInternalServerError)
        return
    }
    
    w.Header().Set("Content-Type", "application/json")
    w.Write(output)
```

Database Schema Requirements:
- UserRecipeRating table for preferences (user_id, recipe_id, rating)
- RecipeIngredient table for ingredients matching
- Ensure all nutritional fields are populated in Recipe table
"""
