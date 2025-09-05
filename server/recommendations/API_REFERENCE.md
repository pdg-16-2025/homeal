# Homeal Recommendation API Reference

This document provides a complete reference for the recommendation API endpoints, including request formats, response structures, and validation requirements.

## Base URL

```
http://localhost:3000/recommendations
```

## Endpoint Overview

The recommendation system provides three main types of recommendations:

1. **Ingredients-based** - Uses leftover ingredients to suggest recipes
2. **Nutriment-based** - Suggests recipes based on nutritional needs
3. **Preference-based** - Recommends recipes based on user ratings and similar users

## Common Parameters

All endpoints accept these common query parameters:

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `type` | string | Yes | Recommendation type: `ingredients`, `nutriments`, `preferences`, or `random` |
| `data` | JSON string | Yes | URL-encoded JSON data specific to the recommendation type |
| `number` | integer | No | Number of recommendations to return (default: 5, max: 20) |

## 1. Ingredients-Based Recommendations

**Type:** `ingredients`

**Purpose:** Suggests recipes that use available leftover ingredients, minimizing waste.

### Request Format

```bash
GET /recommendations?type=ingredients&data={JSON_DATA}&number=5
```

### Data Schema

```json
{
  "ingredients": [
    {
      "name": "string",
      "quantity": "number",
      "unit": "string",
      "expiration_date": "string (ISO date)"
    }
  ]
}
```

### Field Requirements

| Field | Type | Required | Description | Example |
|-------|------|----------|-------------|---------|
| `ingredients` | array | Yes | List of available ingredients | - |
| `name` | string | Yes | Ingredient name (case-insensitive) | "tomato", "cheese" |
| `quantity` | number | Yes | Available amount | 2, 200 |
| `unit` | string | Yes | Unit of measurement | "pieces", "grams", "cups" |
| `expiration_date` | string | Yes | ISO 8601 date format | "2024-01-15" |

### Valid Units

- **Weight:** `grams`, `g`, `kilograms`, `kg`, `pounds`, `lbs`, `ounces`, `oz`
- **Volume:** `cups`, `milliliters`, `ml`, `liters`, `l`, `tablespoons`, `tbsp`, `teaspoons`, `tsp`
- **Count:** `pieces`, `slices`, `cloves`, `bunches`, `heads`
- **Custom:** Any string is accepted, but standardized units provide better matching

### Example Request

```bash
curl "http://localhost:3000/recommendations?type=ingredients&data=%7B%22ingredients%22%3A%5B%7B%22name%22%3A%22tomato%22%2C%22quantity%22%3A2%2C%22unit%22%3A%22pieces%22%2C%22expiration_date%22%3A%222024-01-15%22%7D%2C%7B%22name%22%3A%22cheese%22%2C%22quantity%22%3A200%2C%22unit%22%3A%22grams%22%2C%22expiration_date%22%3A%222024-01-20%22%7D%5D%7D&number=3"
```

**Decoded data:**
```json
{
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
      "unit": "grams",
      "expiration_date": "2024-01-20"
    }
  ]
}
```

### Response Format

```json
{
  "type": "ingredients",
  "recommendations": [
    {
      "id": 123,
      "name": "Margherita Pizza",
      "total_time": 45,
      "image_url": "https://example.com/pizza.jpg",
      "calories": 650.0,
      "aggregated_rating": 4.5,
      "match_score": 0.85
    }
  ],
  "message": "Recipes optimized for your leftover ingredients",
  "filters_applied": {
    "dietary_regime": null,
    "blacklisted_ingredients": [],
    "allergies": [],
    "max_calories": null
  }
}
```

### Score Field

- **`match_score`**: Percentage (0.0-1.0) of recipe ingredients that match available ingredients
- Higher scores indicate better use of available ingredients

---

## 2. Nutriment-Based Recommendations

**Type:** `nutriments`

**Purpose:** Suggests recipes based on nutritional needs calculated from user profile.

### Request Format

```bash
GET /recommendations?type=nutriments&data={JSON_DATA}&number=5
```

### Data Schema

```json
{
  "age": "integer",
  "gender": "string",
  "weight": "number",
  "height": "number",
  "activity_level": "string",
  "meal_type": "string (optional)"
}
```

### Field Requirements

| Field | Type | Required | Description | Valid Values |
|-------|------|----------|-------------|--------------|
| `age` | integer | Yes | Age in years | 13-120 |
| `gender` | string | Yes | Biological gender for BMR calculation | `"male"`, `"female"`, `"other"` |
| `weight` | number | Yes | Weight in kilograms | 30.0-300.0 |
| `height` | number | Yes | Height in centimeters | 100.0-250.0 |
| `activity_level` | string | Yes | Daily activity level | See Activity Levels below |
| `meal_type` | string | No | Type of meal (affects portion size) | See Meal Types below |

### Valid Activity Levels

