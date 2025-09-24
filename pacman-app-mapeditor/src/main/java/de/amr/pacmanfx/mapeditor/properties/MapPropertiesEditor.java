/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.mapeditor.properties;

import de.amr.pacmanfx.lib.worldmap.LayerID;
import de.amr.pacmanfx.lib.worldmap.WorldMap;
import de.amr.pacmanfx.lib.worldmap.WorldMapLayer;
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

import static de.amr.pacmanfx.model.DefaultWorldMapPropertyName.*;
import static java.util.Objects.requireNonNull;

public class MapPropertiesEditor extends BorderPane {

    public static final int NAME_EDITOR_WIDTH = 170;
    public static final int VALUE_EDITOR_WIDTH = 120;

    private static final String NEW_COLOR_PROPERTY_NAME = "color_RENAME_ME";
    private static final String NEW_POSITION_PROPERTY_NAME = "pos_RENAME_ME";
    private static final String NEW_TEXT_PROPERTY_NAME  = "RENAME_ME";

    public static final String DEFAULT_COLOR_VALUE = "rgba(0,0,0,1.0)";
    public static final String DEFAULT_TILE_VALUE = "(0,0)";
    public static final String DEFAULT_TEXT_VALUE = "";

    public static final Map<LayerID, Set<String>> PREDEFINED_PROPERTY_NAMES = Map.of(
        LayerID.TERRAIN, Set.of(COLOR_WALL_FILL, COLOR_WALL_STROKE, COLOR_DOOR, POS_HOUSE_MIN_TILE, POS_HOUSE_MAX_TILE),
        LayerID.FOOD,    Set.of(COLOR_FOOD)
    );

    public static final Map<LayerID, Set<String>> HIDDEN_PROPERTY_NAMES = Map.of(
        LayerID.TERRAIN, Set.of(POS_HOUSE_MIN_TILE, POS_HOUSE_MAX_TILE),
        LayerID.FOOD,    Set.of()
    );

    public static final Set<String> COLOR_PREFIXES = Set.of("color_");
    public static final Set<String> TILE_PREFIXES = Set.of("pos_", "tile_", "vec_");

    // As long as the property type is not stored in the map file, we derive it from the name
    public static MapEditorPropertyType determinePropertyType(String propertyName) {
        requireNonNull(propertyName);
        for (String prefix : COLOR_PREFIXES) {
            if (propertyName.startsWith(prefix)) {
                return MapEditorPropertyType.COLOR_RGBA;
            }
        }
        for (String prefix : TILE_PREFIXES) {
            if (propertyName.startsWith(prefix)) {
                return MapEditorPropertyType.TILE;
            }
        }
        return MapEditorPropertyType.STRING;
    }

    private final TileMapEditorUI ui;
    private final LayerID layerID;
    private final BooleanProperty enabled = new SimpleBooleanProperty(true);
    private final List<AbstractPropertyEditor> propertyEditors = new ArrayList<>();
    private final GridPane grid = new GridPane(2, 2);

    public MapPropertiesEditor(TileMapEditorUI ui, LayerID layerID) {
        this.ui = requireNonNull(ui);
        this.layerID = requireNonNull(layerID);
        setTop(createButtonBar());
        setCenter(grid);
        ui.editor().currentWorldMapProperty().addListener((py, ov, nv) -> createPropertyEditors());
    }

    public BooleanProperty enabledProperty() {
        return enabled;
    }

    public void updateEditorValues() {
        propertyEditors.forEach(editor -> {
            String value = layer().propertyMap().get(editor.property().name());
            editor.property().setValue(value);
            editor.updateState();
        });
    }

