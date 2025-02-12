/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.tilemap.editor;

import de.amr.games.pacman.lib.Vector2i;
import de.amr.games.pacman.lib.tilemap.FoodTiles;
import de.amr.games.pacman.lib.tilemap.TerrainTiles;
import de.amr.games.pacman.tilemap.rendering.TerrainColorScheme;
import javafx.scene.paint.Color;

import static de.amr.games.pacman.lib.Globals.HTS;
import static de.amr.games.pacman.lib.Globals.TS;

public class TileMatcher {

    private final TerrainColorScheme terrainColorScheme;
    private final Color foodColor;

    public TileMatcher(TerrainColorScheme terrainColorScheme, Color foodColor) {
        this.terrainColorScheme = terrainColorScheme;
        this.foodColor = foodColor;
    }

    public byte identifyFoodTile(Vector2i tile, Color[][] tileColors) {
        int numFoodPixels = 0;
        for (int row = 0; row < TS; ++row) {
            for (int col = 0; col < TS; ++col) {
                if (tileColors[row][col].equals(foodColor)) numFoodPixels++;
            }
        }
        if (numFoodPixels == 4 &&
                tileColors[3][3].equals(foodColor) &&
                tileColors[3][4].equals(foodColor) &&
                tileColors[4][3].equals(foodColor) &&
                tileColors[4][4].equals(foodColor)) {
            return FoodTiles.PELLET;
        }
        if (numFoodPixels > 50) return FoodTiles.ENERGIZER;
        return FoodTiles.EMPTY;
    }


    public byte identifyTerrainTile(Vector2i tile, Color[][] tileColors) {
        if (empty(tileColors)) {
            return TerrainTiles.EMPTY;
        }
        if (isNWCorner(tileColors)) {
            return TerrainTiles.CORNER_NW;
        }
        if (isSWCorner(tileColors)) {
            return TerrainTiles.CORNER_SW;
        }
        if (isNECorner(tileColors)) {
            return TerrainTiles.CORNER_NE;
        }
        if (isSECorner(tileColors)) {
            return TerrainTiles.CORNER_SE;
        }
        if (isHWall(tileColors)) {
            return TerrainTiles.WALL_H;
        }
        if (isVWall(tileColors)) {
            return TerrainTiles.WALL_V;
        }
        return TerrainTiles.EMPTY;
    }

    private boolean empty(Color[][] tileColors) {
        for (int row = 0; row < TS; ++row) {
            for (int col = 0; col < TS; ++col) {
                if (!tileColors[row][col].equals(Color.TRANSPARENT))
                    return false;
            }
        }
        return true;
    }

    private boolean isHWall(Color[][] tileColors) {
        boolean upperHalfEmpty = true;
        for (int row = 0; row < HTS; ++row) {
            for (int col = 0; col < TS; ++col) {
                if (!tileColors[row][col].equals(Color.TRANSPARENT))
                    upperHalfEmpty = false;
            }
        }
        boolean lowerHalfEmpty = true;
        for (int row = HTS; row < TS; ++row) {
            for (int col = 0; col < TS; ++col) {
                if (!tileColors[row][col].equals(Color.TRANSPARENT))
                    lowerHalfEmpty = false;
            }
        }
        return upperHalfEmpty || lowerHalfEmpty;
    }

    private boolean isVWall(Color[][] tileColors) {
        boolean leftHalfEmpty = true;
        for (int row = 0; row < TS; ++row) {
            for (int col = 0; col < HTS; ++col) {
                if (!tileColors[row][col].equals(Color.TRANSPARENT))
                    leftHalfEmpty = false;
            }
        }
        boolean rightHalfEmpty = true;
        for (int row = 0; row < TS; ++row) {
            for (int col = HTS + 1; col < TS; ++col) {
                if (!tileColors[row][col].equals(Color.TRANSPARENT))
                    rightHalfEmpty = false;
            }
        }
        return leftHalfEmpty || rightHalfEmpty;
    }

    private boolean isNWCorner(Color[][] tileColors) {
        for (int row = 0; row < TS; ++row) {
            for (int col = 0; col < TS - row; ++col) {
                if (!tileColors[row][col].equals(Color.TRANSPARENT)) {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean isSWCorner(Color[][] tileColors) {
        for (int row = 0; row < TS; ++row) {
            for (int col = 0; col <= row; ++col) {
                if (!tileColors[row][col].equals(Color.TRANSPARENT)) {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean isNECorner(Color[][] tileColors) {
        for (int row = 0; row < TS; ++row) {
            for (int col = row; col < TS; ++col) {
                if (!tileColors[row][col].equals(Color.TRANSPARENT)) {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean isSECorner(Color[][] tileColors) {
        for (int row = 0; row < TS; ++row) {
            for (int col = TS - row; col < TS; ++col) {
                if (!tileColors[row][col].equals(Color.TRANSPARENT)) {
                    return false;
                }
            }
        }
        return true;
    }

}
