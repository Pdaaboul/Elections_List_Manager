package com.election;

import com.election.model.Candidate;
import com.election.service.ConfigService;
import com.election.service.DatabaseService;
import com.election.service.LoggingService;
import com.election.service.PdfService;
import com.election.ui.CandidateCell;
import com.election.ui.StatsDialog;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class ElectionApp extends Application {
    
    private ConfigService configService;
    private DatabaseService databaseService;
    private PdfService pdfService;
    private LoggingService loggingService;
    
    private ObservableList<Candidate> listACandidates;
    private ObservableList<Candidate> listBCandidates;
    private int currentSelectionCount = 0;
    private int maxSelections = 9;
    
    // UI elements we need to reference
    private Label selectionCountLabel;
    private ListView<Candidate> listAView;
    private ListView<Candidate> listBView;
    
    @Override
    public void start(Stage primaryStage) {
        // Initialize services
        configService = ConfigService.getInstance();
        databaseService = DatabaseService.getInstance();
        pdfService = PdfService.getInstance();
        loggingService = LoggingService.getInstance();
        
        loggingService.log("Application started");
        
        // Load candidate data
        listACandidates = FXCollections.observableArrayList(configService.loadCandidatesFromList("listA"));
        listBCandidates = FXCollections.observableArrayList(configService.loadCandidatesFromList("listB"));
        
        // Create UI
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(10));
        
        // Header
        Label headerLabel = new Label("Internal Election Voting System");
        headerLabel.setFont(Font.font("System", FontWeight.BOLD, 24));
        headerLabel.setPadding(new Insets(0, 0, 10, 0));
        
        Label instructionsLabel = new Label("Select up to 9 candidates in your preferred order:");
        instructionsLabel.setFont(Font.font("System", FontWeight.NORMAL, 16));
        
        VBox headerBox = new VBox(10, headerLabel, instructionsLabel);
        headerBox.setAlignment(Pos.CENTER);
        root.setTop(headerBox);
        
        // Candidate lists
        listAView = createCandidateListView(listACandidates, "List A");
        listBView = createCandidateListView(listBCandidates, "List B");
        
        // Set up grid for the lists
        GridPane listsGrid = new GridPane();
        listsGrid.setHgap(20);
        listsGrid.setPadding(new Insets(10));
        
        // List A title and container
        Label listALabel = new Label("List A");
        listALabel.setFont(Font.font("System", FontWeight.BOLD, 18));
        
        VBox listABox = new VBox(10, listALabel, listAView);
        listABox.setAlignment(Pos.TOP_CENTER);
        GridPane.setHgrow(listABox, Priority.ALWAYS);
        GridPane.setVgrow(listABox, Priority.ALWAYS);
        
        // List B title and container
        Label listBLabel = new Label("List B");
        listBLabel.setFont(Font.font("System", FontWeight.BOLD, 18));
        
        VBox listBBox = new VBox(10, listBLabel, listBView);
        listBBox.setAlignment(Pos.TOP_CENTER);
        GridPane.setHgrow(listBBox, Priority.ALWAYS);
        GridPane.setVgrow(listBBox, Priority.ALWAYS);
        
        // Add list containers to grid
        listsGrid.add(listABox, 0, 0);
        listsGrid.add(listBBox, 1, 0);
        
        // Selection counter
        selectionCountLabel = new Label("Selections: 0 / 9");
        selectionCountLabel.setFont(Font.font("System", FontWeight.BOLD, 16));
        
        // Set center content
        VBox centerContent = new VBox(10, listsGrid, selectionCountLabel);
        centerContent.setAlignment(Pos.CENTER);
        root.setCenter(centerContent);
        
        // Bottom action buttons
        Button saveButton = new Button("Save Selection");
        saveButton.setStyle("-fx-background-color: #1E88E5; -fx-text-fill: white;");
        saveButton.setPrefWidth(150);
        saveButton.setOnAction(e -> saveSelection(primaryStage));
        
        Button statsButton = new Button("View Statistics");
        statsButton.setPrefWidth(150);
        statsButton.setOnAction(e -> showStats(primaryStage));
        
        Button resetButton = new Button("Reset");
        resetButton.setPrefWidth(150);
        resetButton.setOnAction(e -> resetSelection());
        
        HBox bottomBox = new HBox(20, saveButton, statsButton, resetButton);
        bottomBox.setAlignment(Pos.CENTER);
        bottomBox.setPadding(new Insets(10, 0, 0, 0));
        root.setBottom(bottomBox);
        
        // Set up scene
        Scene scene = new Scene(root, 800, 600);
        primaryStage.setTitle("Election Application");
        primaryStage.setScene(scene);
        primaryStage.show();
        
        // Ensure output directory exists
        File outputDir = new File("output");
        if (!outputDir.exists()) {
            outputDir.mkdir();
        }
    }
    
    private ListView<Candidate> createCandidateListView(ObservableList<Candidate> candidates, String listName) {
        ListView<Candidate> listView = new ListView<>(candidates);
        listView.setCellFactory(param -> new CandidateCell());
        listView.setPrefHeight(400);
        
        // Handle selection
        listView.setOnMouseClicked(e -> {
            Candidate selectedCandidate = listView.getSelectionModel().getSelectedItem();
            if (selectedCandidate != null) {
                handleCandidateSelection(selectedCandidate);
                listView.refresh(); // Refresh the list view to update the cell appearance
            }
        });
        
        return listView;
    }
    
    private void handleCandidateSelection(Candidate candidate) {
        int currentOrder = candidate.getSelectionOrder();
        
        if (currentOrder > 0) {
            // Deselect candidate
            loggingService.logDeselection(candidate.getName(), candidate.getList());
            
            // Update other candidates' order
            updateCandidateOrders(currentOrder);
            
            // Reset this candidate's order
            candidate.setSelectionOrder(0);
            currentSelectionCount--;
        } else {
            // Check if we've reached the maximum selections
            if (currentSelectionCount >= maxSelections) {
                showAlert("Maximum Selections Reached", 
                         "You cannot select more than " + maxSelections + " candidates.");
                return;
            }
            
            // Select candidate
            currentSelectionCount++;
            candidate.setSelectionOrder(currentSelectionCount);
            
            loggingService.logSelection(candidate.getName(), candidate.getList(), currentSelectionCount);
        }
        
        // Update selection counter in UI
        updateSelectionCounter();
    }
    
    private void updateCandidateOrders(int removedOrder) {
        // Update list A
        for (Candidate c : listACandidates) {
            if (c.getSelectionOrder() > removedOrder) {
                c.setSelectionOrder(c.getSelectionOrder() - 1);
            }
        }
        
        // Update list B
        for (Candidate c : listBCandidates) {
            if (c.getSelectionOrder() > removedOrder) {
                c.setSelectionOrder(c.getSelectionOrder() - 1);
            }
        }
    }
    
    private void updateSelectionCounter() {
        selectionCountLabel.setText("Selections: " + currentSelectionCount + " / " + maxSelections);
    }
    
    private void resetSelection() {
        // Reset all candidates
        for (Candidate c : listACandidates) {
            c.setSelectionOrder(0);
        }
        
        for (Candidate c : listBCandidates) {
            c.setSelectionOrder(0);
        }
        
        currentSelectionCount = 0;
        updateSelectionCounter();
        
        // Refresh list views
        listAView.refresh();
        listBView.refresh();
        
        loggingService.log("Selection reset");
    }
    
    private void saveSelection(Stage owner) {
        if (currentSelectionCount == 0) {
            showAlert("No Selections", "Please select at least one candidate before saving.");
            return;
        }
        
        // Get all selected candidates
        List<Candidate> selectedCandidates = new ArrayList<>();
        
        for (Candidate c : listACandidates) {
            if (c.getSelectionOrder() > 0) {
                selectedCandidates.add(c);
            }
        }
        
        for (Candidate c : listBCandidates) {
            if (c.getSelectionOrder() > 0) {
                selectedCandidates.add(c);
            }
        }
        
        // Sort by selection order
        selectedCandidates.sort(Comparator.comparing(Candidate::getSelectionOrder));
        
        // Save to database
        databaseService.saveSelection(selectedCandidates);
        
        // Generate PDF
        String pdfPath = pdfService.generateSelectionReport(selectedCandidates);
        
        // Log
        loggingService.logSave(selectedCandidates.size(), pdfPath);
        
        // Show success message
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Save Successful");
        alert.setHeaderText("Selection Saved Successfully");
        alert.setContentText("Your selection has been saved to the database and a PDF report has been generated at: " + pdfPath);
        alert.initOwner(owner);
        alert.showAndWait();
    }
    
    private void showStats(Stage owner) {
        StatsDialog statsDialog = new StatsDialog(owner);
        statsDialog.showAndWait();
    }
    
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}