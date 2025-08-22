package extractor

import (
	"fmt"
	"reflect"
	"strings"

	"github.com/BasileBux/homeal/build-src/globals"
	"github.com/xitongsys/parquet-go-source/local"
	"github.com/xitongsys/parquet-go/reader"
)

type Product struct {
	name          string
	code          string
	ingredient_id int
}

func ProductsExtract(passSize int) ([]Product, error) {

	fr, err := local.NewLocalFileReader(globals.PRODUCT_DB_FILE)
	if err != nil {
		return nil, err
	}
	defer fr.Close()

	pr, err := reader.NewParquetReader(fr, nil, 4)
	if err != nil {
		return nil, err
	}
	defer pr.ReadStop()

	rowNumber := int(pr.GetNumRows())

	products := make([]Product, 300000)

	for n := range rowNumber/passSize + 1 {
		records, err := pr.ReadByNumber(passSize)
		if err != nil {
			return nil, err
		}

		for i, record := range records {
			v := reflect.ValueOf(record)
			t := reflect.TypeOf(record)

			if v.Kind() == reflect.Ptr {
				v = v.Elem()
				t = t.Elem()
			}

			product := Product{}

			if v.Kind() == reflect.Struct {
				var countriesTags any
				var productName any

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
					} else if fieldName == "Product_name" || fieldName == "product_name" {
						productName = value
					} else if fieldName == "Code" || fieldName == "code" {
						product.code = fmt.Sprintf("%v", value)
					}
				}

				// Check if countries_tags contains "en:switzerland"
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
					product.name = getProductName(productName)
					product.ingredient_id = 0 // Placeholder, to be updated later
					fmt.Printf("Row %d:\n", i+n*passSize)
					fmt.Printf("  countries_tags: %v\n", countriesTags)
					fmt.Printf("  product_name: %v\n", product.name)
					fmt.Printf("  code: %s\n", product.code)
					fmt.Println()
					products = append(products, product)
				}
			}
		}
	}

	return products, nil
}

func getProductName(nameBlock any) string {
	if nameBlock == nil {
		return ""
	}
	switch v := nameBlock.(type) {
	case string:
		return v
	case []string:
		ret := ""
		for _, str := range v {
			if ret != "" {
				ret += " "
			}
			ret += str
		}
		return ret

	case []any:
		if len(v) > 0 {
			if str, ok := v[0].(string); ok {
				return str
			}
		}
	}
	return ""
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