    private void createPropertyEditors() {
        propertyEditors.clear();
        layer().propertiesSortedByName().forEach(property -> {
            String propertyName = property.getKey();
            String propertyValue = property.getValue();

            var editorProperty = new MapEditorProperty();
            editorProperty.setName(propertyName);
            editorProperty.setValue(propertyValue);
            editorProperty.setType(determinePropertyType(propertyName));
            if (PREDEFINED_PROPERTY_NAMES.get(layerID).contains(propertyName)) {
                editorProperty.attributes().add(MapEditorPropertyAttribute.PREDEFINED);
            }
            if (HIDDEN_PROPERTY_NAMES.get(layerID).contains(propertyName)) {
                editorProperty.attributes().add((MapEditorPropertyAttribute.HIDDEN));
            }
            propertyEditors.add(createEditor(editorProperty));
            Logger.info("Added editor for {}", editorProperty);
        });
        rebuildGrid();
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

    private WorldMapLayer layer() {
        return worldMap().layer(layerID);
    }

    private MapEditorProperty addEditorProperty(String propertyName, MapEditorPropertyType type, String initialValue) {
        if (layer().propertyMap().containsKey(propertyName)) {
            ui.messageDisplay().showMessage("Property '%s' already exists".formatted(propertyName), 2, MessageType.WARNING);
            return null;
        }

        var editorProperty = new MapEditorProperty();
        editorProperty.setName(propertyName);
        editorProperty.setValue(initialValue);
        editorProperty.setType(type);

        propertyEditors.addFirst(createEditor(editorProperty));
        rebuildGrid();

        layer().propertyMap().put(propertyName, initialValue);
        ui.editor().setWorldMapChanged();
        ui.editor().setEdited(true);

        ui.messageDisplay().showMessage("New property '%s' added".formatted(propertyName), 2, MessageType.INFO);

        return editorProperty;
    }

    private void addNewColorProperty() {
        focusAndSelect(addEditorProperty(NEW_COLOR_PROPERTY_NAME, MapEditorPropertyType.COLOR_RGBA, DEFAULT_COLOR_VALUE));
    }

    private void addNewPositionProperty() {
        focusAndSelect(addEditorProperty(NEW_POSITION_PROPERTY_NAME, MapEditorPropertyType.TILE, DEFAULT_TILE_VALUE));
    }

    private void addNewTextProperty() {
        focusAndSelect(addEditorProperty(NEW_TEXT_PROPERTY_NAME, MapEditorPropertyType.STRING, DEFAULT_TEXT_VALUE));
    }

    private void focusAndSelect(MapEditorProperty editorProperty) {
        if (editorProperty != null) {
            findEditorFor(editorProperty).ifPresent(editor -> {
                editor.nameEditor.selectAll();
                editor.nameEditor.requestFocus();
            });
        }
    }

    private void deleteEditorProperty(MapEditorProperty editorProperty) {
        if (layer().propertyMap().containsKey(editorProperty.name())) {
            layer().propertyMap().remove(editorProperty.name());
            ui.editor().setWorldMapChanged();
            ui.editor().setEdited(true);
            findEditorFor(editorProperty).ifPresent(propertyEditors::remove);
            rebuildGrid();
            ui.messageDisplay().showMessage("Property '%s' deleted".formatted(editorProperty.name()), 3, MessageType.INFO);
        }
    }

    private Optional<AbstractPropertyEditor> findEditorFor(MapEditorProperty editorProperty) {
        return propertyEditors.stream().filter(editor -> editor.property().name().equals(editorProperty.name())).findFirst();
    }

    private AbstractPropertyEditor createEditor(MapEditorProperty editorProperty) {
        AbstractPropertyEditor editor = switch (editorProperty.type()) {
            case COLOR_RGBA -> new ColorPropertyEditor(ui, layerID, layer(), editorProperty);
            case TILE       -> new TilePropertyEditor(ui, layerID, layer(), editorProperty);
            case STRING     -> new TextPropertyEditor(ui, layerID, layer(), editorProperty);
        };
        editor.enabledProperty().bind(enabled);
        editor.worldMapProperty().bind(ui.editor().currentWorldMapProperty());
        return editor;
    }

    private void rebuildGrid() {
        grid.getChildren().clear();
        int rowIndex = -1;
        for (AbstractPropertyEditor editor : propertyEditors) {
            if (editor.property().is(MapEditorPropertyAttribute.HIDDEN)) {
                continue;
            }
            ++rowIndex;
            grid.add(editor.nameEditor, 0, rowIndex);
            grid.add(editor.valueEditor(), 1, rowIndex);
            if (editor.property().is(MapEditorPropertyAttribute.PREDEFINED)) {
                grid.add(spacer(), 2, rowIndex);
            } else {
                Button btnDelete = editor.createDeleteButton();
                btnDelete.setOnAction(e -> deleteEditorProperty(editor.property()));
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