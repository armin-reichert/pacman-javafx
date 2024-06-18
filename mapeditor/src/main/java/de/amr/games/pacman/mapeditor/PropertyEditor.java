/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.mapeditor;

import de.amr.games.pacman.lib.Vector2i;
import de.amr.games.pacman.lib.tilemap.TileMap;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.scene.control.*;
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
    private final TileMap tileMap;
    private Properties editedProperties;
    private final GridPane grid = new GridPane();
    private int numRows;

    public PropertyEditor(String title, TileMapEditor editor, TileMap tileMap) {
        this.editor = editor;
        this.tileMap = tileMap;

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
            String propertyName = entry.getKey().toString();
            String propertyValue = entry.getValue().toString();
            TextField nameEditor = new TextField(propertyName);
            int nameColumnMinWidth = 150;
            nameEditor.setMinWidth(nameColumnMinWidth);
            nameEditor.disableProperty().bind(enabledPy.not());
            grid.add(nameEditor, 0, row);
            if (propertyName.startsWith("color_")) {
                var colorPicker = new ColorPicker();
                colorPicker.setValue(TileMapUtil.parseColor(propertyValue));
                colorPicker.setOnAction(e -> saveEditedProperty(nameEditor, TileMapUtil.formatColor(colorPicker.getValue())));
                colorPicker.disableProperty().bind(enabledPy.not());
                nameEditor.setOnAction(e -> saveEditedProperty(nameEditor, TileMapUtil.formatColor(colorPicker.getValue())));
                grid.add(colorPicker, 1, row);
            } else if (propertyName.startsWith("pos_")) {
                var spinnerX  = new Spinner<Integer>(0, tileMap.numCols() - 1, 0);
                spinnerX.disableProperty().bind(enabledPy.not());
                var spinnerY  = new Spinner<Integer>(0, tileMap.numRows() - 1, 0);
                spinnerY.disableProperty().bind(enabledPy.not());
                HBox hbox = new HBox(spinnerX, spinnerY);
                Vector2i tile = TileMap.parseVector2i(propertyValue);
                if (tile != null) {
                    spinnerX.getValueFactory().setValue(tile.x());
                    spinnerY.getValueFactory().setValue(tile.y());
                }
                ChangeListener<Number> saveSpinnerValue = (py,ov,nv) ->
                    saveEditedProperty(nameEditor, TileMap.formatTile(new Vector2i(spinnerX.getValue(), spinnerY.getValue())));
                spinnerX.valueProperty().addListener(saveSpinnerValue);
                spinnerY.valueProperty().addListener(saveSpinnerValue);
                grid.add(hbox, 1, row);
            }
            else {
                var inputField = new TextField();
                inputField.setText(propertyValue);
                inputField.setOnAction(e -> saveEditedProperty(nameEditor, inputField.getText()));
                inputField.disableProperty().bind(enabledPy.not());
                nameEditor.setOnAction(e -> saveEditedProperty(nameEditor, inputField.getText()));
                grid.add(inputField, 1, row);
            }
            ++row;
        }
        numRows = row;
    }

    private void saveEditedProperty(TextField nameEditor, Object value) {
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
