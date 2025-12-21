/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.mapeditor.actions;

import de.amr.pacmanfx.lib.math.Vector2i;
import de.amr.pacmanfx.mapeditor.TileMapEditor;
import de.amr.pacmanfx.model.world.FoodTile;

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
        editor.currentWorldMap().foodLayer().setContent(tile, FoodTile.EMPTY.$);
        editor.setFoodMapChanged();
        editor.setEdited(true);
        return null;
    }
}
