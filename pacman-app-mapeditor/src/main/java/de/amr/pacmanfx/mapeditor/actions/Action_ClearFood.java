/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.mapeditor.actions;

import de.amr.pacmanfx.lib.worldmap.FoodTile;
import de.amr.pacmanfx.mapeditor.TileMapEditor;

public class Action_ClearFood extends EditorAction<Void> {

    public Action_ClearFood(TileMapEditor editor) {
        super(editor);
    }

    @Override
    public Void execute() {
        editor.currentWorldMap().foodLayer().setAll(FoodTile.EMPTY.$);
        editor.setFoodMapChanged();
        editor.setEdited(true);
        return null;
    }
}
