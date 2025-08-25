package main

import (
	"net/http"
)

func main() {
	fs := http.FileServer(http.Dir("./landing-page"))
	http.Handle("/", fs)
	http.ListenAndServe(":3000", nil)
}
