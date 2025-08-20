package main

import (
	"fmt"
	"os"
	// "github.com/xitongsys/parquet-go/reader"
	// "github.com/xitongsys/parquet-go-source/local"
)

func main() {
	_, err := ParseArgs(os.Args[1:])
	if err != nil {
		fmt.Println(err)
		return
	}
}

// func main() {
// 	// Open the parquet file
// 	fr, err := local.NewLocalFileReader("food.parquet")
// 	if err != nil {
// 		log.Fatalf("Failed to open parquet file: %v", err)
// 	}
// 	defer fr.Close()
//
// 	// Create parquet reader
// 	pr, err := reader.NewParquetReader(fr, nil, 4)
// 	if err != nil {
// 		log.Fatalf("Failed to create parquet reader: %v", err)
// 	}
// 	defer pr.ReadStop()
//
// 	// Print schema information
// 	fmt.Printf("Number of rows: %d\n", pr.GetNumRows())
// 	fmt.Printf("Schema tree:\n")
//
// 	// Get the schema tree
// 	schema := pr.SchemaHandler.SchemaElements
// 	for i, element := range schema {
// 		if element.Name != "" {
// 			fmt.Printf("Field %d: %s", i, element.Name)
// 			if element.Type != nil {
// 				fmt.Printf(" (Type: %s)", element.Type.String())
// 			}
// 			fmt.Println()
// 		}
// 	}
//
// 	// Read first few rows to understand the data structure
// 	fmt.Println("\nFirst few rows:")
//
// 	// Read records as interface{} to see the structure
// 	records, err := pr.ReadByNumber(1)
// 	if err != nil {
// 		log.Fatalf("Failed to read records: %v", err)
// 	}
//
// 	for i, record := range records {
// 		fmt.Printf("Row %d: %+v\n", i, record)
// 	}
// }
