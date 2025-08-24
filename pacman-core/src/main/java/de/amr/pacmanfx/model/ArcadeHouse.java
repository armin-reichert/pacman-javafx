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

    private static final Vector2i MIN_TILE = Vector2i.of(10, 15);
    private static final Vector2i MAX_TILE = Vector2i.of(17, 19);

    public ArcadeHouse() {
        super(MIN_TILE, MAX_TILE, MIN_TILE.plus(3, 0), MIN_TILE.plus(4, 0));
    }

    //TODO return immutable copy?
    public byte[][] content() {
        return CONTENT;
    }
}
