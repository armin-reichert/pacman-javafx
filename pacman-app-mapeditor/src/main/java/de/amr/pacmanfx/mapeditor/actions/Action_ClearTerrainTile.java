/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.mapeditor.actions;

import de.amr.basics.math.Vector2i;
import de.amr.pacmanfx.core.model.world.TerrainTile;
import de.amr.pacmanfx.mapeditor.TileMapEditor;

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
