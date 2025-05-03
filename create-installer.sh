#!/bin/bash

# Colors for output
GREEN='\033[0;32m'
RED='\033[0;31m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${BLUE}=== Election Manager Application Packager ===${NC}"

# Determine OS
OS="unknown"
if [[ "$OSTYPE" == "darwin"* ]]; then
    OS="mac"
    echo -e "${GREEN}Detected macOS${NC}"
elif [[ "$OSTYPE" == "msys" || "$OSTYPE" == "win32" || "$OSTYPE" == "cygwin" ]]; then
    OS="windows"
    echo -e "${GREEN}Detected Windows${NC}"
else
    echo -e "${GREEN}Detected Linux/Unix${NC}"
fi

# Ensure JDK 17 or higher is installed
JAVA_VERSION=$(java -version 2>&1 | awk -F '"' '/version/ {print $2}')
if [[ -z "$JAVA_VERSION" ]]; then
    echo -e "${RED}Error: Java not found. Please install JDK 17 or higher.${NC}"
    exit 1
fi

echo -e "${GREEN}Java version: $JAVA_VERSION${NC}"

# Build the application
echo -e "${BLUE}Building application with Maven...${NC}"
mvn clean package
if [ $? -ne 0 ]; then
    echo -e "${RED}Maven build failed.${NC}"
    exit 1
fi

echo -e "${GREEN}Maven build successful.${NC}"

# Create resources folder for icon if it doesn't exist
mkdir -p src/main/resources
if [ ! -f src/main/resources/icon.png ]; then
    # Create a simple icon if it doesn't exist
    echo -e "${BLUE}Creating a default application icon...${NC}"
    cat > src/main/resources/icon.png << EOF
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
EOF
fi

# Create platform-specific installer
if [ "$OS" == "mac" ]; then
    echo -e "${BLUE}Creating macOS DMG installer...${NC}"
    
    # Use jpackage to create a DMG installer
    jpackage --type dmg \
             --name "ElectionManager" \
             --app-version "1.0" \
             --input target \
             --main-jar election-app-1.0-SNAPSHOT.jar \
             --main-class com.election.ElectionAppLauncher \
             --dest target/installer \
             --resource-dir src/main/resources \
             --icon src/main/resources/icon.png \
             --mac-package-name "ElectionManager"

elif [ "$OS" == "windows" ]; then
    echo -e "${BLUE}Creating Windows EXE installer...${NC}"
    
    # Use jpackage to create a Windows EXE installer
    jpackage --type exe \
             --name "ElectionManager" \
             --app-version "1.0" \
             --input target \
             --main-jar election-app-1.0-SNAPSHOT.jar \
             --main-class com.election.ElectionAppLauncher \
             --dest target/installer \
             --resource-dir src/main/resources \
             --icon src/main/resources/icon.png \
             --win-dir-chooser \
             --win-shortcut \
             --win-menu
else
    echo -e "${BLUE}Creating Linux DEB installer...${NC}"
    
    # Create a Linux deb package
    jpackage --type deb \
             --name "ElectionManager" \
             --app-version "1.0" \
             --input target \
             --main-jar election-app-1.0-SNAPSHOT.jar \
             --main-class com.election.ElectionAppLauncher \
             --dest target/installer \
             --resource-dir src/main/resources \
             --icon src/main/resources/icon.png
fi

# Check if installer was created successfully
if [ $? -eq 0 ]; then
    echo -e "${GREEN}Installer created successfully in target/installer directory${NC}"
    ls -la target/installer
else
    echo -e "${RED}Failed to create installer${NC}"
    exit 1
fi

echo -e "${BLUE}=== Installation package creation complete ===${NC}" 