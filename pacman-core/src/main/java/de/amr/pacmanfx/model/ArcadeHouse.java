/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.model;

import de.amr.pacmanfx.Validations;
import de.amr.pacmanfx.lib.Direction;
import de.amr.pacmanfx.lib.Vector2f;
import de.amr.pacmanfx.lib.Vector2i;

import static de.amr.pacmanfx.Globals.*;
import static de.amr.pacmanfx.lib.worldmap.TerrainTile.*;
import static java.util.Objects.requireNonNull;

/**
 * Ghost house as used in Arcade Pac-Man games (8x5 tiles, entry with double door on top).
 */
public class ArcadeHouse implements House {

    /**
     * Size of house in tiles (x=width, y=height).
     */
    public static final Vector2i SIZE_IN_TILES = Vector2i.of(8, 5);

    public static final byte[][] CONTENT = {
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

    public ArcadeHouse(Vector2i minTile) {
        this.minTile = requireNonNull(minTile);
        ghostRevivalTiles[RED_GHOST_SHADOW]   = minTile.plus(3, 2);
        ghostRevivalTiles[PINK_GHOST_SPEEDY]  = minTile.plus(3, 2);
        ghostRevivalTiles[CYAN_GHOST_BASHFUL] = minTile.plus(1, 2);
        ghostRevivalTiles[ORANGE_GHOST_POKEY] = minTile.plus(5, 2);
    }

    @Override
    public Direction ghostStartDirection(byte personality) {
        return switch (personality) {
            case RED_GHOST_SHADOW -> Direction.LEFT;
            case PINK_GHOST_SPEEDY -> Direction.DOWN;
            case CYAN_GHOST_BASHFUL, ORANGE_GHOST_POKEY -> Direction.UP;
            default -> throw new IllegalStateException("Unexpected value: " + personality);
        };
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
        return minTile.plus(4, 0);
    }

    /**
     * @return position at which ghosts can enter the house, one tile above and horizontally between the two door tiles
     */
    public Vector2f entryPosition() {
        return rightDoorTile().toVector2f().scaled(TS).minus(HTS, TS);
    }

    /**
     * @return terrain map encoding of the house area
     */
    public byte[][] content() {
        return copyOfContent();
    }

    @Override
    public Vector2i ghostRevivalTile(byte personality) {
        Validations.requireValidGhostPersonality(personality);
        return ghostRevivalTiles[personality];
    }
}