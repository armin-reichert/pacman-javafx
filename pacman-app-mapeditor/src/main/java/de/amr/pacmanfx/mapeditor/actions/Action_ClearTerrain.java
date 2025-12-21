/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.mapeditor.actions;

import de.amr.pacmanfx.mapeditor.TileMapEditor;
import de.amr.pacmanfx.model.world.TerrainTile;

public class Action_ClearTerrain extends EditorAction<Void> {

    public Action_ClearTerrain(TileMapEditor editor) {
        super(editor);
    }

    @Override
    public Void execute() {
        editor.currentWorldMap().terrainLayer().setAll(TerrainTile.EMPTY.$);
        new Action_DeleteArcadeHouse(editor).execute();
        editor.setTerrainMapChanged();
        editor.setEdited(true);
        return null;
    }
}
