/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.mapeditor.properties;

import de.amr.pacmanfx.mapeditor.EditorUtil;
import de.amr.pacmanfx.mapeditor.TileMapEditorUI;
import de.amr.pacmanfx.model.world.WorldMapLayer;
import de.amr.pacmanfx.model.world.WorldMapLayerID;
import javafx.scene.Node;
import javafx.scene.control.ColorPicker;
import javafx.scene.paint.Color;

class ColorPropertyEditor extends AbstractPropertyEditor {

    private final ColorPicker colorPicker;

    public ColorPropertyEditor(TileMapEditorUI ui, WorldMapLayerID layerID, WorldMapLayer layer, MapEditorProperty property) {
        super(ui, layerID, layer, property);
        colorPicker = new ColorPicker();
        colorPicker.setPrefWidth(MapLayerPropertiesEditor.VALUE_EDITOR_WIDTH);
        colorPicker.setValue(Color.valueOf(property.value()));
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
        return EditorUtil.formatRGBA(colorPicker.getValue());
    }
}