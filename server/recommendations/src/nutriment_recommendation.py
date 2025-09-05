#!/usr/bin/env python3
"""
Nutriment-based recommendation system.
Uses TDEE calculations and nutritional targets to recommend balanced meals.
"""

import json
import sqlite3
import math
from typing import List, Dict, Any, Optional
from dataclasses import dataclass
from enum import Enum


class ActivityLevel(Enum):
    SEDENTARY = "sedentary"
    LIGHTLY_ACTIVE = "lightly_active"
    MODERATELY_ACTIVE = "moderately_active"
    VERY_ACTIVE = "very_active"
    EXTRA_ACTIVE = "extra_active"  # Added to match test data
    EXTREMELY_ACTIVE = "extremely_active"


class Gender(Enum):
    MALE = "male"
    FEMALE = "female"
    OTHER = "other"


class MealType(Enum):
    BREAKFAST = "breakfast"
    LUNCH = "lunch"
    DINNER = "dinner"
    SNACK = "snack"


@dataclass
class UserProfile:
    age: int
    gender: Gender
    weight: float  # kg
    height: float  # cm
    activity_level: ActivityLevel
    meal_type: Optional[MealType] = None


@dataclass
class NutritionalTargets:
    calories: float
    protein: float  # grams
    carbs: float    # grams
    fat: float      # grams
    fiber: float    # grams
    sodium_limit: float  # mg


def calculate_bmr(user: UserProfile) -> float:
    """Calculate Basal Metabolic Rate using Mifflin-St Jeor equation."""
    if user.gender == Gender.MALE:
        return (10 * user.weight) + (6.25 * user.height) - (5 * user.age) + 5
    else:  # FEMALE or OTHER treated as female for calculation
        return (10 * user.weight) + (6.25 * user.height) - (5 * user.age) - 161


def calculate_tdee(user: UserProfile) -> float:
    """Calculate Total Daily Energy Expenditure."""
    bmr = calculate_bmr(user)
    
    activity_multipliers = {
        ActivityLevel.SEDENTARY: 1.2,
        ActivityLevel.LIGHTLY_ACTIVE: 1.375,
        ActivityLevel.MODERATELY_ACTIVE: 1.55,
        ActivityLevel.VERY_ACTIVE: 1.725,
        ActivityLevel.EXTRA_ACTIVE: 1.85,  # Added for test compatibility
        ActivityLevel.EXTREMELY_ACTIVE: 1.9
    }
    
    return bmr * activity_multipliers[user.activity_level]


def calculate_nutritional_targets(user: UserProfile) -> NutritionalTargets:
    """Calculate daily nutritional targets for user."""
    daily_calories = calculate_tdee(user)
    
    # USDA recommended macronutrient distribution (middle of ranges)
    carb_percentage = 0.55  # 55% of calories
    protein_percentage = 0.20  # 20% of calories
    fat_percentage = 0.25   # 25% of calories
    
    # Calculate grams
    daily_carbs = (daily_calories * carb_percentage) / 4  # 4 kcal/g
    daily_protein = (daily_calories * protein_percentage) / 4  # 4 kcal/g
    daily_fat = (daily_calories * fat_percentage) / 9  # 9 kcal/g
    
    # Fiber target (gender-based)
    daily_fiber = 38 if user.gender == Gender.MALE else 25
    
    # Adjust for meal type if specified
    if user.meal_type:
        meal_percentages = {
            MealType.BREAKFAST: 0.25,  # 25% of daily calories
            MealType.LUNCH: 0.35,      # 35% of daily calories  
            MealType.DINNER: 0.30,     # 30% of daily calories
            MealType.SNACK: 0.10       # 10% of daily calories
        }
        
        multiplier = meal_percentages[user.meal_type]
        daily_calories *= multiplier
        daily_carbs *= multiplier
        daily_protein *= multiplier
        daily_fat *= multiplier
        daily_fiber *= multiplier
    
    return NutritionalTargets(
        calories=daily_calories,
        protein=daily_protein,
        carbs=daily_carbs,
        fat=daily_fat,
        fiber=daily_fiber,
        sodium_limit=2300 / 3  # Assuming 3 meals per day
    )


