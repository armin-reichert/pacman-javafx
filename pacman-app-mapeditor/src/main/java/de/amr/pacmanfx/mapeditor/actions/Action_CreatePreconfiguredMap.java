/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.mapeditor.actions;

import de.amr.pacmanfx.lib.math.Vector2i;
import de.amr.pacmanfx.mapeditor.TileMapEditor;
import de.amr.pacmanfx.model.world.WorldMap;
import de.amr.pacmanfx.model.world.WorldMapPropertyName;

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
        WorldMap newMap = new WorldMap(numCols, numRows);
        new Action_SetDefaultMapColors(editor, newMap).execute();
        new Action_SetDefaultScatterPositions(editor, newMap).execute();
        new Action_AddBorderWall(editor, newMap).execute();
        if (newMap.numRows() >= 20) {
            Vector2i houseMinTile = Vector2i.of(numCols / 2 - 4, numRows / 2 - 3);
            new Action_PlaceArcadeHouse(editor, newMap, houseMinTile).execute();
            newMap.terrainLayer().propertyMap().put(WorldMapPropertyName.POS_PAC,   String.valueOf(houseMinTile.plus(3, 11)));
            newMap.terrainLayer().propertyMap().put(WorldMapPropertyName.POS_BONUS, String.valueOf(houseMinTile.plus(3, 5)));
        }
        newMap.terrainLayer().buildObstacleList();
        return newMap;
    }
}