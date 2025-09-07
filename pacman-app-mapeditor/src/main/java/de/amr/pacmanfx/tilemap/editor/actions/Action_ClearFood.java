/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.tilemap.editor.actions;

import de.amr.pacmanfx.lib.tilemap.FoodTile;
import de.amr.pacmanfx.lib.tilemap.LayerID;
import de.amr.pacmanfx.tilemap.editor.TileMapEditor;

public class Action_ClearFood extends AbstractEditorAction<Void> {

    public Action_ClearFood(TileMapEditor editor) {
        super(editor);
    }

    @Override
    public Void execute() {
        editor.currentWorldMap().layer(LayerID.FOOD).setAll(FoodTile.EMPTY.code());
        editor.setFoodMapChanged();
        editor.setEdited(true);
        return null;
    }
}
