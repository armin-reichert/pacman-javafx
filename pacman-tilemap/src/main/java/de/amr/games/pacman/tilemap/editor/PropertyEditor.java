package de.amr.games.pacman.tilemap.editor;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.HashSet;
import java.util.Properties;

public class PropertyEditor extends BorderPane {

    private int nameColumnMinWidth = 100;

    private Properties editedProperties;
    private final GridPane grid = new GridPane();
    private int numRows;

    public PropertyEditor(String title) {
        var lblTitle = new Label(title);
        lblTitle.setFont(Font.font("Sans", FontWeight.BOLD, 14));

        var btnAddEntry = new Button("+");
        btnAddEntry.setStyle("-fx-padding: 0 2 0 2");
        btnAddEntry.setOnAction(e -> {
            editedProperties.put("New Property", "");
            updateTable();
        });
        var header = new HBox(lblTitle, btnAddEntry);
        header.setSpacing(5);

        setTop(header);
        setCenter(grid);
    }

    public Properties getEditedProperties() {
        return editedProperties;
    }

    public void setEditedProperties(Properties editedProperties) {
        this.editedProperties = editedProperties;
        updateTable();
    }

    public void updateTable() {
        grid.getChildren().clear();
        grid.setHgap(2);
        grid.setVgap(1);
        int row = 0;
        for (var entry : editedProperties.entrySet()) {
            TextField nameEditor = new TextField(String.valueOf(entry.getKey()));
            TextField valueEditor = new TextField(String.valueOf(entry.getValue()));
            nameEditor.setMinWidth(nameColumnMinWidth);
            nameEditor.setOnAction(e -> saveEditedEntry(nameEditor, valueEditor));
            valueEditor.setOnAction(e -> saveEditedEntry(nameEditor, valueEditor));
            grid.add(nameEditor, 0, row);
            grid.add(valueEditor, 1, row);
            ++row;
        }
        numRows = row;
    }

    private void saveEditedEntry(TextField nameEditor, TextField valueEditor) {
        if (!nameEditor.getText().trim().isBlank()) {
            editedProperties.put(nameEditor.getText().trim(), valueEditor.getText());
        }
        var names = new HashSet<>();
        for (int row = 0; row < numRows; ++row) {
            TextField ne = (TextField) grid.getChildren().get(2*row);
            names.add(ne.getText());
        }
        for (var key : editedProperties.keySet()) {
            if (!names.contains(key)) {
                editedProperties.remove(key);
            }
        }
        updateTable();
        editedProperties.list(System.out);
    }
}
