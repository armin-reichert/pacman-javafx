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

import static de.amr.pacmanfx.tilemap.editor.EditorUtil.formatColor;
import static de.amr.pacmanfx.tilemap.editor.EditorUtil.parseColor;
import static java.util.Objects.requireNonNull;

public class WorldMapLayerPropertiesEditor extends BorderPane {

    private static final int NAME_EDITOR_MIN_WIDTH = 180;
    private static final Pattern PATTERN_PROPERTY_NAME = Pattern.compile("[a-zA-Z]([a-zA-Z0-9_])*");

    private static boolean isValidPropertyName(String s) {
        return PATTERN_PROPERTY_NAME.matcher(s).matches();
    }

    private abstract class SinglePropertyEditor {

        protected final ObjectProperty<WorldMap> worldMap = new SimpleObjectProperty<>();
        protected String propertyName;
        protected final TextField nameEditor;

        protected SinglePropertyEditor(String propertyName) {
            this.propertyName = propertyName;
            nameEditor = new TextField(propertyName);
            nameEditor.setMinWidth(NAME_EDITOR_MIN_WIDTH);
            nameEditor.disableProperty().bind(enabled.not());
            nameEditor.setOnAction(e -> onPropertyNameEdited());
        }

        public ObjectProperty<WorldMap> worldMapProperty() {
            return worldMap;
        }

        public WorldMap worldMap() {
            return worldMap.get();
        }

        protected void onPropertyNameEdited() {
            if (worldMap() == null) {
                return;
            }
            String editedName = nameEditor.getText().trim();
            if (editedName.isBlank() || propertyName.equals(editedName)) {
                nameEditor.setText(propertyName);
                return;
            }
            if (!isValidPropertyName(editedName)) {
                nameEditor.setText(propertyName);
                ui.messageDisplay().showMessage("Property name %s is invalid".formatted(editedName), 2, MessageType.ERROR);
                return;
            }
            if (worldMap().properties(layerID).containsKey(editedName)) {
                ui.messageDisplay().showMessage("Property name already used", 2, MessageType.ERROR);
                nameEditor.setText(propertyName);
                return;
            }
            worldMap().properties(layerID).remove(propertyName);
            worldMap().properties(layerID).put(editedName, formattedPropertyValue());
            propertyName = editedName;
            rebuildPropertyEditors(); // sort order might have changed
            ui.editor().setWorldMapChanged();
            ui.editor().setEdited(true);
            ui.messageDisplay().showMessage("Property %s renamed to %s".formatted(propertyName, editedName), 2, MessageType.INFO);
        }

        protected void storePropertyValue() {
            worldMap().properties(layerID).put(propertyName, formattedPropertyValue());
            ui.editor().setWorldMapChanged();
            ui.editor().setEdited(true);
        }

        protected abstract String formattedPropertyValue();

        protected abstract void updateEditorFromProperty();

        protected abstract Node valueEditor();

        protected Node nameEditor() {
            return nameEditor;
        }
    }

    private class TextPropertyEditor extends SinglePropertyEditor {

        private final TextField textEditor;

        public TextPropertyEditor(String propertyName, String propertyValue) {
            super(propertyName);
            textEditor = new TextField();
            textEditor.setText(propertyValue);
            textEditor.disableProperty().bind(enabled.not());
            textEditor.setOnAction(e -> storePropertyValue());
        }

        @Override
        protected void updateEditorFromProperty() {
            textEditor.setText(worldMap().properties(layerID).get(propertyName));
        }

        @Override
        protected Node valueEditor() {
            return textEditor;
        }

        @Override
        protected String formattedPropertyValue() {
            return textEditor.getText().strip();
        }
    }

    private class ColorPropertyEditor extends SinglePropertyEditor {
        private final ColorPicker colorPicker;

        public ColorPropertyEditor(String propertyName, String propertyValue) {
            super(propertyName);
            colorPicker = new ColorPicker();
            colorPicker.setUserData(propertyName);
            colorPicker.setValue(parseColor(propertyValue));
            colorPicker.disableProperty().bind(enabled.not());
            colorPicker.setOnAction(e -> storePropertyValue());
        }

