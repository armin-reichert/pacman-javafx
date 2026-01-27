/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.mapeditor.actions;

import de.amr.pacmanfx.lib.math.Vector2i;
import de.amr.pacmanfx.mapeditor.TileMapEditor;
import de.amr.pacmanfx.model.world.TerrainTile;

/**
 * Ignores symmetric edit mode!
 */
public class Action_ClearTerrainTile extends EditorAction<Void> {

    private final Vector2i tile;

    public Action_ClearTerrainTile(TileMapEditor editor, Vector2i tile) {
        super(editor);
        this.tile = tile;
    }

    @Override
    public Void execute() {
        editor.currentWorldMap().terrainLayer().setContent(tile, TerrainTile.EMPTY.$);
        editor.setTerrainMapChanged();
        editor.setEdited(true);
        return null;
    }
}
