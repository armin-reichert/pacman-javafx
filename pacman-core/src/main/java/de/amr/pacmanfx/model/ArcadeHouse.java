/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.model;

import de.amr.pacmanfx.lib.Vector2f;
import de.amr.pacmanfx.lib.Vector2i;
import de.amr.pacmanfx.model.actors.GhostID;

import java.util.HashMap;
import java.util.Map;

import static de.amr.pacmanfx.Globals.TS;
import static de.amr.pacmanfx.lib.tilemap.TerrainTile.*;
import static java.util.Objects.requireNonNull;

public class ArcadeHouse implements House {

    private static final byte[][] CONTENT = {
        { ARC_NW.$, WALL_H.$, WALL_H.$, DOOR.$,   DOOR.$,   WALL_H.$, WALL_H.$, ARC_NE.$ },
        { WALL_V.$, EMPTY.$,  EMPTY.$,  EMPTY.$,  EMPTY.$,  EMPTY.$,  EMPTY.$,  WALL_V.$ },
        { WALL_V.$, EMPTY.$,  EMPTY.$,  EMPTY.$,  EMPTY.$,  EMPTY.$,  EMPTY.$,  WALL_V.$ },
        { WALL_V.$, EMPTY.$,  EMPTY.$,  EMPTY.$,  EMPTY.$,  EMPTY.$,  EMPTY.$,  WALL_V.$ },
        { ARC_SW.$, WALL_H.$, WALL_H.$, WALL_H.$, WALL_H.$, WALL_H.$, WALL_H.$, ARC_SE.$ }
    };

    public static final Vector2i DEFAULT_MIN_TILE = Vector2i.of(10, 15);
    public static final Vector2i DEFAULT_MAX_TILE = Vector2i.of(17, 19);

    private final Vector2i minTile;
    private final Vector2i maxTile;
    private final Vector2i leftDoorTile;
    private final Vector2i rightDoorTile;
    private final Map<GhostID, Vector2i> ghostRevivalTileMap = new HashMap<>();

    public ArcadeHouse(Vector2i minTile) {
        this.minTile = requireNonNull(minTile);
        this.maxTile = minTile.plus(7, 4);
        this.leftDoorTile = minTile.plus(3, 0);
        this.rightDoorTile = minTile.plus(4, 0);
    }

    @Override
    public Vector2i minTile() {
        return minTile;
    }

    @Override
    public Vector2i maxTile() {
        return maxTile;
    }

    @Override
    public Vector2i leftDoorTile() {
        return leftDoorTile;
    }

    @Override
    public Vector2i rightDoorTile() {
        return rightDoorTile;
    }

    @Override
    public void setGhostRevivalTile(GhostID ghostID, Vector2i tile) {
        requireNonNull(tile);
        ghostRevivalTileMap.put(ghostID, tile);
    }

    @Override
    public Vector2i ghostRevivalTile(GhostID ghostID) {
        return ghostRevivalTileMap.get(ghostID);
    }

    //TODO return immutable copy?
    public byte[][] content() {
        return CONTENT;
    }
}
