/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.mapeditor.actions;

import de.amr.pacmanfx.mapeditor.TileMapEditor;

import static java.util.Objects.requireNonNull;

public class Action_SetFoodProperty extends EditorAction<Void> {

    private final String propertyName;
    private final String value;

    public Action_SetFoodProperty(TileMapEditor editor, String propertyName, String value) {
        super(editor);
        this.propertyName = requireNonNull(propertyName);
        this.value = requireNonNull(value);
    }

    @Override
    public Void execute() {
        var foodProperties = editor.currentWorldMap().foodLayer().propertyMap();
        if (foodProperties.containsKey(propertyName) && foodProperties.get(propertyName).equals(value)) {
            return null;
        }
        foodProperties.put(propertyName, value);
        editor.setFoodMapChanged();
        editor.setEdited(true);
        return null;
    }
}
