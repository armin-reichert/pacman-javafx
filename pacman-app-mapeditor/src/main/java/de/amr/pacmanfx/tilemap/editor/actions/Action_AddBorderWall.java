/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.tilemap.editor.actions;

import de.amr.pacmanfx.lib.tilemap.LayerID;
import de.amr.pacmanfx.lib.tilemap.TerrainTile;
import de.amr.pacmanfx.lib.tilemap.WorldMap;
import de.amr.pacmanfx.model.GameLevel;
import de.amr.pacmanfx.tilemap.editor.TileMapEditor;

import static java.util.Objects.requireNonNull;

public class Action_AddBorderWall extends AbstractEditorAction<Void> {

    private final WorldMap worldMap;

    public Action_AddBorderWall(TileMapEditor editor) {
        this(editor, editor.currentWorldMap());
    }

    public Action_AddBorderWall(TileMapEditor editor, WorldMap worldMap) {
        super(editor);
        this.worldMap = requireNonNull(worldMap);
    }

    private void setTerrain(int row, int col, TerrainTile terrainTile) {
        worldMap.layer(LayerID.TERRAIN).set(row, col, terrainTile.$);
    }

    @Override
    public Void execute() {
        int firstRow = GameLevel.EMPTY_ROWS_BELOW_MAZE + 1;
        int lastRow = worldMap.numRows() - 1 - GameLevel.EMPTY_ROWS_BELOW_MAZE;
        int firstCol = 0;
        int lastCol = worldMap.numCols() - 1;

        // Corners
        setTerrain(firstRow, firstCol, TerrainTile.ARC_NW);
        setTerrain(firstRow, lastCol,  TerrainTile.ARC_NE);
        setTerrain(lastRow,  firstCol, TerrainTile.ARC_SW);
        setTerrain(lastRow,  lastCol,  TerrainTile.ARC_SE);

        // Left and right border
        for (int row = firstRow + 1; row < lastRow; ++row) {
            setTerrain(row, firstCol, TerrainTile.WALL_V);
            setTerrain(row, lastCol,  TerrainTile.WALL_V);
        }

        // Top and bottom border
        for (int col = 1; col < lastCol; ++col) {
            setTerrain(firstRow, col, TerrainTile.WALL_H);
            setTerrain(lastRow,  col, TerrainTile.WALL_H);
        }

        editor.setTerrainMapChanged();
        editor.setEdited(true);

        return null;
    }
}