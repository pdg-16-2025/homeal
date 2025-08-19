import pandas as pd

print('=== VERIFICATION ===')
print()

# Verify recipes file
recipes_df = pd.read_parquet('recipes.parquet')
print('RECIPES FILE:')
print('Shape:', recipes_df.shape)
print('Columns:', list(recipes_df.columns))
print()

# Verify reviews file
reviews_df = pd.read_parquet('reviews.parquet')
print('REVIEWS FILE:')
print('Shape:', reviews_df.shape)
print('Columns:', list(reviews_df.columns))
print()

# Check if the columns mentioned in TOREMOVE.md are indeed removed
recipe_removed_cols = ['AuthorId', 'AuthorName', 'DatePublished']
review_removed_cols = ['AuthorName', 'Review', 'DateSubmitted', 'DateModified']

print('=== REMOVED COLUMNS VERIFICATION ===')
print('Recipe columns that should be removed:', recipe_removed_cols)
recipe_still_present = [col for col in recipe_removed_cols if col in recipes_df.columns]
print('Recipe columns still present (should be empty):', recipe_still_present)
print()

print('Review columns that should be removed:', review_removed_cols)
review_still_present = [col for col in review_removed_cols if col in reviews_df.columns]
print('Review columns still present (should be empty):', review_still_present)
print()

if not recipe_still_present and not review_still_present:
    print('✅ SUCCESS: All specified columns have been successfully removed!')
else:
    print('❌ WARNING: Some columns were not removed properly')
