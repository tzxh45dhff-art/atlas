@echo off
rem Compile and run the ATLAS tests without Maven. Tests use a temporary data folder.
cd /d "%~dp0"
if not exist out mkdir out
javac -encoding UTF-8 -d out src\main\java\com\atlas\*.java src\test\java\com\atlas\*.java
if errorlevel 1 exit /b 1
set TMP_DIR=%TEMP%\atlas-test-%RANDOM%
mkdir "%TMP_DIR%"
java -Datlas.data="%TMP_DIR%" -cp out com.atlas.AtlasTests
set RESULT=%ERRORLEVEL%
rmdir /s /q "%TMP_DIR%"
exit /b %RESULT%
