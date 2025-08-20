package main

import "fmt"

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
	version ReleaseType
	debug   bool
}

const usageStr = "Invalid program argument(s)\n" +
	"Usage: build release-type [version-type]\n" +
	"release-type: build, release\n" +
	"version-type: proud, major, minor\n"

func ParseArgs(args []string) (Args, error) {
	if len(args) <= 0 {
		return Args{}, fmt.Errorf(usageStr)
	}
	if args[0] == "help" {
		return Args{}, fmt.Errorf(usageStr)
	}
	if args[0] == "debug" {
		return Args{
			version: NONE,
			debug:   true,
		}, nil
	}
	if args[0] == "release" {
		if len(args) != 2 {
			return Args{}, fmt.Errorf(usageStr)
		}
		if args[1] == "proud" {
			return Args{
				version: PROUD,
				debug:   false,
			}, nil
		}
		if args[1] == "major" {
			return Args{
				version: MAJOR,
				debug:   false,
			}, nil
		}
		if args[1] == "minor" {
			return Args{
				version: MINOR,
				debug:   false,
			}, nil
		}
	}
	return Args{}, fmt.Errorf(usageStr)
}
