/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.mapeditor;

import de.amr.games.pacman.lib.Vector2i;
import de.amr.games.pacman.lib.tilemap.TileMap;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import org.tinylog.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.regex.Pattern;

import static de.amr.games.pacman.lib.tilemap.TileMap.formatTile;
import static de.amr.games.pacman.lib.tilemap.TileMap.parseVector2i;
import static de.amr.games.pacman.mapeditor.TileMapUtil.formatColor;
import static de.amr.games.pacman.mapeditor.TileMapUtil.parseColor;
import static java.util.Comparator.comparing;

/**
 * @author Armin Reichert
 */
public class PropertyEditorPane extends BorderPane {

    private static final int NAME_COLUMN_MIN_WIDTH = 180;
    private static final String DELETE_MARKER = ":del";

    private static boolean isValidPropertyName(String s) {
        return Pattern.matches("[a-zA-Z]([a-zA-Z0-9_])*", s);
    }

    private static boolean matchesDeletePattern(String s) {
        if (s == null || !s.endsWith(DELETE_MARKER)) {
            return false;
        }
        s = s.substring(0, s.length() - DELETE_MARKER.length());
        return isValidPropertyName(s);
    }

    private static String removeDeleteMarker(String s) {
        if (matchesDeletePattern(s)) {
            return s.substring(0, s.length() - DELETE_MARKER.length());
        }
        return s;
    }

    public final BooleanProperty enabledPy = new SimpleBooleanProperty(true);

    private final TileMapEditor editor;
    private final List<PropertyEditor> editors = new ArrayList<>();
    private final GridPane grid = new GridPane();
    private TileMap tileMap;
    private Properties editedProperties;

    private abstract class PropertyEditor {

        String propertyName;
        final TextField nameEditor;

        PropertyEditor(String propertyName) {
            this.propertyName = propertyName;
            nameEditor = new TextField(propertyName);
            nameEditor.setMinWidth(NAME_COLUMN_MIN_WIDTH);
            nameEditor.disableProperty().bind(enabledPy.not());
            nameEditor.setOnAction(e -> onPropertyNameEdited());
        }

        void onPropertyNameEdited() {
            String editedName = nameEditor.getText().trim();
            if (editedName.isBlank() || propertyName.equals(editedName)) {
                nameEditor.setText(propertyName);
                return;
            }
            if (matchesDeletePattern(editedName)) {
                String deletePropertyName = removeDeleteMarker(editedName);
                if (deletePropertyName.equals(propertyName)) {
                    editedProperties.remove(propertyName);
                    Logger.info("Property {} deleted", propertyName);
                    editor.markMapEdited();
                    rebuildEditors(); //TODO check
                } else {
                    nameEditor.setText(propertyName);
                    Logger.error("Cannot delete other property {} from here", deletePropertyName);
                }
                return;
            }
            if (!isValidPropertyName(editedName)) {
                nameEditor.setText(propertyName);
                Logger.error("Property name {} is invalid", editedName); //TODO UI
                return;
            }
            if (editedProperties.get(editedName) != null) {
                Logger.error("Property name already used"); //TODO UI
                nameEditor.setText(propertyName);
                return;
            }
            propertyName = editedName;
            editedProperties.put(propertyName, formattedPropertyValue());
            editor.markMapEdited();
        }

        void storePropertyValue() {
            editedProperties.put(propertyName, formattedPropertyValue());
            editor.markMapEdited();
        }

        abstract String formattedPropertyValue();

        abstract void updateEditorFromProperty();

        abstract Node valueEditor();
    }

    private class TextPropertyEditor extends PropertyEditor {

        final TextField textEditor;

        TextPropertyEditor(String propertyName, String propertyValue) {
            super(propertyName);
            textEditor = new TextField();
            textEditor.setText(propertyValue);
            textEditor.disableProperty().bind(enabledPy.not());
            textEditor.setOnAction(e -> storePropertyValue());
        }

        @Override
        void updateEditorFromProperty() {
            textEditor.setText(editedProperties.getProperty(propertyName));
        }

        @Override
        Node valueEditor() {
            return textEditor;
        }

        @Override
        String formattedPropertyValue() {
            return textEditor.getText().strip();
        }
    }

    private class ColorPropertyEditor extends PropertyEditor {
        final ColorPicker colorPicker;

        ColorPropertyEditor(String propertyName, String propertyValue) {
            super(propertyName);
            colorPicker = new ColorPicker();
            colorPicker.setUserData(propertyName);
            colorPicker.setValue(parseColor(propertyValue));
            colorPicker.disableProperty().bind(enabledPy.not());
            colorPicker.setOnAction(e -> storePropertyValue());
        }

