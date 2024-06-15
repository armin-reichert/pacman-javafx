/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.model.world;

import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.Vector2f;
import de.amr.games.pacman.lib.Vector2i;
import de.amr.games.pacman.lib.tilemap.WorldMap;
import de.amr.games.pacman.model.GameModel;
import org.tinylog.Logger;

import java.util.Optional;

import static de.amr.games.pacman.lib.Globals.*;
import static de.amr.games.pacman.lib.tilemap.Tiles.*;
import static de.amr.games.pacman.model.GameModel.checkGhostID;

/**
 * @author Armin Reichert
 */
public class House {

    public static House createArcadeHouse(int topRow, int topCol) {
        var house = new House();
        house.setTopLeftTile(new Vector2i(topCol, topRow));
        house.setSize(v2i(8, 5));
        house.setDoor(new Door(v2i(topCol + 3, topRow), v2i(topCol + 4, topRow)));
        house.setGhostDirections(new Direction[] {Direction.LEFT, Direction.DOWN, Direction.UP, Direction.UP});
        return house;
    }

    private Vector2f pacPosition;
    private Vector2f[] ghostPositions;
    private Direction[] ghostDirections;
    private Vector2i minTile;
    private Vector2i size;
    private Door door;

    public void setTopLeftTile(Vector2i minTile) {
        this.minTile = checkTileNotNull(minTile);
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
        return minTile;
    }

    public Vector2i size() {
        return size;
    }

    public Door door() {
        return door;
    }

    public Vector2f center() {
        return minTile.toFloatVec().scaled(TS).plus(size.toFloatVec().scaled(HTS));
    }

    /**
     * @param tile some tile
     * @return tells if the given tile is part of this house
     */
    public boolean contains(Vector2i tile) {
        Vector2i max = minTile.plus(size().minus(1, 1));
        return tile.x() >= minTile.x() && tile.x() <= max.x() //
            && tile.y() >= minTile.y() && tile.y() <= max.y();
    }

    private Vector2f positionHalfTileRightOf(Vector2i tile) {
        return tile.scaled(TS).plus(HTS, 0).toFloatVec();
    }

    public void setPacPositionFromMap(WorldMap map) {
        Optional<Vector2i> pacHomeTile = map.terrain().tiles(PAC_HOME).findFirst();
        if (pacHomeTile.isEmpty()) {
            Logger.warn("No Pac home tile found in map, using default");
        }
        pacPosition = pacHomeTile.orElse(new Vector2i(13, 26)).toFloatVec().scaled(TS).plus(HTS, 0);
    }

    public void setGhostPositionsFromMap(WorldMap map) {
        ghostPositions = new Vector2f[4];

        Optional<Vector2i> homeTileRed = map.terrain().tiles(HOME_RED_GHOST).findFirst();
        if (homeTileRed.isEmpty()) {
            Logger.warn("No home tile set for red ghost, using default");
        }
        ghostPositions[GameModel.RED_GHOST] = positionHalfTileRightOf(homeTileRed.orElse(new Vector2i(13, 14)));

        Optional<Vector2i> homeTilePink = map.terrain().tiles(HOME_PINK_GHOST).findFirst();
        if (homeTilePink.isEmpty()) {
            Logger.warn("No home tile set for pink ghost, using default");
        }
        ghostPositions[GameModel.PINK_GHOST] = positionHalfTileRightOf(homeTilePink.orElse(new Vector2i(13, 17)));

        Optional<Vector2i> homeTileCyan = map.terrain().tiles(HOME_CYAN_GHOST).findFirst();
        if (homeTileCyan.isEmpty()) {
            Logger.warn("No home tile set for cyan ghost, using default");
        }
        ghostPositions[GameModel.CYAN_GHOST] = positionHalfTileRightOf(homeTileCyan.orElse(new Vector2i(11, 17)));

        Optional<Vector2i> homeTileOrange = map.terrain().tiles(HOME_ORANGE_GHOST).findFirst();
        if (homeTileOrange.isEmpty()) {
            Logger.warn("No home tile set for orange ghost, using default");
        }
        ghostPositions[GameModel.ORANGE_GHOST] = positionHalfTileRightOf(homeTileOrange.orElse(new Vector2i(15, 17)));
    }

    public void setPacPosition(Vector2f tile) {
        pacPosition = tile;
    }

    public Vector2f pacPosition() {
        return pacPosition;
    }

    public void setGhostPositions(Vector2f[] tiles) {
        ghostPositions = tiles;
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