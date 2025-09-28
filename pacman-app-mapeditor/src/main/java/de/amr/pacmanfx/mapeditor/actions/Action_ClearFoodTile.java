/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.mapeditor.actions;

import de.amr.pacmanfx.lib.Vector2i;
import de.amr.pacmanfx.lib.worldmap.FoodTile;
import de.amr.pacmanfx.mapeditor.TileMapEditor;

import static java.util.Objects.requireNonNull;

/**
 * Ignores symmetric edit mode!
 */
public class Action_ClearFoodTile extends EditorAction<Void> {

    private final Vector2i tile;

    public Action_ClearFoodTile(TileMapEditor editor, Vector2i tile) {
        super(editor);
        this.tile = requireNonNull(tile);
    }

    @Override
    public Void execute() {
        editor.currentWorldMap().foodLayer().set(tile, FoodTile.EMPTY.$);
        editor.setFoodMapChanged();
        editor.setEdited(true);
        return null;
    }
}
