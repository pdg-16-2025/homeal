#!/usr/bin/env python3
"""
Leftover-based recommendation system.
Minimizes waste by using available ingredients and minimizing shopping needs.
"""

import json
import sqlite3
from typing import List, Dict, Any
from dataclasses import dataclass
from datetime import datetime, timedelta


@dataclass
class LeftoverIngredient:
    name: str
    quantity: float
    unit: str
    expiration_date: str  # ISO format date


@dataclass
class ShortRecipe:
    id: int
    name: str
    total_time: int
    image_url: str
    match_score: float = 0.0


def calculate_ingredient_match_score(recipe_ingredients: List[str], available_ingredients: List[str]) -> float:
    """Calculate how well available ingredients match recipe requirements."""
    if not recipe_ingredients:
        return 0.0
    
    matched = sum(1 for ingredient in recipe_ingredients if any(avail.lower() in ingredient.lower() for avail in available_ingredients))
    return matched / len(recipe_ingredients)


def get_leftover_recommendations(db_path: str, leftover_data: str, number: int = 5) -> List[Dict[str, Any]]:
    """
    Get recipe recommendations based on leftover ingredients.
    
    Args:
        db_path: Path to SQLite database
        leftover_data: JSON string containing leftover ingredients
        number: Number of recommendations to return
    
    Returns:
        List of recommended recipes sorted by ingredient match score
    """
    try:
        leftovers_json = json.loads(leftover_data)
        leftovers = [LeftoverIngredient(**item) for item in leftovers_json.get('ingredients', [])]
    except (json.JSONDecodeError, TypeError) as e:
        raise ValueError(f"Invalid leftover data format: {e}")
    
    if not leftovers:
        return []
    
    # Extract ingredient names for matching
    available_ingredient_names = [leftover.name.lower() for leftover in leftovers]
    
    # Sort by expiration date (use soonest expiring first)
    today = datetime.now()
    priority_ingredients = []
    for leftover in leftovers:
        try:
            exp_date = datetime.fromisoformat(leftover.expiration_date.replace('Z', '+00:00'))
            days_until_expiry = (exp_date - today).days
            if days_until_expiry <= 3:  # Prioritize ingredients expiring soon
                priority_ingredients.append(leftover.name.lower())
        except ValueError:
            # Invalid date format, treat as medium priority
            pass
    
    conn = sqlite3.connect(db_path)
    cursor = conn.cursor()
    
    # Get recipes with their ingredients (using proper schema with ingredient names)
    query = """
    SELECT DISTINCT r.id, r.name, r.total_time, r.images,
           GROUP_CONCAT(i.name) as ingredients
    FROM Recipe r
    LEFT JOIN RecipeIngredient ri ON r.id = ri.recipe_id
    LEFT JOIN Ingredient i ON ri.ingredient_id = i.id
    GROUP BY r.id, r.name, r.total_time, r.images
    LIMIT ?
    """
    
    cursor.execute(query, (number * 3,))  # Get more recipes to filter
    recipes = cursor.fetchall()
    conn.close()
    
    recommendations = []
    for recipe_row in recipes:
        recipe_id, name, total_time, images, ingredients_str = recipe_row
        
        # Parse ingredients (assuming comma-separated)
        recipe_ingredients = ingredients_str.split(',') if ingredients_str else []
        recipe_ingredients = [ing.strip().lower() for ing in recipe_ingredients]
        
        # Calculate match score
        match_score = calculate_ingredient_match_score(recipe_ingredients, available_ingredient_names)
        
        # Bonus for using priority (soon-expiring) ingredients
        priority_bonus = 0.0
        if priority_ingredients:
            priority_matches = sum(1 for ing in recipe_ingredients if any(p in ing for p in priority_ingredients))
            priority_bonus = priority_matches * 0.2  # 20% bonus per priority ingredient
        
        final_score = match_score + priority_bonus
        
        if final_score > 0.1:  # Only include recipes with some ingredient match
            recipe = ShortRecipe(
                id=recipe_id,
                name=name,
                total_time=total_time or 0,
                image_url=images or "",
                match_score=final_score
            )
            recommendations.append(recipe)
    
    # Sort by match score (highest first) and limit results
    recommendations.sort(key=lambda x: x.match_score, reverse=True)
    
    # Convert to dict format
    return [
        {
            "id": recipe.id,
            "name": recipe.name,
            "total_time": recipe.total_time,
            "image_url": recipe.image_url,
            "match_score": round(recipe.match_score, 2)
        }
        for recipe in recommendations[:number]
    ]


if __name__ == "__main__":
    # Test example
    test_data = {
        "ingredients": [
            {"name": "tomato", "quantity": 2, "unit": "pieces", "expiration_date": "2024-01-15"},
            {"name": "cheese", "quantity": 200, "unit": "g", "expiration_date": "2024-01-20"},
            {"name": "bread", "quantity": 1, "unit": "loaf", "expiration_date": "2024-01-12"}
        ]
    }
    
    # This would need a real database path
    # recommendations = get_leftover_recommendations("homeal.db", json.dumps(test_data))
    # print(json.dumps(recommendations, indent=2))
    print("Leftover recommendation system ready")
