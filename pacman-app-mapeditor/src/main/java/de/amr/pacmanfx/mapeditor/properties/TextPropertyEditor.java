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

class TextPropertyEditor extends AbstractPropertyEditor {

    private final TextField textEditor;

    public TextPropertyEditor(TileMapEditorUI ui, LayerID layerID, WorldMapLayer layer, MapEditorProperty property) {
        super(ui, layerID, layer, property);
        textEditor = new TextField();
        textEditor.setPrefWidth(MapLayerPropertiesEditor.NAME_EDITOR_WIDTH);
        textEditor.setMinWidth(MapLayerPropertiesEditor.VALUE_EDITOR_WIDTH);
        textEditor.setMaxWidth(MapLayerPropertiesEditor.VALUE_EDITOR_WIDTH);
        textEditor.setText(property.value());
        textEditor.disableProperty().bind(enabled.not());
        textEditor.setOnAction(e -> storeValueInMapLayer());
    }

    @Override
    public void updateState() {
        textEditor.setText(property().value());
    }

    @Override
    protected Node valueEditor() {
        return textEditor;
    }

    @Override
    protected String formattedValue() {
        return textEditor.getText().strip();
    }
}