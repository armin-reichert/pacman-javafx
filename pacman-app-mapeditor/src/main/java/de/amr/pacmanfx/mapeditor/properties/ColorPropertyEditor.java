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

class ColorPropertyEditor extends PropertyEditor {

    private final ColorPicker colorPicker;

    public ColorPropertyEditor(TileMapEditorUI ui, LayerID layerID, WorldMapLayer layer, WorldMapLayer.Property property) {
        super(ui, layerID, layer, property);
        colorPicker = new ColorPicker();
        colorPicker.setPrefWidth(MapPropertiesEditor.VALUE_EDITOR_WIDTH);
        colorPicker.setValue(EditorUtil.parseColor(property.value()));
        colorPicker.disableProperty().bind(enabled.not());
        colorPicker.setOnAction(e -> storePropertyValue(ui));
    }

    @Override
    protected void updateEditorFromProperty() {
        String colorExpression = layer.properties().get(property.name());
        colorPicker.setValue(EditorUtil.parseColor(colorExpression));
    }

    @Override
    protected Node valueEditor() {
        return colorPicker;
    }

    @Override
    protected String formattedPropertyValue() {
        return EditorUtil.formatColor(colorPicker.getValue());
    }
}