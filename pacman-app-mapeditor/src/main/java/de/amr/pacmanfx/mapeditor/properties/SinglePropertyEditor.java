/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.mapeditor.properties;

import de.amr.pacmanfx.lib.worldmap.LayerID;
import de.amr.pacmanfx.lib.worldmap.WorldMap;
import de.amr.pacmanfx.lib.worldmap.WorldMapPropertyInfo;
import de.amr.pacmanfx.mapeditor.TileMapEditorUI;
import de.amr.pacmanfx.mapeditor.MessageType;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Node;
import javafx.scene.control.TextField;

import static java.util.Objects.requireNonNull;

abstract class SinglePropertyEditor {

    protected final BooleanProperty enabled = new SimpleBooleanProperty(true);
    protected final ObjectProperty<WorldMap> worldMap = new SimpleObjectProperty<>();

    protected final LayerID layerID;
    protected WorldMapPropertyInfo propertyInfo;
    protected final TextField nameEditor;

    protected SinglePropertyEditor(TileMapEditorUI ui, LayerID layerID, WorldMapPropertyInfo propertyInfo) {
        requireNonNull(ui);
        this.layerID = requireNonNull(layerID);
        this.propertyInfo = requireNonNull(propertyInfo);

        nameEditor = new TextField(propertyInfo.name());
        nameEditor.setMinWidth(MapPropertiesEditor.NAME_EDITOR_MIN_WIDTH);
        if (propertyInfo.protectedProperty()) {
            nameEditor.setDisable(true);
        } else {
            nameEditor.disableProperty().bind(enabled.not());
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
            nameEditor.setText(propertyInfo.name());
            return;
        }
        if (propertyInfo.name().equals(editedName)) {
            return;
        }
        if (WorldMapPropertyInfo.isInvalidPropertyName(editedName)) {
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
        boolean permanent = MapPropertiesEditor.isProtectedProperty(editedName, layerID);
        propertyInfo = new WorldMapPropertyInfo(editedName, propertyInfo.type(), permanent);

        ui.editor().setWorldMapChanged();
        ui.editor().setEdited(true);
    }

    protected void storePropertyValue(TileMapEditorUI ui) {
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
