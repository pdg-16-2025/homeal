# Architecture

## 1. Introduction
The **Homeal** application is an Android **offline-first** app designed to generate recipes based on the ingredients available.  
It does not rely on any external server: all data is stored and accessed locally.

---

## 2. Technical choices
- **Database**: SQLite (natively integrated into Android).  
- **Architecture pattern**: **MVC** (Model – View – Controller).  
- **Operating mode**: 100% offline (no network synchronization).  
- **Data**:
  - General recipes (shipped statically with the app).  
  - Personal recipes (created by the user).  
  - Saved recipes (favorites).  
  - OpenFoodFacts data (provided statically with the app).  

---

## 3. Logical architecture

### 3.1 View (UI Tabs)
- **Calendar Tab**: displays daily meals, user can assign recipes.  
- **Fridge Tab**: displays fridge ingredients, can be updated manually or by scanning.  
- **Shopping List Tab**: lists items to buy, auto-updates when scanned.  
- **Scan Tab**: scans product barcodes, adds them to fridge or removes them from shopping list.  
- **Settings Tab**: app preferences and user options.  

### 3.2 Controllers
- Handle user interactions from each tab.  
- Communicate with the database to fetch or update data.  

### 3.3 Model (SQLite Database)
- Single local database with multiple tables:
  - `recipes_global` : general recipes (preloaded).  
  - `recipes_user` : user-created recipes.  
  - `recipes_saved` : saved/favorite recipes.  
  - `openfoodfacts` : static OpenFoodFacts dataset.  
  - `fridge_ingredients` : current fridge content.  
  - `shopping_list` : items to purchase.  

---

## 4. Architecture diagram

![Architecture Homeal](Images/ArchitectureHomeal.png)


---

## 5. Key points

- **Offline-first**:  
  The app is fully functional without an Internet connection.  

- **Static and dynamic data**:  
  - Base data (global recipes + OpenFoodFacts) is embedded at build time.  
  - User data (personal recipes, saved recipes, fridge, shopping list) is stored in dedicated tables.  

- **Simplicity**:  
  - No server, no API.  
  - Direct read/write in SQLite via controllers.  

---

## 6. Advantages of this architecture
- **Performance**: no network latency.  
- **Low maintenance**: single local database.  
- **Portability**: SQLite is lightweight and fully integrated into Android.  
- **User experience**: app remains usable even in airplane mode.  

---

## 7. Implementation Details
- **Language**: Java/Kotlin
- **Minimum SDK**: Android API 21+ (Android 5.0)
- **Permissions**: Camera (scanning), Storage (data)

---
## 8. Data Model (Draft Schema)

| Table | Purpose | Key Columns (type) | Notes |
|-------|---------|--------------------|-------|
| `recipes_global` | Preloaded base recipes | `id` (INT PK), `title` (TEXT), `instructions` (TEXT), `time_minutes` (INT), `difficulty` (INT), `json_ingredients` (TEXT) | Shipped read‑only; JSON list initially |
| `recipes_user` | User created recipes | `id` (INT PK), `title`, `instructions`, `created_at` (INT), `json_ingredients` | Editable |
| `recipes_saved` | Favorites / bookmarks | `user_recipe_id` (INT FK), `saved_at` (INT) | Composite PK (`user_recipe_id`) |
| `Products` | Static product reference | `barcode` (TEXT PK), `product_name` (TEXT), `brands` (TEXT), `categories` (TEXT), `nutri_score` (TEXT), `quantity` (TEXT), `image_url` (TEXT) | Filtered subset |
| `fridge_ingredients` | Current stock items | `id` (INT PK), `name` (TEXT), `quantity_value` (REAL), `quantity_unit` (TEXT), `expires_at` (INT nullable), `added_at` (INT) | Index on `expires_at` |
| `shopping_list` | Planned purchase items | `id` (INT PK), `name` (TEXT), `expected_qty_value` (REAL), `expected_qty_unit` (TEXT), `added_at` (INT), `resolved` (INT BOOL) | Index on `resolved` |
| `scan_events` (optional) | Audit of scans | `id` (INT PK), `barcode` (TEXT), `action` (TEXT), `ts` (INT) | Debug / metrics |

> Time values = epoch seconds. Units textual first iteration.

### Initial Indices
- `fridge_ingredients(expires_at)`
- `openfoodfacts(barcode)`
- `shopping_list(resolved)`

## 9. Core Data Flows

### 9.1 Scan Product → Add to Fridge
1. Scan barcode → lookup in `openfoodfacts`.
2. Found: prefill & insert fridge row.
3. Not found: manual entry dialog.
4. Refresh fridge list.

### 9.2 Plan Meal
1. Select day + meal slot.
2. Recipe picker (global + user + saved).
3. Assign recipe (future `meal_plan` table).
4. Later: confirm cooking → adjust stock.

### 9.3 Generate Shopping List
1. Aggregate planned recipes ingredients.
2. Subtract fridge quantities (name match heuristic).
3. Insert / update `shopping_list` items.

### 9.4 Expiration Recommendations
1. Query near-expiring items.
2. Match recipes containing them.
3. Rank by count of expiring ingredients used.

## 10. Controller Responsibilities (Draft)
| Controller | Scope | Key Operations |
|------------|-------|----------------|
| ScanController | Barcode acquisition, lookup | decodeBarcode(), lookupProduct(), createFridgeEntry() |
| FridgeController | Stock CRUD | listItems(), addManual(), updateQuantity(), removeExpired(), recommendUseSoon() |
| RecipeController | Recipes & favorites | listAll(), search(term), addUserRecipe(), saveFavorite() |
| ShoppingController | Shopping lifecycle | generateFromPlan(), listActive(), toggleResolved(), clearResolved() |
| CalendarController | Meal plan | assignRecipe(mealSlot, recipeId), unassign(), listDay() |
| SettingsController | Preferences | loadSettings(), updateSetting(key,val) |

## 11. Testing Strategy (Initial)
| Test Type | Scope | Example |
|-----------|-------|---------|
| Unit | Controllers (in-memory DB) | Adding ingredient updates counts |
| Instrumented | SQLite | Migration preserves data |
| UI (later) | Navigation flows | Scan → item appears |
| Property (future) | Aggregation | Ingredient sum stable |

Test Data: small curated OpenFoodFacts subset (≤ 200 rows).

## 12. Performance & Size Considerations
- Trim OpenFoodFacts columns.
- Compress preloaded DB asset.
- Defer hi-res images (optional future network module).

## 13. Future Evolution
- Optional remote sync layer.
- Normalize ingredients (recipe_ingredients table).
- Nutrition analysis & goals.
- Room ORM adoption (if starting raw).
- Improved recommendation scoring.

## 14. Open Questions
- Min SDK final (21 vs 24 vs 26).
- Migration tooling (Room auto vs manual).
- Units normalization strategy.

---