| Value | Description | Multiplier |
|-------|-------------|------------|
| `"sedentary"` | Little or no exercise | 1.2 |
| `"lightly_active"` | Light exercise 1-3 days/week | 1.375 |
| `"moderately_active"` | Moderate exercise 3-5 days/week | 1.55 |
| `"very_active"` | Hard exercise 6-7 days/week | 1.725 |
| `"extra_active"` | Very hard exercise, physical job | 1.85 |
| `"extremely_active"` | Extra hard exercise, training twice a day | 1.9 |

### Valid Meal Types

| Value | Description | Portion Size |
|-------|-------------|--------------|
| `"breakfast"` | Morning meal | 25% of daily calories |
| `"lunch"` | Midday meal | 35% of daily calories |
| `"dinner"` | Evening meal | 35% of daily calories |
| `"snack"` | Between meals | 5% of daily calories |

### Example Request

```bash
curl "http://localhost:3000/recommendations?type=nutriments&data=%7B%22age%22%3A25%2C%22gender%22%3A%22male%22%2C%22weight%22%3A70%2C%22height%22%3A175%2C%22activity_level%22%3A%22moderately_active%22%2C%22meal_type%22%3A%22lunch%22%7D&number=5"
```

**Decoded data:**
```json
{
  "age": 25,
  "gender": "male",
  "weight": 70,
  "height": 175,
  "activity_level": "moderately_active",
  "meal_type": "lunch"
}
```

### Response Format

```json
{
  "type": "nutriments",
  "recommendations": [
    {
      "id": 456,
      "name": "Grilled Chicken Salad",
      "total_time": 25,
      "image_url": "https://example.com/salad.jpg",
      "calories": 420.0,
      "aggregated_rating": 4.2,
      "nutrient_score": 0.92
    }
  ],
  "message": "Recipes tailored to your nutritional needs",
  "filters_applied": {
    "dietary_regime": null,
    "blacklisted_ingredients": [],
    "allergies": [],
    "max_calories": null
  }
}
```

### Score Field

- **`nutrient_score`**: Percentage (0.0-1.0) indicating how well the recipe meets nutritional targets
- Based on calorie content, macronutrient balance, and meal timing

---

## 3. Preference-Based Recommendations

**Type:** `preferences`

**Purpose:** Suggests recipes based on user ratings and similar user preferences.

### Request Format

```bash
GET /recommendations?type=preferences&data={JSON_DATA}&number=5
```

### Data Schema

```json
{
  "user_id": "integer",
  "ratings": [
    {
      "recipe_id": "integer",
      "rating": "number"
    }
  ]
}
```

### Field Requirements

| Field | Type | Required | Description | Valid Values |
|-------|------|----------|-------------|--------------|
| `user_id` | integer | Yes | Unique user identifier | 1-999999 |
| `ratings` | array | Yes | List of user's recipe ratings | - |
| `recipe_id` | integer | Yes | ID of rated recipe | Must exist in database |
| `rating` | number | Yes | User's rating of the recipe | 1.0-5.0 (0.5 increments) |

### Rating Scale

| Rating | Description |
|--------|-------------|
| 1.0 | Disliked very much |
| 2.0 | Disliked |
| 3.0 | Neutral |
| 4.0 | Liked |
| 5.0 | Liked very much |

**Note:** Ratings can use 0.5 increments (e.g., 3.5, 4.5)

### Example Request

```bash
curl "http://localhost:3000/recommendations?type=preferences&data=%7B%22user_id%22%3A999%2C%22ratings%22%3A%5B%7B%22recipe_id%22%3A24768%2C%22rating%22%3A5.0%7D%2C%7B%22recipe_id%22%3A12345%2C%22rating%22%3A4.5%7D%5D%7D&number=5"
```

**Decoded data:**
```json
{
  "user_id": 999,
  "ratings": [
    {
      "recipe_id": 24768,
      "rating": 5.0
    },
    {
      "recipe_id": 12345,
      "rating": 4.5
    }
  ]
}
```

### Response Format

```json
{
  "type": "preferences",
  "recommendations": [
    {
      "id": 789,
      "name": "Pasta Carbonara",
      "total_time": 30,
      "image_url": "https://example.com/carbonara.jpg",
      "calories": 580.0,
      "aggregated_rating": 4.7,
      "preference_score": 4.8
    }
  ],
  "message": "Recipes recommended based on similar users' preferences",
  "filters_applied": {
    "dietary_regime": null,
    "blacklisted_ingredients": [],
    "allergies": [],
    "max_calories": null
  }
}
```

### Score Field

- **`preference_score`**: Rating (1.0-5.0) indicating predicted user preference
- Based on collaborative filtering with similar users

---

## 4. Random Recommendations

**Type:** `random`

**Purpose:** Returns random recipes with optional filtering.

### Request Format

```bash
GET /recommendations?type=random&data={JSON_DATA}&number=5
```

