/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.mapeditor;

import de.amr.games.pacman.lib.Vector2i;
import de.amr.games.pacman.lib.tilemap.TileMap;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import org.tinylog.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

import static de.amr.games.pacman.lib.tilemap.TileMap.formatTile;
import static de.amr.games.pacman.lib.tilemap.TileMap.parseVector2i;
import static de.amr.games.pacman.mapeditor.TileMapUtil.formatColor;
import static de.amr.games.pacman.mapeditor.TileMapUtil.parseColor;

/**
 * @author Armin Reichert
 */
public class PropertyEditorPane extends BorderPane {

    private static final int NAME_COLUMN_MIN_WIDTH = 180;
    private static final Pattern PATTERN_PROPERTY_NAME = Pattern.compile("[a-zA-Z]([a-zA-Z0-9_])*");

    private static final String DELETE_COMMAND = ":del";

    private static boolean isValidPropertyName(String s) {
        return PATTERN_PROPERTY_NAME.matcher(s).matches();
    }

    // assumes s ends with suffix
    private static String chop(String s, String suffix) {
        return s.substring(0, s.length() - suffix.length());
    }

    private static boolean matchesDeleteCommand(String s) {
        return s != null && s.endsWith(DELETE_COMMAND) && isValidPropertyName(chop(s, DELETE_COMMAND));
    }

    private static String chopDeleteCommand(String s) {
        return matchesDeleteCommand(s) ? chop(s, DELETE_COMMAND) : s;
    }

    public final BooleanProperty enabledPy = new SimpleBooleanProperty(this, "enabled", true);

    private final TileMapEditor tileMapEditor;
    private final GridPane grid = new GridPane();
    private final List<PropertyEditor> propertyEditors = new ArrayList<>();
    private TileMap tileMap;

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
            if (matchesDeleteCommand(editedName)) {
                String deletePropertyName = chopDeleteCommand(editedName);
                if (deletePropertyName.equals(propertyName)) {
                    tileMap.getProperties().remove(propertyName);
                    Logger.debug("Property {} deleted", propertyName);
                    tileMapEditor.markMapEdited();
                    rebuildPropertyEditors(); //TODO check
                    tileMapEditor.showMessage("Property %s deleted".formatted(propertyName), 1, MessageType.INFO);
                } else {
                    nameEditor.setText(propertyName);
                    tileMapEditor.showMessage("Cannot delete other property %s".formatted(deletePropertyName), 2, MessageType.ERROR);
                }
                return;
            }
            if (!isValidPropertyName(editedName)) {
                nameEditor.setText(propertyName);
                tileMapEditor.showMessage("Property name %s is invalid".formatted(editedName), 2, MessageType.ERROR);
                return;
            }
            if (tileMap.getProperties().get(editedName) != null) {
                tileMapEditor.showMessage("Property name already used", 2, MessageType.ERROR);
                nameEditor.setText(propertyName);
                return;
            }
            tileMap.getProperties().remove(propertyName);
            tileMap.getProperties().put(editedName, formattedPropertyValue());
            tileMapEditor.showMessage("Property %s renamed to %s".formatted(propertyName, editedName), 2, MessageType.INFO);
            propertyName = editedName;
            rebuildPropertyEditors(); // sort order might have changed
            tileMapEditor.markMapEdited();
        }

        void storePropertyValue() {
            tileMap.getProperties().put(propertyName, formattedPropertyValue());
            tileMapEditor.markMapEdited();
        }

        abstract String formattedPropertyValue();

        abstract void updateEditorFromProperty();

        abstract Node valueEditor();

        Node nameEditor() {
            return nameEditor;
        }
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
            textEditor.setText(tileMap.getProperties().getProperty(propertyName));
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
            String propertyValue = tileMap.getProperties().getProperty(propertyName);
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
            String propertyValue = tileMap.getProperties().getProperty(propertyName);
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

    public PropertyEditorPane(TileMapEditor tileMapEditor) {
        this.tileMapEditor = tileMapEditor;

        var btnAddColorEntry = new Button("Color");
        btnAddColorEntry.setOnAction(e -> {
            String propertyName = "color_RENAME_ME";
            tileMap.getProperties().put(propertyName, "green");
            tileMapEditor.showMessage("New property %s added".formatted(propertyName), 1, MessageType.INFO);
            rebuildPropertyEditors();
        });
        btnAddColorEntry.disableProperty().bind(enabledPy.not());

        var btnAddPosEntry = new Button("Position");
        btnAddPosEntry.setOnAction(e -> {
            String propertyName = "pos_RENAME_ME";
            tileMap.getProperties().put(propertyName, "(0,0)");
            tileMapEditor.showMessage("New property %s added".formatted(propertyName), 1, MessageType.INFO);
            rebuildPropertyEditors();
        });
        btnAddPosEntry.disableProperty().bind(enabledPy.not());

        var btnAddGenericEntry = new Button("Text");
        btnAddGenericEntry.setOnAction(e -> {
            String propertyName = "RENAME_ME";
            tileMap.getProperties().put(propertyName, "any text");
            tileMapEditor.showMessage("New property %s added".formatted(propertyName), 1, MessageType.INFO);
            rebuildPropertyEditors();
        });
        btnAddGenericEntry.disableProperty().bind(enabledPy.not());

        var buttonBar = new HBox(new Label("New"), btnAddColorEntry, btnAddPosEntry, btnAddGenericEntry);
        buttonBar.setSpacing(3);
        buttonBar.setPadding(new Insets(2,2,6,2));
        buttonBar.setAlignment(Pos.CENTER_LEFT);

        grid.setHgap(2);
        grid.setVgap(2);

        setTop(buttonBar);
        setCenter(grid);
    }

    public void setTileMap(TileMap tileMap) {
        this.tileMap = Objects.requireNonNull(tileMap);
        rebuildPropertyEditors();
    }

    public void updatePropertyEditorValues() {
        for (var editor : propertyEditors) {
            editor.updateEditorFromProperty();
        }
    }

    private void rebuildPropertyEditors() {
        Logger.debug("Rebuild editors");
        propertyEditors.clear();
        tileMap.getProperties().stringPropertyNames().stream().sorted().forEach(propertyName -> {
            String propertyValue = tileMap.getProperty(propertyName);
            // primitive way of discriminating but fulfills its purpose
            if (propertyName.startsWith("color_")) {
                propertyEditors.add(new ColorPropertyEditor(propertyName, propertyValue));
            } else if (propertyName.startsWith("pos_") || propertyName.startsWith("tile_") || propertyName.startsWith("vec_")) {
                propertyEditors.add(new TilePropertyEditor(propertyName, propertyValue));
            } else {
                propertyEditors.add(new TextPropertyEditor(propertyName, propertyValue));
            }
        });

        int row = 0;
        grid.getChildren().clear();
        for (var propertyEditor : propertyEditors) {
            grid.add(propertyEditor.nameEditor(), 0, row);
            grid.add(propertyEditor.valueEditor(), 1, row);
            ++row;
        }
    }
}