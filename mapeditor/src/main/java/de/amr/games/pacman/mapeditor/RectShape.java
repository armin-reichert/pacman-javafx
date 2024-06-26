/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.mapeditor;

import de.amr.games.pacman.lib.tilemap.TileMap;

/**
 * @author Armin Reichert
 */
public class RectShape {

    private final byte[][] data;

    public RectShape(byte[][] data) {
        this.data = data;
    }

    public int sizeX() {
        return data[0].length;
    }

    public int sizeY() {
        return data.length;
    }

    public void addToMap(TileMap map, int targetRow, int targetCol) {
        for (int row = 0; row < sizeY(); ++row) {
            for (int col = 0; col < sizeX(); ++col) {
                map.set(targetRow + row, targetCol + col, data[row][col]);
            }
        }
    }
}
