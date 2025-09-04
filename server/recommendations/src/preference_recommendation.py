#!/usr/bin/env python3
"""
Enhanced preference-based recommendation system using real review data.
Uses review_light.parquet to find similar users and recommend 5-star recipes.
"""

import json
import sqlite3
import pandas as pd
import numpy as np
from typing import List, Dict, Any, Optional
from dataclasses import dataclass
from datetime import datetime
import os


@dataclass
class UserRating:
    user_id: int
    recipe_id: int
    rating: float


@dataclass
class SimilarUser:
    user_id: int
    similarity_score: float


def load_review_data(parquet_path: str) -> pd.DataFrame:
    """Load review data from parquet file."""
    if not os.path.exists(parquet_path):
        # Try alternative paths
        alternative_paths = [
            "../../../homeal-db/review_light.parquet",
            "../../homeal-db/review_light.parquet",
            "./review_light.parquet"
        ]
        for alt_path in alternative_paths:
            if os.path.exists(alt_path):
                parquet_path = alt_path
                break
        else:
            raise FileNotFoundError(f"Could not find review_light.parquet in expected locations")
    
    return pd.read_parquet(parquet_path)


def create_intelligent_mock_ratings(review_df: pd.DataFrame, num_users: int = 5) -> List[Dict[str, Any]]:
    """
    Create intelligent mock user rating data based on real review patterns.
    
    Args:
        review_df: DataFrame with real review data
        num_users: Number of mock users to create
        
    Returns:
        List of mock user data with realistic rating patterns
    """
    mock_users = []
    
    # Get recipes with the most reviews (popular recipes)
    popular_recipes = review_df['RecipeId'].value_counts().head(20).index.tolist()
    
    # Get some real author patterns for inspiration
    real_authors = review_df['AuthorId'].unique()[:100]
    
    for i in range(num_users):
        user_id = 90000 + i  # Mock user IDs starting from 90000
        
        # Each mock user rates 3-8 recipes
        num_ratings = np.random.randint(3, 9)
        
        # Mix of popular and random recipes
        if len(popular_recipes) >= num_ratings:
            rated_recipes = np.random.choice(popular_recipes, size=num_ratings, replace=False)
        else:
            # If not enough popular recipes, add some random ones
            all_recipes = review_df['RecipeId'].unique()
            rated_recipes = np.random.choice(all_recipes, size=num_ratings, replace=False)
        
        ratings = []
        for recipe_id in rated_recipes:
            # Create realistic rating distribution (slightly skewed toward higher ratings)
            rating = np.random.choice([3.0, 3.5, 4.0, 4.5, 5.0], 
                                    p=[0.1, 0.15, 0.25, 0.3, 0.2])
            ratings.append({"recipe_id": int(recipe_id), "rating": rating})
        
        mock_users.append({
            "user_id": user_id,
            "ratings": ratings
        })
    
    return mock_users


def calculate_user_similarity_enhanced(user1_ratings: Dict[int, float], 
                                     user2_ratings: Dict[int, float]) -> float:
    """
    Calculate similarity between two users based on their recipe ratings.
    Uses Jaccard similarity for recipe overlap since all reviews are 5-star.
    """
    # Find common recipes
    common_recipes = set(user1_ratings.keys()) & set(user2_ratings.keys())
    
    if len(common_recipes) == 0:
        return 0.0
    
    # Calculate Jaccard similarity (since all reviews are 5-star, focus on recipe overlap)
    all_recipes = set(user1_ratings.keys()) | set(user2_ratings.keys())
    jaccard_similarity = len(common_recipes) / len(all_recipes)
    
    # Weight by number of common recipes (more overlap = higher confidence)
    confidence = min(len(common_recipes) / 3.0, 1.0)  # Max confidence at 3+ common recipes
    
    # Also consider rating similarity for user's rated recipes vs 5-star baseline
    rating_similarity = 0.0
    if common_recipes:
        user1_avg = np.mean([user1_ratings[recipe] for recipe in common_recipes])
        user2_avg = np.mean([user2_ratings[recipe] for recipe in common_recipes])
        # Both users like the same recipes, check how much they like them
        rating_diff = abs(user1_avg - user2_avg)
        rating_similarity = max(0, 1 - rating_diff / 5.0)  # Scale by max possible difference
    
    # Combine Jaccard similarity with rating similarity
    combined_similarity = (jaccard_similarity * 0.7) + (rating_similarity * 0.3)
    
    return combined_similarity * confidence


