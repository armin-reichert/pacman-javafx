/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.mapeditor.actions;

import de.amr.pacmanfx.lib.math.Vector2i;
import de.amr.pacmanfx.mapeditor.TileMapEditor;
import de.amr.pacmanfx.model.world.TerrainTile;
import de.amr.pacmanfx.model.world.WorldMap;

import static java.util.Objects.requireNonNull;

public class Action_CarveTunnel extends EditorAction<Void> {

    private final WorldMap worldMap;
    private final Vector2i tunnelExit;

    public Action_CarveTunnel(TileMapEditor editor, WorldMap worldMap, Vector2i tunnelExit) {
        super(editor);
        this.worldMap = requireNonNull(worldMap);
        this.tunnelExit = requireNonNull(tunnelExit);
    }

    public Action_CarveTunnel(TileMapEditor editor, Vector2i tunnelExit) {
        this(editor, editor.currentWorldMap(), tunnelExit);
    }

    @Override
    public Void execute() {
        int depth = tunnelExit.x() + 1;
        if (canCarveTunnel(depth)) {
            boolean savedSymmetricMode = editor.symmetricEditMode();
            editor.setSymmetricEditMode(true);
            if (tunnelExit.x() == 0) {
                carveTunnelOfDepthOne();
            } else {
                carveTunnel();
            }
            editor.setSymmetricEditMode(savedSymmetricMode);
        }
        return null;
    }

    private boolean canCarveTunnel(int depth) {
        int col = tunnelExit.x(), row = tunnelExit.y();
        if (col >= worldMap.numCols() / 2) {
            return false;
        }
        int upperBorderY = worldMap.terrainLayer().emptyRowsOverMaze();
        int lowerBorderY = worldMap.numRows() - 1 - worldMap.terrainLayer().emptyRowsBelowMaze();
        if (depth == 1) {
            return row > upperBorderY && row < lowerBorderY;
        }
        else {
            return row - 2 > upperBorderY && row + 2 < lowerBorderY;
        }
    }

    private void carveTunnelOfDepthOne() {
        int col = tunnelExit.x(), row = tunnelExit.y();
        int upperBorderY = worldMap.terrainLayer().emptyRowsOverMaze();
        int lowerBorderY = worldMap.numRows() - 1 - worldMap.terrainLayer().emptyRowsBelowMaze();
        placeSymmetric(row - 1, col, row == upperBorderY + 1 ? TerrainTile.WALL_H.$ : TerrainTile.ARC_SE.$);
        placeSymmetric(row, col, TerrainTile.TUNNEL.$);
        placeSymmetric(row + 1, col, row == lowerBorderY - 1 ? TerrainTile.WALL_H.$ : TerrainTile.ARC_NE.$);
    }

    private void carveTunnel() {
        int row = tunnelExit.y();
        for (int col = tunnelExit.x(); col >= 0; --col) {
            if (col == 0) {
                placeSymmetric(row - 2, col, TerrainTile.ARC_SW.$);
                placeSymmetric(row - 1, col, TerrainTile.WALL_H.$);
                placeSymmetric(row + 1, col, TerrainTile.WALL_H.$);
                placeSymmetric(row + 2, col, TerrainTile.ARC_NW.$);
            }
            else if (col == tunnelExit.x()) {
                placeSymmetric(row - 2, col, TerrainTile.ARC_NE.$);
                placeSymmetric(row - 1, col, TerrainTile.ARC_SE.$);
                placeSymmetric(row + 1, col, TerrainTile.ARC_NE.$);
                placeSymmetric(row + 2, col, TerrainTile.ARC_SE.$);
            }
            else {
                placeSymmetric(row - 2, col, TerrainTile.WALL_H.$);
                placeSymmetric(row - 1, col, TerrainTile.WALL_H.$);
                placeSymmetric(row + 1, col, TerrainTile.WALL_H.$);
                placeSymmetric(row + 2, col, TerrainTile.WALL_H.$);
            }
            placeSymmetric(row, col, TerrainTile.TUNNEL.$);
        }
    }

    private void placeSymmetric(int row, int col, byte code) {
        new Action_SetTerrainTileCode(editor, worldMap, row, col, code).execute();
    }
}