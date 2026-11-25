@echo off
rem Compile and run ATLAS without Maven (needs only a JDK, version 17 or newer).
cd /d "%~dp0"
if not exist out mkdir out
javac -encoding UTF-8 -d out src\main\java\com\atlas\*.java
if errorlevel 1 exit /b 1
java -cp out com.atlas.Main
