/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.model;

import de.amr.pacmanfx.Validations;
import de.amr.pacmanfx.lib.Vector2i;

import static de.amr.pacmanfx.Globals.*;
import static de.amr.pacmanfx.lib.worldmap.TerrainTile.*;
import static java.util.Objects.requireNonNull;

/**
 * Ghost house as used in Arcade Pac-Man games (8x5 tiles, entry with double door on top).
 */
public class ArcadeHouse implements House {

    /**
     * Top-left tile of ghost house in original Arcade maps (Pac-Man, Ms. Pac-Man).
     */
    public static final Vector2i ARCADE_MAP_HOUSE_MIN_TILE = Vector2i.of(10, 15);

    /**
     * Size of house in tiles (x=width, y=height).
     */
    public static final Vector2i SIZE_IN_TILES = Vector2i.of(8, 5);

    private static final byte[][] CONTENT = {
        { ARC_NW.$, WALL_H.$, WALL_H.$, DOOR.$,   DOOR.$,   WALL_H.$, WALL_H.$, ARC_NE.$ },
        { WALL_V.$, EMPTY.$,  EMPTY.$,  EMPTY.$,  EMPTY.$,  EMPTY.$,  EMPTY.$,  WALL_V.$ },
        { WALL_V.$, EMPTY.$,  EMPTY.$,  EMPTY.$,  EMPTY.$,  EMPTY.$,  EMPTY.$,  WALL_V.$ },
        { WALL_V.$, EMPTY.$,  EMPTY.$,  EMPTY.$,  EMPTY.$,  EMPTY.$,  EMPTY.$,  WALL_V.$ },
        { ARC_SW.$, WALL_H.$, WALL_H.$, WALL_H.$, WALL_H.$, WALL_H.$, WALL_H.$, ARC_SE.$ }
    };

    private static byte[][] copyOfContent() {
        byte[][] copy = new byte[CONTENT.length][];
        for (int i = 0; i < CONTENT.length; i++) {
            copy[i] = CONTENT[i].clone();
        }
        return copy;
    }

    private final Vector2i minTile;

    private final Vector2i[] ghostRevivalTiles = new Vector2i[4];

    public ArcadeHouse() {
        this(ARCADE_MAP_HOUSE_MIN_TILE);
        ghostRevivalTiles[RED_GHOST_SHADOW]   = Vector2i.of(13, 17);
        ghostRevivalTiles[PINK_GHOST_SPEEDY]  = Vector2i.of(13, 17);
        ghostRevivalTiles[CYAN_GHOST_BASHFUL] = Vector2i.of(11, 17);
        ghostRevivalTiles[ORANGE_GHOST_POKEY] = Vector2i.of(15, 17);
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
        return copyOfContent();
    }

    @Override
    public void setGhostRevivalTile(byte personality, Vector2i tile) {
        Validations.requireValidGhostPersonality(personality);
        requireNonNull(tile);
        ghostRevivalTiles[personality] = tile;
    }

    @Override
    public Vector2i ghostRevivalTile(byte personality) {
        Validations.requireValidGhostPersonality(personality);
        return ghostRevivalTiles[personality];
    }
}