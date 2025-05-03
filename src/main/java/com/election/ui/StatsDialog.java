package com.election.ui;

import com.election.service.DatabaseService;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Window;

import java.util.List;
import java.util.Map;

public class StatsDialog extends Dialog<Void> {
    
    private final DatabaseService databaseService;
    
    public StatsDialog(Window owner) {
        databaseService = DatabaseService.getInstance();
        
        // Configure dialog
        setTitle("Election Statistics");
        setHeaderText("Election Statistics & History");
        initModality(Modality.APPLICATION_MODAL);
        initOwner(owner);
        
        // Set dialog size
        setResizable(true);
        getDialogPane().setPrefWidth(600);
        getDialogPane().setPrefHeight(500);
        
        // Create tabbed view
        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        
        // Create tabs
        Tab candidateStatsTab = createCandidateStatsTab();
        Tab savedListsTab = createSavedListsTab();
        Tab identicalBatchesTab = createIdenticalBatchesTab();
        
        tabPane.getTabs().addAll(candidateStatsTab, savedListsTab, identicalBatchesTab);
        
        // Add close button
        ButtonType closeButton = new ButtonType("Close", ButtonBar.ButtonData.OK_DONE);
        getDialogPane().getButtonTypes().add(closeButton);
        
        // Set content
        getDialogPane().setContent(tabPane);
    }
    
    private Tab createCandidateStatsTab() {
        Tab tab = new Tab("Candidate Statistics");
        
        // Load statistics data
        Map<String, Integer> stats = databaseService.getCandidateStats();
        
        // Create content
        VBox content = new VBox(10);
        content.setPadding(new Insets(10));
        
        // Create table view for stats
        TableView<Map.Entry<String, Integer>> tableView = new TableView<>();
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        
        // Define columns
        TableColumn<Map.Entry<String, Integer>, String> candidateColumn = 
                new TableColumn<>("Candidate");
        candidateColumn.setCellValueFactory(param -> 
                javafx.beans.binding.Bindings.createStringBinding(
                        () -> param.getValue().getKey()));
        
        TableColumn<Map.Entry<String, Integer>, Number> countColumn = 
                new TableColumn<>("Selection Count");
        countColumn.setCellValueFactory(param -> 
                javafx.beans.binding.Bindings.createIntegerBinding(
                        () -> param.getValue().getValue()));
        countColumn.setStyle("-fx-alignment: CENTER-RIGHT;");
        
        // Add columns to table
        tableView.getColumns().add(candidateColumn);
        tableView.getColumns().add(countColumn);
        
        // Add data to table
        stats.entrySet().forEach(entry -> 
                tableView.getItems().add(entry));
        
        // Sort by count descending
        tableView.getSortOrder().add(countColumn);
        countColumn.setSortType(TableColumn.SortType.DESCENDING);
        tableView.sort();
        
        VBox.setVgrow(tableView, Priority.ALWAYS);
        content.getChildren().add(tableView);
        
        // Add summary stats
        int totalSelections = stats.values().stream().mapToInt(Integer::intValue).sum();
        Label summaryLabel = new Label("Total Selections: " + totalSelections);
        summaryLabel.setPadding(new Insets(10, 0, 0, 0));
        content.getChildren().add(summaryLabel);
        
        tab.setContent(content);
        return tab;
    }
    