def find_similar_users_enhanced(target_ratings: Dict[int, float], 
                               review_df: pd.DataFrame, 
                               min_common_recipes: int = 2, 
                               top_k: int = 10) -> List[SimilarUser]:
    """
    Find users with similar preferences using the review dataset.
    
    Args:
        target_ratings: Target user's recipe ratings {recipe_id: rating}
        review_df: DataFrame with review data
        min_common_recipes: Minimum number of common recipes required
        top_k: Number of similar users to return
        
    Returns:
        List of similar users sorted by similarity score
    """
    target_recipes = set(target_ratings.keys())
    similar_users = []
    
    # Group reviews by author to get each user's rating profile
    user_profiles = {}
    for author_id, group in review_df.groupby('AuthorId'):
        user_profiles[author_id] = dict(zip(group['RecipeId'], group['Rating']))
    
    for author_id, author_ratings in user_profiles.items():
        author_recipes = set(author_ratings.keys())
        common_recipes = target_recipes & author_recipes
        
        if len(common_recipes) >= min_common_recipes:
            similarity = calculate_user_similarity_enhanced(target_ratings, author_ratings)
            if similarity > 0.05:  # Lower threshold for testing
                similar_users.append(SimilarUser(user_id=author_id, similarity_score=similarity))
    
    # Sort by similarity score (descending) and return top k
    similar_users.sort(key=lambda x: x.similarity_score, reverse=True)
    return similar_users[:top_k]


def get_recommendation_candidates_enhanced(similar_users: List[SimilarUser], 
                                         review_df: pd.DataFrame,
                                         target_user_ratings: Dict[int, float],
                                         min_rating: float = 4.5) -> Dict[int, float]:
    """
    Get recipe recommendations from similar users' 5-star reviews.
    
    Args:
        similar_users: List of similar users
        review_df: DataFrame with review data
        target_user_ratings: Recipes already rated by target user
        min_rating: Minimum rating to consider for recommendations
        
    Returns:
        Dictionary of {recipe_id: weighted_score}
    """
    candidates = {}
    target_recipes = set(target_user_ratings.keys())
    
    for similar_user in similar_users:
        # Get this user's high-rated recipes
        user_reviews = review_df[
            (review_df['AuthorId'] == similar_user.user_id) & 
            (review_df['Rating'] >= min_rating)
        ]
        
        for _, review in user_reviews.iterrows():
            recipe_id = review['RecipeId']
            
            # Skip recipes already rated by target user
            if recipe_id in target_recipes:
                continue
            
            # Weight the recipe by user similarity and rating
            weighted_score = similar_user.similarity_score * review['Rating']
            
            if recipe_id in candidates:
                candidates[recipe_id] += weighted_score
            else:
                candidates[recipe_id] = weighted_score
    
    return candidates


