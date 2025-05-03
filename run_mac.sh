#!/bin/bash

# Mac launcher for Election Manager
echo "Starting Election Manager..."

# Find Java installation
if command -v java &> /dev/null; then
    JAVA_PATH=$(command -v java)
else
    # Try common Java locations
    if [ -x "/Library/Java/JavaVirtualMachines/jdk-17.jdk/Contents/Home/bin/java" ]; then
        JAVA_PATH="/Library/Java/JavaVirtualMachines/jdk-17.jdk/Contents/Home/bin/java"
    elif [ -x "/Library/Java/JavaVirtualMachines/jdk-11.jdk/Contents/Home/bin/java" ]; then
        JAVA_PATH="/Library/Java/JavaVirtualMachines/jdk-11.jdk/Contents/Home/bin/java"
    else
        echo "Java not found. Please install Java 11 or later."
        echo "You can download it from: https://www.oracle.com/java/technologies/downloads/"
        exit 1
    fi
fi

# Run the application with JavaFX modules
"$JAVA_PATH" --module-path "lib" --add-modules javafx.controls,javafx.fxml -jar election-app-1.0-SNAPSHOT.jar

echo "Application closed." 