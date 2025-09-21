/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.mapeditor.properties;

import de.amr.pacmanfx.lib.worldmap.LayerID;
import de.amr.pacmanfx.lib.worldmap.WorldMap;
import de.amr.pacmanfx.mapeditor.TileMapEditorUI;
import javafx.scene.Node;
import javafx.scene.control.TextField;

class TextPropertyEditor extends SinglePropertyEditor {

    private final TextField textEditor;

    public TextPropertyEditor(TileMapEditorUI ui, LayerID layerID, String propertyName, WorldMap.Property property, String propertyValue) {
        super(ui, layerID, propertyName, property);
        textEditor = new TextField();
        textEditor.setPrefWidth(MapPropertiesEditor.NAME_EDITOR_WIDTH);
        textEditor.setMinWidth(MapPropertiesEditor.VALUE_EDITOR_WIDTH);
        textEditor.setMaxWidth(MapPropertiesEditor.VALUE_EDITOR_WIDTH);
        textEditor.setText(propertyValue);
        textEditor.disableProperty().bind(enabled.not());
        textEditor.setOnAction(e -> storePropertyValue(ui));
    }

    @Override
    protected void updateEditorFromProperty() {
        String text = worldMap().properties(layerID).get(propertyName);
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
