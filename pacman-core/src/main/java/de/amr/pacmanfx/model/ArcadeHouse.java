/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.model;

import de.amr.pacmanfx.lib.Vector2i;

import static de.amr.pacmanfx.lib.tilemap.TerrainTile.*;

public class ArcadeHouse extends House {

    private static final byte[][] CONTENT = {
        { ARC_NW.$, WALL_H.$, WALL_H.$, DOOR.$,   DOOR.$,   WALL_H.$, WALL_H.$, ARC_NE.$ },
        { WALL_V.$, EMPTY.$,  EMPTY.$,  EMPTY.$,  EMPTY.$,  EMPTY.$,  EMPTY.$,  WALL_V.$ },
        { WALL_V.$, EMPTY.$,  EMPTY.$,  EMPTY.$,  EMPTY.$,  EMPTY.$,  EMPTY.$,  WALL_V.$ },
        { WALL_V.$, EMPTY.$,  EMPTY.$,  EMPTY.$,  EMPTY.$,  EMPTY.$,  EMPTY.$,  WALL_V.$ },
        { ARC_SW.$, WALL_H.$, WALL_H.$, WALL_H.$, WALL_H.$, WALL_H.$, WALL_H.$, ARC_SE.$ }
    };

    public static final Vector2i DEFAULT_MIN_TILE = Vector2i.of(10, 15);
    public static final Vector2i DEFAULT_MAX_TILE = Vector2i.of(17, 19);

    public ArcadeHouse(Vector2i minTile) {
        super(minTile, minTile.plus(7, 4), minTile.plus(3, 0), minTile.plus(4, 0));
    }

    //TODO return immutable copy?
    public byte[][] content() {
        return CONTENT;
    }
}
