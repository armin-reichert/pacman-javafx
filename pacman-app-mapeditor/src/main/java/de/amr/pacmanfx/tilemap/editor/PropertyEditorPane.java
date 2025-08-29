/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.tilemap.editor;

import de.amr.pacmanfx.lib.tilemap.LayerID;
import de.amr.pacmanfx.lib.tilemap.WorldMap;
import de.amr.pacmanfx.lib.tilemap.WorldMapFormatter;
import de.amr.pacmanfx.lib.tilemap.WorldMapParser;
import de.amr.pacmanfx.tilemap.editor.actions.Action_DeleteMapProperty;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
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
import java.util.regex.Pattern;

import static de.amr.pacmanfx.tilemap.editor.TileMapEditorUtil.formatColor;
import static de.amr.pacmanfx.tilemap.editor.TileMapEditorUtil.parseColor;
import static java.util.Objects.requireNonNull;

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

    private final ObjectProperty<WorldMap> worldMapPy = new SimpleObjectProperty<>() {
        @Override
        protected void invalidated() {
        rebuildPropertyEditors();
        }
    };

    private LayerID layerID = LayerID.TERRAIN; // TODO check

    public void setTileMap(WorldMap worldMap, LayerID layerID) {
        worldMapPy.set(requireNonNull(worldMap));
        this.layerID = layerID;
        rebuildPropertyEditors();
    }

    private final TileMapEditorUI ui;
    private final GridPane grid = new GridPane();
    private final List<AbstractPropertyEditor> propertyEditors = new ArrayList<>();

    private abstract class AbstractPropertyEditor {

        String propertyName;
        final TextField nameEditor;

        AbstractPropertyEditor(String propertyName) {
            this.propertyName = propertyName;
            nameEditor = new TextField(propertyName);
            nameEditor.setMinWidth(NAME_COLUMN_MIN_WIDTH);
            nameEditor.disableProperty().bind(enabledPy.not());
            nameEditor.setOnAction(e -> onPropertyNameEdited());
        }

        void onPropertyNameEdited() {
            WorldMap worldMap = worldMapPy.get();

            String editedName = nameEditor.getText().trim();
            if (editedName.isBlank() || propertyName.equals(editedName)) {
                nameEditor.setText(propertyName);
                return;
            }
            if (matchesDeleteCommand(editedName)) {
                String deletePropertyName = chopDeleteCommand(editedName);
                if (deletePropertyName.equals(propertyName)) {
                    deleteProperty(propertyName);
                } else {
                    nameEditor.setText(propertyName);
                    ui.messageDisplay().showMessage("Cannot delete other property %s".formatted(deletePropertyName), 2, MessageType.ERROR);
                }
                return;
            }
            if (!isValidPropertyName(editedName)) {
                nameEditor.setText(propertyName);
                ui.messageDisplay().showMessage("Property name %s is invalid".formatted(editedName), 2, MessageType.ERROR);
                return;
            }
            if (worldMap.properties(layerID).containsKey(editedName)) {
                ui.messageDisplay().showMessage("Property name already used", 2, MessageType.ERROR);
                nameEditor.setText(propertyName);
                return;
            }
            worldMap.properties(layerID).remove(propertyName);
            worldMap.properties(layerID).put(editedName, formattedPropertyValue());
            ui.messageDisplay().showMessage("Property %s renamed to %s".formatted(propertyName, editedName), 2, MessageType.INFO);
            propertyName = editedName;
            rebuildPropertyEditors(); // sort order might have changed
            ui.editor().setWorldMapChanged();
            ui.editor().setEdited(true);
        }

        void storePropertyValue() {
            WorldMap worldMap = worldMapPy.get();
            worldMap.properties(layerID).put(propertyName, formattedPropertyValue());
            ui.editor().setWorldMapChanged();
            ui.editor().setEdited(true);
        }

        abstract String formattedPropertyValue();

        abstract void updateEditorFromProperty();

        abstract Node valueEditor();

        Node nameEditor() {
            return nameEditor;
        }
    }

    private class TextPropertyEditor extends AbstractPropertyEditor {

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
            textEditor.setText(worldMapPy.get().properties(layerID).get(propertyName));
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

    private class ColorPropertyEditor extends AbstractPropertyEditor {
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
            String propertyValue = worldMapPy.get().properties(layerID).get(propertyName);
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

    private class TilePropertyEditor extends AbstractPropertyEditor {
        final Spinner<Integer> spinnerX;
        final Spinner<Integer> spinnerY;
        final HBox valueEditorPane;

        TilePropertyEditor(String propertyName, String propertyValue) {
            super(propertyName);

            spinnerX = new Spinner<>(0, worldMapPy.get().numCols() - 1, 0);
            spinnerX.setMaxWidth(60);
            spinnerX.disableProperty().bind(enabledPy.not());

            spinnerY = new Spinner<>(0, worldMapPy.get().numRows() - 1, 0);
            spinnerY.setMaxWidth(60);
            spinnerY.disableProperty().bind(enabledPy.not());

            WorldMapParser.parseTile(propertyValue).ifPresent(tile -> {
                spinnerX.getValueFactory().setValue(tile.x());
                spinnerY.getValueFactory().setValue(tile.y());
            });

            spinnerX.valueProperty().addListener((py,ov,nv) -> storePropertyValue());
            spinnerY.valueProperty().addListener((py,ov,nv) -> storePropertyValue());

            valueEditorPane = new HBox(spinnerX, spinnerY);
        }

        @Override
        protected void updateEditorFromProperty() {
            String propertyValue = worldMapPy.get().properties(layerID).get(propertyName);
            WorldMapParser.parseTile(propertyValue).ifPresent(tile -> {
                spinnerX.getValueFactory().setValue(tile.x());
                spinnerY.getValueFactory().setValue(tile.y());
            });
        }

        @Override
        protected String formattedPropertyValue() {
            return WorldMapFormatter.formatTile(spinnerX.getValue(), spinnerY.getValue());
        }

        @Override
        protected Node valueEditor() {
            return valueEditorPane;
        }
    }

    public PropertyEditorPane(TileMapEditorUI ui) {
        this.ui = requireNonNull(ui);

        var btnAddColorEntry = new Button("Color");
        btnAddColorEntry.setOnAction(e -> {
            String propertyName = "color_RENAME_ME";
            worldMapPy.get().properties(layerID).put(propertyName, "green");
            ui.messageDisplay().showMessage("New property %s added".formatted(propertyName), 1, MessageType.INFO);
            rebuildPropertyEditors();
        });
        btnAddColorEntry.disableProperty().bind(enabledPy.not());

        var btnAddPosEntry = new Button("Position");
        btnAddPosEntry.setOnAction(e -> {
            String propertyName = "pos_RENAME_ME";
            worldMapPy.get().properties(layerID).put(propertyName, "(0,0)");
            ui.messageDisplay().showMessage("New property %s added".formatted(propertyName), 1, MessageType.INFO);
            rebuildPropertyEditors();
        });
        btnAddPosEntry.disableProperty().bind(enabledPy.not());

        var btnAddGenericEntry = new Button("Text");
        btnAddGenericEntry.setOnAction(e -> {
            String propertyName = "RENAME_ME";
            worldMapPy.get().properties(layerID).put(propertyName, "any text");
            ui.messageDisplay().showMessage("New property %s added".formatted(propertyName), 1, MessageType.INFO);
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

    public void updatePropertyEditorValues() {
        for (var propertyEditor : propertyEditors) {
            propertyEditor.updateEditorFromProperty();
        }
    }

    public void rebuildPropertyEditors() {
        Logger.debug("Rebuild editors");
        propertyEditors.clear();
        worldMapPy.get().propertyNames(layerID).forEach(propertyName -> {
            String propertyValue = worldMapPy.get().properties(layerID).get(propertyName);
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
            var btnDelete = new Button("X");
            btnDelete.disableProperty().bind(enabledPy.not());
            btnDelete.setOnAction(e -> deleteProperty(propertyEditor.propertyName));
            grid.add(btnDelete, 2, row);
            ++row;
        }

        updatePropertyEditorValues();
    }

    void deleteProperty(String propertyName) {
        new Action_DeleteMapProperty(ui.editor(), worldMapPy.get(), layerID, propertyName).execute();
        ui.messageDisplay().showMessage("Property %s deleted".formatted(propertyName), 3, MessageType.INFO);
        Logger.debug("Property {} deleted", propertyName);
    }
}