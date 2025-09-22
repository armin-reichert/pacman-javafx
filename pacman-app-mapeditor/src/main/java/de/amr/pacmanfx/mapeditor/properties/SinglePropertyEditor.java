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
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Node;
import javafx.scene.control.TextField;
import javafx.scene.layout.Background;
import javafx.scene.paint.Color;

import java.util.EnumSet;

import static java.util.Objects.requireNonNull;

abstract class SinglePropertyEditor {

    protected final BooleanProperty enabled = new SimpleBooleanProperty(true);
    protected final ObjectProperty<WorldMap> worldMap = new SimpleObjectProperty<>();

    protected final LayerID layerID;
    protected final WorldMapLayer layer;
    protected WorldMap.Property property;
    protected final TextField nameEditor;
    protected String propertyName;

    protected SinglePropertyEditor(TileMapEditorUI ui, LayerID layerID, WorldMapLayer layer, String propertyName, WorldMap.Property property) {
        requireNonNull(ui);
        this.propertyName = requireNonNull(propertyName);
        this.layerID = requireNonNull(layerID);
        this.layer = requireNonNull(layer);
        this.property = requireNonNull(property);

        nameEditor = new TextField(propertyName);
        nameEditor.setMinWidth(MapPropertiesEditor.NAME_EDITOR_WIDTH);
        nameEditor.setMaxWidth(MapPropertiesEditor.NAME_EDITOR_WIDTH);
        nameEditor.disableProperty().bind(enabled.not());
        if (property.is(WorldMap.PropertyAttribute.PREDEFINED)) {
            nameEditor.setEditable(false);
            nameEditor.setBackground(Background.fill(Color.LIGHTGRAY));
        } else {
            nameEditor.setEditable(true);
            nameEditor.setOnAction(e -> onPropertyNameEdited(ui));
            nameEditor.focusedProperty().addListener((py, ov, hasFocus) -> {
                if (!hasFocus) {
                    onPropertyNameEdited(ui);
                }
            });
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

    protected void onPropertyNameEdited(TileMapEditorUI ui) {
        if (worldMap() == null) {
            return;
        }
        String editedName = nameEditor.getText().trim();
        if (editedName.isBlank()) {
            nameEditor.setText(propertyName);
            return;
        }
        if (propertyName.equals(editedName)) {
            return;
        }
        if (WorldMap.Property.isInvalidPropertyName(editedName)) {
            nameEditor.setText(propertyName);
            ui.messageDisplay().showMessage("Property name '%s' is invalid".formatted(editedName), 2, MessageType.ERROR);
            return;
        }
        if (MapPropertiesEditor.PREDEFINED_PROPERTIES.get(layerID).contains(editedName)) {
            ui.messageDisplay().showMessage("Property name is reserved", 2, MessageType.ERROR);
            nameEditor.setText(propertyName);
            return;
        }
        if (MapPropertiesEditor.HIDDEN_PROPERTIES.get(layerID).contains(editedName)) {
            ui.messageDisplay().showMessage("Property name is reserved", 2, MessageType.ERROR);
            nameEditor.setText(propertyName);
            return;
        }
        if (layer.properties().containsKey(editedName)) {
            ui.messageDisplay().showMessage("Property name already in use", 2, MessageType.ERROR);
            nameEditor.setText(propertyName);
            return;
        }
        ui.messageDisplay().showMessage("Property '%s' renamed to '%s'"
            .formatted(propertyName, editedName), 2, MessageType.INFO);

        layer.properties().remove(propertyName);
        layer.properties().put(editedName, formattedPropertyValue());

        property = new WorldMap.Property(property.type(), EnumSet.noneOf(WorldMap.PropertyAttribute.class));

        ui.editor().setWorldMapChanged();
        ui.editor().setEdited(true);
    }

    protected void storePropertyValue(TileMapEditorUI ui) {
        layer.properties().put(propertyName, formattedPropertyValue());
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
