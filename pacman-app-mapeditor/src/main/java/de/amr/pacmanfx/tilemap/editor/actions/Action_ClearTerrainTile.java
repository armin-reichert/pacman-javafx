/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.tilemap.editor.actions;

import de.amr.pacmanfx.lib.Vector2i;
import de.amr.pacmanfx.lib.tilemap.LayerID;
import de.amr.pacmanfx.lib.tilemap.TerrainTile;
import de.amr.pacmanfx.tilemap.editor.TileMapEditor;

/**
 * Ignores symmetric edit mode!
 */
public class Action_ClearTerrainTile extends AbstractEditorAction<Void> {

    private final Vector2i tile;

    public Action_ClearTerrainTile(TileMapEditor editor, Vector2i tile) {
        super(editor);
        this.tile = tile;
    }

    @Override
    public Void execute() {
        editor.currentWorldMap().setContent(LayerID.TERRAIN, tile, TerrainTile.EMPTY.$);
        editor.setTerrainMapChanged();
        editor.setEdited(true);
        return null;
    }
}
