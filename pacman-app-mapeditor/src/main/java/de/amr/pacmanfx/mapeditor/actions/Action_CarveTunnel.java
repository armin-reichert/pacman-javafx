/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.mapeditor.actions;

import de.amr.pacmanfx.lib.Vector2i;
import de.amr.pacmanfx.lib.worldmap.TerrainTile;
import de.amr.pacmanfx.lib.worldmap.WorldMap;
import de.amr.pacmanfx.mapeditor.TileMapEditor;
import de.amr.pacmanfx.model.GameLevel;

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
            boolean symmetricModeBefore = editor.symmetricEditMode();
            editor.setSymmetricEditMode(true);
            switch (depth) {
                case 1 -> carveTunnel1();
                default -> carveTunnel(depth);
            }
            editor.setSymmetricEditMode(symmetricModeBefore);
        }
        return null;
    }

    private boolean canCarveTunnel(int depth) {
        int x = tunnelExit.x(), y = tunnelExit.y();
        if (x >= worldMap.numCols() / 2) {
            return false;
        }
        int upperBorderY = GameLevel.EMPTY_ROWS_OVER_MAZE;
        int lowerBorderY = worldMap.numRows() - 1 - GameLevel.EMPTY_ROWS_BELOW_MAZE;
        if (depth == 1) {
            return y > upperBorderY && y < lowerBorderY;
        }
        else {
            return y - 2 > upperBorderY && y + 2 < lowerBorderY;
        }
    }

    private void carveTunnel1() {
        Vector2i tileAbove = tunnelExit.minus(0, 1);
        Vector2i tileBelow = tunnelExit.plus(0, 1);
        byte above = tileAbove.y() == GameLevel.EMPTY_ROWS_OVER_MAZE ? TerrainTile.WALL_H.$ : TerrainTile.ARC_SE.$;
        byte below = tileBelow.y() == worldMap.numRows() - 1 - GameLevel.EMPTY_ROWS_BELOW_MAZE ? TerrainTile.WALL_H.$ : TerrainTile.ARC_NE.$;
        new Action_SetTerrainTileCode(editor, worldMap, tileAbove, above).execute();
        new Action_SetTerrainTileCode(editor, worldMap, tunnelExit, TerrainTile.TUNNEL.$).execute();
        new Action_SetTerrainTileCode(editor, worldMap, tileBelow, below).execute();
    }

    private void carveTunnel(int depth) {
        int row = tunnelExit.y();;
        for (int x = tunnelExit.x(); x >= 0; --x) {
            if (x == 0) {
                new Action_SetTerrainTileCode(editor, worldMap, row - 2, x, TerrainTile.ARC_SW.$).execute();
                new Action_SetTerrainTileCode(editor, worldMap, row - 1, x, TerrainTile.WALL_H.$).execute();
                new Action_SetTerrainTileCode(editor, worldMap, row + 1, x, TerrainTile.WALL_H.$).execute();
                new Action_SetTerrainTileCode(editor, worldMap, row + 2, x, TerrainTile.ARC_NW.$).execute();
            }
            else if (x == tunnelExit.x()) {
                new Action_SetTerrainTileCode(editor, worldMap, row - 2, x, TerrainTile.ARC_NE.$).execute();
                new Action_SetTerrainTileCode(editor, worldMap, row - 1, x, TerrainTile.ARC_SE.$).execute();
                new Action_SetTerrainTileCode(editor, worldMap, row + 1, x, TerrainTile.ARC_NE.$).execute();
                new Action_SetTerrainTileCode(editor, worldMap, row + 2, x, TerrainTile.ARC_SE.$).execute();
            }
            else {
                new Action_SetTerrainTileCode(editor, worldMap, row - 2, x, TerrainTile.WALL_H.$).execute();
                new Action_SetTerrainTileCode(editor, worldMap, row - 1, x, TerrainTile.WALL_H.$).execute();
                new Action_SetTerrainTileCode(editor, worldMap, row + 1, x, TerrainTile.WALL_H.$).execute();
                new Action_SetTerrainTileCode(editor, worldMap, row + 2, x, TerrainTile.WALL_H.$).execute();
            }
            new Action_SetTerrainTileCode(editor, worldMap, row,     x, TerrainTile.TUNNEL.$).execute();
        }
    }
}
