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
import org.tinylog.Logger;

import java.util.*;

/**
 * @author Armin Reichert
 */
public class PropertyEditor extends BorderPane {

    public final BooleanProperty enabledPy = new SimpleBooleanProperty(true);

    private final TileMapEditor editor;
    private TileMap tileMap;
    private Properties editedProperties;
    private final GridPane grid = new GridPane();
    private final List<ColorPicker> colorPickers = new ArrayList<>();
    private final List<Spinner<Integer>> tileXEditors = new ArrayList<>();
    private final List<Spinner<Integer>> tileYEditors = new ArrayList<>();

    private int numRows;

    public PropertyEditor(String title, TileMapEditor editor) {
        this.editor = editor;

        var lblTitle = new Label(title);
        lblTitle.setFont(Font.font("Sans", FontWeight.BOLD, 14));

        var btnAddEntry = new Button("+");
        btnAddEntry.setStyle("-fx-padding: 0 2 0 2");
        btnAddEntry.setOnAction(e -> {
            editedProperties.put("a_new_property", "42");
            rebuildEditors();
        });
        btnAddEntry.disableProperty().bind(enabledPy.not());
        var header = new HBox(lblTitle, btnAddEntry);
        header.setSpacing(5);

        setTop(header);
        setCenter(grid);
    }

    public void setMap(TileMap tileMap) {
        this.tileMap = tileMap;
        this.editedProperties = tileMap.getProperties();
        rebuildEditors();
    }

    private void rebuildEditors() {
        colorPickers.clear();
        tileXEditors.clear();
        tileYEditors.clear();
        grid.getChildren().clear();
        grid.setHgap(2);
        grid.setVgap(1);
        int row = 0;
        var sortedEntries = editedProperties.entrySet().stream().sorted(Comparator.comparing(Object::toString)).toList();
        for (var entry : sortedEntries) {
            String propertyName = entry.getKey().toString();
            String propertyValue = entry.getValue().toString();
            var nameEditor = new TextField(propertyName);
            int nameColumnMinWidth = 160;
            nameEditor.setMinWidth(nameColumnMinWidth);
            nameEditor.disableProperty().bind(enabledPy.not());
            nameEditor.setOnAction(e -> rebuildEditors());
            grid.add(nameEditor, 0, row);
            if (propertyName.startsWith("color_")) {
                var colorPicker = new ColorPicker();
                colorPicker.setUserData(propertyName);
                colorPicker.setValue(TileMapUtil.parseColor(propertyValue));
                colorPicker.setOnAction(e -> saveEditedProperty(nameEditor, TileMapUtil.formatColor(colorPicker.getValue())));
                colorPicker.disableProperty().bind(enabledPy.not());
                nameEditor.setOnAction(e -> saveEditedProperty(nameEditor, TileMapUtil.formatColor(colorPicker.getValue())));
                colorPickers.add(colorPicker);
                grid.add(colorPicker, 1, row);
            } else if (propertyName.startsWith("pos_")) {
                var spinnerX  = new Spinner<Integer>(0, tileMap.numCols() - 1, 0);
                spinnerX.setUserData(propertyName);
                spinnerX.disableProperty().bind(enabledPy.not());

                var spinnerY  = new Spinner<Integer>(0, tileMap.numRows() - 1, 0);
                spinnerY.setUserData(propertyName);
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
                spinnerX.setMaxWidth(60);
                spinnerY.setMaxWidth(60);
                tileXEditors.add(spinnerX);
                tileYEditors.add(spinnerY);
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
        Logger.info("Editors rebuild");
    }

    public void updateEditorValues() {
        for (var colorPicker : colorPickers) {
            String propertyName = (String) colorPicker.getUserData();
            String propertyValue = (String) editedProperties.get(propertyName);
            colorPicker.setValue(TileMapUtil.parseColor(propertyValue));
        }
        for (int i = 0; i < tileXEditors.size(); ++i) {
            String propertyName = (String) tileXEditors.get(i).getUserData();
            String propertyValue = (String) editedProperties.get(propertyName);
            Vector2i tile = TileMap.parseVector2i(propertyValue);
            if (tile != null) {
                tileXEditors.get(i).getValueFactory().setValue(tile.x());
                tileYEditors.get(i).getValueFactory().setValue(tile.y());
            }
        }
        Logger.info("Editor values updated");
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
    }
}