        @Override
        protected void updateEditorFromProperty() {
            String propertyValue = worldMap().properties(layerID).get(propertyName);
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

    private class TilePropertyEditor extends SinglePropertyEditor {

        private final Spinner<Integer> spinnerX;
        private final Spinner<Integer> spinnerY;
        private final HBox valueEditorPane;

        public TilePropertyEditor(String propertyName, String propertyValue) {
            super(propertyName);

            spinnerX = new Spinner<>(0, 1000, 0);
            spinnerX.setMaxWidth(60);
            spinnerX.disableProperty().bind(enabled.not());

            spinnerY = new Spinner<>(0, 1000, 0);
            spinnerY.setMaxWidth(60);
            spinnerY.disableProperty().bind(enabled.not());

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
            // what an idiotic API or am I the idiot?
            if (spinnerX.getValueFactory() instanceof SpinnerValueFactory.IntegerSpinnerValueFactory ivf
                && ivf.getMax() != worldMap().numCols() - 1) {
                    spinnerX.setValueFactory(
                        new SpinnerValueFactory.IntegerSpinnerValueFactory(
                            0, worldMap().numCols() - 1, spinnerX.getValue()));
            }
            if (spinnerY.getValueFactory() instanceof SpinnerValueFactory.IntegerSpinnerValueFactory ivf
                && ivf.getMax() != worldMap().numRows() - 1) {
                    spinnerY.setValueFactory(
                        new SpinnerValueFactory.IntegerSpinnerValueFactory(
                            0, worldMap().numRows() - 1, spinnerY.getValue()));
            }
            String propertyValue = worldMap().properties(layerID).get(propertyName);
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

    // main class

    private final EditorUI ui;
    private final LayerID layerID;

    private final BooleanProperty enabled = new SimpleBooleanProperty(true);

    private final ObjectProperty<WorldMap> worldMap = new SimpleObjectProperty<>() {
        @Override
        protected void invalidated() {
            rebuildPropertyEditors();
        }
    };

    private final List<SinglePropertyEditor> propertyEditors = new ArrayList<>();
    private final GridPane grid = new GridPane();

    public WorldMapLayerPropertiesEditor(EditorUI ui, LayerID layerID) {
        this.ui = requireNonNull(ui);
        this.layerID = requireNonNull(layerID);
        worldMap.bind(ui.editor().currentWorldMapProperty());

        var btnAddColorEntry = new Button("Color");
        btnAddColorEntry.setOnAction(e -> {
            String propertyName = "color_RENAME_ME";
            worldMap.get().properties(layerID).put(propertyName, "green");
            ui.messageDisplay().showMessage("New property %s added".formatted(propertyName), 1, MessageType.INFO);
            rebuildPropertyEditors();
        });
        btnAddColorEntry.disableProperty().bind(enabled.not());

        var btnAddPosEntry = new Button("Position");
        btnAddPosEntry.setOnAction(e -> {
            String propertyName = "pos_RENAME_ME";
            worldMap.get().properties(layerID).put(propertyName, "(0,0)");
            ui.messageDisplay().showMessage("New property %s added".formatted(propertyName), 1, MessageType.INFO);
            rebuildPropertyEditors();
        });
        btnAddPosEntry.disableProperty().bind(enabled.not());

        var btnAddGenericEntry = new Button("Text");
        btnAddGenericEntry.setOnAction(e -> {
            String propertyName = "RENAME_ME";
            worldMap.get().properties(layerID).put(propertyName, "any text");
            ui.messageDisplay().showMessage("New property %s added".formatted(propertyName), 1, MessageType.INFO);
            rebuildPropertyEditors();
        });
        btnAddGenericEntry.disableProperty().bind(enabled.not());

        var buttonBar = new HBox(new Label("New"), btnAddColorEntry, btnAddPosEntry, btnAddGenericEntry);
        buttonBar.setSpacing(3);
        buttonBar.setPadding(new Insets(2,2,6,2));
        buttonBar.setAlignment(Pos.CENTER_LEFT);

        grid.setHgap(2);
        grid.setVgap(2);

        setTop(buttonBar);
        setCenter(grid);
    }

    public BooleanProperty enabledProperty() {
        return enabled;
    }

    public WorldMap worldMap() {
        return worldMap.get();
    }

    public void updatePropertyEditorValues() {
        for (var propertyEditor : propertyEditors) {
            propertyEditor.updateEditorFromProperty();
        }
    }

    public void rebuildPropertyEditors() {
        propertyEditors.clear();
        if (worldMap() == null) {
            Logger.info("World map not yet set, cannot build property editors");
            return;
        }
        worldMap().propertyNames(layerID).forEach(propertyName -> {
            String propertyValue = worldMap.get().properties(layerID).get(propertyName);
            // primitive way of discriminating but fulfills its purpose
            SinglePropertyEditor propertyEditor;
            if (propertyName.startsWith("color_")) {
                propertyEditor = new ColorPropertyEditor(propertyName, propertyValue);
            } else if (propertyName.startsWith("pos_") || propertyName.startsWith("tile_") || propertyName.startsWith("vec_")) {
                propertyEditor = new TilePropertyEditor(propertyName, propertyValue);
            } else {
                propertyEditor = new TextPropertyEditor(propertyName, propertyValue);
            }
            propertyEditor.worldMapProperty().bind(worldMap);
            propertyEditors.add(propertyEditor);
        });

        int row = 0;
        grid.getChildren().clear();
        for (var propertyEditor : propertyEditors) {
            grid.add(propertyEditor.nameEditor(), 0, row);
            grid.add(propertyEditor.valueEditor(), 1, row);
            var btnDelete = new Button("X");
            btnDelete.disableProperty().bind(enabled.not());
            btnDelete.setOnAction(e -> deleteProperty(propertyEditor.propertyName));
            grid.add(btnDelete, 2, row);
            ++row;
        }

        updatePropertyEditorValues();
    }

    private void deleteProperty(String propertyName) {
        new Action_DeleteMapProperty(ui.editor(), worldMap(), layerID, propertyName).execute();
        ui.messageDisplay().showMessage("Property %s deleted".formatted(propertyName), 3, MessageType.INFO);
        Logger.debug("Property {} deleted", propertyName);
    }
}