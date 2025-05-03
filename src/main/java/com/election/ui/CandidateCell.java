package com.election.ui;

import com.election.model.Candidate;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class CandidateCell extends ListCell<Candidate> {
    
    private HBox content;
    private Label nameLabel;
    private StackPane selectionIndicator;
    private Label selectionOrderLabel;
    private Circle circle;
    
    public CandidateCell() {
        // Create UI components
        nameLabel = new Label();
        nameLabel.setFont(Font.font("System", FontWeight.NORMAL, 14));
        
        selectionOrderLabel = new Label();
        selectionOrderLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
        selectionOrderLabel.setTextFill(Color.WHITE);
        selectionOrderLabel.setAlignment(Pos.CENTER);
        
        circle = new Circle(15);
        circle.setFill(Color.GRAY);
        
        selectionIndicator = new StackPane();
        selectionIndicator.getChildren().addAll(circle, selectionOrderLabel);
        selectionIndicator.setAlignment(Pos.CENTER);
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        content = new HBox(10);
        content.setPadding(new Insets(10));
        content.setAlignment(Pos.CENTER_LEFT);
        content.getChildren().addAll(nameLabel, spacer, selectionIndicator);
    }
    
    @Override
    protected void updateItem(Candidate candidate, boolean empty) {
        super.updateItem(candidate, empty);
        
        if (empty || candidate == null) {
            setGraphic(null);
        } else {
            nameLabel.setText(candidate.getName());
            
            int selectionOrder = candidate.getSelectionOrder();
            if (selectionOrder > 0) {
                circle.setFill(Color.DODGERBLUE);
                selectionOrderLabel.setText(String.valueOf(selectionOrder));
            } else {
                circle.setFill(Color.LIGHTGRAY);
                selectionOrderLabel.setText("");
            }
            
            setGraphic(content);
        }
    }
} 