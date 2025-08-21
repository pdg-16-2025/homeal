package downloader

import (
	"fmt"
	"os"

	"github.com/BasileBux/homeal/build-src/globals"
	"github.com/BasileBux/homeal/build-src/utils"
)

// We don't need to write tests for this function as it only calls other functions
// that are already tested.

func EnsureInstalled(force bool) error {
	if !force && utils.PathExists(globals.PRODUCT_DB_FILE+".parquet") {
		fmt.Println("Product database already exists, skipping download.")
		return nil
	}
	fmt.Println("Downloading product database...")
	err := utils.DownloadFile(globals.PRODUCT_DB_URL, globals.PRODUCT_DB_FILE)
	if err != nil {
		return err
	}
	if !force && utils.PathExists(globals.RECIPES_DB_FILES+".parquet") && utils.PathExists(globals.REVIEWS_DB_FILES+".parquet") {
		fmt.Println("Recipes databases already exist, skipping download.")
		return nil
	}
	fmt.Println("Downloading recipes database...")
	err = utils.DownloadFile(globals.RECIPES_DB_URL, globals.RECIPES_DB_ZIP_FILE)
	if err != nil {
		return err
	}

	// Unzip, and delete unwated csv files
	err = utils.UnzipFile(globals.RECIPES_DB_ZIP_FILE)
	if err != nil {
		return err
	}
	os.Remove(globals.RECIPES_DB_FILES + ".csv")
	os.Remove(globals.REVIEWS_DB_FILES + ".csv")
	os.Remove(globals.RECIPES_DB_ZIP_FILE)

	fmt.Println("Databases downloaded successfully.")
	return nil
}
