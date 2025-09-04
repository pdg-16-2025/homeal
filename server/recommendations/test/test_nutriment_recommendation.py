#!/usr/bin/env python3
"""
Unit tests for nutriment recommendation system.
"""

import unittest
import json
import tempfile
import sqlite3
import os
import sys

# Add src directory to path for imports
sys.path.insert(0, os.path.join(os.path.dirname(__file__), '..', 'src'))

from nutriment_recommendation import (
    ActivityLevel,
    Gender,
    MealType,
    UserProfile,
    NutritionalTargets,
    calculate_bmr,
    calculate_tdee,
    calculate_nutritional_targets,
    calculate_nutrition_score,
    get_nutriment_recommendations
)


class TestNutrimentRecommendation(unittest.TestCase):
    
    def setUp(self):
        """Set up test database and sample data."""
        self.db_fd, self.db_path = tempfile.mkstemp()
        self.conn = sqlite3.connect(self.db_path)
        self.create_test_tables()
        self.populate_test_data()
    
    def tearDown(self):
        """Clean up test database."""
        self.conn.close()
        os.close(self.db_fd)
        os.unlink(self.db_path)
    
    def create_test_tables(self):
        """Create test database tables."""
        cursor = self.conn.cursor()
        
        cursor.execute("""
            CREATE TABLE Recipe (
                id INTEGER PRIMARY KEY,
                name TEXT NOT NULL,
                total_time INTEGER,
                images TEXT,
                calories REAL,
                protein_content REAL,
                carbohydrate_content REAL,
                fat_content REAL,
                fiber_content REAL,
                sodium_content REAL,
                aggregated_rating REAL
            )
        """)
        
        self.conn.commit()
    
    def populate_test_data(self):
        """Populate test database with sample recipes."""
        cursor = self.conn.cursor()
        
        recipes = [
            (1, "High Protein Salad", 15, "salad.jpg", 300, 25, 15, 12, 8, 500, 4.5),
            (2, "Pasta Carbonara", 25, "pasta.jpg", 650, 20, 85, 25, 5, 800, 4.2),
            (3, "Grilled Chicken", 30, "chicken.jpg", 400, 35, 5, 18, 2, 600, 4.8),
            (4, "Vegetarian Bowl", 20, "bowl.jpg", 350, 15, 45, 10, 12, 400, 4.0)
        ]
        
        cursor.executemany("""
            INSERT INTO Recipe VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """, recipes)
        
        self.conn.commit()
    
    def test_user_profile_creation(self):
        """Test UserProfile dataclass creation."""
        user = UserProfile(
            age=30,
            gender=Gender.MALE,
            weight=75.0,
            height=180.0,
            activity_level=ActivityLevel.MODERATELY_ACTIVE,
            meal_type=MealType.LUNCH
        )
        
        self.assertEqual(user.age, 30)
        self.assertEqual(user.gender, Gender.MALE)
        self.assertEqual(user.weight, 75.0)
        self.assertEqual(user.height, 180.0)
        self.assertEqual(user.activity_level, ActivityLevel.MODERATELY_ACTIVE)
        self.assertEqual(user.meal_type, MealType.LUNCH)
    
    def test_nutritional_targets_creation(self):
        """Test NutritionalTargets dataclass creation."""
        targets = NutritionalTargets(
            calories=2000,
            protein=150,
            carbs=250,
            fat=67,
            fiber=25,
            sodium_limit=2300
        )
        
        self.assertEqual(targets.calories, 2000)
        self.assertEqual(targets.protein, 150)
        self.assertEqual(targets.carbs, 250)
        self.assertEqual(targets.fat, 67)
        self.assertEqual(targets.fiber, 25)
        self.assertEqual(targets.sodium_limit, 2300)
    
    def test_calculate_bmr_male(self):
        """Test BMR calculation for male."""
        user = UserProfile(
            age=30,
            gender=Gender.MALE,
            weight=75,
            height=180,
            activity_level=ActivityLevel.SEDENTARY
        )
        
        bmr = calculate_bmr(user)
        expected_bmr = (10 * 75) + (6.25 * 180) - (5 * 30) + 5
        self.assertEqual(bmr, expected_bmr)
        self.assertEqual(bmr, 1730)  # 750 + 1125 - 150 + 5
    
    def test_calculate_bmr_female(self):
        """Test BMR calculation for female."""
        user = UserProfile(
            age=25,
            gender=Gender.FEMALE,
            weight=60,
            height=165,
            activity_level=ActivityLevel.SEDENTARY
        )
        
        bmr = calculate_bmr(user)
        expected_bmr = (10 * 60) + (6.25 * 165) - (5 * 25) - 161
        self.assertEqual(bmr, expected_bmr)
        self.assertEqual(bmr, 1314.25)  # 600 + 1031.25 - 125 - 161
    
    def test_calculate_bmr_other_gender(self):
        """Test BMR calculation for other gender (treated as female)."""
        user = UserProfile(
            age=30,
            gender=Gender.OTHER,
            weight=70,
            height=175,
            activity_level=ActivityLevel.SEDENTARY
        )
        
        bmr = calculate_bmr(user)
        expected_bmr = (10 * 70) + (6.25 * 175) - (5 * 30) - 161
        self.assertEqual(bmr, expected_bmr)
        self.assertEqual(bmr, 1532.75)  # 700 + 1093.75 - 150 - 161
    
    def test_calculate_tdee_all_activity_levels(self):
        """Test TDEE calculation for all activity levels."""
        user = UserProfile(
            age=30,
            gender=Gender.MALE,
            weight=75,
            height=180,
            activity_level=ActivityLevel.SEDENTARY
        )
        
        bmr = calculate_bmr(user)  # 1730
        
        # Test each activity level
        user.activity_level = ActivityLevel.SEDENTARY
        tdee = calculate_tdee(user)
        self.assertEqual(tdee, bmr * 1.2)
        
        user.activity_level = ActivityLevel.LIGHTLY_ACTIVE
        tdee = calculate_tdee(user)
        self.assertEqual(tdee, bmr * 1.375)
        
        user.activity_level = ActivityLevel.MODERATELY_ACTIVE
        tdee = calculate_tdee(user)
        self.assertEqual(tdee, bmr * 1.55)
        
        user.activity_level = ActivityLevel.VERY_ACTIVE
        tdee = calculate_tdee(user)
        self.assertEqual(tdee, bmr * 1.725)
        
        user.activity_level = ActivityLevel.EXTREMELY_ACTIVE
        tdee = calculate_tdee(user)
        self.assertEqual(tdee, bmr * 1.9)
    
    def test_calculate_nutritional_targets_daily(self):
        """Test nutritional targets calculation for daily (no meal type)."""
        user = UserProfile(
            age=30,
            gender=Gender.MALE,
            weight=75,
            height=180,
            activity_level=ActivityLevel.MODERATELY_ACTIVE
        )
        
        targets = calculate_nutritional_targets(user)
        
        # TDEE should be 1730 * 1.55 = 2681.5
        expected_calories = 1730 * 1.55
        self.assertAlmostEqual(targets.calories, expected_calories, places=1)
        
        # Macros: 55% carbs, 20% protein, 25% fat
        expected_carbs = (expected_calories * 0.55) / 4
        expected_protein = (expected_calories * 0.20) / 4
        expected_fat = (expected_calories * 0.25) / 9
        
        self.assertAlmostEqual(targets.carbs, expected_carbs, places=1)
        self.assertAlmostEqual(targets.protein, expected_protein, places=1)
        self.assertAlmostEqual(targets.fat, expected_fat, places=1)
        
        # Fiber should be 38 for male
        self.assertEqual(targets.fiber, 38)
        
        # Sodium limit should be 2300/3
        self.assertAlmostEqual(targets.sodium_limit, 2300/3, places=1)
    
    def test_calculate_nutritional_targets_with_meal_type(self):
        """Test nutritional targets calculation with meal type."""
        user = UserProfile(
            age=30,
            gender=Gender.FEMALE,
            weight=60,
            height=165,
            activity_level=ActivityLevel.LIGHTLY_ACTIVE,
            meal_type=MealType.LUNCH
        )
        
        targets = calculate_nutritional_targets(user)
        
        # TDEE should be 1314.25 * 1.375 = 1807.09375
        # Lunch should be 35% of daily calories
        expected_daily_calories = 1314.25 * 1.375
        expected_meal_calories = expected_daily_calories * 0.35
        
        self.assertAlmostEqual(targets.calories, expected_meal_calories, places=1)
        
        # Fiber should be 25 for female, adjusted for meal
        expected_fiber = 25 * 0.35
        self.assertAlmostEqual(targets.fiber, expected_fiber, places=1)
    
    def test_calculate_nutrition_score(self):
        """Test nutrition score calculation."""
        targets = NutritionalTargets(
            calories=500,
            protein=25,
            carbs=62.5,
            fat=14,
            fiber=8,
            sodium_limit=767
        )
        
        # Perfect match recipe
        recipe_nutrition = {
            'calories': 500,
            'protein_content': 25,
            'carbohydrate_content': 62.5,
            'fat_content': 14,
            'fiber_content': 8,
            'sodium_content': 400
        }
        
        score = calculate_nutrition_score(recipe_nutrition, targets)
        self.assertGreater(score, 0.8)  # Should be very high
        
        # Poor match recipe
        recipe_nutrition = {
            'calories': 1000,  # Double target
            'protein_content': 5,   # Much lower
            'carbohydrate_content': 100,  # Much higher
            'fat_content': 30,     # Much higher
            'fiber_content': 1,    # Much lower
            'sodium_content': 1500  # Much higher
        }
        
        score = calculate_nutrition_score(recipe_nutrition, targets)
        self.assertLess(score, 0.5)  # Should be low
    
    def test_calculate_nutrition_score_zero_targets(self):
        """Test nutrition score with zero calorie targets."""
        targets = NutritionalTargets(
            calories=0,
            protein=0,
            carbs=0,
            fat=0,
            fiber=0,
            sodium_limit=0
        )
        
        recipe_nutrition = {
            'calories': 500,
            'protein_content': 25,
            'carbohydrate_content': 62.5,
            'fat_content': 14,
            'fiber_content': 8,
            'sodium_content': 400
        }
        
        score = calculate_nutrition_score(recipe_nutrition, targets)
        self.assertEqual(score, 0.0)
    
    def test_get_nutriment_recommendations_valid_input(self):
        """Test getting recommendations with valid user data."""
        user_data = {
            "age": 30,
            "gender": "male",
            "weight": 75.0,
            "height": 180.0,
            "activity_level": "moderately_active",
            "meal_type": "lunch"
        }
        
        recommendations = get_nutriment_recommendations(
            self.db_path,
            json.dumps(user_data),
            3
        )
        
        self.assertIsInstance(recommendations, list)
        self.assertLessEqual(len(recommendations), 3)
        
        if recommendations:
            for rec in recommendations:
                self.assertIn("id", rec)
                self.assertIn("name", rec)
                self.assertIn("nutrition_score", rec)
                self.assertIn("combined_score", rec)
                self.assertIn("calories", rec)
    
    def test_get_nutriment_recommendations_invalid_json(self):
        """Test error handling for invalid JSON input."""
        with self.assertRaises(ValueError):
            get_nutriment_recommendations(self.db_path, "invalid json", 5)
    
    def test_get_nutriment_recommendations_missing_fields(self):
        """Test error handling for missing required fields."""
        user_data = {
            "age": 30,
            "gender": "male"
            # Missing weight, height, activity_level
        }
        
        with self.assertRaises(ValueError):
            get_nutriment_recommendations(
                self.db_path,
                json.dumps(user_data),
                5
            )
    
    def test_get_nutriment_recommendations_invalid_enum_values(self):
        """Test error handling for invalid enum values."""
        user_data = {
            "age": 30,
            "gender": "invalid_gender",
            "weight": 75.0,
            "height": 180.0,
            "activity_level": "moderately_active"
        }
        
        with self.assertRaises(ValueError):
            get_nutriment_recommendations(
                self.db_path,
                json.dumps(user_data),
                5
            )
    
    def test_recommendation_sorting_by_combined_score(self):
        """Test that recommendations are sorted by combined score."""
        user_data = {
            "age": 30,
            "gender": "male",
            "weight": 75.0,
            "height": 180.0,
            "activity_level": "moderately_active"
        }
        
        recommendations = get_nutriment_recommendations(
            self.db_path,
            json.dumps(user_data),
            5
        )
        
        # Verify recommendations are sorted by combined_score (descending)
        if len(recommendations) > 1:
            for i in range(len(recommendations) - 1):
                self.assertGreaterEqual(
                    recommendations[i]["combined_score"],
                    recommendations[i + 1]["combined_score"]
                )
    
    def test_meal_type_adjustment(self):
        """Test that meal type properly adjusts targets."""
        user_data_breakfast = {
            "age": 30,
            "gender": "male",
            "weight": 75.0,
            "height": 180.0,
            "activity_level": "moderately_active",
            "meal_type": "breakfast"
        }
        
        user_data_dinner = {
            "age": 30,
            "gender": "male",
            "weight": 75.0,
            "height": 180.0,
            "activity_level": "moderately_active",
            "meal_type": "dinner"
        }
        
        # Both should return different recommendations based on meal type
        breakfast_recs = get_nutriment_recommendations(
            self.db_path,
            json.dumps(user_data_breakfast),
            3
        )
        
        dinner_recs = get_nutriment_recommendations(
            self.db_path,
            json.dumps(user_data_dinner),
            3
        )
        
        # Should both return valid recommendations
        self.assertIsInstance(breakfast_recs, list)
        self.assertIsInstance(dinner_recs, list)


if __name__ == "__main__":
    unittest.main()
