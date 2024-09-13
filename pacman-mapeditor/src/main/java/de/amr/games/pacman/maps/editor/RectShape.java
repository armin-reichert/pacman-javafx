/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.maps.editor;

import de.amr.games.pacman.lib.Vector2i;
import de.amr.games.pacman.lib.tilemap.TileMap;

/**
 * @author Armin Reichert
 */
class RectShape {

    private final byte[][] content;

    public RectShape(byte[][] data) {
        this.content = data;
    }

    public int sizeX() {
        return content[0].length;
    }

    public int sizeY() {
        return content.length;
    }

    public void addToMap(TileMapEditor editor, TileMap map, Vector2i originTile) {
        for (int row = 0; row < sizeY(); ++row) {
            for (int col = 0; col < sizeX(); ++col) {
                editor.setTileValue(map, originTile.plus(col, row), content[row][col]);
            }
        }
        editor.markTileMapEdited(map);
    }
}
