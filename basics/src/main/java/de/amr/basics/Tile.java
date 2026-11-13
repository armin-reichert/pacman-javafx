/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.basics;

import de.amr.basics.math.Vector2f;
import de.amr.basics.math.Vector2i;

import static de.amr.basics.math.Vector2f.vec2_float;

public final class Tile {

    private Tile() {}

    /** Half tile size (4 pixel). */
    public static final int HTS = 4;

    /** Tile size (8 pixel). */
    public static final int TS = 8;

    public static Vector2i createTile(int x, int y) {
        return new Vector2i(x, y);
    }

    /**
     * @param numTiles number of tiles
     * @return pixels corresponding to given number of tiles
     */
    public static float tilesPx(double numTiles) { return (float) numTiles * TS; }

    /**
     * @param tileX tile x coordinate
     * @param tileY tile y coordinate
     * @return position (scaled by tile size) half tile right of tile origin
     */
    public static Vector2f halfTileRightOf(int tileX, int tileY) {
        return vec2_float(TS * tileX + HTS, TS * tileY);
    }

    /**
     * @param tile some tile
     * @return position (scaled by tile size) half tile right of tile origin
     */
    public static Vector2f halfTileRightOf(Vector2i tile) {
        return halfTileRightOf(tile.x(), tile.y());
    }
}
