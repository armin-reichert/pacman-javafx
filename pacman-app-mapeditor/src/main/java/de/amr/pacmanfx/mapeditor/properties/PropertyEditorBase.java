/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.mapeditor.properties;

import de.amr.pacmanfx.lib.worldmap.LayerID;
import de.amr.pacmanfx.lib.worldmap.WorldMap;
import de.amr.pacmanfx.lib.worldmap.WorldMapLayer;
import de.amr.pacmanfx.mapeditor.EditorGlobals;
import de.amr.pacmanfx.mapeditor.MessageType;
import de.amr.pacmanfx.mapeditor.TileMapEditorUI;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.Background;
import javafx.scene.paint.Color;

import static java.util.Objects.requireNonNull;

abstract class PropertyEditorBase {

    private static final String SYMBOL_DELETE = "\u274C";

    protected final BooleanProperty enabled = new SimpleBooleanProperty(true);
    protected final ObjectProperty<WorldMap> worldMap = new SimpleObjectProperty<>();

    protected final TileMapEditorUI ui;
    protected final LayerID layerID;
    protected final WorldMapLayer layer;
    protected final TextField nameEditor;

    private WorldMapLayer.Property property;

    protected PropertyEditorBase(TileMapEditorUI ui, LayerID layerID, WorldMapLayer layer, WorldMapLayer.Property property) {
        this.ui = requireNonNull(ui);
        this.layerID = requireNonNull(layerID);
        this.layer = requireNonNull(layer);
        this.property = requireNonNull(property);

        nameEditor = new TextField(property.name());
        nameEditor.setMinWidth(MapPropertiesEditor.NAME_EDITOR_WIDTH);
        nameEditor.setMaxWidth(MapPropertiesEditor.NAME_EDITOR_WIDTH);
        nameEditor.disableProperty().bind(enabled.not());
        if (property.is(WorldMapLayer.PropertyAttribute.PREDEFINED)) {
            nameEditor.setEditable(false);
            nameEditor.setBackground(Background.fill(Color.LIGHTGRAY));
        } else {
            nameEditor.setEditable(true);
            nameEditor.setOnAction(e -> editPropertyName());
            nameEditor.focusedProperty().addListener((py, ov, hasFocus) -> {
                if (!hasFocus) { // focus lost
                    editPropertyName();
                }
            });
        }
    }

    public Button createDeleteButton() {
        var btnDelete = new Button(SYMBOL_DELETE);
        btnDelete.disableProperty().bind(enabled.not());
        Tooltip tooltip = new Tooltip("Delete"); //TODO localize
        tooltip.setFont(EditorGlobals.FONT_TOOL_TIPS);
        btnDelete.setTooltip(tooltip);
        return btnDelete;
    }

    public WorldMapLayer.Property property() {
        return property;
    }

    public void setProperty(WorldMapLayer.Property property) {
        this.property = property;
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

    private void rejectName(String newName, String reason) {
        nameEditor.setText(property.name());
        ui.messageDisplay().showMessage(reason.formatted(newName), 2, MessageType.ERROR);
    }

    private void editPropertyName() {
        String newName = nameEditor.getText().trim();
        if (property.name().equals(newName)) {
            return;
        }
        if (newName.isBlank()) {
            nameEditor.setText(property.name());
            return;
        }
        if (WorldMapLayer.Property.isInvalidPropertyName(newName)) {
            rejectName(newName, "Property name '%s' is invalid");
            return;
        }
        if (MapPropertiesEditor.PREDEFINED_PROPERTIES.get(layerID).contains(newName)) {
            rejectName(newName, "Property name '%s' is reserved");
            return;
        }
        if (MapPropertiesEditor.HIDDEN_PROPERTIES.get(layerID).contains(newName)) {
            rejectName(newName, "Property name '%s' is reserved");
            return;
        }
        if (layer.properties().containsKey(newName)) {
            rejectName(newName, "Property name '%s' already in use");
            return;
        }
        if (MapPropertiesEditor.COLOR_PREFIXES.contains(newName)) {
            rejectName(newName, "Color property name '%s' is too short");
            return;
        }
        if (MapPropertiesEditor.TILE_PREFIXES.contains(newName)) {
            rejectName(newName, "Tile property name '%s' is too short");
            return;
        }

        WorldMapLayer.PropertyType newType = MapPropertiesEditor.determinePropertyType(newName);
        if (newType != property.type()) {
            rejectName(newName, "Renaming property to '%s' would change type");
            return;
        }

        String oldName = property.name();
        layer.properties().remove(oldName);
        layer.properties().put(newName, formattedValue());
        ui.editor().setWorldMapChanged();
        ui.editor().setEdited(true);

        property = new WorldMapLayer.Property(
            newName,
            formattedValue(),
            property.type(),
            WorldMapLayer.Property.emptyAttributeSet());

        ui.messageDisplay().showMessage("Renamed property '%s' to '%s'".formatted(oldName, newName), 2, MessageType.INFO);
    }

    protected void storePropertyValue(TileMapEditorUI ui) {
        layer.properties().put(property.name(), formattedValue());
        ui.editor().setWorldMapChanged();
        ui.editor().setEdited(true);
    }

    protected abstract String formattedValue();

    protected abstract Node valueEditor();
}