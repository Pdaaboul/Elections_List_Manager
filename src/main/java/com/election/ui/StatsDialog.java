package com.election.ui;

import com.election.service.DatabaseService;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Window;

import java.util.Map;

public class StatsDialog extends Dialog<Void> {
    
    public StatsDialog(Window owner) {
        // Configure dialog
        setTitle("Election Statistics");
        setHeaderText("Candidate Selection Statistics");
        initModality(Modality.APPLICATION_MODAL);
        initOwner(owner);
        
        // Set dialog size
        setResizable(true);
        getDialogPane().setPrefWidth(500);
        getDialogPane().setPrefHeight(400);
        
        // Load statistics data
        Map<String, Integer> stats = DatabaseService.getInstance().getCandidateStats();
        
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
        
        // Add close button
        ButtonType closeButton = new ButtonType("Close", ButtonBar.ButtonData.OK_DONE);
        getDialogPane().getButtonTypes().add(closeButton);
        
        // Set content
        getDialogPane().setContent(content);
    }
} 