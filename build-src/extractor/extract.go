package extractor

import (
	"database/sql"
	"fmt"

	_ "github.com/mattn/go-sqlite3"

	"github.com/BasileBux/homeal/build-src/globals"
	"github.com/BasileBux/homeal/build-src/utils"
)

// We cannot read the whole file at once, so we read it in chunks. 100,000 rows
// is aproximately 11GB ram at peak usage, which works on 16GB RAM machines. However,
// if your machine has less RAM, you need to reduce this value. The higher the value,
// the faster the extraction, but also the more RAM used.
const PASS_SIZE = 100000

func CreateDB() error {
	if utils.PathExists(globals.DB_OUTPUT_FILE) {
		fmt.Println("Database file already exists, skipping creation.")
		return nil
	}

	db, err := sql.Open("sqlite3", globals.DB_OUTPUT_FILE)
	if err != nil {
		return fmt.Errorf("error opening database: %w", err)
	}
	defer db.Close()

	_, err = db.Exec(globals.DB_SCHEMA)
	if err != nil {
		return fmt.Errorf("error executing schema: %w", err)
	}

	_, err = ProductsExtract(PASS_SIZE)
	if err != nil {
		return fmt.Errorf("error extracting products: %w", err)
	}

	return nil
}
