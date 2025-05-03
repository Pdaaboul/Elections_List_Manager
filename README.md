# Election Manager Application

A JavaFX application for managing elections, candidates, and voting processes.

## Features

- Manage candidates and voter information
- Create and organize voting processes
- Generate reports and statistics
- View results and analytics

## Installation

### Prerequisites

- Java 17 or higher is required to run the application
- Maven for building from source

### Using the Installer

#### Windows
1. Download the Windows installer from the releases page
2. Run the ElectionManager installer (.exe)
3. Follow the installation wizard
4. After installation, you can launch the application from the Start menu

#### macOS
1. Download the macOS DMG from the releases page
2. Open the DMG file
3. Drag ElectionManager to your Applications folder
4. Launch the application from your Applications folder

#### Linux
1. Download the DEB package from the releases page
2. Install using your package manager:
   ```
   sudo dpkg -i ElectionManager.deb
   ```
3. Launch from your applications menu or by running `ElectionManager` in terminal

## Building from Source

If you prefer to build the application from source:

1. Clone the repository:
   ```
   git clone https://github.com/yourusername/election-manager.git
   cd election-manager
   ```

2. Build with Maven:
   ```
   mvn clean package
   ```

3. Run the application:
   ```
   java -jar target/election-app-1.0-SNAPSHOT.jar
   ```

## Creating Installers

To create installers for different operating systems, use the provided script:

```
./create-installer.sh
```

This will:
1. Build the application with Maven
2. Create an installer appropriate for your current operating system
3. Place the installer in the `target/installer` directory

## Usage

1. Launch the application
2. Create a new election or open an existing one
3. Add candidates and voter information
4. Configure voting processes and settings
5. Generate reports and view statistics

## License

This project is licensed under the MIT License - see the LICENSE file for details. 