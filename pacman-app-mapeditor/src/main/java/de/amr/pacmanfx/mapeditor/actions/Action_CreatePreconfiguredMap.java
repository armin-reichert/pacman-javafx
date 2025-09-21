/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.mapeditor.actions;

import de.amr.pacmanfx.lib.Vector2i;
import de.amr.pacmanfx.lib.worldmap.LayerID;
import de.amr.pacmanfx.lib.worldmap.WorldMap;
import de.amr.pacmanfx.mapeditor.TileMapEditor;
import de.amr.pacmanfx.model.DefaultWorldMapProperties;

public class Action_CreatePreconfiguredMap extends EditorAction<WorldMap> {

    private final int numRows;
    private final int numCols;

    public Action_CreatePreconfiguredMap(TileMapEditor editor, int numCols, int numRows) {
        super(editor);
        this.numCols = numCols;
        this.numRows = numRows;
    }

    @Override
    public WorldMap execute() {
        WorldMap newMap = WorldMap.emptyMap(numCols, numRows);
        new Action_SetDefaultMapColors(editor, newMap).execute();
        new Action_SetDefaultScatterPositions(editor, newMap).execute();
        new Action_AddBorderWall(editor, newMap).execute();
        if (newMap.numRows() >= 20) {
            Vector2i houseMinTile = Vector2i.of(numCols / 2 - 4, numRows / 2 - 3);
            new Action_PlaceArcadeHouse(editor, newMap, houseMinTile).execute();
            newMap.properties(LayerID.TERRAIN).put(DefaultWorldMapProperties.POS_PAC,   String.valueOf(houseMinTile.plus(3, 11)));
            newMap.properties(LayerID.TERRAIN).put(DefaultWorldMapProperties.POS_BONUS, String.valueOf(houseMinTile.plus(3, 5)));
        }
        newMap.buildObstacleList();
        return newMap;
    }
}