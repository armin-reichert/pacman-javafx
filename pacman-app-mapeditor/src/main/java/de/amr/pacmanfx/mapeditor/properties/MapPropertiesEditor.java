/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.mapeditor.properties;

import de.amr.pacmanfx.lib.worldmap.LayerID;
import de.amr.pacmanfx.lib.worldmap.WorldMap;
import de.amr.pacmanfx.lib.worldmap.WorldMapLayer;
import de.amr.pacmanfx.lib.worldmap.WorldMapLayer.PropertyAttribute;
import de.amr.pacmanfx.lib.worldmap.WorldMapLayer.PropertyType;
import de.amr.pacmanfx.mapeditor.MessageType;
import de.amr.pacmanfx.mapeditor.TileMapEditorUI;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import org.tinylog.Logger;

import java.util.*;

import static de.amr.pacmanfx.model.DefaultWorldMapProperties.*;
import static java.util.Objects.requireNonNull;

public class MapPropertiesEditor extends BorderPane {

    public static final int NAME_EDITOR_WIDTH = 170;
    public static final int VALUE_EDITOR_WIDTH = 120;

    private static final String NEW_COLOR_PROPERTY_NAME = "color_RENAME_ME";
    private static final String NEW_POSITION_PROPERTY_NAME = "pos_RENAME_ME";
    private static final String NEW_TEXT_PROPERTY_NAME  = "RENAME_ME";

    public static final String DEFAULT_COLOR_VALUE = "rgba(0,0,0,1.0)";
    public static final String DEFAULT_TILE_VALUE = "(0,0)";
    public static final String DEFAULT_TEXT_VALUE = "no_text";

    public static final Map<LayerID, Set<String>> PREDEFINED_PROPERTIES = Map.of(
        LayerID.TERRAIN, Set.of(COLOR_WALL_FILL, COLOR_WALL_STROKE, COLOR_DOOR, POS_HOUSE_MIN_TILE, POS_HOUSE_MAX_TILE),
        LayerID.FOOD,    Set.of(COLOR_FOOD)
    );

    public static final Map<LayerID, Set<String>> HIDDEN_PROPERTIES = Map.of(
        LayerID.TERRAIN, Set.of(POS_HOUSE_MIN_TILE, POS_HOUSE_MAX_TILE),
        LayerID.FOOD,    Set.of()
    );

    public static final Set<String> COLOR_PREFIXES = Set.of("color_");
    public static final Set<String> TILE_PREFIXES = Set.of("pos_", "tile_", "vec_");

    // As long as the property type is not stored in the map file, we derive it from the name
    public static PropertyType determinePropertyType(String propertyName) {
        requireNonNull(propertyName);
        for (String prefix : COLOR_PREFIXES) {
            if (propertyName.startsWith(prefix)) {
                return PropertyType.COLOR_RGBA;
            }
        }
        for (String prefix : TILE_PREFIXES) {
            if (propertyName.startsWith(prefix)) {
                return PropertyType.TILE;
            }
        }
        return PropertyType.STRING;
    }

    private final TileMapEditorUI ui;
    private final LayerID layerID;
    private final BooleanProperty enabled = new SimpleBooleanProperty(true);
    private final List<PropertyEditorBase> propertyEditors = new ArrayList<>();
    private final GridPane grid = new GridPane(2, 2);

    public MapPropertiesEditor(TileMapEditorUI ui, LayerID layerID) {
        this.ui = requireNonNull(ui);
        this.layerID = requireNonNull(layerID);
        setTop(createButtonBar());
        setCenter(grid);
        ui.editor().currentWorldMapProperty().addListener((py, ov, worldMap) -> rebuildFromWorldMap(worldMap));
    }

    private Pane createButtonBar() {
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
        buttonBar.setPadding(new Insets(2, 2, 6, 2));
        buttonBar.setAlignment(Pos.CENTER_LEFT);

        return buttonBar;
    }
    
    private WorldMap worldMap() {
        return ui.editor().currentWorldMap();
    }

    private void addNewProperty(String propertyName, PropertyType type, String initialValue) {
        if (worldMap().properties(layerID).containsKey(propertyName)) {
            ui.messageDisplay().showMessage("Property %s already exists".formatted(propertyName), 1, MessageType.INFO);
            return;
        }
        WorldMapLayer.Property property = new WorldMapLayer.Property(
            propertyName, initialValue, type, WorldMapLayer.Property.emptyAttributeSet());
        PropertyEditorBase editor = createEditor(property);
        propertyEditors.add(0, editor);
        rebuildGrid();

        worldMap().properties(layerID).put(propertyName, property.value());
        ui.editor().setWorldMapChanged();
        ui.editor().setEdited(true);
        ui.messageDisplay().showMessage("New property %s added".formatted(propertyName), 1, MessageType.INFO);
    }