### Data Schema

```json
{
  "dietary_regime": "string (optional)",
  "blacklisted_ingredients": ["array of strings (optional)"],
  "allergies": ["array of strings (optional)"],
  "max_calories": "number (optional)"
}
```

### Example Request

```bash
curl "http://localhost:3000/recommendations?type=random&data=%7B%22dietary_regime%22%3A%22vegetarian%22%2C%22max_calories%22%3A500%7D&number=3"
```

---

## Dietary Filtering

All recommendation types support optional dietary filtering through the `data` parameter:

### Filter Options

| Filter | Type | Description | Example Values |
|--------|------|-------------|----------------|
| `dietary_regime` | string | Dietary restrictions | `"vegan"`, `"vegetarian"`, `"keto"`, `"gluten_free"` |
| `blacklisted_ingredients` | array | Ingredients to avoid | `["onions", "mushrooms"]` |
| `allergies` | array | Allergens to avoid | `["nuts", "dairy", "eggs"]` |
| `max_calories` | number | Maximum calories per recipe | `500` |

### Supported Dietary Regimes

| Regime | Description | Keywords |
|--------|-------------|----------|
| `vegan` | No animal products | "vegan" |
| `vegetarian` | No meat, may include dairy/eggs | "vegetarian", "vegan" |
| `pescatarian` | Fish and seafood allowed | "pescatarian", "fish", "seafood" |
| `keto` | Low-carb, high-fat | "keto", "ketogenic", "low carb" |
| `gluten_free` | No gluten-containing grains | "gluten free", "gluten-free" |
| `dairy_free` | No dairy products | "dairy free", "dairy-free" |
| `paleo` | Paleolithic diet | "paleo" |
| `low_fat` | Reduced fat content | "low fat", "low-fat" |
| `low_sodium` | Reduced sodium content | "low sodium", "low-sodium" |

### Common Allergens

| Allergen | Keywords |
|----------|----------|
| `nuts` | almond, walnut, pecan, cashew, pistachio, hazelnut, peanut |
| `dairy` | milk, cheese, butter, cream, yogurt, sour cream |
| `eggs` | egg, eggs |
| `gluten` | wheat, flour, bread, pasta |
| `shellfish` | shrimp, crab, lobster, clam, oyster |
| `soy` | soy, tofu, soy sauce |
| `fish` | salmon, tuna, cod, fish |

### Example with Filtering

```json
{
  "ingredients": [
    {
      "name": "tomato",
      "quantity": 2,
      "unit": "pieces",
      "expiration_date": "2024-01-15"
    }
  ],
  "dietary_regime": "vegan",
  "allergies": ["nuts"],
  "max_calories": 400
}
```

---

## Response Format

All endpoints return the same JSON structure:

### Success Response

```json
{
  "type": "string",
  "recommendations": [
    {
      "id": "integer",
      "name": "string",
      "total_time": "integer",
      "image_url": "string",
      "calories": "number",
      "aggregated_rating": "number",
      "score_field": "number"
    }
  ],
  "message": "string",
  "filters_applied": {
    "dietary_regime": "string|null",
    "blacklisted_ingredients": ["array"],
    "allergies": ["array"],
    "max_calories": "number|null"
  }
}
```

### Error Response

```json
{
  "error": "string",
  "type": "string"
}
```

### Response Fields

| Field | Type | Description |
|-------|------|-------------|
| `type` | string | Recommendation type requested |
| `recommendations` | array | List of recommended recipes |
| `message` | string | Human-readable description |
| `filters_applied` | object | Summary of applied filters |
| `error` | string | Error message (only in error responses) |

### Recipe Object Fields

| Field | Type | Description |
|-------|------|-------------|
| `id` | integer | Unique recipe identifier |
| `name` | string | Recipe name |
| `total_time` | integer | Cooking time in minutes |
| `image_url` | string | URL to recipe image |
| `calories` | number | Calories per serving |
| `aggregated_rating` | number | Average user rating (1.0-5.0) |
| `score_field` | number | Type-specific score (see Score Fields section) |

---

## Error Handling

### Common Error Codes

| HTTP Status | Error Type | Description |
|-------------|------------|-------------|
| 400 | Bad Request | Invalid JSON, missing required fields |
| 500 | Internal Server Error | Database error, Python script error |

### Error Response Examples

```json
// Invalid JSON data
{
  "error": "Invalid JSON data: Expecting ',' delimiter: line 1 column 20",
  "type": "ingredients"
}

// Missing required field
{
  "error": "Missing required field: user_id",
  "type": "preferences"
}

// Invalid activity level
{
  "error": "Invalid activity_level: 'super_active'. Valid values: sedentary, lightly_active, moderately_active, very_active, extra_active, extremely_active",
  "type": "nutriments"
}

// Database connection error
{
  "error": "Internal error: database connection failed",
  "type": "ingredients"
}
```

---