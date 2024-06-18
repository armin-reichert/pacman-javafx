/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.mapeditor;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.control.Button;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.Comparator;
import java.util.HashSet;
import java.util.Properties;

/**
 * @author Armin Reichert
 */
public class PropertyEditor extends BorderPane {

    public final BooleanProperty enabledPy = new SimpleBooleanProperty(true);

    private final TileMapEditor editor;
    private Properties editedProperties;
    private final GridPane grid = new GridPane();
    private int numRows;

    public PropertyEditor(String title, TileMapEditor editor) {
        this.editor = editor;

        var lblTitle = new Label(title);
        lblTitle.setFont(Font.font("Sans", FontWeight.BOLD, 14));

        var btnAddEntry = new Button("+");
        btnAddEntry.setStyle("-fx-padding: 0 2 0 2");
        btnAddEntry.setOnAction(e -> {
            editedProperties.put("a_new_property", "42");
            updateEditors();
        });
        btnAddEntry.disableProperty().bind(enabledPy.not());
        var header = new HBox(lblTitle, btnAddEntry);
        header.setSpacing(5);

        setTop(header);
        setCenter(grid);
    }

    public void edit(Properties properties) {
        this.editedProperties = properties;
        updateEditors();
    }

    public void updateEditors() {
        grid.getChildren().clear();
        grid.setHgap(2);
        grid.setVgap(1);
        int row = 0;
        var sortedEntries = editedProperties.entrySet().stream().sorted(Comparator.comparing(Object::toString)).toList();
        for (var entry : sortedEntries) {
            TextField nameEditor = new TextField(String.valueOf(entry.getKey()));
            int nameColumnMinWidth = 100;
            nameEditor.setMinWidth(nameColumnMinWidth);
            nameEditor.disableProperty().bind(enabledPy.not());
            grid.add(nameEditor, 0, row);
            if (entry.getKey().toString().endsWith("_color") || entry.getKey().toString().startsWith("color_")) {
                var colorPicker = new ColorPicker();
                colorPicker.setValue(TileMapUtil.parseColor(String.valueOf(entry.getValue())));
                colorPicker.setOnAction(e -> editProperty(nameEditor, TileMapUtil.formatColor(colorPicker.getValue())));
                colorPicker.disableProperty().bind(enabledPy.not());
                nameEditor.setOnAction(e -> editProperty(nameEditor, TileMapUtil.formatColor(colorPicker.getValue())));
                grid.add(colorPicker, 1, row);
            } else {
                var inputField = new TextField();
                inputField.setText(String.valueOf(entry.getValue()));
                inputField.setOnAction(e -> editProperty(nameEditor, inputField.getText()));
                inputField.disableProperty().bind(enabledPy.not());
                nameEditor.setOnAction(e -> editProperty(nameEditor, inputField.getText()));
                grid.add(inputField, 1, row);
            }
            ++row;
        }
        numRows = row;
    }

    private void editProperty(TextField nameEditor, Object value) {
        String name = nameEditor.getText().trim();
        if (!name.isBlank()) {
            editedProperties.put(name, value);
        }
        var propertyNames = new HashSet<>();
        for (int row = 0; row < numRows; ++row) {
            var nameField = (TextField) grid.getChildren().get(2 * row);
            propertyNames.add(nameField.getText());
        }
        for (var propertyName : editedProperties.stringPropertyNames()) {
            if (!propertyNames.contains(propertyName)) {
                editedProperties.remove(propertyName);
            }
        }
        editor.markMapEdited();
        updateEditors();
    }
}
