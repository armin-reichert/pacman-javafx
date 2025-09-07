package de.amr.pacmanfx.tilemap.editor.properties;

import de.amr.pacmanfx.lib.tilemap.LayerID;
import de.amr.pacmanfx.tilemap.editor.EditorUI;
import javafx.scene.Node;
import javafx.scene.control.ColorPicker;

import static de.amr.pacmanfx.tilemap.editor.EditorUtil.formatColor;
import static de.amr.pacmanfx.tilemap.editor.EditorUtil.parseColor;

class ColorPropertyEditor extends SinglePropertyEditor {

    private final ColorPicker colorPicker;

    public ColorPropertyEditor(EditorUI ui, LayerID layerID, PropertyInfo propertyInfo, String propertyValue) {
        super(ui, layerID, propertyInfo);
        colorPicker = new ColorPicker();
        colorPicker.setValue(parseColor(propertyValue));
        colorPicker.disableProperty().bind(enabled.not());
        colorPicker.setOnAction(e -> storePropertyValue(ui));
    }

    @Override
    protected void updateEditorFromProperty() {
        String colorExpression = worldMap().properties(layerID).get(propertyInfo.name());
        colorPicker.setValue(parseColor(colorExpression));
    }

    @Override
    protected Node valueEditor() {
        return colorPicker;
    }

    @Override
    protected String formattedPropertyValue() {
        return formatColor(colorPicker.getValue());
    }
}
