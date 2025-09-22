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

abstract class PropertyEditor {

    private static final String SYMBOL_DELETE = "\u274C";

    protected final BooleanProperty enabled = new SimpleBooleanProperty(true);
    protected final ObjectProperty<WorldMap> worldMap = new SimpleObjectProperty<>();

    protected final TileMapEditorUI ui;
    protected final LayerID layerID;
    protected final WorldMapLayer layer;
    protected final TextField nameEditor;

    private WorldMapLayer.Property property;

    protected PropertyEditor(TileMapEditorUI ui, LayerID layerID, WorldMapLayer layer, WorldMapLayer.Property property) {
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
            nameEditor.setOnAction(e -> onPropertyNameEdited());
            nameEditor.focusedProperty().addListener((py, ov, hasFocus) -> {
                if (!hasFocus) {
                    onPropertyNameEdited();
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

    protected void onPropertyNameEdited() {
        String newPropertyName = nameEditor.getText().trim();
        if (property.name().equals(newPropertyName)) {
            return;
        }
        if (newPropertyName.isBlank()) {
            nameEditor.setText(property.name());
            return;
        }
        if (WorldMapLayer.Property.isInvalidPropertyName(newPropertyName)) {
            nameEditor.setText(property.name());
            ui.messageDisplay().showMessage("Property name '%s' is invalid".formatted(newPropertyName), 2, MessageType.ERROR);
            return;
        }
        if (MapPropertiesEditor.PREDEFINED_PROPERTIES.get(layerID).contains(newPropertyName)) {
            nameEditor.setText(property.name());
            ui.messageDisplay().showMessage("Property name is reserved", 2, MessageType.ERROR);
            return;
        }
        if (MapPropertiesEditor.HIDDEN_PROPERTIES.get(layerID).contains(newPropertyName)) {
            nameEditor.setText(property.name());
            ui.messageDisplay().showMessage("Property name is reserved", 2, MessageType.ERROR);
            return;
        }
        if (layer.properties().containsKey(newPropertyName)) {
            ui.messageDisplay().showMessage("Property name already in use", 2, MessageType.ERROR);
            nameEditor.setText(property.name());
            return;
        }
        ui.messageDisplay().showMessage("Property '%s' renamed to '%s'".formatted(
            property.name(), newPropertyName), 2, MessageType.INFO);

        //TODO handle change of property type!

        layer.properties().remove(property.name());
        layer.properties().put(newPropertyName, formattedValue());

        property = new WorldMapLayer.Property(
            newPropertyName,
            formattedValue(),
            property.type(),
            WorldMapLayer.Property.emptyAttributeSet());

        ui.editor().setWorldMapChanged();
        ui.editor().setEdited(true);
    }

    protected void storePropertyValue(TileMapEditorUI ui) {
        layer.properties().put(property.name(), formattedValue());
        ui.editor().setWorldMapChanged();
        ui.editor().setEdited(true);
    }

    protected abstract String formattedValue();

    protected abstract Node valueEditor();
}