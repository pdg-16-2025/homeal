package cli

import (
	"fmt"
	globals "github.com/BasileBux/homeal/build-src/globals"
)

// ./build debug
// ./build release version-type

type ReleaseType = uint8

const (
	PROUD ReleaseType = iota
	MAJOR
	MINOR
	NONE
)

type Args = struct {
	Version ReleaseType
	Debug   bool
}



func ParseArgs(args []string) (Args, error) {
	if len(args) <= 0 {
		return Args{}, fmt.Errorf(globals.USAGE_STR)
	}
	if args[0] == "help" {
		return Args{}, fmt.Errorf(globals.USAGE_STR)
	}
	if args[0] == "debug" {
		return Args{
			Version: NONE,
			Debug:   true,
		}, nil
	}
	if args[0] == "release" {
		if len(args) != 2 {
			return Args{}, fmt.Errorf(globals.USAGE_STR)
		}
		if args[1] == "proud" {
			return Args{
				Version: PROUD,
				Debug:   false,
			}, nil
		}
		if args[1] == "major" {
			return Args{
				Version: MAJOR,
				Debug:   false,
			}, nil
		}
		if args[1] == "minor" {
			return Args{
				Version: MINOR,
				Debug:   false,
			}, nil
		}
	}
	return Args{}, fmt.Errorf(globals.USAGE_STR)
}
