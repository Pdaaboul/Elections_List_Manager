# Internal Election Application

A desktop JavaFX application for conducting internal elections, allowing users to select candidates in a specific order and persisting selections to local storage.

## Features

- Display two lists, each with 9 candidates (18 total candidates)
- Select up to 9 candidates across both lists in a specific order
- Save selections to a local SQLite database
- Export selections to PDF reports in the `output/` directory
- View statistics on candidate selection counts
- Works completely offline with all data stored locally

## Technical Stack

- JavaFX for the user interface
- SQLite with JDBC for data persistence
- iText for PDF generation
- No external services or internet connection required

## Setup and Installation

### Requirements

- Java Development Kit (JDK) 17 or later
- Maven 3.6 or later

### Building the Application

1. Clone or download this repository
2. Navigate to the project directory in a terminal
3. Build the project with Maven:

```bash
mvn clean package
```

This will create an executable JAR file in the `target/` directory.

### Running the Application

Run the application using:

```bash
java -jar target/election-app-1.0-SNAPSHOT.jar
```

Or double-click the JAR file if your system allows it.

## Usage

1. When the application starts, you'll see two lists of candidates.
2. Click on a candidate to select them. Each candidate will show their selection order (1-9).
3. To deselect a candidate, click on them again.
4. You can select up to 9 candidates total across both lists.
5. After making your selections, click "Save Selection" to:
   - Save selections to the database
   - Generate a PDF report in the `output/` folder
6. Click "View Statistics" to see the selection counts for all candidates.
7. Click "Reset" to clear all current selections.

## Configuration

The candidate names are loaded from `config/names.json`. You can edit this file to change the candidate names.

## Data Storage

- SQLite database: `election.db` (created on first run)
- PDF reports: `output/` directory (created on first run)
- Logs: `log.txt` in the application directory

## Deployment

The application can be deployed on multiple machines, and each will maintain its own local database. No server or internet connection is required. 