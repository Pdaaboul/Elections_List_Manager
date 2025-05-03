package com.election.service;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

public class LoggingService {
    private static final String LOG_FILE = "log.txt";
    private static LoggingService instance;
    
    private LoggingService() {
        // Create the log file if it doesn't exist
        try {
            File logFile = new File(LOG_FILE);
            if (!logFile.exists()) {
                logFile.createNewFile();
                log("Logging service initialized");
            }
        } catch (IOException e) {
            System.err.println("Error initializing log file: " + e.getMessage());
        }
    }
    
    public static LoggingService getInstance() {
        if (instance == null) {
            instance = new LoggingService();
        }
        return instance;
    }
    
    public void log(String message) {
        String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        String logEntry = String.format("[%s] %s", timestamp, message);
        
        try (PrintWriter writer = new PrintWriter(new FileWriter(LOG_FILE, true))) {
            writer.println(logEntry);
        } catch (IOException e) {
            System.err.println("Error writing to log file: " + e.getMessage());
        }
    }
    
    public void logSelection(String candidateName, String listName, int selectionOrder) {
        log(String.format("Selected: %s from list %s with order %d", candidateName, listName, selectionOrder));
    }
    
    public void logDeselection(String candidateName, String listName) {
        log(String.format("Deselected: %s from list %s", candidateName, listName));
    }
    
    public void logSave(int totalSelections, String pdfPath) {
        log(String.format("Saved %d selections to PDF: %s", totalSelections, pdfPath));
    }
    
    public void logError(String context, Exception e) {
        log(String.format("ERROR in %s: %s - %s", context, e.getClass().getSimpleName(), e.getMessage()));
    }
    
    // Method to clear all logs
    public boolean clearLogs() {
        try {
            File logFile = new File(LOG_FILE);
            if (logFile.exists()) {
                // Create a new empty file to replace the existing one
                try (PrintWriter writer = new PrintWriter(new FileWriter(LOG_FILE, false))) {
                    writer.println("[" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) + "] Logs cleared");
                }
                return true;
            }
            return false;
        } catch (IOException e) {
            System.err.println("Error clearing log file: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
} 