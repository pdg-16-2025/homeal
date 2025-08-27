# REST-API Description

## GET /scan
**Arguments:**
- `code` (string, required): The code to be scanned.
**Returns:**
- `200 OK`: JSON object with scan results (Ingredient).
- `400 Bad Request`: If the `code` parameter is missing or invalid.
- `500 Internal Server Error`: If an unexpected error occurs.

## GET /
