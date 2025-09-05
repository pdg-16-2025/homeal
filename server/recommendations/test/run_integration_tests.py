#!/usr/bin/env python3
"""
Integration test runner for recommendation system using real database.
This runs tests against the actual homeal.db database instead of mock data.
"""

import unittest
import sys
import os

# Add src directory to path for imports
sys.path.insert(0, os.path.join(os.path.dirname(__file__), '..', 'src'))

# Import integration test modules
from test_integration_leftover import TestLeftoverRecommendationIntegration
from test_integration_nutriment import TestNutrimentRecommendationIntegration
from test_integration_preference_simple import TestPreferenceRecommendationSimple


def run_all_integration_tests():
    """Run all recommendation integration tests."""
    
    # Create test suite
    test_suite = unittest.TestSuite()
    
    # Add all integration test classes
    test_classes = [
        TestLeftoverRecommendationIntegration,
        TestNutrimentRecommendationIntegration,
        TestPreferenceRecommendationSimple
    ]
    
    for test_class in test_classes:
        tests = unittest.TestLoader().loadTestsFromTestCase(test_class)
        test_suite.addTests(tests)
    
    # Run tests
    runner = unittest.TextTestRunner(verbosity=2)
    result = runner.run(test_suite)
    
    # Return success/failure
    return result.wasSuccessful()


def run_specific_integration_test(test_name):
    """Run a specific integration test module."""
    
    test_modules = {
        'leftover': TestLeftoverRecommendationIntegration,
        'nutriment': TestNutrimentRecommendationIntegration,
        'preference': TestPreferenceRecommendationSimple
    }
    
    if test_name not in test_modules:
        print(f"Unknown test: {test_name}")
        print(f"Available integration tests: {list(test_modules.keys())}")
        return False
    
    test_class = test_modules[test_name]
    test_suite = unittest.TestLoader().loadTestsFromTestCase(test_class)
    
    runner = unittest.TextTestRunner(verbosity=2)
    result = runner.run(test_suite)
    
    return result.wasSuccessful()


def check_database_availability():
    """Check if the real database is available for testing."""
    db_path = os.path.join(os.path.dirname(__file__), '..', '..', 'homeal.db')
    
    if not os.path.exists(db_path):
        print(f"❌ Database not found at: {db_path}")
        print("Please ensure the homeal.db database exists in the server directory.")
        return False
    
    # Test database connectivity
    try:
        import sqlite3
        conn = sqlite3.connect(db_path)
        cursor = conn.cursor()
        
        # Check for required tables
        required_tables = ['Recipe', 'RecipeIngredient', 'Ingredient', 'UserRecipeRating']
        cursor.execute("SELECT name FROM sqlite_master WHERE type='table'")
        existing_tables = [row[0] for row in cursor.fetchall()]
        
        missing_tables = [table for table in required_tables if table not in existing_tables]
        if missing_tables:
            print(f"❌ Missing required tables: {missing_tables}")
            return False
        
        # Check for data
        cursor.execute("SELECT COUNT(*) FROM Recipe")
        recipe_count = cursor.fetchone()[0]
        if recipe_count == 0:
            print("❌ No recipes found in database")
            return False
        
        cursor.execute("SELECT COUNT(*) FROM RecipeIngredient")
        ingredient_count = cursor.fetchone()[0]
        if ingredient_count == 0:
            print("❌ No recipe ingredients found in database")
            return False
        
        conn.close()
        print(f"✅ Database ready: {recipe_count} recipes, {ingredient_count} recipe ingredients")
        return True
        
    except Exception as e:
        print(f"❌ Database error: {e}")
        return False


def main():
    """Main entry point for integration test runner."""
    
    print("=== Homeal Recommendation System Integration Tests ===")
    print("Testing against real database (homeal.db)\n")
    
    # Check database availability first
    if not check_database_availability():
        print("\n❌ Integration tests cannot run without proper database setup.")
        sys.exit(1)
    
    print()
    
    if len(sys.argv) < 2:
        print("Running all integration tests...")
        success = run_all_integration_tests()
    else:
        test_name = sys.argv[1]
        print(f"Running {test_name} integration tests...")
        success = run_specific_integration_test(test_name)
    
    print()
    if success:
        print("✅ All integration tests passed!")
        print("The recommendation system works correctly with the real database.")
        sys.exit(0)
    else:
        print("❌ Some integration tests failed!")
        print("Please check the errors above and fix any issues.")
        sys.exit(1)


if __name__ == "__main__":
    main()
