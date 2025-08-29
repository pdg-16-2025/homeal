-- SQLite
PRAGMA foreign_keys = ON;

CREATE TABLE Ingredient (
	id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
	name TEXT NOT NULL
);

CREATE TABLE Recipe (
	id INTEGER PRIMARY KEY NOT NULL,
	name TEXT NOT NULL,
	author_id INTEGER,
	cook_time INTEGER,
	prep_time INTEGER,
	total_time INTEGER,
	description TEXT,
	images TEXT,
	category TEXT,
	keywords TEXT,
	aggregated_rating REAL,
	review_count INTEGER,
	calories REAL,
	fat_content REAL,
	saturated_fat_content REAL,
	cholesterol_content REAL,
	sodium_content REAL,
	carbohydrate_content REAL,
	fiber_content REAL,
	sugar_content REAL,
	protein_content REAL,
	recipe_servings INTEGER,
	recipe_yield TEXT,
	recipe_instructions TEXT
);

CREATE TABLE Review (
	id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
	author_id INTEGER,
	recipe_id INTEGER NOT NULL,
	rating REAL CHECK (rating >= 1 AND rating <= 5),
	FOREIGN KEY (recipe_id) REFERENCES Recipe(id) ON DELETE CASCADE
);

CREATE TABLE Product (
	id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
	code TEXT NOT NULL,
	ingredient_id INTEGER,
	FOREIGN KEY (ingredient_id) REFERENCES Ingredient(id) ON DELETE SET NULL,
	UNIQUE (code)
);

CREATE TABLE RecipeIngredient (
	id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
	recipe_id INTEGER NOT NULL,
	ingredient_id INTEGER NOT NULL,
	quantity REAL NOT NULL CHECK (quantity > 0),
	unit TEXT NOT NULL,
	FOREIGN KEY (recipe_id) REFERENCES Recipe(id) ON DELETE CASCADE,
	FOREIGN KEY (ingredient_id) REFERENCES Ingredient(id) ON DELETE CASCADE,
	UNIQUE (recipe_id, ingredient_id)
);

CREATE INDEX idx_product_ingredient ON Product(ingredient_id);
CREATE INDEX idx_recipe_ingredient_recipe ON RecipeIngredient(recipe_id);
CREATE INDEX idx_recipe_ingredient_ingredient ON RecipeIngredient(ingredient_id);

CREATE INDEX idx_product_code ON Product(code);
CREATE INDEX idx_ingredient_name ON Ingredient(name);