def calculate_nutrition_score(recipe_nutrition: Dict, targets: NutritionalTargets) -> float:
    """Calculate how well a recipe matches nutritional targets."""
    # Extract recipe nutrition (per serving)
    recipe_cals = recipe_nutrition.get('calories', 0)
    recipe_protein = recipe_nutrition.get('protein_content', 0)
    recipe_carbs = recipe_nutrition.get('carbohydrate_content', 0)
    recipe_fat = recipe_nutrition.get('fat_content', 0)
    recipe_fiber = recipe_nutrition.get('fiber_content', 0)
    recipe_sodium = recipe_nutrition.get('sodium_content', 0)
    
    # Avoid division by zero
    if targets.calories == 0:
        return 0.0
    
    # Calculate individual scores (closer to target = higher score)
    calorie_score = max(0, 1 - abs(recipe_cals - targets.calories) / targets.calories)
    
    # Macro scores
    protein_score = max(0, 1 - abs(recipe_protein - targets.protein) / targets.protein) if targets.protein > 0 else 0
    carb_score = max(0, 1 - abs(recipe_carbs - targets.carbs) / targets.carbs) if targets.carbs > 0 else 0
    fat_score = max(0, 1 - abs(recipe_fat - targets.fat) / targets.fat) if targets.fat > 0 else 0
    
    macro_score = (protein_score + carb_score + fat_score) / 3
    
    # Health modifiers
    fiber_bonus = min(recipe_fiber / targets.fiber, 1) if targets.fiber > 0 else 0
    sodium_penalty = max(0, 1 - (recipe_sodium / targets.sodium_limit)) if targets.sodium_limit > 0 else 1
    
    health_score = (fiber_bonus + sodium_penalty) / 2
    
    # Combined weighted score
    nutrition_score = (calorie_score * 0.4) + (macro_score * 0.4) + (health_score * 0.2)
    
    return min(nutrition_score, 1.0)


def get_nutriment_recommendations(db_path: str, user_data: str, number: int = 5) -> List[Dict[str, Any]]:
    """
    Get recipe recommendations based on nutritional needs.
    
    Args:
        db_path: Path to SQLite database
        user_data: JSON string containing user profile
        number: Number of recommendations to return
    
    Returns:
        List of recommended recipes sorted by nutritional fit
    """
    try:
        user_json = json.loads(user_data)
        user = UserProfile(
            age=user_json['age'],
            gender=Gender(user_json['gender']),
            weight=user_json['weight'],
            height=user_json['height'],
            activity_level=ActivityLevel(user_json['activity_level']),
            meal_type=MealType(user_json.get('meal_type')) if user_json.get('meal_type') else None
        )
    except (json.JSONDecodeError, KeyError, ValueError) as e:
        raise ValueError(f"Invalid user data format: {e}")
    
    targets = calculate_nutritional_targets(user)
    
    conn = sqlite3.connect(db_path)
    cursor = conn.cursor()
    
    # Get recipes with nutritional information
    query = """
    SELECT id, name, total_time, images, calories, protein_content, 
           carbohydrate_content, fat_content, fiber_content, sodium_content,
           aggregated_rating
    FROM Recipe 
    WHERE calories IS NOT NULL AND calories > 0
    ORDER BY aggregated_rating DESC
    LIMIT ?
    """
    
    cursor.execute(query, (number * 4,))  # Get more to filter and rank
    recipes = cursor.fetchall()
    conn.close()
    
    recommendations = []
    for recipe_row in recipes:
        (recipe_id, name, total_time, images, calories, protein, carbs, 
         fat, fiber, sodium, rating) = recipe_row
        
        recipe_nutrition = {
            'calories': calories or 0,
            'protein_content': protein or 0,
            'carbohydrate_content': carbs or 0,
            'fat_content': fat or 0,
            'fiber_content': fiber or 0,
            'sodium_content': sodium or 0
        }
        
        nutrition_score = calculate_nutrition_score(recipe_nutrition, targets)
        
        # Combine nutrition score with rating (if available)
        rating_score = (rating or 0) / 5.0  # Normalize to 0-1
        combined_score = (nutrition_score * 0.7) + (rating_score * 0.3)
        
        recommendations.append({
            "id": recipe_id,
            "name": name,
            "total_time": total_time or 0,
            "image_url": images or "",
            "nutrient_score": round(nutrition_score, 2),  # Changed from nutrition_score
            "combined_score": round(combined_score, 2),
            "calories": calories,
            "protein_content": protein,  # Changed from protein
            "carbohydrate_content": carbs,  # Changed from carbs  
            "fat_content": fat  # Changed from fat
        })
    
    # Sort by combined score and return top results
    recommendations.sort(key=lambda x: x['combined_score'], reverse=True)
    return recommendations[:number]


if __name__ == "__main__":
    # Test example
    test_data = {
        "age": 30,
        "gender": "male",
        "weight": 75.0,
        "height": 180.0,
        "activity_level": "moderately_active",
        "meal_type": "lunch"
    }
    
    print("Nutriment recommendation system ready")
