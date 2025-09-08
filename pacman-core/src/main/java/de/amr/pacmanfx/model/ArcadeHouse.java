/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.model;

import de.amr.pacmanfx.Validations;
import de.amr.pacmanfx.lib.Vector2i;
import de.amr.pacmanfx.model.actors.GhostID;

import java.util.HashMap;
import java.util.Map;

import static de.amr.pacmanfx.lib.worldmap.TerrainTile.*;
import static java.util.Objects.requireNonNull;

/**
 * Ghost house as used in Arcade Pac-Man games (8x5 tiles, entry on top).
 */
public class ArcadeHouse implements House {

    private static final byte[][] TERRAIN_ENCODING = {
        { ARC_NW.$, WALL_H.$, WALL_H.$, DOOR.$,   DOOR.$,   WALL_H.$, WALL_H.$, ARC_NE.$ },
        { WALL_V.$, EMPTY.$,  EMPTY.$,  EMPTY.$,  EMPTY.$,  EMPTY.$,  EMPTY.$,  WALL_V.$ },
        { WALL_V.$, EMPTY.$,  EMPTY.$,  EMPTY.$,  EMPTY.$,  EMPTY.$,  EMPTY.$,  WALL_V.$ },
        { WALL_V.$, EMPTY.$,  EMPTY.$,  EMPTY.$,  EMPTY.$,  EMPTY.$,  EMPTY.$,  WALL_V.$ },
        { ARC_SW.$, WALL_H.$, WALL_H.$, WALL_H.$, WALL_H.$, WALL_H.$, WALL_H.$, ARC_SE.$ }
    };

    private static byte[][] copyContent() {
        byte[][] copy = new byte[TERRAIN_ENCODING.length][];
        for (int i = 0; i < TERRAIN_ENCODING.length; i++) {
            copy[i] = TERRAIN_ENCODING[i].clone();
        }
        return copy;
    }

    /**
     * Top-left tile of ghost house in original Arcade games.
     */
    public static final Vector2i ORIGINAL_MIN_TILE = Vector2i.of(10, 15);

    /**
     * Size of house in tiles (x=width, y=height).
     */
    public static final Vector2i SIZE_IN_TILES = Vector2i.of(8, 5);

    private final Vector2i minTile;
    private final Map<GhostID, Vector2i> ghostRevivalTileMap = new HashMap<>(4);

    public ArcadeHouse() {
        this(ORIGINAL_MIN_TILE);
    }

    public ArcadeHouse(Vector2i minTile) {
        this.minTile = requireNonNull(minTile);
    }

    @Override
    public Vector2i minTile() {
        return minTile;
    }

    @Override
    public Vector2i maxTile() {
        return minTile.plus(SIZE_IN_TILES).minus(1, 1);
    }

    @Override
    public Vector2i leftDoorTile() {
        return minTile.plus(3, 0);
    }

    @Override
    public Vector2i rightDoorTile() {
        return leftDoorTile().plus(1, 0);
    }

    /**
     * @return terrain map encoding of the house area
     */
    public byte[][] content() {
        return copyContent();
    }

    @Override
    public void setGhostRevivalTile(GhostID ghostID, Vector2i tile) {
        requireNonNull(ghostID);
        Validations.requireValidGhostPersonality(ghostID.personality());
        requireNonNull(tile);
        ghostRevivalTileMap.put(ghostID, tile);
    }

    @Override
    public Vector2i ghostRevivalTile(GhostID ghostID) {
        requireNonNull(ghostID);
        Validations.requireValidGhostPersonality(ghostID.personality());
        return ghostRevivalTileMap.get(ghostID);
    }
}