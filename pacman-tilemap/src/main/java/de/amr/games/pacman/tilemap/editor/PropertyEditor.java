package de.amr.games.pacman.tilemap.editor;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.Properties;

public class PropertyEditor extends BorderPane {

    private Properties editedProperties;
    private GridPane grid = new GridPane();
    private int nameColumnMinWidth = 100;
    private String title;

    public PropertyEditor(String title) {
        this.title = title;
        var lblTitle = new Label(title);
        lblTitle.setFont(Font.font("Sans", FontWeight.BOLD, 14));

        var btnAddEntry = new Button("+");
        btnAddEntry.setStyle("-fx-padding: 0 2 0 2");
        btnAddEntry.setOnAction(e -> {
            editedProperties.put("New Property", "");
            updateUI();
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
        updateUI();
    }

    public void updateUI() {
        grid.getChildren().clear();
        grid.setHgap(2);
        grid.setVgap(1);
        int row = 0;
        for (var entry : editedProperties.entrySet()) {
            TextField nameEditor = new TextField(String.valueOf(entry.getKey()));
            TextField valueEditor = new TextField(String.valueOf(entry.getValue()));
            grid.add(nameEditor, 0, row);
            grid.add(valueEditor, 1, row);
            nameEditor.setMinWidth(nameColumnMinWidth);
            nameEditor.setOnAction(e -> editedProperties.put(nameEditor.getText(), valueEditor.getText()));
            valueEditor.setOnAction(e -> editedProperties.put(nameEditor.getText(), valueEditor.getText()));
            ++row;
        }
    }

    private void commit(String name, String value) {

    }

}