        @Override
        void updateEditorFromProperty() {
            String propertyValue = editedProperties.getProperty(propertyName);
            colorPicker.setValue(parseColor(propertyValue));
        }

        @Override
        protected Node valueEditor() {
            return colorPicker;
        }

        @Override
        protected String formattedPropertyValue() {
            return formatColor(colorPicker.getValue());
        }
    }

    private class TilePropertyEditor extends PropertyEditor {
        final Spinner<Integer> spinnerX;
        final Spinner<Integer> spinnerY;
        final HBox valueEditorPane;

        TilePropertyEditor(String propertyName, String propertyValue) {
            super(propertyName);
            Vector2i tile = parseVector2i(propertyValue);

            spinnerX = new Spinner<>(0, tileMap.numCols() - 1, 0);
            spinnerX.setMaxWidth(60);
            spinnerX.disableProperty().bind(enabledPy.not());
            if (tile != null) {
                spinnerX.getValueFactory().setValue(tile.x());
            }

            spinnerY = new Spinner<>(0, tileMap.numRows() - 1, 0);
            spinnerY.setMaxWidth(60);
            spinnerY.disableProperty().bind(enabledPy.not());
            if (tile != null) {
                spinnerY.getValueFactory().setValue(tile.y());
            }

            spinnerX.valueProperty().addListener((py,ov,nv) -> storePropertyValue());
            spinnerY.valueProperty().addListener((py,ov,nv) -> storePropertyValue());

            valueEditorPane = new HBox(spinnerX, spinnerY);
        }

        @Override
        protected void updateEditorFromProperty() {
            String propertyValue = editedProperties.getProperty(propertyName);
            Vector2i tile = parseVector2i(propertyValue);
            if (tile != null) {
                spinnerX.getValueFactory().setValue(tile.x());
                spinnerY.getValueFactory().setValue(tile.y());
            }
        }

        @Override
        protected String formattedPropertyValue() {
            return formatTile(new Vector2i(spinnerX.getValue(), spinnerY.getValue()));
        }

        @Override
        protected Node valueEditor() {
            return valueEditorPane;
        }
    }

    public PropertyEditorPane(TileMapEditor editor) {
        this.editor = editor;

        var btnAddColorEntry = new Button("Color");
        btnAddColorEntry.setOnAction(e -> {
            editedProperties.put("color_RENAME_ME", "green");
            rebuildEditors();
        });
        btnAddColorEntry.disableProperty().bind(enabledPy.not());

        var btnAddPosEntry = new Button("Position");
        btnAddPosEntry.setOnAction(e -> {
            editedProperties.put("pos_RENAME_ME", "(0,0)");
            rebuildEditors();
        });
        btnAddPosEntry.disableProperty().bind(enabledPy.not());

        var btnAddGenericEntry = new Button("Text");
        btnAddGenericEntry.setOnAction(e -> {
            editedProperties.put("RENAME_ME", "any text");
            rebuildEditors();
        });
        btnAddGenericEntry.disableProperty().bind(enabledPy.not());

        var buttonBar = new HBox(new Label("New"), btnAddColorEntry, btnAddPosEntry, btnAddGenericEntry);
        buttonBar.setSpacing(3);
        buttonBar.setAlignment(Pos.CENTER_LEFT);

        grid.setHgap(2);
        grid.setVgap(1);

        setTop(buttonBar);
        setCenter(grid);
    }

    public void setMap(TileMap tileMap) {
        this.tileMap = tileMap;
        this.editedProperties = tileMap.getProperties();
        rebuildEditors();
    }

    public void updateEditorValues() {
        for (var editor : editors) {
            editor.updateEditorFromProperty();
        }
    }

    private void rebuildEditors() {
        Logger.info("Rebuild editors");
        editors.clear();

        var sortedProperties = editedProperties.entrySet().stream()
            .sorted(comparing(entry -> entry.getKey().toString()))
            .toList();

        grid.getChildren().clear();
        int row = 0;
        for (var property : sortedProperties) {
            String propertyName = property.getKey().toString();
            String propertyValue = property.getValue().toString();
            PropertyEditor editor;
            if (propertyName.startsWith("color_")) {
                editor = new ColorPropertyEditor(propertyName, propertyValue);
            } else if (propertyName.startsWith("pos_")) {
                editor = new TilePropertyEditor(propertyName, propertyValue);
            } else {
                editor = new TextPropertyEditor(propertyName, propertyValue);
            }
            editors.add(editor);
            grid.add(editor.nameEditor, 0, row);
            grid.add(editor.valueEditor(), 1, row);
            ++row;
        }
    }
}