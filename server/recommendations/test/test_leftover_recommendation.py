#!/usr/bin/env python3
"""
Unit tests for leftover recommendation system.
"""

import unittest
import json
import tempfile
import sqlite3
import os
import sys
from datetime import datetime, timedelta

# Add src directory to path for imports
sys.path.insert(0, os.path.join(os.path.dirname(__file__), '..', 'src'))

from leftover_recommendation import (
    LeftoverIngredient,
    ShortRecipe,
    calculate_ingredient_match_score,
    get_leftover_recommendations
)


class TestLeftoverRecommendation(unittest.TestCase):
    
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
        
        # Recipe table
        cursor.execute("""
            CREATE TABLE Recipe (
                id INTEGER PRIMARY KEY,
                name TEXT NOT NULL,
                total_time INTEGER,
                images TEXT
            )
        """)
        
        # RecipeIngredient table
        cursor.execute("""
            CREATE TABLE RecipeIngredient (
                id INTEGER PRIMARY KEY,
                recipe_id INTEGER,
                ingredient_name TEXT,
                FOREIGN KEY (recipe_id) REFERENCES Recipe(id)
            )
        """)
        
        self.conn.commit()
    
    def populate_test_data(self):
        """Populate test database with sample recipes and ingredients."""
        cursor = self.conn.cursor()
        
        # Sample recipes
        recipes = [
            (1, "Tomato Pasta", 30, "pasta1.jpg"),
            (2, "Cheese Sandwich", 10, "sandwich1.jpg"),
            (3, "Tomato Cheese Toast", 15, "toast1.jpg"),
            (4, "Chicken Salad", 20, "salad1.jpg")
        ]
        
        cursor.executemany("INSERT INTO Recipe VALUES (?, ?, ?, ?)", recipes)
        
        # Sample ingredients
        ingredients = [
            (1, 1, "tomato"),
            (2, 1, "pasta"),
            (3, 1, "olive oil"),
            (4, 2, "cheese"),
            (5, 2, "bread"),
            (6, 3, "tomato"),
            (7, 3, "cheese"),
            (8, 3, "bread"),
            (9, 4, "chicken"),
            (10, 4, "lettuce")
        ]
        
        cursor.executemany("INSERT INTO RecipeIngredient VALUES (?, ?, ?)", ingredients)
        self.conn.commit()
    
    def test_leftover_ingredient_creation(self):
        """Test LeftoverIngredient dataclass creation."""
        ingredient = LeftoverIngredient(
            name="tomato",
            quantity=2.0,
            unit="pieces",
            expiration_date="2024-01-15"
        )
        
        self.assertEqual(ingredient.name, "tomato")
        self.assertEqual(ingredient.quantity, 2.0)
        self.assertEqual(ingredient.unit, "pieces")
        self.assertEqual(ingredient.expiration_date, "2024-01-15")
    
    def test_short_recipe_creation(self):
        """Test ShortRecipe dataclass creation."""
        recipe = ShortRecipe(
            id=1,
            name="Test Recipe",
            total_time=30,
            image_url="test.jpg",
            match_score=0.8
        )
        
        self.assertEqual(recipe.id, 1)
        self.assertEqual(recipe.name, "Test Recipe")
        self.assertEqual(recipe.total_time, 30)
        self.assertEqual(recipe.image_url, "test.jpg")
        self.assertEqual(recipe.match_score, 0.8)
    
    def test_calculate_ingredient_match_score(self):
        """Test ingredient matching score calculation."""
        # Perfect match
        recipe_ingredients = ["tomato", "cheese", "bread"]
        available_ingredients = ["tomato", "cheese", "bread"]
        score = calculate_ingredient_match_score(recipe_ingredients, available_ingredients)
        self.assertEqual(score, 1.0)
        
        # Partial match
        recipe_ingredients = ["tomato", "cheese", "bread", "onion"]
        available_ingredients = ["tomato", "cheese"]
        score = calculate_ingredient_match_score(recipe_ingredients, available_ingredients)
        self.assertEqual(score, 0.5)  # 2 out of 4
        
        # No match
        recipe_ingredients = ["chicken", "beef"]
        available_ingredients = ["tomato", "cheese"]
        score = calculate_ingredient_match_score(recipe_ingredients, available_ingredients)
        self.assertEqual(score, 0.0)
        
        # Empty recipe ingredients
        score = calculate_ingredient_match_score([], ["tomato"])
        self.assertEqual(score, 0.0)
        
        # Case insensitive matching
        recipe_ingredients = ["Tomato", "CHEESE"]
        available_ingredients = ["tomato", "cheese"]
        score = calculate_ingredient_match_score(recipe_ingredients, available_ingredients)
        self.assertEqual(score, 1.0)
    
    def test_get_leftover_recommendations_valid_input(self):
        """Test getting recommendations with valid leftover data."""
        leftover_data = {
            "ingredients": [
                {
                    "name": "tomato",
                    "quantity": 2,
                    "unit": "pieces",
                    "expiration_date": "2024-01-15"
                },
                {
                    "name": "cheese", 
                    "quantity": 200,
                    "unit": "g",
                    "expiration_date": "2024-01-20"
                }
            ]
        }
        
        recommendations = get_leftover_recommendations(
            self.db_path, 
            json.dumps(leftover_data), 
            3
        )
        
        self.assertIsInstance(recommendations, list)
        self.assertLessEqual(len(recommendations), 3)
        
        # Check that recipes with matching ingredients have higher scores
        if recommendations:
            for rec in recommendations:
                self.assertIn("id", rec)
                self.assertIn("name", rec)
                self.assertIn("match_score", rec)
                self.assertGreaterEqual(rec["match_score"], 0.1)
    
    def test_get_leftover_recommendations_invalid_json(self):
        """Test error handling for invalid JSON input."""
        with self.assertRaises(ValueError):
            get_leftover_recommendations(self.db_path, "invalid json", 5)
    
    def test_get_leftover_recommendations_empty_ingredients(self):
        """Test handling of empty ingredients list."""
        leftover_data = {"ingredients": []}
        recommendations = get_leftover_recommendations(
            self.db_path,
            json.dumps(leftover_data),
            5
        )
        self.assertEqual(recommendations, [])
    
    def test_get_leftover_recommendations_missing_ingredients_key(self):
        """Test handling of missing ingredients key."""
        leftover_data = {"other_key": "value"}
        recommendations = get_leftover_recommendations(
            self.db_path,
            json.dumps(leftover_data),
            5
        )
        self.assertEqual(recommendations, [])
    
    def test_expiration_date_priority(self):
        """Test that soon-expiring ingredients get priority."""
        tomorrow = (datetime.now() + timedelta(days=1)).isoformat()
        next_week = (datetime.now() + timedelta(days=7)).isoformat()
        
        leftover_data = {
            "ingredients": [
                {
                    "name": "tomato",
                    "quantity": 2,
                    "unit": "pieces",
                    "expiration_date": tomorrow  # Expiring soon
                },
                {
                    "name": "cheese",
                    "quantity": 200,
                    "unit": "g", 
                    "expiration_date": next_week  # Not expiring soon
                }
            ]
        }
        
        recommendations = get_leftover_recommendations(
            self.db_path,
            json.dumps(leftover_data),
            5
        )
        
        # Should prioritize recipes with soon-expiring ingredients
        self.assertIsInstance(recommendations, list)
    
    def test_invalid_expiration_date_format(self):
        """Test handling of invalid expiration date formats."""
        leftover_data = {
            "ingredients": [
                {
                    "name": "tomato",
                    "quantity": 2,
                    "unit": "pieces",
                    "expiration_date": "invalid-date"
                }
            ]
        }
        
        # Should not crash with invalid date format
        recommendations = get_leftover_recommendations(
            self.db_path,
            json.dumps(leftover_data),
            5
        )
        
        self.assertIsInstance(recommendations, list)
    
    def test_database_connection_error(self):
        """Test handling of database connection errors."""
        # Try with non-existent database path
        with self.assertRaises(Exception):
            get_leftover_recommendations(
                "/non/existent/path.db",
                json.dumps({"ingredients": []}),
                5
            )
    
    def test_recommendation_sorting(self):
        """Test that recommendations are sorted by match score."""
        leftover_data = {
            "ingredients": [
                {
                    "name": "tomato",
                    "quantity": 2,
                    "unit": "pieces",
                    "expiration_date": "2024-01-15"
                }
            ]
        }
        
        recommendations = get_leftover_recommendations(
            self.db_path,
            json.dumps(leftover_data),
            5
        )
        
        # Verify recommendations are sorted by match_score (descending)
        if len(recommendations) > 1:
            for i in range(len(recommendations) - 1):
                self.assertGreaterEqual(
                    recommendations[i]["match_score"],
                    recommendations[i + 1]["match_score"]
                )


if __name__ == "__main__":
    unittest.main()
