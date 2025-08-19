import pandas as pd

# Load recipes file
recipes_df = pd.read_parquet('recipes.parquet')
print('Original recipes shape:', recipes_df.shape)
print('Original recipes columns:', list(recipes_df.columns))

# Remove specified columns
columns_to_remove = ['AuthorId', 'AuthorName', 'DatePublished']
recipes_cleaned = recipes_df.drop(columns=columns_to_remove)

print('New recipes shape:', recipes_cleaned.shape)
print('New recipes columns:', list(recipes_cleaned.columns))

# Save the cleaned file
recipes_cleaned.to_parquet('recipes.parquet')
print('Recipes file updated successfully')




# Load reviews file
reviews_df = pd.read_parquet('reviews.parquet')
print('Original reviews shape:', reviews_df.shape)
print('Original reviews columns:', list(reviews_df.columns))

# Remove specified columns
columns_to_remove = ['AuthorName', 'Review', 'DateSubmitted', 'DateModified']
reviews_cleaned = reviews_df.drop(columns=columns_to_remove)

print('New reviews shape:', reviews_cleaned.shape)
print('New reviews columns:', list(reviews_cleaned.columns))

# Save the cleaned file
reviews_cleaned.to_parquet('reviews.parquet')
print('Reviews file updated successfully')