    private Tab createSavedListsTab() {
        Tab tab = new Tab("Saved Lists History");
        
        // Load saved sessions
        List<Map<String, Object>> sessions = databaseService.getSavedSessions();
        
        VBox content = new VBox(10);
        content.setPadding(new Insets(10));
        
        if (sessions.isEmpty()) {
            Label noDataLabel = new Label("No saved lists found.");
            noDataLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
            content.getChildren().add(noDataLabel);
        } else {
            Label titleLabel = new Label("Saved Lists History");
            titleLabel.setFont(Font.font("System", FontWeight.BOLD, 16));
            content.getChildren().add(titleLabel);
            
            // Create master VBox to hold all lists
            VBox listsContainer = new VBox(15);
            listsContainer.setPadding(new Insets(5));
            
            // Add each session
            for (Map<String, Object> session : sessions) {
                String sessionName = (String) session.get("name");
                String formattedTime = (String) session.get("formattedTime");
                int candidateCount = (int) session.get("candidateCount");
                String sessionId = (String) session.get("id");
                
                // Session header
                TitledPane sessionPane = new TitledPane();
                sessionPane.setText(sessionName + " - " + formattedTime + " (" + candidateCount + " selections)");
                
                // Session content - just a simple list
                ListView<String> candidatesList = new ListView<>();
                
                // Get candidates for this session
                List<Map<String, Object>> candidates = databaseService.getSessionCandidates(sessionId);
                
                // Add candidates to list with their order
                for (Map<String, Object> candidate : candidates) {
                    String name = (String) candidate.get("name");
                    String list = (String) candidate.get("list");
                    int order = (int) candidate.get("order");
                    
                    candidatesList.getItems().add(String.format("#%d - %s (%s)", order, name, list));
                }
                
                sessionPane.setContent(candidatesList);
                sessionPane.setExpanded(false);
                
                listsContainer.getChildren().add(sessionPane);
            }
            
            // Add lists to a scroll pane
            ScrollPane scrollPane = new ScrollPane(listsContainer);
            scrollPane.setFitToWidth(true);
            scrollPane.setPrefHeight(350);
            
            content.getChildren().add(scrollPane);
            VBox.setVgrow(scrollPane, Priority.ALWAYS);
        }
        
        tab.setContent(content);
        return tab;
    }
    
    private Tab createIdenticalBatchesTab() {
        Tab tab = new Tab("Selection Patterns");
        
        // Load identical batches data
        List<Map<String, Object>> batches = databaseService.getIdenticalSelectionBatches();
        
        VBox content = new VBox(10);
        content.setPadding(new Insets(10));
        
        if (batches.isEmpty()) {
            Label noDataLabel = new Label("No selection patterns found.");
            noDataLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
            content.getChildren().add(noDataLabel);
        } else {
            Label titleLabel = new Label("Identical Selection Patterns");
            titleLabel.setFont(Font.font("System", FontWeight.BOLD, 16));
            content.getChildren().add(titleLabel);
            
            Label descriptionLabel = new Label(
                "This section shows patterns of identical selections - " +
                "lists where exactly the same candidates were selected in the same order."
            );
            descriptionLabel.setWrapText(true);
            content.getChildren().add(descriptionLabel);
            
            // Create container for batches
            VBox batchesContainer = new VBox(15);
            batchesContainer.setPadding(new Insets(5));
            
            // Add each batch
            for (Map<String, Object> batch : batches) {
                String batchName = (String) batch.get("name");
                int count = (int) batch.get("count");
                
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> candidates = (List<Map<String, Object>>) batch.get("candidates");
                
                // Batch header
                TitledPane batchPane = new TitledPane();
                batchPane.setText(batchName + " (Occurred " + count + " times)");
                
                // Create content
                VBox batchContent = new VBox(5);
                batchContent.setPadding(new Insets(10));
                
                // Add a label for stronger emphasis on the count
                Label countLabel = new Label("This exact selection pattern has been chosen " + count + " times.");
                countLabel.setFont(Font.font("System", FontWeight.BOLD, 12));
                countLabel.setPadding(new Insets(0, 0, 10, 0));
                batchContent.getChildren().add(countLabel);
                
                // Add candidates list with their order
                ListView<String> candidatesList = new ListView<>();
                
                for (Map<String, Object> candidate : candidates) {
                    String name = (String) candidate.get("name");
                    String list = (String) candidate.get("list");
                    int order = (int) candidate.get("order");
                    
                    candidatesList.getItems().add(String.format("#%d - %s (%s)", order, name, list));
                }
                
                batchContent.getChildren().add(candidatesList);
                VBox.setVgrow(candidatesList, Priority.ALWAYS);
                
                batchPane.setContent(batchContent);
                batchPane.setExpanded(false);
                
                batchesContainer.getChildren().add(batchPane);
            }
            
            // Add batches to a scroll pane
            ScrollPane scrollPane = new ScrollPane(batchesContainer);
            scrollPane.setFitToWidth(true);
            scrollPane.setPrefHeight(350);
            
            content.getChildren().add(scrollPane);
            VBox.setVgrow(scrollPane, Priority.ALWAYS);
        }
        
        tab.setContent(content);
        return tab;
    }
} 