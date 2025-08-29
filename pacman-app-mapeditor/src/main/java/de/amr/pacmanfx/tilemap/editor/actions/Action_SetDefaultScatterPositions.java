package de.amr.pacmanfx.tilemap.editor.actions;

import de.amr.pacmanfx.lib.Vector2i;
import de.amr.pacmanfx.lib.tilemap.LayerID;
import de.amr.pacmanfx.lib.tilemap.WorldMap;
import de.amr.pacmanfx.lib.tilemap.WorldMapFormatter;
import de.amr.pacmanfx.model.WorldMapProperty;
import de.amr.pacmanfx.tilemap.editor.TileMapEditor;

import static java.util.Objects.requireNonNull;

public class Action_SetDefaultScatterPositions extends AbstractEditorAction<Void> {

    private final WorldMap worldMap;

    public Action_SetDefaultScatterPositions(TileMapEditor editor, WorldMap worldMap) {
        super(editor);
        this.worldMap = requireNonNull(worldMap);
    }

    @Override
    public Void execute() {
        int numCols = worldMap.numCols(), numRows = worldMap.numRows();
        if (numCols >= 3 && numRows >= 2) {
            worldMap.properties(LayerID.TERRAIN).put(WorldMapProperty.POS_SCATTER_RED_GHOST,    WorldMapFormatter.formatTile(Vector2i.of(numCols - 3, 0)));
            worldMap.properties(LayerID.TERRAIN).put(WorldMapProperty.POS_SCATTER_PINK_GHOST,   WorldMapFormatter.formatTile(Vector2i.of(2, 0)));
            worldMap.properties(LayerID.TERRAIN).put(WorldMapProperty.POS_SCATTER_CYAN_GHOST,   WorldMapFormatter.formatTile(Vector2i.of(numCols - 1, numRows - 2)));
            worldMap.properties(LayerID.TERRAIN).put(WorldMapProperty.POS_SCATTER_ORANGE_GHOST, WorldMapFormatter.formatTile(Vector2i.of(0, numRows - 2)));
            editor.setTerrainMapChanged();
        }
        return null;
    }
}
