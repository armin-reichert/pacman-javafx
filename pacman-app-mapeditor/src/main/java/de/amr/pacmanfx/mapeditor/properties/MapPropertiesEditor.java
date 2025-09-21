/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.mapeditor.properties;

import de.amr.pacmanfx.lib.worldmap.LayerID;
import de.amr.pacmanfx.lib.worldmap.WorldMap;
import de.amr.pacmanfx.mapeditor.EditorGlobals;
import de.amr.pacmanfx.mapeditor.MessageType;
import de.amr.pacmanfx.mapeditor.TileMapEditorUI;
import de.amr.pacmanfx.model.DefaultWorldMapProperties;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import org.tinylog.Logger;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static de.amr.pacmanfx.model.DefaultWorldMapProperties.*;
import static java.util.Objects.requireNonNull;

public class MapPropertiesEditor extends BorderPane {

    public static final int NAME_EDITOR_WIDTH = 170;
    public static final int VALUE_EDITOR_WIDTH = 120;

    private static final String SYMBOL_DELETE = "\u274C";

    private static final String NEW_COLOR_PROPERTY_NAME = "color_RENAME_ME";
    private static final String NEW_POSITION_PROPERTY_NAME = "pos_RENAME_ME";
    private static final String NEW_TEXT_PROPERTY_NAME  = "RENAME_ME";

    public static final String DEFAULT_COLOR_VALUE = "rgba(0,0,0,1.0)";
    public static final String DEFAULT_TILE_VALUE = "(0,0)";
    public static final String DEFAULT_TEXT_VALUE = "";

    public static boolean isPredefinedProperty(String propertyName, LayerID layerID) {
        return switch (layerID) {
            case TERRAIN ->
                   COLOR_WALL_FILL.equals(propertyName)
                || COLOR_WALL_STROKE.equals(propertyName)
                || COLOR_DOOR.equals(propertyName)
                || POS_HOUSE_MIN_TILE.equals(propertyName)
                || POS_HOUSE_MAX_TILE.equals(propertyName);
            case FOOD -> DefaultWorldMapProperties.COLOR_FOOD.equals(propertyName);
        };
    }

    public static boolean isHiddenProperty(String propertyName, LayerID layerID) {
        return switch (layerID) {
            case TERRAIN -> POS_HOUSE_MIN_TILE.equals(propertyName)
                || POS_HOUSE_MAX_TILE.equals(propertyName);
            case FOOD -> false;
        };
    }


    private final TileMapEditorUI ui;
    private final LayerID layerID;

    private final BooleanProperty enabled = new SimpleBooleanProperty(true);
    private final BooleanProperty sorted = new SimpleBooleanProperty(true);

    private final List<SinglePropertyEditor> propertyEditors = new ArrayList<>();
    private final GridPane grid = new GridPane();

