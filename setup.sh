#!/usr/bin/env bash

mkdir -p build
cd build-src
go build .
mv build-src ../build/build
cd ..