def get_enhanced_preference_recommendations(db_path: str, 
                                          user_data: str, 
                                          number: int = 5) -> List[Dict[str, Any]]:
    """
    Get recipe recommendations based on user preferences using real review data.
    
    Args:
        db_path: Path to SQLite database
        user_data: JSON string with user ratings
        number: Number of recommendations to return
        
    Returns:
        List of recommended recipes with preference scores
    """
    try:
        user_json = json.loads(user_data)
        user_id = user_json.get('user_id')
        ratings_data = user_json.get('ratings', [])
        
        if user_id is None:
            raise ValueError("Missing user_id in request data")
        
        if not ratings_data:
            return []  # Can't recommend without any ratings
        
        # Convert ratings to dictionary
        target_ratings = {rating['recipe_id']: rating['rating'] for rating in ratings_data}
        
        # Load review data
        try:
            review_df = load_review_data("review_light.parquet")
        except FileNotFoundError as e:
            # Fallback to original preference recommendation if parquet not found
            from preference_recommendation import get_preference_recommendations
            return get_preference_recommendations(db_path, user_data, number)
        
        # Find similar users
        similar_users = find_similar_users_enhanced(
            target_ratings, 
            review_df, 
            min_common_recipes=1,  # Allow users with even 1 common recipe
            top_k=20
        )
        
        if not similar_users:
            return []  # No similar users found
        
        # Get recommendation candidates
        candidates = get_recommendation_candidates_enhanced(
            similar_users, 
            review_df, 
            target_ratings,
            min_rating=5.0  # Only recommend 5-star recipes
        )
        
        if not candidates:
            return []  # No suitable candidates
        
        # Get recipe details from database and filter to existing recipes
        conn = sqlite3.connect(db_path)
        cursor = conn.cursor()
        
        recommendations = []
        
        # Sort candidates by weighted score and query database
        sorted_candidates = sorted(candidates.items(), key=lambda x: x[1], reverse=True)
        
        for recipe_id, preference_score in sorted_candidates:
            if len(recommendations) >= number:
                break  # Got enough recommendations
                
            cursor.execute("""
                SELECT id, name, total_time, images, aggregated_rating, review_count
                FROM Recipe 
                WHERE id = ?
            """, (recipe_id,))
            
            recipe_data = cursor.fetchone()
            if recipe_data:  # Recipe exists in database
                recipe_id, name, total_time, images, avg_rating, review_count = recipe_data
                
                recommendations.append({
                    "id": recipe_id,
                    "name": name,
                    "total_time": total_time or 0,
                    "image_url": images or "",
                    "preference_score": round(preference_score, 2),
                    "avg_rating": avg_rating or 0.0,
                    "review_count": review_count or 0,
                    "similar_users_count": len(similar_users)
                })
        
        conn.close()
        return recommendations
        
    except (json.JSONDecodeError, KeyError, TypeError) as e:
        raise ValueError(f"Invalid user data format: {e}")
    except Exception as e:
        raise Exception(f"Database or processing error: {e}")


