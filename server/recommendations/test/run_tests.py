#!/usr/bin/env python3
"""
Test runner for all recommendation system tests.
"""

import unittest
import sys
import os

# Add src directory to path for imports
sys.path.insert(0, os.path.join(os.path.dirname(__file__), '..', 'src'))

# Import all test modules
from test_leftover_recommendation import TestLeftoverRecommendation
from test_nutriment_recommendation import TestNutrimentRecommendation


def run_all_tests():
    """Run all recommendation system tests."""
    
    # Create test suite
    test_suite = unittest.TestSuite()
    
    # Add all test classes
    test_classes = [
        TestLeftoverRecommendation,
        TestNutrimentRecommendation
    ]
    
    for test_class in test_classes:
        tests = unittest.TestLoader().loadTestsFromTestCase(test_class)
        test_suite.addTests(tests)
    
    # Run tests
    runner = unittest.TextTestRunner(verbosity=2)
    result = runner.run(test_suite)
    
    # Return success/failure
    return result.wasSuccessful()


def run_specific_test(test_name):
    """Run a specific test module."""
    
    test_modules = {
        'leftover': TestLeftoverRecommendation,
        'nutriment': TestNutrimentRecommendation
    }
    
    if test_name not in test_modules:
        print(f"Unknown test: {test_name}")
        print(f"Available tests: {list(test_modules.keys())}")
        return False
    
    test_class = test_modules[test_name]
    test_suite = unittest.TestLoader().loadTestsFromTestCase(test_class)
    
    runner = unittest.TextTestRunner(verbosity=2)
    result = runner.run(test_suite)
    
    return result.wasSuccessful()


def main():
    """Main entry point for test runner."""
    
    if len(sys.argv) < 2:
        print("Running all tests...")
        success = run_all_tests()
    else:
        test_name = sys.argv[1]
        print(f"Running {test_name} tests...")
        success = run_specific_test(test_name)
    
    if success:
        print("\n✅ All tests passed!")
        sys.exit(0)
    else:
        print("\n❌ Some tests failed!")
        sys.exit(1)


if __name__ == "__main__":
    main()
