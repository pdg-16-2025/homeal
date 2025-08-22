package utils

import (
	"os"
	"testing"
)

func TestPathExists(t *testing.T) {
	tests := []struct {
		path     string
		expected bool
	}{
		{"./utils_test.go", true},
		{"./non_existent_file.txt", false},
	}

	for _, test := range tests {
		result := PathExists(test.path)
		if result != test.expected {
			t.Errorf("PathExists(%q) = %v; want %v", test.path, result, test.expected)
		}
	}
}

// This test can only be validated it PathExists passes its tests
func TestDownloadFile(t *testing.T) {
	tests := []struct {
		url      string
		dest     string
		expected bool
	}{
		{"https://www.example.com", "example-com.txt", true},
		{"https://www.nonexistenturl.com", "nonexistent-url.txt", false},
	}

	for _, test := range tests {
		dlErr := DownloadFile(test.url, test.dest)
		exists := PathExists(test.dest)
		success := (dlErr == nil) && exists
		if success != test.expected {
			t.Errorf("DownloadFile(%q, %q): download success = %v, file exists = %v; want both = %v",
				test.url, test.dest, dlErr == nil, exists, test.expected)
		}
		os.Remove(test.dest)
	}
}
