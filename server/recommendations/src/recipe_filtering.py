#!/usr/bin/env python3
"""
Recipe filtering system for dietary regimes and ingredient blacklists.
Ensures all APIs return appropriate recipes based on user constraints.
"""

import sqlite3
import json
from typing import List, Dict, Any, Optional, Set
from dataclasses import dataclass


@dataclass
class DietaryFilter:
    """Dietary filtering preferences."""
    regime: Optional[str] = None  # vegan, vegetarian, pescatarian, keto, etc.
    blacklisted_ingredients: List[str] = None  # ingredients to avoid
    allergies: List[str] = None  # allergens to avoid
    max_calories: Optional[float] = None  # calorie limit
    min_rating: Optional[float] = None  # minimum recipe rating
    
    def __post_init__(self):
        if self.blacklisted_ingredients is None:
            self.blacklisted_ingredients = []
        if self.allergies is None:
            self.allergies = []


class RecipeFilter:
    """Main recipe filtering class."""
    
    # Dietary regime mappings
    REGIME_KEYWORDS = {
        'vegan': ['vegan'],
        'vegetarian': ['vegetarian', 'vegan'],  # vegan recipes are also vegetarian
        'pescatarian': ['pescatarian', 'fish', 'seafood'],
        'keto': ['keto', 'ketogenic', 'low carb', 'very low carbs'],
        'gluten_free': ['gluten free', 'gluten-free', 'wheat free', 'wheat-free'],
        'dairy_free': ['dairy free', 'dairy-free'],
        'paleo': ['paleo'],
        'low_fat': ['low fat', 'low-fat'],
        'low_sodium': ['low sodium', 'low-sodium']
    }
    
    # Common allergens and problematic ingredients
    ALLERGEN_INGREDIENTS = {
        'nuts': ['almond', 'walnut', 'pecan', 'cashew', 'pistachio', 'hazelnut', 'peanut'],
        'dairy': ['milk', 'cheese', 'butter', 'cream', 'yogurt', 'sour cream'],
        'eggs': ['egg', 'eggs'],
        'gluten': ['wheat', 'flour', 'bread', 'pasta'],
        'shellfish': ['shrimp', 'crab', 'lobster', 'clam', 'oyster'],
        'soy': ['soy', 'tofu', 'soy sauce'],
        'fish': ['salmon', 'tuna', 'cod', 'fish']
    }
    
    def __init__(self, db_path: str):
        self.db_path = db_path
    
    def get_recipe_ingredients(self, recipe_id: int) -> List[str]:
        """Get all ingredient names for a recipe."""
        conn = sqlite3.connect(self.db_path)
        cursor = conn.cursor()
        
        cursor.execute("""
            SELECT i.name 
            FROM RecipeIngredient ri 
            JOIN Ingredient i ON ri.ingredient_id = i.id 
            WHERE ri.recipe_id = ?
        """, (recipe_id,))
        
        ingredients = [row[0].lower() for row in cursor.fetchall()]
        conn.close()
        return ingredients
    
    def matches_dietary_regime(self, recipe: Dict[str, Any], regime: str) -> bool:
        """Check if recipe matches dietary regime."""
        if not regime or regime.lower() == 'none':
            return True
        
        regime = regime.lower()
        if regime not in self.REGIME_KEYWORDS:
            return True  # Unknown regime, don't filter
        
        keywords = recipe.get('keywords', '') or ''
        keywords_lower = keywords.lower()
        
        # Check if recipe contains required keywords for the regime
        required_keywords = self.REGIME_KEYWORDS[regime]
        
        # Special handling for different regimes
        if regime == 'vegan':
            # Must contain vegan keyword OR be explicitly marked as vegan
            return any(keyword in keywords_lower for keyword in required_keywords)
        
        elif regime == 'vegetarian':
            # Can be vegan OR vegetarian, but exclude obvious meat
            if any(keyword in keywords_lower for keyword in required_keywords):
                return True
            # Also exclude recipes with obvious meat ingredients
            meat_keywords = ['chicken', 'beef', 'pork', 'lamb', 'turkey', 'meat', 'bacon', 'sausage']
            return not any(meat in keywords_lower for meat in meat_keywords)
        
        elif regime == 'keto':
            # Look for keto or low carb keywords
            return any(keyword in keywords_lower for keyword in required_keywords)
        
        elif regime == 'gluten_free':
            # For gluten-free, be more inclusive - check for gluten-free keywords OR absence of gluten ingredients
            if any(keyword in keywords_lower for keyword in required_keywords):
                return True
            # Also check if recipe doesn't contain obvious gluten ingredients
            gluten_keywords = ['flour', 'wheat', 'bread', 'pasta', 'noodle', 'biscuit', 'cake', 'cookie']
            return not any(gluten in keywords_lower for gluten in gluten_keywords)
        
        else:
            # For other regimes, just check for presence of keywords
            return any(keyword in keywords_lower for keyword in required_keywords)
    
    def has_blacklisted_ingredients(self, recipe_id: int, blacklisted: List[str], allergies: List[str] = None) -> bool:
        """Check if recipe contains blacklisted ingredients or allergens."""
        if not blacklisted and not allergies:
            return False
        
        recipe_ingredients = self.get_recipe_ingredients(recipe_id)
        
        # Check direct blacklisted ingredients
        for blacklisted_item in (blacklisted or []):
            if any(blacklisted_item.lower() in ingredient for ingredient in recipe_ingredients):
                return True
        
        # Check allergens
        for allergen in (allergies or []):
            allergen_lower = allergen.lower()
            if allergen_lower in self.ALLERGEN_INGREDIENTS:
                allergen_ingredients = self.ALLERGEN_INGREDIENTS[allergen_lower]
                for allergen_ingredient in allergen_ingredients:
                    if any(allergen_ingredient in ingredient for ingredient in recipe_ingredients):
                        return True
        
        return False
    
    def filter_recipes(self, recipes: List[Dict[str, Any]], dietary_filter: DietaryFilter) -> List[Dict[str, Any]]:
        """
        Filter recipes based on dietary constraints.
        
        Args:
            recipes: List of recipe dictionaries
            dietary_filter: Dietary filtering preferences
            
        Returns:
            Filtered list of recipes
        """
        filtered_recipes = []
        
        for recipe in recipes:
            # Check dietary regime
            if not self.matches_dietary_regime(recipe, dietary_filter.regime):
                continue
            
            # Check blacklisted ingredients and allergens
            if self.has_blacklisted_ingredients(
                recipe['id'], 
                dietary_filter.blacklisted_ingredients, 
                dietary_filter.allergies
            ):
                continue
            
            # Check calorie limit
            if dietary_filter.max_calories and recipe.get('calories'):
                if recipe['calories'] > dietary_filter.max_calories:
                    continue
            
            # Check minimum rating
            if dietary_filter.min_rating and recipe.get('aggregated_rating'):
                if recipe['aggregated_rating'] < dietary_filter.min_rating:
                    continue
            
            filtered_recipes.append(recipe)
        
        return filtered_recipes
    
    def get_filtered_recipes(self, dietary_filter: DietaryFilter, limit: int = 50) -> List[Dict[str, Any]]:
        """
        Get recipes from database with dietary filtering applied.
        
        Args:
            dietary_filter: Dietary filtering preferences
            limit: Maximum number of recipes to return
            
        Returns:
            List of filtered recipes
        """
        conn = sqlite3.connect(self.db_path)
        cursor = conn.cursor()
        
        # Build base query
        base_query = """
            SELECT id, name, total_time, images, keywords, calories, aggregated_rating, review_count
            FROM Recipe 
            WHERE 1=1
        """
        params = []
        
        # Add calorie filter to SQL query for efficiency
        if dietary_filter.max_calories:
            base_query += " AND (calories IS NULL OR calories <= ?)"
            params.append(dietary_filter.max_calories)
        
        # Add rating filter to SQL query
        if dietary_filter.min_rating:
            base_query += " AND (aggregated_rating IS NULL OR aggregated_rating >= ?)"
            params.append(dietary_filter.min_rating)
        
        # Add dietary regime filter to SQL query for efficiency
        if dietary_filter.regime and dietary_filter.regime.lower() != 'none':
            regime_keywords = self.REGIME_KEYWORDS.get(dietary_filter.regime.lower(), [])
            if regime_keywords:
                keyword_conditions = " OR ".join(["LOWER(keywords) LIKE ?" for _ in regime_keywords])
                base_query += f" AND ({keyword_conditions})"
                params.extend([f"%{keyword}%" for keyword in regime_keywords])
        
        base_query += f" ORDER BY RANDOM() LIMIT ?"
        params.append(limit * 3)  # Get more recipes to filter for blacklisted ingredients
        
        cursor.execute(base_query, params)
        
        recipes = []
        for row in cursor.fetchall():
            recipe = {
                'id': row[0],
                'name': row[1],
                'total_time': row[2] or 0,
                'image_url': row[3] or '',
                'keywords': row[4] or '',
                'calories': row[5],
                'aggregated_rating': row[6],
                'review_count': row[7] or 0
            }
            recipes.append(recipe)
        
        conn.close()
        
        # Apply ingredient blacklist filtering
        if dietary_filter.blacklisted_ingredients or dietary_filter.allergies:
            filtered_recipes = []
            for recipe in recipes:
                if not self.has_blacklisted_ingredients(
                    recipe['id'], 
                    dietary_filter.blacklisted_ingredients, 
                    dietary_filter.allergies
                ):
                    filtered_recipes.append(recipe)
                    if len(filtered_recipes) >= limit:
                        break
            return filtered_recipes
        
        return recipes[:limit]
    
    def ensure_minimum_recipes(self, recipes: List[Dict[str, Any]], 
                             dietary_filter: DietaryFilter, 
                             minimum: int = 3) -> List[Dict[str, Any]]:
        """
        Ensure we have at least minimum number of recipes.
        If not enough, gradually relax constraints.
        
        Args:
            recipes: Current filtered recipes
            dietary_filter: Current dietary filter
            minimum: Minimum number of recipes required
            
        Returns:
            List with at least minimum recipes (if available in database)
        """
        if len(recipes) >= minimum:
            return recipes
        
        # Strategy 1: Remove calorie limit
        if dietary_filter.max_calories:
            relaxed_filter = DietaryFilter(
                regime=dietary_filter.regime,
                blacklisted_ingredients=dietary_filter.blacklisted_ingredients,
                allergies=dietary_filter.allergies,
                max_calories=None,  # Remove calorie limit
                min_rating=dietary_filter.min_rating
            )
            additional_recipes = self.get_filtered_recipes(relaxed_filter, minimum * 2)
            combined = recipes + [r for r in additional_recipes if r['id'] not in [existing['id'] for existing in recipes]]
            if len(combined) >= minimum:
                return combined[:minimum * 2]
        
        # Strategy 2: Remove rating requirement
        if dietary_filter.min_rating:
            relaxed_filter = DietaryFilter(
                regime=dietary_filter.regime,
                blacklisted_ingredients=dietary_filter.blacklisted_ingredients,
                allergies=dietary_filter.allergies,
                max_calories=None,
                min_rating=None  # Remove rating requirement
            )
            additional_recipes = self.get_filtered_recipes(relaxed_filter, minimum * 2)
            combined = recipes + [r for r in additional_recipes if r['id'] not in [existing['id'] for existing in recipes]]
            if len(combined) >= minimum:
                return combined[:minimum * 2]
        
        # Strategy 3: Keep essential constraints but get more recipes
        essential_filter = DietaryFilter(
            regime=dietary_filter.regime,  # Keep dietary regime
            blacklisted_ingredients=dietary_filter.blacklisted_ingredients,  # Keep safety constraints
            allergies=dietary_filter.allergies,  # Keep safety constraints
            max_calories=None,
            min_rating=None
        )
        additional_recipes = self.get_filtered_recipes(essential_filter, minimum * 3)
        combined = recipes + [r for r in additional_recipes if r['id'] not in [existing['id'] for existing in recipes]]
        
        return combined[:max(minimum, len(combined))]


