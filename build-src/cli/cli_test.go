package cli

import (
	"testing"
	globals "github.com/BasileBux/homeal/build-src/globals"
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
		{[]string{"release"}, globals.USAGE_STR},
		{[]string{"help"}, globals.USAGE_STR},
		{[]string{"unknown"}, globals.USAGE_STR},
		{[]string{}, globals.USAGE_STR},
		{[]string{"release", "unknown"}, globals.USAGE_STR},
		{[]string{"debug", "extra"}, globals.USAGE_STR},
		{[]string{"release", "proud", "extra"}, globals.USAGE_STR},
	}

	for _, test := range tests {
		_, err := ParseArgs(test.args)
		if err != nil && err.Error() != test.expected {
			t.Errorf("ParseArgs(%v) = %v; want %v", test.args, err.Error(), test.expected)
		}
	}
}
