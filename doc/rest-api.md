# REST-API Description

## GET /scan
**Arguments:**
- `code` (string, required): The code to be scanned.
**Returns:**
- `200 OK`: JSON object with scan results (Ingredient).
- `400 Bad Request`: If the `code` parameter is missing or invalid.
- `404 Not Found`: If no ingredient is found with the given code.
- `500 Internal Server Error`: If an unexpected error occurs.

## GET /recommendations
**Arguments:**
- `type` (string, required): The type of recommendations to create
- `data` (string, required): The data to base recommendations on
- `number` (integer, optional): The number of recommendations to return (default is 5)
**Returns:**
- `200 OK`: JSON array of recommended items.
- `400 Bad Request`: If required parameters are missing or invalid.
- `500 Internal Server Error`: If an unexpected error occurs.

## GET /recipe
**Arguments:**
- `id` (int, required): The ID of the recipe to retrieve.
**Returns:**
- `200 OK`: JSON object with recipe details (Recipe).
- `400 Bad Request`: If the `id` parameter is missing or invalid.
- `404 Not Found`: If no recipe is found with the given ID.
- `500 Internal Server Error`: If an unexpected error occurs.

## GET /ingredients
**Arguments:**
- `search` (string, optional): A search term to filter ingredients by name.
- `limit` (integer, optional): The maximum number of ingredients to return (default is 10).
**Returns:**
- `200 OK`: JSON array of ingredients.
- `400 Bad Request`: If parameters are invalid.
- `500 Internal Server Error`: If an unexpected error occurs.

## GET /recipe-ingredients
**Arguments:**
- `recipe_id` (int, required): The ID of the recipe to retrieve ingredients for.
**Returns:**
- `200 OK`: JSON array of ingredients for the specified recipe.
- `400 Bad Request`: If the `recipe_id` parameter is missing or invalid.
- `404 Not Found`: If no recipe is found with the given ID.
- `500 Internal Server Error`: If an unexpected error occurs.
