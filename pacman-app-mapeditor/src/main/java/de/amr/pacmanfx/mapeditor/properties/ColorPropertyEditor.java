/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.mapeditor.properties;

import de.amr.pacmanfx.lib.worldmap.LayerID;
import de.amr.pacmanfx.mapeditor.EditorUI;
import de.amr.pacmanfx.mapeditor.EditorUtil;
import javafx.scene.Node;
import javafx.scene.control.ColorPicker;

class ColorPropertyEditor extends SinglePropertyEditor {

    private final ColorPicker colorPicker;

    public ColorPropertyEditor(EditorUI ui, LayerID layerID, PropertyInfo propertyInfo, String propertyValue) {
        super(ui, layerID, propertyInfo);
        colorPicker = new ColorPicker();
        colorPicker.setValue(EditorUtil.parseColor(propertyValue));
        colorPicker.disableProperty().bind(enabled.not());
        colorPicker.setOnAction(e -> storePropertyValue(ui));
    }

    @Override
    protected void updateEditorFromProperty() {
        String colorExpression = worldMap().properties(layerID).get(propertyInfo.name());
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
