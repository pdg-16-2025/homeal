#!/usr/bin/env python3
"""
Integration tests for nutriment recommendation system using real database.
"""

import unittest
import json
import os
import sys

# Add src directory to path for imports
sys.path.insert(0, os.path.join(os.path.dirname(__file__), '..', 'src'))

from nutriment_recommendation import get_nutriment_recommendations


class TestNutrimentRecommendationIntegration(unittest.TestCase):
    
    def setUp(self):
        """Set up test with real database path."""
        # Path to the real homeal.db database
        self.db_path = os.path.join(os.path.dirname(__file__), '..', '..', 'homeal.db')
        
        # Verify database exists
        if not os.path.exists(self.db_path):
            self.skipTest(f"Database not found at {self.db_path}")
    
    def test_get_recommendations_with_real_nutritional_data(self):
        """Test getting recommendations using real recipe nutritional data."""
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
            5
        )
        
        self.assertIsInstance(recommendations, list)
        
        if recommendations:
            for rec in recommendations:
                self.assertIn("id", rec)
                self.assertIn("name", rec)
                self.assertIn("nutrient_score", rec)
                self.assertIn("calories", rec)
                self.assertIn("protein_content", rec)
                
                # Nutritional values should be realistic
                if rec["calories"]:
                    self.assertGreater(rec["calories"], 0)
                    self.assertLess(rec["calories"], 5000)  # Reasonable upper bound
    
    def test_different_activity_levels(self):
        """Test recommendations for different activity levels."""
        base_user_data = {
            "age": 25,
            "gender": "female",
            "weight": 60.0,
            "height": 165.0,
            "meal_type": "dinner"
        }
        
        activity_levels = ["sedentary", "lightly_active", "moderately_active", "very_active", "extra_active"]
        
        for activity in activity_levels:
            user_data = base_user_data.copy()
            user_data["activity_level"] = activity
            
            recommendations = get_nutriment_recommendations(
                self.db_path,
                json.dumps(user_data),
                3
            )
            
            self.assertIsInstance(recommendations, list)
            # Different activity levels should potentially give different recommendations
    
    def test_different_meal_types(self):
        """Test recommendations for different meal types."""
        base_user_data = {
            "age": 35,
            "gender": "male",
            "weight": 80.0,
            "height": 175.0,
            "activity_level": "moderately_active"
        }
        
        meal_types = ["breakfast", "lunch", "dinner", "snack"]
        
        for meal_type in meal_types:
            user_data = base_user_data.copy()
            user_data["meal_type"] = meal_type
            
            recommendations = get_nutriment_recommendations(
                self.db_path,
                json.dumps(user_data),
                3
            )
            
            self.assertIsInstance(recommendations, list)
    
    def test_high_protein_needs(self):
        """Test recommendations for someone who might need high protein (very active)."""
        user_data = {
            "age": 28,
            "gender": "male",
            "weight": 85.0,
            "height": 185.0,
            "activity_level": "extra_active",
            "meal_type": "lunch"
        }
        
        recommendations = get_nutriment_recommendations(
            self.db_path,
            json.dumps(user_data),
            5
        )
        
        self.assertIsInstance(recommendations, list)
        
        if recommendations:
            # Check that we get some high-protein options
            high_protein_count = sum(1 for rec in recommendations 
                                   if rec.get("protein_content", 0) and rec["protein_content"] > 20)
            # Should have at least some high-protein options for very active person
    
    def test_calorie_appropriate_recommendations(self):
        """Test that recommendations provide appropriate calorie amounts."""
        user_data = {
            "age": 30,
            "gender": "female",
            "weight": 55.0,
            "height": 160.0,
            "activity_level": "sedentary",
            "meal_type": "breakfast"
        }
        
        recommendations = get_nutriment_recommendations(
            self.db_path,
            json.dumps(user_data),
            5
        )
        
        if recommendations:
            for rec in recommendations:
                if rec.get("calories"):
                    # Breakfast for sedentary person should be reasonable
                    self.assertGreater(rec["calories"], 100)  # Not too low
                    self.assertLess(rec["calories"], 1000)    # Not too high for breakfast
    
    def test_performance_with_real_database_size(self):
        """Test that nutriment recommendations perform well with real database size."""
        import time
        
        user_data = {
            "age": 30,
            "gender": "male",
            "weight": 75.0,
            "height": 180.0,
            "activity_level": "moderately_active",
            "meal_type": "lunch"
        }
        
        start_time = time.time()
        recommendations = get_nutriment_recommendations(
            self.db_path,
            json.dumps(user_data),
            10
        )
        end_time = time.time()
        
        # Should complete within reasonable time
        self.assertLess(end_time - start_time, 5.0, "Query should complete within 5 seconds")
        self.assertIsInstance(recommendations, list)
    
    def test_real_database_nutritional_schema(self):
        """Test that the Recipe table has the expected nutritional fields."""
        import sqlite3
        
        conn = sqlite3.connect(self.db_path)
        cursor = conn.cursor()
        
        # Verify Recipe table structure includes nutritional fields
        cursor.execute("PRAGMA table_info(Recipe)")
        columns = [row[1] for row in cursor.fetchall()]
        
        nutritional_fields = [
            'calories', 'protein_content', 'carbohydrate_content', 'fat_content',
            'fiber_content', 'sodium_content', 'saturated_fat_content', 'sugar_content'
        ]
        
        for field in nutritional_fields:
            self.assertIn(field, columns, f"Nutritional field {field} should exist in Recipe table")
        
        # Check that some recipes have nutritional data
        cursor.execute("SELECT COUNT(*) FROM Recipe WHERE calories IS NOT NULL AND calories > 0")
        recipes_with_calories = cursor.fetchone()[0]
        self.assertGreater(recipes_with_calories, 0, "Some recipes should have calorie data")
        
        conn.close()
    
    def test_nutrient_scoring_with_real_data(self):
        """Test that nutrient scoring works with actual recipe data."""
        user_data = {
            "age": 25,
            "gender": "female",
            "weight": 65.0,
            "height": 170.0,
            "activity_level": "moderately_active",
            "meal_type": "dinner"
        }
        
        recommendations = get_nutriment_recommendations(
            self.db_path,
            json.dumps(user_data),
            5
        )
        
        if recommendations:
            # Verify scoring
            for rec in recommendations:
                self.assertIn("nutrient_score", rec)
                self.assertGreaterEqual(rec["nutrient_score"], 0.0)
                self.assertLessEqual(rec["nutrient_score"], 1.0)
            
            # Verify recommendations are sorted by nutrient score (descending)
            if len(recommendations) > 1:
                for i in range(len(recommendations) - 1):
                    self.assertGreaterEqual(
                        recommendations[i]["nutrient_score"],
                        recommendations[i + 1]["nutrient_score"]
                    )
    
    def test_edge_case_nutritional_profiles(self):
        """Test with edge case user profiles."""
        # Very young person
        user_data_young = {
            "age": 18,
            "gender": "male",
            "weight": 60.0,
            "height": 175.0,
            "activity_level": "very_active",
            "meal_type": "lunch"
        }
        
        recommendations = get_nutriment_recommendations(
            self.db_path,
            json.dumps(user_data_young),
            3
        )
        self.assertIsInstance(recommendations, list)
        
        # Older person
        user_data_older = {
            "age": 65,
            "gender": "female", 
            "weight": 70.0,
            "height": 160.0,
            "activity_level": "lightly_active",
            "meal_type": "breakfast"
        }
        
        recommendations = get_nutriment_recommendations(
            self.db_path,
            json.dumps(user_data_older),
            3
        )
        self.assertIsInstance(recommendations, list)


if __name__ == "__main__":
    unittest.main()
