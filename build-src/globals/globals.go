package globals

const PRODUCT_DB_URL = "https://huggingface.co/datasets/openfoodfacts/product-database/resolve/main/food.parquet?download=true"
const PRODUCT_DB_FILE = "food.parquet"

const RECIPES_DB_URL = "https://www.kaggle.com/api/v1/datasets/download/irkaal/foodcom-recipes-and-reviews"
const RECIPES_DB_ZIP_FILE = "recipes.zip"
const RECIPES_DB_FILES = "recipes"
const REVIEWS_DB_FILES = "reviews"

const USAGE_STR = "Invalid program argument(s)\n" +
	"Usage: build release-type [version-type]\n" +
	"release-type: build, release\n" +
	"version-type: proud, major, minor\n"
