package main

import (
	"fmt"
	"net/http"
)

func main() {
	go func() {
		mux1 := http.NewServeMux()
		fs := http.FileServer(http.Dir("./landing-page"))
		mux1.Handle("/", fs)
		if err := http.ListenAndServe(":80", mux1); err != nil {
			fmt.Printf("Server landing-page error: %v\n", err)
		}
	}()

	mux2 := http.NewServeMux()
	mux2.HandleFunc("/", func(w http.ResponseWriter, r *http.Request) {
		fmt.Fprintf(w, "Hello, World!")
	})
	if err := http.ListenAndServe(":3000", mux2); err != nil {
		fmt.Printf("Server API error: %v\n", err)
	}

}
