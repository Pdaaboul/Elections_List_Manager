@echo off
echo Starting Election Manager...

REM Check if Java is installed
java -version >nul 2>&1
IF %ERRORLEVEL% NEQ 0 (
    echo Java not found. Please install Java 11 or later.
    echo You can download it from: https://www.oracle.com/java/technologies/downloads/
    pause
    exit /b 1
)

REM Run the application with JavaFX modules
java --module-path "lib" --add-modules javafx.controls,javafx.fxml -jar election-app-1.0-SNAPSHOT.jar

echo Application closed.
pause 