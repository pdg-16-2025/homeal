package main

import (
	"testing"
)

func TestParseArgs(t *testing.T) {
	tests := []struct {
		args     []string
		expected string
	}{
		{[]string{"debug"}, ""},
		{[]string{"release", "proud"}, ""},
		{[]string{"release", "major"}, ""},
		{[]string{"release", "minor"}, ""},
		{[]string{"release"}, "Invalid program argument(s)\nUsage: build release-type [version-type]\nrelease-type: build, release\nversion-type: proud, major, minor\n"},
		{[]string{"help"}, "Invalid program argument(s)\nUsage: build release-type [version-type]\nrelease-type: build, release\nversion-type: proud, major, minor\n"},
	}

	for _, test := range tests {
		_, err := ParseArgs(test.args)
		if err != nil && err.Error() != test.expected {
			t.Errorf("ParseArgs(%v) = %v; want %v", test.args, err.Error(), test.expected)
		}
	}
}
