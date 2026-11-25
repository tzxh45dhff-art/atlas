#!/bin/bash
# Compile and run ATLAS without Maven (needs only a JDK, version 17 or newer).
set -e
cd "$(dirname "$0")"
mkdir -p out
javac -encoding UTF-8 -d out src/main/java/com/atlas/*.java
java -cp out com.atlas.Main
