package com.election.service;

import com.election.model.Candidate;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;

public class ConfigService {
    private static final String CONFIG_FILE = "config/names.json";
    
    private static ConfigService instance;
    
    private ConfigService() {}
    
    public static ConfigService getInstance() {
        if (instance == null) {
            instance = new ConfigService();
        }
        return instance;
    }
    
    public List<Candidate> loadCandidatesFromList(String listName) {
        List<Candidate> candidates = new ArrayList<>();
        
        try (JsonReader reader = Json.createReader(new FileReader(CONFIG_FILE))) {
            JsonObject config = reader.readObject();
            JsonArray names = config.getJsonArray(listName);
            
            for (int i = 0; i < names.size(); i++) {
                String name = names.getString(i);
                candidates.add(new Candidate(name, listName, i));
            }
            
        } catch (IOException e) {
            System.err.println("Error loading configuration: " + e.getMessage());
            e.printStackTrace();
            
            // Fallback to default values if config file can't be loaded
            if (listName.equals("listA")) {
                for (int i = 0; i < 9; i++) {
                    candidates.add(new Candidate("Candidate A" + (i+1), listName, i));
                }
            } else if (listName.equals("listB")) {
                for (int i = 0; i < 9; i++) {
                    candidates.add(new Candidate("Candidate B" + (i+1), listName, i));
                }
            }
        }
        
        return candidates;
    }
} 