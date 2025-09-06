/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.tilemap.editor;

import de.amr.pacmanfx.lib.tilemap.LayerID;
import de.amr.pacmanfx.lib.tilemap.WorldMap;
import de.amr.pacmanfx.lib.tilemap.WorldMapFormatter;
import de.amr.pacmanfx.lib.tilemap.WorldMapParser;
import de.amr.pacmanfx.model.WorldMapProperty;
import de.amr.pacmanfx.tilemap.editor.actions.Action_DeleteArcadeHouse;
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
import javafx.scene.layout.Region;
import org.tinylog.Logger;

import java.util.ArrayList;
import java.util.List;

import static de.amr.pacmanfx.model.WorldMapProperty.POS_HOUSE_MAX_TILE;
import static de.amr.pacmanfx.model.WorldMapProperty.POS_HOUSE_MIN_TILE;
import static de.amr.pacmanfx.tilemap.editor.EditorUtil.formatColor;
import static de.amr.pacmanfx.tilemap.editor.EditorUtil.parseColor;
import static java.util.Objects.requireNonNull;

public class WorldMapLayerPropertiesEditor extends BorderPane {

    static final int NAME_EDITOR_MIN_WIDTH = 180;
    static final String SYMBOL_DELETE = "\u274C";

    public static boolean isDefaultColorProperty(String propertyName, LayerID layerID) {
        return switch (layerID) {
            case TERRAIN -> WorldMapProperty.COLOR_WALL_FILL.equals(propertyName)
                    || WorldMapProperty.COLOR_WALL_STROKE.equals(propertyName)
                    || WorldMapProperty.COLOR_DOOR.equals(propertyName);
            case FOOD -> WorldMapProperty.COLOR_FOOD.equals(propertyName);
        };
    }

    private static abstract class SinglePropertyEditor {

        protected final BooleanProperty enabled = new SimpleBooleanProperty(true);
        protected final ObjectProperty<WorldMap> worldMap = new SimpleObjectProperty<>();

        protected final LayerID layerID;
        protected PropertyInfo propertyInfo;
        protected final TextField nameEditor;

        protected SinglePropertyEditor(EditorUI ui, LayerID layerID, PropertyInfo propertyInfo) {
            requireNonNull(ui);
            this.layerID = requireNonNull(layerID);
            this.propertyInfo = requireNonNull(propertyInfo);

            nameEditor = new TextField(propertyInfo.name());
            nameEditor.setMinWidth(NAME_EDITOR_MIN_WIDTH);
            if (propertyInfo.permanent()) {
                nameEditor.setDisable(true);
            }
            else {
                nameEditor.disableProperty().bind(enabled.not());
                nameEditor.setOnAction(e -> onPropertyNameEdited(ui));
            }
        }

        public BooleanProperty enabledProperty() {
            return enabled;
        }

        public ObjectProperty<WorldMap> worldMapProperty() {
            return worldMap;
        }

        public WorldMap worldMap() {
            return worldMap.get();
        }

        protected void onPropertyNameEdited(EditorUI ui) {
            if (worldMap() == null) {
                return;
            }
            String editedName = nameEditor.getText().trim();
            if (editedName.isBlank()) {
                nameEditor.setText(propertyInfo.name());
                return;
            }
            if (propertyInfo.name().equals(editedName)) {
                return;
            }
            if (PropertyInfo.isInValidPropertyName(editedName)) {
                nameEditor.setText(propertyInfo.name());
                ui.messageDisplay().showMessage("Property name '%s' is invalid".formatted(editedName), 2, MessageType.ERROR);
                return;
            }
            if (worldMap().properties(layerID).containsKey(editedName)) {
                ui.messageDisplay().showMessage("Property name already in use", 2, MessageType.ERROR);
                nameEditor.setText(propertyInfo.name());
                return;
            }
            ui.messageDisplay().showMessage("Property '%s' renamed to '%s'"
                .formatted(propertyInfo.name(), editedName), 2, MessageType.INFO);

            worldMap().properties(layerID).remove(propertyInfo.name());
            worldMap().properties(layerID).put(editedName, formattedPropertyValue());
            boolean permanent = isDefaultColorProperty(editedName, layerID);
            propertyInfo = new PropertyInfo(editedName, propertyInfo.type(), permanent);

            ui.editor().setWorldMapChanged();
            ui.editor().setEdited(true);
        }

