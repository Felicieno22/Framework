@echo off
setlocal

REM Set the servlet API path
set "SERVLET_API=%cd%\lib\servlet-api.jar"

REM Set the GSON path
set "GSON=%cd%\lib\gson-2.8.8.jar"

REM Set the Commons FileUpload path
set "FILEUPLOAD=%cd%\lib\commons-fileupload-1.4.jar"

REM Set source and output directories
set "src=%cd%\src"
set "OUTPUT_DIR=%cd%\bin"
set "MYLIB_DIR=%OUTPUT_DIR%\myLib"
set "JAR_FILE=%MYLIB_DIR%\framework.jar"

REM Create directories if they don't exist
if not exist "%OUTPUT_DIR%" mkdir "%OUTPUT_DIR%"
if not exist "%MYLIB_DIR%" mkdir "%MYLIB_DIR%"

REM Clean the output directory except myLib directory
for /D %%d in ("%OUTPUT_DIR%\*") do (
    if /I not "%%~nxd"=="myLib" rd /S /Q "%%d"
)
for %%f in ("%OUTPUT_DIR%\*") do (
    if /I not "%%~nxf"=="myLib" del /Q "%%f"
)

REM Compile all Java files
for /R "%src%" %%f in (*.java) do (
    javac -cp "%SERVLET_API%;%src%;%GSON%;%FILEUPLOAD%" -d "%OUTPUT_DIR%" "%%f"
)

REM Create the JAR file
cd /d "%OUTPUT_DIR%"
jar cvf "%JAR_FILE%" com

echo JAR creation completed.
pause