    private void addNewColorProperty() {
        addNewProperty(NEW_COLOR_PROPERTY_NAME, PropertyType.COLOR_RGBA, DEFAULT_COLOR_VALUE);
    }

    private void addNewPositionProperty() {
        addNewProperty(NEW_POSITION_PROPERTY_NAME, PropertyType.TILE, DEFAULT_TILE_VALUE);
    }

    private void addNewTextProperty() {
        addNewProperty(NEW_TEXT_PROPERTY_NAME, PropertyType.STRING, DEFAULT_TEXT_VALUE);
    }

    private void deleteProperty(WorldMapLayer.Property property) {
        var layerProperties = worldMap().properties(layerID);
        if (layerProperties.containsKey(property.name())) {
            layerProperties.remove(property.name());
            ui.messageDisplay().showMessage("Property '%s' deleted".formatted(property.name()), 3, MessageType.INFO);
            ui.editor().setWorldMapChanged();
            ui.editor().setEdited(true);
            findEditorFor(property).ifPresent(propertyEditors::remove);
            rebuildGrid();
        }
    }

    private Optional<PropertyEditorBase> findEditorFor(WorldMapLayer.Property property) {
        return propertyEditors.stream().filter(pe -> pe.property().name().equals(property.name())).findFirst();
    }

    public BooleanProperty enabledProperty() {
        return enabled;
    }

    private void rebuildFromWorldMap(WorldMap worldMap) {
        propertyEditors.clear();

        worldMap.propertyNames(layerID).sorted().forEach(propertyName -> {

            PropertyType type = determinePropertyType(propertyName);
            Set<PropertyAttribute> attributes = WorldMapLayer.Property.emptyAttributeSet();
            if (PREDEFINED_PROPERTIES.get(layerID).contains(propertyName)) {
                attributes.add(PropertyAttribute.PREDEFINED);
            }
            if (HIDDEN_PROPERTIES.get(layerID).contains(propertyName)) {
                attributes.add((PropertyAttribute.HIDDEN));
            }

            String propertyValue = worldMap.properties(layerID).get(propertyName);
            var property = new WorldMapLayer.Property(propertyName, propertyValue, type, attributes);

            PropertyEditorBase propertyEditor = createEditor(property);
            propertyEditors.add(propertyEditor);

            Logger.info("Added editor for property {}", propertyEditor.property());
        });

        rebuildGrid();
    }

    private PropertyEditorBase createEditor(WorldMapLayer.Property property) {
        PropertyEditorBase propertyEditor = switch (property.type()) {
            case COLOR_RGBA -> new ColorPropertyEditor(ui, layerID, worldMap().layer(layerID), property);
            case TILE       -> new TilePropertyEditor(ui, layerID, worldMap().layer(layerID), property);
            case STRING     -> new TextPropertyEditor(ui, layerID, worldMap().layer(layerID), property);
        };
        propertyEditor.enabledProperty().bind(enabled);
        propertyEditor.worldMapProperty().bind(ui.editor().currentWorldMapProperty());
        return propertyEditor;
    }

    private void rebuildGrid() {
        grid.getChildren().clear();
        int rowIndex = -1;
        for (PropertyEditorBase editor : propertyEditors) {
            if (editor.property().is(PropertyAttribute.HIDDEN)) {
                continue;
            }
            ++rowIndex;
            grid.add(editor.nameEditor, 0, rowIndex);
            grid.add(editor.valueEditor(), 1, rowIndex);
            if (editor.property().is(PropertyAttribute.PREDEFINED)) {
                grid.add(spacer(), 2, rowIndex);
            } else {
                Button btnDelete = editor.createDeleteButton();
                btnDelete.setOnAction(e -> deleteProperty(editor.property()));
                grid.add(btnDelete, 2, rowIndex);
            }
        }
    }

    private Node spacer() {
        var spacer = new Region();
        spacer.setMinWidth(30);
        spacer.setMaxWidth(30);
        spacer.setPrefWidth(30);
        return spacer;
    }

}