#!/usr/bin/env python3
"""
Integration tests for leftover recommendation system using real database.
"""

import unittest
import json
import os
import sys

# Add src directory to path for imports
sys.path.insert(0, os.path.join(os.path.dirname(__file__), '..', 'src'))

from leftover_recommendation import get_leftover_recommendations


class TestLeftoverRecommendationIntegration(unittest.TestCase):
    
    def setUp(self):
        """Set up test with real database path."""
        # Path to the real homeal.db database
        self.db_path = os.path.join(os.path.dirname(__file__), '..', '..', 'homeal.db')
        
        # Verify database exists
        if not os.path.exists(self.db_path):
            self.skipTest(f"Database not found at {self.db_path}")
    
    def test_get_recommendations_with_real_data_common_ingredients(self):
        """Test getting recommendations with common ingredients from real database."""
        leftover_data = {
            "ingredients": [
                {
                    "name": "butter",
                    "quantity": 100,
                    "unit": "g",
                    "expiration_date": "2024-01-15"
                },
                {
                    "name": "turmeric", 
                    "quantity": 1,
                    "unit": "tsp",
                    "expiration_date": "2024-01-20"
                }
            ]
        }
        
        recommendations = get_leftover_recommendations(
            self.db_path, 
            json.dumps(leftover_data), 
            5
        )
        
        self.assertIsInstance(recommendations, list)
        self.assertGreater(len(recommendations), 0, "Should find recipes with common ingredients")
        
        # Verify response format
        for rec in recommendations:
            self.assertIn("id", rec)
            self.assertIn("name", rec)
            self.assertIn("match_score", rec)
            self.assertIn("total_time", rec)
            self.assertIn("image_url", rec)
            self.assertGreater(rec["match_score"], 0.0)
    
    def test_get_recommendations_with_specific_ingredients(self):
        """Test with ingredients that should have good matches."""
        leftover_data = {
            "ingredients": [
                {
                    "name": "pork chops",
                    "quantity": 2,
                    "unit": "pieces",
                    "expiration_date": "2024-01-15"
                }
            ]
        }
        
        recommendations = get_leftover_recommendations(
            self.db_path,
            json.dumps(leftover_data),
            3
        )
        
        self.assertIsInstance(recommendations, list)
        if recommendations:
            # Check that recipes with pork chops have higher match scores
            for rec in recommendations:
                self.assertGreaterEqual(rec["match_score"], 0.1)
    
    def test_get_recommendations_with_uncommon_ingredients(self):
        """Test with ingredients that might not match many recipes."""
        leftover_data = {
            "ingredients": [
                {
                    "name": "very_rare_ingredient_xyz",
                    "quantity": 1,
                    "unit": "piece",
                    "expiration_date": "2024-01-15"
                }
            ]
        }
        
        recommendations = get_leftover_recommendations(
            self.db_path,
            json.dumps(leftover_data),
            5
        )
        
        # Should return empty list or very low match scores
        self.assertIsInstance(recommendations, list)
        for rec in recommendations:
            # Any matches should have very low scores
            self.assertLessEqual(rec["match_score"], 0.5)
    
    def test_get_recommendations_multiple_ingredients(self):
        """Test with multiple ingredients to see if match scores improve."""
        leftover_data = {
            "ingredients": [
                {
                    "name": "butter",
                    "quantity": 50,
                    "unit": "g",
                    "expiration_date": "2024-01-15"
                },
                {
                    "name": "blue cheese",
                    "quantity": 100,
                    "unit": "g",
                    "expiration_date": "2024-01-20"
                },
                {
                    "name": "angel hair pasta",
                    "quantity": 200,
                    "unit": "g",
                    "expiration_date": "2024-01-25"
                }
            ]
        }
        
        recommendations = get_leftover_recommendations(
            self.db_path,
            json.dumps(leftover_data),
            5
        )
        
        self.assertIsInstance(recommendations, list)
        
        if recommendations:
            # Should find some good matches with multiple ingredients
            best_match = recommendations[0]
            self.assertGreater(best_match["match_score"], 0.2)
    
    def test_database_performance_with_real_data(self):
        """Test that queries perform reasonably well with real database size."""
        import time
        
        leftover_data = {
            "ingredients": [
                {
                    "name": "butter",
                    "quantity": 100,
                    "unit": "g", 
                    "expiration_date": "2024-01-15"
                }
            ]
        }
        
        start_time = time.time()
        recommendations = get_leftover_recommendations(
            self.db_path,
            json.dumps(leftover_data),
            10
        )
        end_time = time.time()
        
        # Should complete within reasonable time (adjust as needed)
        self.assertLess(end_time - start_time, 5.0, "Query should complete within 5 seconds")
        self.assertIsInstance(recommendations, list)
    
    def test_real_database_schema_compatibility(self):
        """Test that the recommendation function works with the real database schema."""
        import sqlite3
        
        # Test database connection and basic query
        conn = sqlite3.connect(self.db_path)
        cursor = conn.cursor()
        
        # Verify required tables exist
        cursor.execute("SELECT name FROM sqlite_master WHERE type='table'")
        tables = [row[0] for row in cursor.fetchall()]
        
        self.assertIn("Recipe", tables)
        self.assertIn("RecipeIngredient", tables)
        self.assertIn("Ingredient", tables)
        
        # Verify basic data exists
        cursor.execute("SELECT COUNT(*) FROM Recipe")
        recipe_count = cursor.fetchone()[0]
        self.assertGreater(recipe_count, 0)
        
        cursor.execute("SELECT COUNT(*) FROM RecipeIngredient")
        ingredient_count = cursor.fetchone()[0]
        self.assertGreater(ingredient_count, 0)
        
        conn.close()


if __name__ == "__main__":
    unittest.main()
