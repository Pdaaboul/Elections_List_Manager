package com.election.service;

import com.election.model.Candidate;

import java.io.File;
import java.sql.*;
import java.util.*;
import java.text.SimpleDateFormat;

public class DatabaseService {
    private static final String DB_PATH = "db/election.db";
    private static final String DB_URL = "jdbc:sqlite:" + DB_PATH;
    private static final String SELECTIONS_TABLE = "selections";
    private static final String CANDIDATES_TABLE = "candidates";
    
    private static DatabaseService instance;
    
    private DatabaseService() {
        initializeDatabase();
    }
    
    public static DatabaseService getInstance() {
        if (instance == null) {
            instance = new DatabaseService();
        }
        return instance;
    }
    
    private void initializeDatabase() {
        // Create the db directory if it doesn't exist
        File dbDir = new File("db");
        if (!dbDir.exists()) {
            dbDir.mkdir();
        }
        
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement()) {
            
            // Create candidates table
            stmt.execute("CREATE TABLE IF NOT EXISTS " + CANDIDATES_TABLE + " (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "name TEXT NOT NULL, " +
                    "list TEXT NOT NULL, " +
                    "index_num INTEGER NOT NULL, " +
                    "selection_count INTEGER DEFAULT 0, " +
                    "UNIQUE(name, list) ON CONFLICT REPLACE)");
            
            // Create selections table
            stmt.execute("CREATE TABLE IF NOT EXISTS " + SELECTIONS_TABLE + " (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "candidate_id INTEGER NOT NULL, " +
                    "selection_order INTEGER NOT NULL, " +
                    "timestamp TEXT NOT NULL, " +
                    "session_id TEXT NOT NULL, " +
                    "FOREIGN KEY (candidate_id) REFERENCES " + CANDIDATES_TABLE + "(id))");
            
        } catch (SQLException e) {
            System.err.println("Error initializing database: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public void saveSelection(List<Candidate> selectedCandidates) {
        String sessionId = String.valueOf(System.currentTimeMillis());
        String timestamp = new Timestamp(System.currentTimeMillis()).toString();
        
        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            conn.setAutoCommit(false);
            
            try {
                // Save each selected candidate
                for (Candidate candidate : selectedCandidates) {
                    // Get or create candidate id
                    int candidateId = getOrCreateCandidate(conn, candidate);
                    
                    // Insert selection
                    try (PreparedStatement pstmt = conn.prepareStatement(
                            "INSERT INTO " + SELECTIONS_TABLE + 
                            " (candidate_id, selection_order, timestamp, session_id) VALUES (?, ?, ?, ?)")) {
                        pstmt.setInt(1, candidateId);
                        pstmt.setInt(2, candidate.getSelectionOrder());
                        pstmt.setString(3, timestamp);
                        pstmt.setString(4, sessionId);
                        pstmt.executeUpdate();
                    }
                    
                    // Update selection count
                    try (PreparedStatement pstmt = conn.prepareStatement(
                            "UPDATE " + CANDIDATES_TABLE + 
                            " SET selection_count = selection_count + 1 WHERE id = ?")) {
                        pstmt.setInt(1, candidateId);
                        pstmt.executeUpdate();
                    }
                }
                
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            System.err.println("Error saving selection: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private int getOrCreateCandidate(Connection conn, Candidate candidate) throws SQLException {
        // Check if candidate exists
        try (PreparedStatement pstmt = conn.prepareStatement(
                "SELECT id FROM " + CANDIDATES_TABLE + 
                " WHERE name = ? AND list = ? AND index_num = ?")) {
            pstmt.setString(1, candidate.getName());
            pstmt.setString(2, candidate.getList());
            pstmt.setInt(3, candidate.getIndex());
            
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("id");
            }
        }
        
        // Insert new candidate
        try (PreparedStatement pstmt = conn.prepareStatement(
                "INSERT INTO " + CANDIDATES_TABLE + 
                " (name, list, index_num, selection_count) VALUES (?, ?, ?, 0)", 
                Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, candidate.getName());
            pstmt.setString(2, candidate.getList());
            pstmt.setInt(3, candidate.getIndex());
            pstmt.executeUpdate();
            
            ResultSet rs = pstmt.getGeneratedKeys();
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        
        throw new SQLException("Failed to get or create candidate");
    }
    
    public Map<String, Integer> getCandidateStats() {
        Map<String, Integer> stats = new HashMap<>();
        
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(
                     "SELECT name, list, selection_count FROM " + CANDIDATES_TABLE + 
                     " ORDER BY selection_count DESC")) {
            
            while (rs.next()) {
                String name = rs.getString("name");
                String list = rs.getString("list");
                int count = rs.getInt("selection_count");
                stats.put(list + ": " + name, count);
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting candidate stats: " + e.getMessage());
            e.printStackTrace();
        }
        
        return stats;
    }
    
    // New method to get all saved sessions
    public List<Map<String, Object>> getSavedSessions() {
        List<Map<String, Object>> sessions = new ArrayList<>();
        
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(
                     "SELECT DISTINCT session_id, timestamp FROM " + SELECTIONS_TABLE + 
                     " ORDER BY timestamp DESC")) {
            
            ResultSet rs = pstmt.executeQuery();
            
            int sessionCount = 1;
            SimpleDateFormat displayFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            
            while (rs.next()) {
                String sessionId = rs.getString("session_id");
                String timestamp = rs.getString("timestamp");
                
                Map<String, Object> session = new HashMap<>();
                session.put("id", sessionId);
                session.put("timestamp", timestamp);
                session.put("name", "Generated List #" + sessionCount++);
                
                Timestamp ts = Timestamp.valueOf(timestamp);
                session.put("formattedTime", displayFormat.format(ts));
                
                // Get the count of candidates in this session
                try (PreparedStatement countStmt = conn.prepareStatement(
                        "SELECT COUNT(*) as count FROM " + SELECTIONS_TABLE + 
                        " WHERE session_id = ?")) {
                    countStmt.setString(1, sessionId);
                    ResultSet countRs = countStmt.executeQuery();
                    if (countRs.next()) {
                        session.put("candidateCount", countRs.getInt("count"));
                    }
                }
                
                sessions.add(session);
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting saved sessions: " + e.getMessage());
            e.printStackTrace();
        }
        
        return sessions;
    }
    
    // New method to get candidates in a specific session
    public List<Map<String, Object>> getSessionCandidates(String sessionId) {
        List<Map<String, Object>> candidates = new ArrayList<>();
        
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(
                     "SELECT c.name, c.list, s.selection_order FROM " + 
                     SELECTIONS_TABLE + " s JOIN " + CANDIDATES_TABLE + " c " +
                     "ON s.candidate_id = c.id WHERE s.session_id = ? " +
                     "ORDER BY s.selection_order")) {
            
            pstmt.setString(1, sessionId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Map<String, Object> candidate = new HashMap<>();
                candidate.put("name", rs.getString("name"));
                candidate.put("list", rs.getString("list"));
                candidate.put("order", rs.getInt("selection_order"));
                
                candidates.add(candidate);
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting session candidates: " + e.getMessage());
            e.printStackTrace();
        }
        
        return candidates;
    }
} 