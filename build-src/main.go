package main

import (
	"github.com/BasileBux/homeal/build-src/extractor"
)

func main() {
	// args, err := cli.ParseArgs(os.Args[1:])
	// if err != nil {
	// 	fmt.Println(err)
	// 	return
	// }
	//
	// if err = downloader.EnsureInstalled(!args.Debug); err != nil {
	// 	fmt.Printf("Error ensuring installation: %v\n", err)
	// 	return
	// }

	extractor.ProductsExtract()
}