        protected void storePropertyValue(EditorUI ui) {
            worldMap().properties(layerID).put(propertyInfo.name(), formattedPropertyValue());
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

    private static class TextPropertyEditor extends SinglePropertyEditor {

        private final TextField textEditor;

        public TextPropertyEditor(EditorUI ui, LayerID layerID, PropertyInfo propertyInfo, String propertyValue) {
            super(ui, layerID, propertyInfo);
            textEditor = new TextField();
            textEditor.setText(propertyValue);
            textEditor.disableProperty().bind(enabled.not());
            textEditor.setOnAction(e -> storePropertyValue(ui));
        }

        @Override
        protected void updateEditorFromProperty() {
            String text = worldMap().properties(layerID).get(propertyInfo.name());
            textEditor.setText(text);
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

    private static class ColorPropertyEditor extends SinglePropertyEditor {

        private final ColorPicker colorPicker;

        public ColorPropertyEditor(EditorUI ui, LayerID layerID, PropertyInfo propertyInfo, String propertyValue) {
            super(ui, layerID, propertyInfo);
            colorPicker = new ColorPicker();
            colorPicker.setValue(parseColor(propertyValue));
            colorPicker.disableProperty().bind(enabled.not());
            colorPicker.setOnAction(e -> storePropertyValue(ui));
        }

        @Override
        protected void updateEditorFromProperty() {
            String colorExpression = worldMap().properties(layerID).get(propertyInfo.name());
            colorPicker.setValue(parseColor(colorExpression));
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

    private static class TilePropertyEditor extends SinglePropertyEditor {

        private final Spinner<Integer> spinnerX;
        private final Spinner<Integer> spinnerY;
        private final SpinnerValueFactory.IntegerSpinnerValueFactory spinnerXModel;
        private final SpinnerValueFactory.IntegerSpinnerValueFactory spinnerYModel;
        private final HBox valueEditorPane;

        public TilePropertyEditor(EditorUI ui, LayerID layerID, PropertyInfo propertyInfo, String propertyValue) {
            super(ui, layerID, propertyInfo);

            spinnerX = new Spinner<>(0, 1000, 0);
            spinnerXModel = (SpinnerValueFactory.IntegerSpinnerValueFactory) spinnerX.getValueFactory();
            spinnerX.setMaxWidth(60);
            spinnerX.disableProperty().bind(enabled.not());

            spinnerY = new Spinner<>(0, 1000, 0);
            spinnerYModel = (SpinnerValueFactory.IntegerSpinnerValueFactory) spinnerY.getValueFactory();
            spinnerY.setMaxWidth(60);
            spinnerY.disableProperty().bind(enabled.not());

            WorldMapParser.parseTile(propertyValue).ifPresent(tile -> {
                spinnerX.getValueFactory().setValue(tile.x());
                spinnerY.getValueFactory().setValue(tile.y());
            });

            spinnerX.valueProperty().addListener((py,ov,nv) -> storePropertyValue(ui));
            spinnerY.valueProperty().addListener((py,ov,nv) -> storePropertyValue(ui));

            valueEditorPane = new HBox(spinnerX, spinnerY);
        }

        @Override
        protected void updateEditorFromProperty() {
            spinnerXModel.setMax(worldMap().numCols() - 1);
            spinnerYModel.setMax(worldMap().numRows() - 1);
            String propertyValue = worldMap().properties(layerID).get(propertyInfo.name());
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

    private final List<SinglePropertyEditor> propertyEditors = new ArrayList<>();
    private final GridPane grid = new GridPane();

    public WorldMapLayerPropertiesEditor(EditorUI ui, LayerID layerID) {
        this.ui = requireNonNull(ui);
        this.layerID = requireNonNull(layerID);

        ui.editor().currentWorldMapProperty().addListener((py, ov, nv) -> rebuildPropertyEditors());

        var btnAddColorEntry = new Button("Color");
        btnAddColorEntry.disableProperty().bind(enabled.not());
        btnAddColorEntry.setOnAction(e -> addNewColorProperty("color_RENAME_ME"));

        var btnAddPosEntry = new Button("Position");
        btnAddPosEntry.setOnAction(e -> addNewPositionProperty("pos_RENAME_ME"));
        btnAddPosEntry.disableProperty().bind(enabled.not());

        var btnAddTextEntry = new Button("Text");
        btnAddTextEntry.setOnAction(e -> addNewTextProperty("RENAME_ME"));
        btnAddTextEntry.disableProperty().bind(enabled.not());

        var buttonBar = new HBox(new Label("New"), btnAddColorEntry, btnAddPosEntry, btnAddTextEntry);
        buttonBar.setSpacing(3);
        buttonBar.setPadding(new Insets(2,2,6,2));
        buttonBar.setAlignment(Pos.CENTER_LEFT);

        grid.setHgap(2);
        grid.setVgap(2);

        setTop(buttonBar);
        setCenter(grid);
    }
    
    private WorldMap worldMap() {
        return ui.editor().currentWorldMap();
    }

    private void addNewColorProperty(String propertyName) {
        if (worldMap().properties(layerID).containsKey(propertyName)) {
            ui.messageDisplay().showMessage("Property %s already exists".formatted(propertyName), 1, MessageType.INFO);
            return;
        }
        worldMap().properties(layerID).put(propertyName, "green");
        ui.editor().setWorldMapChanged();
        ui.editor().setEdited(true);
        ui.messageDisplay().showMessage("New property %s added".formatted(propertyName), 1, MessageType.INFO);
    }

    private void addNewPositionProperty(String propertyName) {
        if (worldMap().properties(layerID).containsKey(propertyName)) {
            ui.messageDisplay().showMessage("Property %s already exists".formatted(propertyName), 1, MessageType.INFO);
            return;
        }
        worldMap().properties(layerID).put(propertyName, "(0,0)");
        ui.messageDisplay().showMessage("New property %s added".formatted(propertyName), 1, MessageType.INFO);
        ui.editor().setWorldMapChanged();
        ui.editor().setEdited(true);
    }

    private void addNewTextProperty(String propertyName) {
        if (worldMap().properties(layerID).containsKey(propertyName)) {
            ui.messageDisplay().showMessage("Property %s already exists".formatted(propertyName), 1, MessageType.INFO);
            return;
        }
        worldMap().properties(layerID).put(propertyName, "any text");
        ui.editor().setWorldMapChanged();
        ui.editor().setEdited(true);
        ui.messageDisplay().showMessage("New property %s added".formatted(propertyName), 1, MessageType.INFO);
    }

    private void deleteProperty(String propertyName) {
        var layerProperties = worldMap().properties(layerID);
        if (layerProperties.containsKey(propertyName)) {
            //TODO find more general solution
            if (POS_HOUSE_MIN_TILE.equals(propertyName) || POS_HOUSE_MAX_TILE.equals(propertyName)) {
                new Action_DeleteArcadeHouse(ui.editor()).execute();
            } else {
                layerProperties.remove(propertyName);
                ui.messageDisplay().showMessage("Property '%s' deleted".formatted(propertyName), 3, MessageType.INFO);
            }
            ui.editor().setWorldMapChanged();
            ui.editor().setEdited(true);
        }
    }

    public BooleanProperty enabledProperty() {
        return enabled;
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
            // primitive way of discriminating but fulfills its purpose
            PropertyInfo propertyInfo;
            if (propertyName.startsWith("color_")) {
                boolean permanent = isDefaultColorProperty(propertyName, layerID);
                propertyInfo = new PropertyInfo(propertyName, PropertyType.COLOR, permanent);
            } else if (propertyName.startsWith("pos_") || propertyName.startsWith("tile_") || propertyName.startsWith("vec_")) {
                propertyInfo = new PropertyInfo(propertyName, PropertyType.TILE, false);
            } else {
                propertyInfo = new PropertyInfo(propertyName, PropertyType.STRING, false);
            }
            String propertyValue = worldMap().properties(layerID).get(propertyName);
            SinglePropertyEditor propertyEditor = createEditor(ui, propertyInfo, propertyValue);
            propertyEditors.add(propertyEditor);
        });

        int row = 0;
        grid.getChildren().clear();
        for (var propertyEditor : propertyEditors) {
            grid.add(propertyEditor.nameEditor(), 0, row);
            grid.add(propertyEditor.valueEditor(), 1, row);
            if (!propertyEditor.propertyInfo.permanent()) {
                var btnDelete = new Button(SYMBOL_DELETE);
                btnDelete.disableProperty().bind(enabled.not());
                btnDelete.setOnAction(e -> deleteProperty(propertyEditor.propertyInfo.name()));
                Tooltip tooltip = new Tooltip("Delete"); //TODO localize
                tooltip.setFont(EditorGlobals.FONT_TOOL_TIPS);
                btnDelete.setTooltip(tooltip);
                grid.add(btnDelete, 2, row);
            }
            else {
                var spacer = new Region();
                spacer.setMinWidth(30);
                grid.add(spacer, 2, row);
            }
            ++row;
        }

        updatePropertyEditorValues();
    }

    private SinglePropertyEditor createEditor(EditorUI ui, PropertyInfo info, String initialValue) {
        SinglePropertyEditor propertyEditor = switch (info.type()) {
            case COLOR -> new ColorPropertyEditor(ui, layerID, info, initialValue);
            case TILE -> new TilePropertyEditor(ui, layerID, info, initialValue);
            case STRING -> new TextPropertyEditor(ui, layerID, info, initialValue);
        };
        propertyEditor.enabledProperty().bind(enabled);
        propertyEditor.worldMapProperty().bind(ui.editor().currentWorldMapProperty());
        return propertyEditor;
    }
}