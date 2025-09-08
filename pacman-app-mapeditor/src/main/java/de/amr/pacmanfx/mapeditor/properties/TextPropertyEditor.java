/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.mapeditor.properties;

import de.amr.pacmanfx.lib.tilemap.LayerID;
import de.amr.pacmanfx.mapeditor.EditorUI;
import javafx.scene.Node;
import javafx.scene.control.TextField;

class TextPropertyEditor extends SinglePropertyEditor {

    private final TextField textEditor;

    public TextPropertyEditor(EditorUI ui, LayerID layerID, PropertyInfo propertyInfo, String propertyValue) {
        super(ui, layerID, propertyInfo);
        textEditor = new TextField();
        textEditor.setText(propertyValue);
        textEditor.disableProperty().bind(enabled.not());
        textEditor.setOnAction(e -> storePropertyValue(ui));
    }

    @Override
    protected void updateEditorFromProperty() {
        String text = worldMap().properties(layerID).get(propertyInfo.name());
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