def get_preference_recommendations(db_path: str, 
                                  user_data: str, 
                                  number: int = 5) -> List[Dict[str, Any]]:
    """
    Get preference-based recommendations using the unified database.
    
    Args:
        db_path: Path to SQLite database
        user_data: JSON string with user ratings
        number: Number of recommendations to return
        
    Returns:
        List of recommended recipes with preference scores
    """
    try:
        data = json.loads(user_data)
        user_id = data.get("user_id")
        if user_id is None:
            raise ValueError("Missing user_id in request data")
        
        user_ratings = {r["recipe_id"]: r["rating"] for r in data.get("ratings", [])}
        if not user_ratings:
            return []  # No ratings provided
        
        # Connect to database
        conn = sqlite3.connect(db_path)
        cursor = conn.cursor()
        
        # Get all user ratings from database
        cursor.execute("""
            SELECT author_id, recipe_id, rating 
            FROM Review 
            WHERE author_id != ? OR author_id IS NULL
        """, (user_id,))
        
        all_ratings = cursor.fetchall()
        
        # Organize ratings by user
        user_rating_dict = {}
        for author_id, recipe_id, rating in all_ratings:
            if author_id not in user_rating_dict:
                user_rating_dict[author_id] = {}
            user_rating_dict[author_id][recipe_id] = rating
        
        # Find similar users using cosine similarity
        similar_users = []
        for other_user_id, other_ratings in user_rating_dict.items():
            # Find common recipes
            common_recipes = set(user_ratings.keys()) & set(other_ratings.keys())
            if len(common_recipes) < 2:  # Need at least 2 common recipes
                continue
            
            # Calculate similarity score
            similarity = calculate_user_similarity_enhanced(user_ratings, other_ratings)
            if similarity > 0.1:  # Only consider users with reasonable similarity
                similar_users.append(SimilarUser(user_id=other_user_id, similarity_score=similarity))
        
        # Sort by similarity and take top users
        similar_users.sort(key=lambda x: x.similarity_score, reverse=True)
        similar_users = similar_users[:10]  # Top 10 similar users
        
        if not similar_users:
            return []  # No similar users found
        
        # Get recommendation candidates
        recipe_scores = {}
        total_similarity = sum(user.similarity_score for user in similar_users)
        
        for similar_user in similar_users:
            user_ratings_other = user_rating_dict[similar_user.user_id]
            weight = similar_user.similarity_score / total_similarity
            
            for recipe_id, rating in user_ratings_other.items():
                if recipe_id not in user_ratings and rating >= 3.0:  # Only well-rated recipes
                    if recipe_id not in recipe_scores:
                        recipe_scores[recipe_id] = 0
                    recipe_scores[recipe_id] += rating * weight
        
        if not recipe_scores:
            return []
        
        # Get recipe details for top candidates
        top_recipes = sorted(recipe_scores.items(), key=lambda x: x[1], reverse=True)[:number * 2]
        recipe_ids = [recipe_id for recipe_id, _ in top_recipes]
        
        placeholders = ",".join("?" for _ in recipe_ids)
        query = f"""
            SELECT id, name, total_time, images, aggregated_rating, review_count
            FROM Recipe 
            WHERE id IN ({placeholders})
            ORDER BY aggregated_rating DESC
            LIMIT ?
        """
        
        cursor.execute(query, recipe_ids + [number])
        results = cursor.fetchall()
        conn.close()
        
        recommendations = []
        for row in results:
            recipe_id, name, total_time, images, avg_rating, review_count = row
            preference_score = recipe_scores.get(recipe_id, 0)
            
            recommendations.append({
                "id": recipe_id,
                "name": name,
                "total_time": total_time,
                "image_url": images,
                "preference_score": round(preference_score, 2),
                "avg_rating": avg_rating if avg_rating is not None else 0.0,
                "review_count": review_count if review_count is not None else 0
            })
        
        # Sort by preference score (descending)
        recommendations.sort(key=lambda x: x["preference_score"], reverse=True)
        
        return recommendations[:number]
        
    except ValueError as e:
        raise e
    except Exception as e:
        raise Exception(f"Database or processing error: {e}")


def get_intelligent_mock_users(db_path: str) -> List[Dict[str, Any]]:
    """
    Generate intelligent mock user data based on real review patterns.
    
    Returns:
        List of mock user profiles with realistic rating patterns
    """
    try:
        review_df = load_review_data("review_light.parquet")
        return create_intelligent_mock_ratings(review_df, num_users=5)
    except Exception:
        # Fallback to simple mock data if parquet not available
        return [
            {
                "user_id": 90001,
                "ratings": [
                    {"recipe_id": 117, "rating": 5.0},
                    {"recipe_id": 374, "rating": 4.5},
                    {"recipe_id": 479, "rating": 4.0}
                ]
            },
            {
                "user_id": 90002,
                "ratings": [
                    {"recipe_id": 117, "rating": 4.5},
                    {"recipe_id": 139, "rating": 5.0},
                    {"recipe_id": 374, "rating": 3.5}
                ]
            }
        ]


if __name__ == "__main__":
    # Test the enhanced preference system
    import sys
    
    if len(sys.argv) >= 2:
        user_data = sys.argv[1]
        number = int(sys.argv[2]) if len(sys.argv) > 2 else 5
        db_path = "../../homeal.db"
        
        try:
            recommendations = get_enhanced_preference_recommendations(db_path, user_data, number)
            print(json.dumps(recommendations, indent=2))
        except Exception as e:
            print(json.dumps({"error": str(e)}, indent=2))
    else:
        # Show example mock users
        mock_users = get_intelligent_mock_users("../../homeal.db")
        print("Intelligent mock users:")
        print(json.dumps(mock_users, indent=2))
