package de.amr.pacmanfx.tilemap.editor;

import de.amr.pacmanfx.lib.tilemap.LayerID;
import de.amr.pacmanfx.lib.tilemap.TerrainTile;
import de.amr.pacmanfx.lib.tilemap.WorldMap;

import static de.amr.pacmanfx.tilemap.editor.TileMapEditor.EMPTY_ROWS_BEFORE_MAZE;
import static de.amr.pacmanfx.tilemap.editor.TileMapEditor.EMPTY_ROWS_BELOW_MAZE;

public class Action_AddBorderWall extends AbstractEditorAction {

    public void setWorldMap(WorldMap worldMap) {
        setArg("worldMap", worldMap);
    }

    @Override
    public void execute(TileMapEditor editor) {
        WorldMap worldMap = getArg("worldMap", WorldMap.class);
        int lastRow = worldMap.numRows() - 1 - EMPTY_ROWS_BELOW_MAZE, lastCol = worldMap.numCols() - 1;
        editor.setTileValueRespectingSymmetry(worldMap, LayerID.TERRAIN, EMPTY_ROWS_BEFORE_MAZE, 0, TerrainTile.ARC_NW.$);
        editor.setTileValueRespectingSymmetry(worldMap, LayerID.TERRAIN, EMPTY_ROWS_BEFORE_MAZE, lastCol, TerrainTile.ARC_NE.$);
        editor.setTileValueRespectingSymmetry(worldMap, LayerID.TERRAIN, lastRow, 0, TerrainTile.ARC_SW.$);
        editor.setTileValueRespectingSymmetry(worldMap, LayerID.TERRAIN, lastRow, lastCol, TerrainTile.ARC_SE.$);
        for (int row = EMPTY_ROWS_BEFORE_MAZE + 1; row < lastRow; ++row) {
            editor.setTileValueRespectingSymmetry(worldMap, LayerID.TERRAIN, row, 0, TerrainTile.WALL_V.$);
            editor.setTileValueRespectingSymmetry(worldMap, LayerID.TERRAIN, row, lastCol, TerrainTile.WALL_V.$);
        }
        for (int col = 1; col < lastCol; ++col) {
            editor.setTileValueRespectingSymmetry(worldMap, LayerID.TERRAIN, EMPTY_ROWS_BEFORE_MAZE, col, TerrainTile.WALL_H.$);
            editor.setTileValueRespectingSymmetry(worldMap, LayerID.TERRAIN, lastRow, col, TerrainTile.WALL_H.$);
        }
        editor.changeManager().setTerrainMapChanged();
    }
}
