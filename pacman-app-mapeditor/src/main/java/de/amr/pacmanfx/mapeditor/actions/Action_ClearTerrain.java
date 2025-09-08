/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.mapeditor.actions;

import de.amr.pacmanfx.lib.worldmap.LayerID;
import de.amr.pacmanfx.lib.worldmap.TerrainTile;
import de.amr.pacmanfx.mapeditor.TileMapEditor;

public class Action_ClearTerrain extends EditorAction<Void> {

    public Action_ClearTerrain(TileMapEditor editor) {
        super(editor);
    }

    @Override
    public Void execute() {
        editor.currentWorldMap().layer(LayerID.TERRAIN).setAll(TerrainTile.EMPTY.$);
        editor.setTerrainMapChanged();
        editor.setEdited(true);
        return null;
    }
}
