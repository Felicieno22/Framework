@echo off
setlocal

REM Chemin vers le répertoire du projet
set "PROJECT_DIR=C:\Users\Felicieno\Documents\fianarana\Fianarana\L2\Mr Naina"

REM Nom du projet
set "PROJECT_NAME=Framework"

REM Répertoire des bibliothèques
set "LIB_DIR=%PROJECT_DIR%\lib"

REM Répertoire source
set "SRC_DIR=%PROJECT_DIR%src\com"

REM Répertoire temporaire pour la compilation
set "TEMP_DIR=%PROJECT_DIR%\temp"

REM Création du répertoire temporaire s'il n'existe pas
if not exist "%TEMP_DIR%" mkdir "%TEMP_DIR%"

REM Compilation du code source
for /r "%SRC_DIR%" %%i in (*.java) do (
    javac -cp "%LIB_DIR%\*;" -sourcepath "%SRC_DIR%" -d "%TEMP_DIR%" "%%i"
)

REM Création du fichier JAR
jar -cf "%PROJECT_NAME%.jar" -C "%TEMP_DIR%" .

REM Déplacement du fichier JAR vers le répertoire lib du projet cible
xcopy /y "%PROJECT_NAME%.jar" "%PROJECT_DIR%\SprintTest5\lib"

REM Suppression du répertoire temporaire
rmdir /s /q "%TEMP_DIR%"

echo Création du JAR terminée.
pause