def parse_dietary_filter_from_data(data: Dict[str, Any]) -> DietaryFilter:
    """
    Parse dietary filter from API request data.
    
    Args:
        data: Request data dictionary
        
    Returns:
        DietaryFilter object
    """
    return DietaryFilter(
        regime=data.get('dietary_regime'),
        blacklisted_ingredients=data.get('blacklisted_ingredients', []),
        allergies=data.get('allergies', []),
        max_calories=data.get('max_calories'),
        min_rating=data.get('min_rating')
    )


if __name__ == "__main__":
    # Test the filtering system
    filter_system = RecipeFilter("../../homeal.db")
    
    # Test vegan filtering
    dietary_filter = DietaryFilter(regime="vegan")
    vegan_recipes = filter_system.get_filtered_recipes(dietary_filter, 5)
    print(f"Found {len(vegan_recipes)} vegan recipes:")
    for recipe in vegan_recipes:
        print(f"  - {recipe['name']} (ID: {recipe['id']})")
    
    # Test with blacklisted ingredients
    dietary_filter = DietaryFilter(
        blacklisted_ingredients=["chicken", "beef"],
        allergies=["dairy"]
    )
    filtered_recipes = filter_system.get_filtered_recipes(dietary_filter, 5)
    print(f"\nFound {len(filtered_recipes)} recipes without chicken, beef, or dairy:")
    for recipe in filtered_recipes:
        print(f"  - {recipe['name']} (ID: {recipe['id']})")
