package de.amr.pacmanfx.tilemap.editor.actions;

import de.amr.pacmanfx.lib.tilemap.LayerID;
import de.amr.pacmanfx.lib.tilemap.TerrainTile;
import de.amr.pacmanfx.tilemap.editor.TileMapEditor;

public class Action_ClearTerrain extends AbstractEditorAction<Void> {

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
