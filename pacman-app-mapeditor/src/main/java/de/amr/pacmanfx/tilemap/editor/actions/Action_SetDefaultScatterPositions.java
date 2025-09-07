package de.amr.pacmanfx.tilemap.editor.actions;

import de.amr.pacmanfx.lib.tilemap.LayerID;
import de.amr.pacmanfx.lib.tilemap.WorldMap;
import de.amr.pacmanfx.tilemap.editor.TileMapEditor;

import static de.amr.pacmanfx.lib.tilemap.WorldMapFormatter.formatTile;
import static de.amr.pacmanfx.model.WorldMapProperty.*;
import static java.util.Objects.requireNonNull;

public class Action_SetDefaultScatterPositions extends AbstractEditorAction<Void> {

    private final WorldMap worldMap;

    public Action_SetDefaultScatterPositions(TileMapEditor editor) {
        this(editor, editor.currentWorldMap());
    }

    public Action_SetDefaultScatterPositions(TileMapEditor editor, WorldMap worldMap) {
        super(editor);
        this.worldMap = requireNonNull(worldMap);
    }

    @Override
    public Void execute() {
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