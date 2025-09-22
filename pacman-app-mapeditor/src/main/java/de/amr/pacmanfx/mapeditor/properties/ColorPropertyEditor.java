/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.mapeditor.properties;

import de.amr.pacmanfx.lib.worldmap.LayerID;
import de.amr.pacmanfx.lib.worldmap.WorldMapLayer;
import de.amr.pacmanfx.mapeditor.EditorUtil;
import de.amr.pacmanfx.mapeditor.TileMapEditorUI;
import javafx.scene.Node;
import javafx.scene.control.ColorPicker;
import javafx.scene.paint.Color;

class ColorPropertyEditor extends PropertyEditorBase {

    private final ColorPicker colorPicker;

    public ColorPropertyEditor(TileMapEditorUI ui, LayerID layerID, WorldMapLayer layer, WorldMapLayer.Property property) {
        super(ui, layerID, layer, property);
        colorPicker = new ColorPicker();
        colorPicker.setPrefWidth(MapPropertiesEditor.VALUE_EDITOR_WIDTH);
        colorPicker.setValue(EditorUtil.parseColor(property.value()));
        colorPicker.disableProperty().bind(enabled.not());
        colorPicker.setOnAction(e -> storeValueInMapLayer());
    }

    @Override
    public void updateState() {
        colorPicker.setValue(Color.valueOf(property().value()));
    }

    @Override
    protected Node valueEditor() {
        return colorPicker;
    }

    @Override
    protected String formattedValue() {
        return EditorUtil.formatColor(colorPicker.getValue());
    }
}