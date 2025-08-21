package extractor

import (
	"fmt"
	"reflect"
	"strings"

	"github.com/BasileBux/homeal/build-src/globals"
	"github.com/xitongsys/parquet-go-source/local"
	"github.com/xitongsys/parquet-go/reader"
)

// We cannot read the whole file at once, so we read it in chunks. 100,000 rows
// is aproximately 11GB ram at peak usage, which works on 16GB RAM machines. However,
// if your machine has less RAM, you need to reduce this value. The higher the value,
// the faster the extraction, but also the more RAM used.
const PASS_SIZE = 100000

func ProductsExtract() error {
	fr, err := local.NewLocalFileReader(globals.PRODUCT_DB_FILE)
	if err != nil {
		return err
	}
	defer fr.Close()

	pr, err := reader.NewParquetReader(fr, nil, 4)
	if err != nil {
		return err
	}
	defer pr.ReadStop()

	fmt.Printf("Number of rows: %d\n", pr.GetNumRows())

	// rowNumber := int(pr.GetNumRows())
	// BUG: FOR DEBUGGING PURPOSES ONLY
	rowNumber := 500000

	for n := range rowNumber/PASS_SIZE + 1 {
		records, err := pr.ReadByNumber(PASS_SIZE)
		if err != nil {
			return err
		}

		for i, record := range records {
			v := reflect.ValueOf(record)
			t := reflect.TypeOf(record)

			if v.Kind() == reflect.Ptr {
				v = v.Elem()
				t = t.Elem()
			}

			if v.Kind() == reflect.Struct {
				var countriesTags any
				var productName any
				var foundCountriesTags, foundProductName bool

				for j := 0; j < v.NumField(); j++ {
					field := v.Field(j)
					fieldType := t.Field(j)
					fieldName := fieldType.Name

					var value any
					if !field.IsValid() || (field.Kind() == reflect.Ptr && field.IsNil()) {
						value = "<nil>"
					} else if field.Kind() == reflect.Ptr {
						elem := field.Elem()
						switch elem.Kind() {
						case reflect.String:
							value = elem.String()
						case reflect.Slice:
							if elem.Type().Elem().Kind() == reflect.Uint8 {
								value = string(elem.Bytes())
							} else {
								value = elem.Interface()
							}
						default:
							value = elem.Interface()
						}
					} else if field.Kind() == reflect.Slice {
						if field.Type().Elem().Kind() == reflect.Ptr {
							var items []string
							for k := 0; k < field.Len(); k++ {
								item := field.Index(k)
								if item.Kind() == reflect.Ptr && !item.IsNil() {
									if item.Elem().Kind() == reflect.String {
										items = append(items, item.Elem().String())
									} else if item.Elem().Kind() == reflect.Struct {
										// Handle pointer to struct (like product names with Lang/Text fields)
										structStrings := extractStringsFromStruct(item.Elem())
										if len(structStrings) > 0 {
											items = append(items, structStrings...)
										}
									} else {
										items = append(items, fmt.Sprintf("%v", item.Elem().Interface()))
									}
								}
							}
							value = items
						} else if field.Type().Elem().Kind() == reflect.Struct {
							// Handle slice of structs
							var items []string
							for k := 0; k < field.Len(); k++ {
								item := field.Index(k)
								if item.Kind() == reflect.Struct {
									structStrings := extractStringsFromStruct(item)
									if len(structStrings) > 0 {
										items = append(items, structStrings...)
									}
								}
							}
							if len(items) > 0 {
								value = items
							} else {
								value = field.Interface()
							}
						} else {
							value = field.Interface()
						}
					} else {
						value = field.Interface()
					}

					if fieldName == "Countries_tags" || fieldName == "countries_tags" {
						countriesTags = value
						foundCountriesTags = true
					} else if fieldName == "Product_name" || fieldName == "product_name" {
						productName = value
						foundProductName = true
					}
				}

				// Check if countries_tags contains "en:switzerland"
				if foundCountriesTags {
					containsSwitzerland := false

					switch ct := countriesTags.(type) {
					case string:
						containsSwitzerland = strings.Contains(ct, "en:switzerland")
					case []string:
						for _, tag := range ct {
							if strings.Contains(tag, "en:switzerland") {
								containsSwitzerland = true
								break
							}
						}
					case []any:
						for _, tag := range ct {
							if str, ok := tag.(string); ok && strings.Contains(str, "en:switzerland") {
								containsSwitzerland = true
								break
							}
						}
					default:
						strValue := fmt.Sprintf("%v", ct)
						containsSwitzerland = strings.Contains(strValue, "en:switzerland")
					}

					if containsSwitzerland {
						fmt.Printf("Row %d:\n", i+n*PASS_SIZE)
						fmt.Printf("  countries_tags: %v\n", countriesTags)
						if foundProductName {
							fmt.Printf("  product_name: %v\n", productName)
						}
						fmt.Println()
					}
				}
			}
		}
	}

	return nil
}

// Helper function to extract string values from struct fields
func extractStringsFromStruct(structValue reflect.Value) []string {
	var result []string

	for i := 0; i < structValue.NumField(); i++ {
		field := structValue.Field(i)

		if !field.IsValid() {
			continue
		}

		if field.Kind() == reflect.Ptr && !field.IsNil() {
			elem := field.Elem()
			if elem.Kind() == reflect.String {
				str := elem.String()
				if str != "" {
					result = append(result, str)
				}
			}
		} else if field.Kind() == reflect.String {
			str := field.String()
			if str != "" {
				result = append(result, str)
			}
		}
	}

	return result
}