    public MapPropertiesEditor(TileMapEditorUI ui, LayerID layerID) {
        this.ui = requireNonNull(ui);
        this.layerID = requireNonNull(layerID);

        ui.editor().currentWorldMapProperty().addListener((py, ov, nv) -> rebuildPropertyEditors());
        sorted.addListener((py, ov, nv) -> rebuildPropertyEditors());

        var btnAddColorEntry = new Button("Color");
        btnAddColorEntry.disableProperty().bind(enabled.not());
        btnAddColorEntry.setOnAction(e -> addNewColorProperty());

        var btnAddPosEntry = new Button("Position");
        btnAddPosEntry.setOnAction(e -> addNewPositionProperty());
        btnAddPosEntry.disableProperty().bind(enabled.not());

        var btnAddTextEntry = new Button("Text");
        btnAddTextEntry.setOnAction(e -> addNewTextProperty());
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

    private void addNewColorProperty() {
        String propertyName = NEW_COLOR_PROPERTY_NAME;
        if (worldMap().properties(layerID).containsKey(propertyName)) {
            ui.messageDisplay().showMessage("Property %s already exists".formatted(propertyName), 1, MessageType.INFO);
            return;
        }
        worldMap().properties(layerID).put(propertyName, DEFAULT_COLOR_VALUE);
        ui.editor().setWorldMapChanged();
        ui.editor().setEdited(true);
        ui.messageDisplay().showMessage("New property %s added".formatted(propertyName), 1, MessageType.INFO);
    }

    private void addNewPositionProperty() {
        String propertyName = NEW_POSITION_PROPERTY_NAME;
        if (worldMap().properties(layerID).containsKey(propertyName)) {
            ui.messageDisplay().showMessage("Property %s already exists".formatted(propertyName), 1, MessageType.INFO);
            return;
        }
        worldMap().properties(layerID).put(propertyName, DEFAULT_TILE_VALUE);
        ui.messageDisplay().showMessage("New property %s added".formatted(propertyName), 1, MessageType.INFO);
        ui.editor().setWorldMapChanged();
        ui.editor().setEdited(true);
    }

    private void addNewTextProperty() {
        String propertyName = NEW_TEXT_PROPERTY_NAME;
        if (worldMap().properties(layerID).containsKey(propertyName)) {
            ui.messageDisplay().showMessage("Property %s already exists".formatted(propertyName), 1, MessageType.INFO);
            return;
        }
        worldMap().properties(layerID).put(propertyName, DEFAULT_TEXT_VALUE);
        ui.editor().setWorldMapChanged();
        ui.editor().setEdited(true);
        ui.messageDisplay().showMessage("New property %s added".formatted(propertyName), 1, MessageType.INFO);
    }

    private void deleteProperty(String propertyName) {
        var layerProperties = worldMap().properties(layerID);
        if (layerProperties.containsKey(propertyName)) {
            layerProperties.remove(propertyName);
            ui.messageDisplay().showMessage("Property '%s' deleted".formatted(propertyName), 3, MessageType.INFO);
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

        Stream<String> propertyNames = sorted.get()
            ? worldMap().propertyNames(layerID).sorted()
            : worldMap().propertyNames(layerID);

        propertyNames.forEach(propertyName -> {
            // primitive way of discriminating type but fulfills its purpose
            WorldMap.PropertyType type = WorldMap.PropertyType.STRING;
            if (propertyName.startsWith("color_")) {
                type = WorldMap.PropertyType.COLOR_RGBA;
            } else if (propertyName.startsWith("pos_") || propertyName.startsWith("tile_") || propertyName.startsWith("vec_")) {
                type = WorldMap.PropertyType.TILE;
            }

            Set<WorldMap.PropertyAttribute> attributes = EnumSet.noneOf(WorldMap.PropertyAttribute.class);
            if (isPredefinedProperty(propertyName, layerID)) attributes.add(WorldMap.PropertyAttribute.PREDEFINED);
            if (isHiddenProperty(propertyName, layerID)) attributes.add((WorldMap.PropertyAttribute.HIDDEN));

            var propertyInfo = new WorldMap.Property(type, attributes);
            String propertyValue = worldMap().properties(layerID).get(propertyName);
            SinglePropertyEditor propertyEditor = createEditor(ui, propertyName, propertyInfo, propertyValue);
            propertyEditors.add(propertyEditor);
        });

        int row = 0;
        grid.getChildren().clear();
        for (var propertyEditor : propertyEditors) {
            if (propertyEditor.property.is(WorldMap.PropertyAttribute.HIDDEN)) {
                continue;
            }
            grid.add(propertyEditor.nameEditor(), 0, row);
            grid.add(propertyEditor.valueEditor(), 1, row);
            if (!propertyEditor.property.is(WorldMap.PropertyAttribute.PREDEFINED)) {
                var btnDelete = new Button(SYMBOL_DELETE);
                btnDelete.disableProperty().bind(enabled.not());
                btnDelete.setOnAction(e -> deleteProperty(propertyEditor.propertyName));
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

    private SinglePropertyEditor createEditor(TileMapEditorUI ui, String propertyName, WorldMap.Property info, String initialValue) {
        SinglePropertyEditor propertyEditor = switch (info.type()) {
            case COLOR_RGBA -> new ColorPropertyEditor(ui, layerID, propertyName, info, initialValue);
            case TILE ->  new TilePropertyEditor(ui, layerID, propertyName, info, initialValue);
            case STRING -> new TextPropertyEditor(ui, layerID, propertyName, info, initialValue);
        };
        propertyEditor.enabledProperty().bind(enabled);
        propertyEditor.worldMapProperty().bind(ui.editor().currentWorldMapProperty());
        return propertyEditor;
    }

}