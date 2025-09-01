# REST-API Description

## GET /scan
**Arguments:**
- `code` (string, required): The code to be scanned.

**Example Request:**
```
GET /scan?code=0007200000021
```

**Returns:**
- `200 OK`: JSON object with scan results (Ingredient).
- `400 Bad Request`: If the `code` parameter is missing or invalid.
- `404 Not Found`: If no ingredient is found with the given code.
- `500 Internal Server Error`: If an unexpected error occurs.

**Example Response:**
```json
{
  "id":32,
  "name":"white wine worcestershire sauce"
}
```

## GET /recommendations
**Arguments:**
- `type` (string, required): The type of recommendations to create
- `data` (string, required): The data to base recommendations on
- `number` (integer, optional): The number of recommendations to return (default is 5)

**Example Request:**
```
GET /recommendations?type=random&data={}&number=5
```

**Returns:**
- `200 OK`: JSON array of recommended items.
- `400 Bad Request`: If required parameters are missing or invalid.
- `500 Internal Server Error`: If an unexpected error occurs.

**Example Response:**
```json
[
  {
    "id":22761,
    "name":"Saucy Salisbury Steak",
    "total_time":30,
    "image_url":""
  },
  {
    "id":6849,
    "name":"Pita Crisps",
    "total_time":2,
    "image_url":"https://img.sndimg.com/food/image/upload/w_555,h_416,c_fit,fl_progressive,q_95/v1/img/recipes/68/49/pickxh7b9.jpg, https://img.sndimg.com/food/image/upload/w_555,h_416,c_fit,fl_progressive,q_95/v1/img/recipes/68/49/picp5xQH0.jpg"
  },
  {
    "id":16383,
    "name":"Pasta Dough #2, Spinach",
    "total_time":63,
    "image_url":""
  },
  {
    "id":22015,
    "name":"Milk Chocolate Fudge",
    "total_time":17,
    "image_url":""
  },
  {
    "id":23977,
    "name":"Cran-Raspberry Waldorf Salad",
    "total_time":200,
    "image_url":"https://img.sndimg.com/food/image/upload/w_555,h_416,c_fit,fl_progressive,q_95/v1/img/recipes/23/97/7/picLY04Vm.jpg"
  }
]
```

## GET /recipe
**Arguments:**
- `id` (int, required): The ID of the recipe to retrieve.

**Example Request:**
```
GET /recipe?id=210
```

**Returns:**
- `200 OK`: JSON object with recipe details (Recipe).
- `400 Bad Request`: If the `id` parameter is missing or invalid.
- `404 Not Found`: If no recipe is found with the given ID.
- `500 Internal Server Error`: If an unexpected error occurs.

**Example Response:**
```json
{
  "id":210,
  "name":"Christmas Snow Punch",
  "author_id":1605,
  "cook_time":-1,
  "prep_time":15,
  "total_time":15,
  "images":"",
  "category":"Punch Beverage",
  "keywords":"Beverages, Winter, Christmas, \u003c 15 Mins, For Large Groups, Easy",
  "aggregated_rating":5,
  "calories":42,
  "fat_content":2,
  "saturated_fat_content":1.2,
  "cholesterol_content":7.9,
  "sodium_content":15.7,
  "carbohydrate_content":5.8,
  "fiber_content":0.1,
  "sugar_content":5.2,
  "protein_content":0.6,
  "recipe_servings":32,
  "recipe_yield":"",
  "recipe_instructions":"1. In punch bowl, combine Hi-C Hula Punch, Sprite and ice cream.\n2. Stir until well blended and chill.\n3. From: Spartan Holiday Cookbook Robin",
  "ingredients":[
    {
      "id":62,
      "name":"vanilla ice cream"
    },
    {
      "id":168,
      "name":"cups"
    },
    {
      "id":816,
      "name":"(46 ounce) can Hi-C Hula Punch, thoroughly chilled"
    }
  ]
}
```

## GET /ingredients
**Arguments:**
- `search` (string, optional): A search term to filter ingredients by name.
- `limit` (integer, optional): The maximum number of ingredients to return (default is 10).

**Example Request:**
```
GET /ingredients?search=tomato
```

**Returns:**
- `200 OK`: JSON array of ingredients.
- `400 Bad Request`: If parameters are invalid.
- `500 Internal Server Error`: If an unexpected error occurs.

**Example Response:**
```json
[
  {
    "id":271,
    "name":"(10 1/2 ounce) can tomato soup"
  },
  {
    "id":158,
    "name":"(16 ounce) jar ragu spaghetti sauce (\"Chunky Gardenstyle, Tomato, Garlic, \u0026 Onion\")"
  },
  {
    "id":771,
    "name":"10-inch sun-dried tomato tortillas"
  },
  {
    "id":549,
    "name":"Contadina diced tomatoes"
  },
  {
    "id":448,
    "name":"Italian plum tomatoes"
  },
  {
    "id":58,
    "name":"Ragu tomato sauce"
  },
  {
    "id":238,
    "name":"Rotel tomatoes \u0026 chilies"
  },
  {
    "id":85,
    "name":"beefsteak tomatoes"
  },
  {
    "id":794,
    "name":"canned plum tomatoes"
  },
  {
    "id":516,
    "name":"canned stewed tomatoes"
  }
]
```

## GET /recipe-ingredients
**Arguments:**
- `recipe_id` (int, required): The ID of the recipe to retrieve ingredients for.

**Example Request:**
```
GET /recipe-ingredients?recipe_id=139
```

**Returns:**
- `200 OK`: JSON array of ingredients for the specified recipe.
- `400 Bad Request`: If the `recipe_id` parameter is missing or invalid.
- `404 Not Found`: If no recipe is found with the given ID.
- `500 Internal Server Error`: If an unexpected error occurs.

**Example Response:**
```json
[
  {
    "id":11,
    "name":"barbecued chicken",
    "quantity":"1",
    "unit":"unit"
  },
  {
    "id":12,
    "name":"coconut",
    "quantity":"1",
    "unit":"cup, shredded"
  },
  {
    "id":20,
    "name":"coconut milk",
    "quantity":"1",
    "unit":"cup"
  },
  {
    "id":15,
    "name":"curry powder",
    "quantity":"1",
    "unit":"teaspoon"
  },
  {
    "id":16,
    "name":"five-spice powder",
    "quantity":"1",
    "unit":"teaspoon"
  },
  {
    "id":18,
    "name":"fresh ginger",
    "quantity":"1",
    "unit":"teaspoon grated"
  },
  {
    "id":17,
    "name":"garlic clove",
    "quantity":"1",
    "unit":", crushed"
  },
  {
    "id":19,
    "name":"mayonnaise",
    "quantity":"1",
    "unit":"cup"
  },
  {
    "id":14,
    "name":"spring onions",
    "quantity":"4",
    "unit":", chopped"
  },
  {
    "id":13,
    "name":"unsweetened pineapple slices",
    "quantity":"1",
    "unit":"(440 g) can, drained"
  }
]
```
