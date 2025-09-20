/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.mapeditor.properties;

import de.amr.pacmanfx.lib.worldmap.LayerID;
import de.amr.pacmanfx.lib.worldmap.WorldMap;
import de.amr.pacmanfx.lib.worldmap.WorldMapPropertyInfo;
import de.amr.pacmanfx.lib.worldmap.WorldMapPropertyType;
import de.amr.pacmanfx.mapeditor.EditorGlobals;
import de.amr.pacmanfx.mapeditor.MessageType;
import de.amr.pacmanfx.mapeditor.TileMapEditorUI;
import de.amr.pacmanfx.mapeditor.actions.Action_DeleteArcadeHouse;
import de.amr.pacmanfx.model.WorldMapProperty;
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
import java.util.List;
import java.util.stream.Stream;

import static de.amr.pacmanfx.model.WorldMapProperty.*;
import static java.util.Objects.requireNonNull;

public class MapPropertiesEditor extends BorderPane {

    public static final int NAME_EDITOR_MIN_WIDTH = 180;

    private static final String NEW_COLOR_PROPERTY_NAME = "color_RENAME_ME";
    private static final String NEW_POSITION_PROPERTY_NAME = "pos_RENAME_ME";
    private static final String NEW_TEXT_PROPERTY_NAME  = "RENAME_ME";

    private static final String SYMBOL_DELETE = "\u274C";

    public static boolean isProtectedProperty(String propertyName, LayerID layerID) {
        return switch (layerID) {
            case TERRAIN -> COLOR_WALL_FILL.equals(propertyName)
                || COLOR_WALL_STROKE.equals(propertyName)
                || COLOR_DOOR.equals(propertyName)
                || POS_HOUSE_MIN_TILE.equals(propertyName)
                || POS_HOUSE_MAX_TILE.equals(propertyName);
            case FOOD -> WorldMapProperty.COLOR_FOOD.equals(propertyName);
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
        worldMap().properties(layerID).put(propertyName, "green");
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
        worldMap().properties(layerID).put(propertyName, "(0,0)");
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
        worldMap().properties(layerID).put(propertyName, "any text");
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
            // primitive way of discriminating but fulfills its purpose
            WorldMapPropertyInfo propertyInfo;
            boolean protectedProperty = isProtectedProperty(propertyName, layerID);
            if (propertyName.startsWith("color_")) {
                propertyInfo = new WorldMapPropertyInfo(propertyName, WorldMapPropertyType.COLOR, protectedProperty);
            } else if (propertyName.startsWith("pos_") || propertyName.startsWith("tile_") || propertyName.startsWith("vec_")) {
                propertyInfo = new WorldMapPropertyInfo(propertyName, WorldMapPropertyType.TILE, protectedProperty);
            } else {
                propertyInfo = new WorldMapPropertyInfo(propertyName, WorldMapPropertyType.STRING, protectedProperty);
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
            if (!propertyEditor.propertyInfo.protectedProperty()) {
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

    private SinglePropertyEditor createEditor(TileMapEditorUI ui, WorldMapPropertyInfo info, String initialValue) {
        SinglePropertyEditor propertyEditor = switch (info.type()) {
            case COLOR -> new ColorPropertyEditor(ui, layerID, info, initialValue);
            case TILE ->  new TilePropertyEditor(ui, layerID, info, initialValue);
            case STRING -> new TextPropertyEditor(ui, layerID, info, initialValue);
        };
        propertyEditor.enabledProperty().bind(enabled);
        propertyEditor.worldMapProperty().bind(ui.editor().currentWorldMapProperty());
        return propertyEditor;
    }

}