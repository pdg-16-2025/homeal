#!/usr/bin/env python3
"""
Simple integration test for preference recommendation system using unified database.
"""

import unittest
import json
import os
import sys

# Add src directory to path for imports
sys.path.insert(0, os.path.join(os.path.dirname(__file__), '..', 'src'))

from preference_recommendation import get_preference_recommendations


class TestPreferenceRecommendationSimple(unittest.TestCase):
    
    def setUp(self):
        """Set up test with real database path."""
        self.db_path = os.path.join(os.path.dirname(__file__), '..', '..', 'homeal.db')
        self.assertTrue(os.path.exists(self.db_path), "Database file not found")
    
    def test_preference_recommendations_with_real_data(self):
        """Test getting preference recommendations with actual review data."""
        user_data = {
            "user_id": 999,  # New user not in database
            "ratings": [
                {"recipe_id": 24768, "rating": 5.0},  # Recipe with most ratings
                {"recipe_id": 18514, "rating": 4.0},  # Recipe with many ratings
                {"recipe_id": 20080, "rating": 3.0}   # Recipe with many ratings
            ]
        }
        
        recommendations = get_preference_recommendations(
            self.db_path,
            json.dumps(user_data),
            5
        )
        
        self.assertIsInstance(recommendations, list)
        self.assertGreater(len(recommendations), 0, "Should return some recommendations")
        self.assertLessEqual(len(recommendations), 5)
        
        # Check recommendation structure
        for rec in recommendations:
            self.assertIn("id", rec)
            self.assertIn("name", rec)
            self.assertIn("preference_score", rec)
            self.assertIn("avg_rating", rec)
            self.assertIsInstance(rec["preference_score"], (int, float))
            self.assertGreater(rec["preference_score"], 0)
            
            # Should not recommend recipes already rated by user
            self.assertNotIn(rec["id"], [24768, 18514, 20080])
    
    def test_preference_recommendations_sorting(self):
        """Test that recommendations are sorted by preference score."""
        user_data = {
            "user_id": 888,
            "ratings": [
                {"recipe_id": 24768, "rating": 5.0},
                {"recipe_id": 18514, "rating": 4.5},
                {"recipe_id": 20080, "rating": 4.0}
            ]
        }
        
        recommendations = get_preference_recommendations(
            self.db_path,
            json.dumps(user_data),
            5
        )
        
        if len(recommendations) > 1:
            for i in range(len(recommendations) - 1):
                self.assertGreaterEqual(
                    recommendations[i]["preference_score"],
                    recommendations[i + 1]["preference_score"],
                    "Recommendations should be sorted by preference score (descending)"
                )
    
    def test_preference_recommendations_no_ratings(self):
        """Test handling of user with no ratings."""
        user_data = {
            "user_id": 777,
            "ratings": []
        }
        
        recommendations = get_preference_recommendations(
            self.db_path,
            json.dumps(user_data),
            5
        )
        
        self.assertEqual(recommendations, [], "Should return empty list for user with no ratings")
    
    def test_preference_recommendations_invalid_user_id(self):
        """Test error handling for missing user_id."""
        user_data = {
            "ratings": [
                {"recipe_id": 24768, "rating": 5.0}
            ]
        }
        
        with self.assertRaises(ValueError) as context:
            get_preference_recommendations(
                self.db_path,
                json.dumps(user_data),
                5
            )
        
        self.assertIn("Missing user_id", str(context.exception))


if __name__ == "__main__":
    unittest.main()
