/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.model.world;

import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.Vector2f;
import de.amr.games.pacman.lib.Vector2i;
import de.amr.games.pacman.lib.tilemap.WorldMap;

import static de.amr.games.pacman.lib.Globals.*;
import static de.amr.games.pacman.model.GameModel.*;

/**
 * @author Armin Reichert
 */
public class House {

    public static House createArcadeHouse(Vector2i topLeftTile) {
        var house = new House();
        house.setTopLeftTile(topLeftTile);
        house.setSize(v2i(8, 5));
        house.setDoor(new Door(v2i(topLeftTile.x() + 3, topLeftTile.y()), v2i(topLeftTile.x() + 4, topLeftTile.y())));
        house.setGhostDirections(new Direction[] {Direction.LEFT, Direction.DOWN, Direction.UP, Direction.UP});
        return house;
    }

    private Vector2f pacPosition;
    private Vector2f[] ghostPositions;
    private Direction[] ghostDirections;
    private Vector2i topLeftTile;
    private Vector2i size;
    private Door door;

    public void setTopLeftTile(Vector2i minTile) {
        this.topLeftTile = checkTileNotNull(minTile);
    }

    public void setSize(Vector2i size) {
        checkNotNull(size);
        if (size.x() < 1 || size.y() < 1) {
            throw new IllegalArgumentException("House size must be larger than one square tile but is: " + size);
        }
        this.size = size;
    }

    public void setDoor(Door door) {
        checkNotNull(door);
        this.door = door;
    }

    public Vector2i topLeftTile() {
        return topLeftTile;
    }

    public Vector2i size() {
        return size;
    }

    public Door door() {
        return door;
    }

    public Vector2f center() {
        return topLeftTile.toFloatVec().scaled(TS).plus(size.toFloatVec().scaled(HTS));
    }

    /**
     * @param tile some tile
     * @return tells if the given tile is part of this house
     */
    public boolean contains(Vector2i tile) {
        Vector2i max = topLeftTile.plus(size().minus(1, 1));
        return tile.x() >= topLeftTile.x() && tile.x() <= max.x() //
            && tile.y() >= topLeftTile.y() && tile.y() <= max.y();
    }

    private Vector2f positionHalfTileRightOf(Vector2i tile) {
        return tile.scaled(TS).plus(HTS, 0).toFloatVec();
    }

    public void setPacPositionFromMap(WorldMap map) {
        Vector2i pacHomeTile = map.terrain().getTileProperty(WorldMap.PROPERTY_POS_PAC, v2i(13, 26));
        pacPosition = pacHomeTile.toFloatVec().scaled(TS).plus(HTS, 0);
    }

    public void setGhostPositionsFromMap(WorldMap map) {
        ghostPositions = new Vector2f[4];

        Vector2i homeTileRed = map.terrain().getTileProperty(WorldMap.PROPERTY_POS_RED_GHOST, v2i(13,14));
        ghostPositions[RED_GHOST] = positionHalfTileRightOf(homeTileRed);

        Vector2i homeTilePink = map.terrain().getTileProperty(WorldMap.PROPERTY_POS_PINK_GHOST, v2i(13,17));
        ghostPositions[PINK_GHOST] = positionHalfTileRightOf(homeTilePink);

        Vector2i homeTileCyan = map.terrain().getTileProperty(WorldMap.PROPERTY_POS_CYAN_GHOST, v2i(11,17));
        ghostPositions[CYAN_GHOST] = positionHalfTileRightOf(homeTileCyan);

        Vector2i homeTileOrange = map.terrain().getTileProperty(WorldMap.PROPERTY_POS_ORANGE_GHOST, v2i(15,17));
        ghostPositions[ORANGE_GHOST] = positionHalfTileRightOf(homeTileOrange);
    }

    public Vector2f pacPosition() {
        return pacPosition;
    }

    public Vector2f ghostPosition(byte ghostID) {
        checkGhostID(ghostID);
        return ghostPositions[ghostID];
    }

    public void setGhostDirections(Direction[] dirs) {
        ghostDirections = dirs;
    }

    public Direction ghostDirection(byte ghostID) {
        checkGhostID(ghostID);
        return ghostDirections[ghostID];
    }
}