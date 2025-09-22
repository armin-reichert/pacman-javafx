/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.mapeditor.properties;

import de.amr.pacmanfx.lib.worldmap.LayerID;
import de.amr.pacmanfx.lib.worldmap.WorldMapLayer;
import de.amr.pacmanfx.mapeditor.TileMapEditorUI;
import javafx.scene.Node;
import javafx.scene.control.TextField;

class TextPropertyEditor extends PropertyEditor {

    private final TextField textEditor;

    public TextPropertyEditor(TileMapEditorUI ui, LayerID layerID, WorldMapLayer layer, WorldMapLayer.Property property) {
        super(ui, layerID, layer, property);
        textEditor = new TextField();
        textEditor.setPrefWidth(MapPropertiesEditor.NAME_EDITOR_WIDTH);
        textEditor.setMinWidth(MapPropertiesEditor.VALUE_EDITOR_WIDTH);
        textEditor.setMaxWidth(MapPropertiesEditor.VALUE_EDITOR_WIDTH);
        textEditor.setText(property.value());
        textEditor.disableProperty().bind(enabled.not());
        textEditor.setOnAction(e -> storePropertyValue(ui));
    }

    @Override
    protected void updateEditorFromProperty() {
        String text = layer.properties().get(property.value());
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