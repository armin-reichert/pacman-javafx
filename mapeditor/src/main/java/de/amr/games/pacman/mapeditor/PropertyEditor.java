package de.amr.games.pacman.mapeditor;

import javafx.beans.property.*;
import javafx.scene.control.Button;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import org.tinylog.Logger;

import java.util.Comparator;
import java.util.HashSet;
import java.util.Properties;

public class PropertyEditor extends BorderPane {

    public static Color parseColor(String colorText) {
        try {
            return Color.web(colorText);
        } catch (Exception x) {
            Logger.error(x);
            return Color.WHITE;
        }
    }

    public static String formatColor(Color color) {
        return String.format("rgb(%d,%d,%d)", (int)(color.getRed()*255), (int)(color.getGreen()*255), (int)(color.getBlue()*255));
    }

    public final BooleanProperty enabledPy = new SimpleBooleanProperty(true);

    private Properties editedProperties;
    private final GridPane grid = new GridPane();
    private int numRows;

    public PropertyEditor(String title) {
        var lblTitle = new Label(title);
        lblTitle.setFont(Font.font("Sans", FontWeight.BOLD, 14));

        var btnAddEntry = new Button("+");
        btnAddEntry.setStyle("-fx-padding: 0 2 0 2");
        btnAddEntry.setOnAction(e -> {
            editedProperties.put("aaa_new_property", "first_value");
            updateTable();
        });
        var header = new HBox(lblTitle, btnAddEntry);
        header.setSpacing(5);

        setTop(header);
        setCenter(grid);
    }

    public void edit(Properties editedProperties) {
        this.editedProperties = editedProperties;
        updateTable();
    }

    private void updateTable() {
        grid.getChildren().clear();
        grid.setHgap(2);
        grid.setVgap(1);
        int row = 0;
        var sortedEntries = editedProperties.entrySet().stream().sorted(Comparator.comparing(Object::toString)).toList();
        for (var entry : sortedEntries) {
            TextField nameEditor = new TextField(String.valueOf(entry.getKey()));
            int nameColumnMinWidth = 100;
            nameEditor.setMinWidth(nameColumnMinWidth);
            grid.add(nameEditor, 0, row);
            if (entry.getKey().toString().endsWith("_color")) {
                var colorPicker = new ColorPicker();
                colorPicker.setValue(parseColor(String.valueOf(entry.getValue())));
                colorPicker.setOnAction(e -> {
                    saveEditedEntry(nameEditor, formatColor(colorPicker.getValue()));
                });
                colorPicker.disableProperty().bind(enabledPy.not());
                nameEditor.setOnAction(e -> saveEditedEntry(nameEditor, formatColor(colorPicker.getValue())));
                grid.add(colorPicker, 1, row);
            } else {
                var inputField = new TextField();
                inputField.setText(String.valueOf(entry.getValue()));
                inputField.setOnAction(e -> saveEditedEntry(nameEditor, inputField.getText()));
                inputField.disableProperty().bind(enabledPy.not());
                nameEditor.setOnAction(e -> saveEditedEntry(nameEditor, inputField.getText()));
                grid.add(inputField, 1, row);
            }
            ++row;
        }
        numRows = row;
    }

    private void saveEditedEntry(TextField nameEditor, Object value) {
        String entryKey = nameEditor.getText().trim();
        Logger.info("Save entry {}: {}", entryKey, value);
        if (!entryKey.isBlank()) {
            editedProperties.put(entryKey, value);
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
