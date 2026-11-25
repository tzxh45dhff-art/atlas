#!/bin/bash
# Compile and run the ATLAS tests without Maven. Tests use a temporary data folder,
# so your real files in data/ are never touched.
set -e
cd "$(dirname "$0")"
mkdir -p out
javac -encoding UTF-8 -d out src/main/java/com/atlas/*.java src/test/java/com/atlas/*.java
TMP_DIR="$(mktemp -d)"
trap 'rm -rf "$TMP_DIR"' EXIT
java -Datlas.data="$TMP_DIR" -cp out com.atlas.AtlasTests
