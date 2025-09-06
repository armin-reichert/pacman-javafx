package de.amr.pacmanfx.tilemap.editor.actions;

import de.amr.pacmanfx.lib.tilemap.LayerID;
import de.amr.pacmanfx.lib.tilemap.WorldMap;
import de.amr.pacmanfx.tilemap.editor.TileMapEditor;

import static de.amr.pacmanfx.lib.tilemap.WorldMapFormatter.formatTile;
import static de.amr.pacmanfx.model.WorldMapProperty.*;

public class Action_SetDefaultScatterPositions extends AbstractEditorAction<Void> {

    public Action_SetDefaultScatterPositions(TileMapEditor editor) {
        super(editor);
    }

    @Override
    public Void execute() {
        WorldMap worldMap = editor.currentWorldMap();
        int numCols = worldMap.numCols(), numRows = worldMap.numRows();
        if (numCols >= 3 && numRows >= 2) {
            worldMap.properties(LayerID.TERRAIN).put(POS_SCATTER_RED_GHOST,    formatTile(numCols - 3, 0));
            worldMap.properties(LayerID.TERRAIN).put(POS_SCATTER_PINK_GHOST,   formatTile(2, 0));
            worldMap.properties(LayerID.TERRAIN).put(POS_SCATTER_CYAN_GHOST,   formatTile(numCols - 1, numRows - 2));
            worldMap.properties(LayerID.TERRAIN).put(POS_SCATTER_ORANGE_GHOST, formatTile(0, numRows - 2));
            editor.setTerrainMapChanged();
        }
        return null;
    }
}