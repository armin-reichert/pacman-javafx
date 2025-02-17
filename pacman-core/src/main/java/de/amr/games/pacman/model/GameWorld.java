/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.model;

import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.Vector2f;
import de.amr.games.pacman.lib.Vector2i;
import de.amr.games.pacman.lib.tilemap.FoodTiles;
import de.amr.games.pacman.lib.tilemap.TerrainTiles;
import de.amr.games.pacman.lib.tilemap.TileMap;
import de.amr.games.pacman.lib.tilemap.WorldMap;
import org.tinylog.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.stream.Stream;

import static de.amr.games.pacman.lib.Globals.*;
import static de.amr.games.pacman.lib.tilemap.TerrainTiles.*;
import static de.amr.games.pacman.lib.tilemap.WorldMap.*;
import static de.amr.games.pacman.model.GameModel.*;

/**
 * @author Armin Reichert
 */
public class GameWorld {

    private static boolean isInaccessible(byte content) {
        return content == WALL_H  || content == WALL_V
            || content == CORNER_NE  || content == CORNER_NW  || content == CORNER_SE  || content == CORNER_SW
            || content == DCORNER_ANGULAR_NE || content == DCORNER_ANGULAR_NW || content == DCORNER_ANGULAR_SE || content == DCORNER_ANGULAR_SW;
    }

    private final WorldMap map;
    private final Vector2f pacPosition;
    private final Vector2f[] ghostPositions = new Vector2f[4];
    private final Direction[] ghostDirections = new Direction[4];
    private final Vector2i[] energizerTiles;
    private final Portal[] portals;

    private Vector2i houseTopLeftTile;
    private Vector2i houseSize;
    private Vector2i leftDoorTile;
    private Vector2i rightDoorTile;

    // instead of Set<Vector2i> we use a bitset indexed by top-down-left-to-right tile index
    private final BitSet eatenFood;
    private final int totalFoodCount;
    private int uneatenFoodCount;

    public GameWorld(WorldMap map) {
        this.map = assertNotNull(map);
        TileMap terrain = map.terrain(), food = map.food();

        var portalList = new ArrayList<Portal>();
        int firstColumn = 0, lastColumn = terrain.numCols() - 1;
        for (int row = 0; row < terrain.numRows(); ++row) {
            Vector2i leftBorderTile = vec_2i(firstColumn, row), rightBorderTile = vec_2i(lastColumn, row);
            if (terrain.get(row, firstColumn) == TUNNEL && terrain.get(row, lastColumn) == TUNNEL) {
                portalList.add(new Portal(leftBorderTile, rightBorderTile, 2));
            }
        }
        portals = portalList.toArray(new Portal[0]);

        pacPosition                  = posHalfTileRightOf(terrain.getTileProperty(PROPERTY_POS_PAC,          vec_2i(13,26)));
        ghostPositions[RED_GHOST]    = posHalfTileRightOf(terrain.getTileProperty(PROPERTY_POS_RED_GHOST,    vec_2i(13,14)));
        ghostPositions[PINK_GHOST]   = posHalfTileRightOf(terrain.getTileProperty(PROPERTY_POS_PINK_GHOST,   vec_2i(13,17)));
        ghostPositions[CYAN_GHOST]   = posHalfTileRightOf(terrain.getTileProperty(PROPERTY_POS_CYAN_GHOST,   vec_2i(11,17)));
        ghostPositions[ORANGE_GHOST] = posHalfTileRightOf(terrain.getTileProperty(PROPERTY_POS_ORANGE_GHOST, vec_2i(15,17)));

        energizerTiles = food.tiles(FoodTiles.ENERGIZER).toArray(Vector2i[]::new);
        eatenFood = new BitSet(food.numCols() * food.numRows());
        uneatenFoodCount = totalFoodCount = (int) food.tiles().filter(tile -> food.get(tile) != EMPTY).count();
    }

    /**
     * @param tile a tile coordinate
     * @return position in world (scaled by tile size) between given tile and right neighbor tile
     */
    private static Vector2f posHalfTileRightOf(Vector2i tile) {
        return tile.scaled(TS).plus(HTS, 0).toVector2f();
    }

    public Vector2i ghostScatterTile(byte ghostID) {
        assertLegalGhostID(ghostID);
        TileMap terrain = map.terrain();
        int numRows = terrain.numRows(), numCols = terrain.numCols();
        return switch (ghostID) {
            case RED_GHOST    -> terrain.getTileProperty(PROPERTY_POS_SCATTER_RED_GHOST, vec_2i(0, numCols - 3));
            case PINK_GHOST   -> terrain.getTileProperty(PROPERTY_POS_SCATTER_PINK_GHOST, vec_2i(0, 3));
            case CYAN_GHOST   -> terrain.getTileProperty(PROPERTY_POS_SCATTER_CYAN_GHOST, vec_2i(numRows - 1, numCols - 1));
            case ORANGE_GHOST -> terrain.getTileProperty(PROPERTY_POS_SCATTER_ORANGE_GHOST, vec_2i(numRows - 1, 0));
            default -> throw new IllegalArgumentException("Illegal ghost ID: " + ghostID);
        };
    }

    public WorldMap map() {
        return map;
    }

    public boolean isOutsideWorld(Vector2i tile) {
        assertTileNotNull(tile);
        return map.terrain().outOfBounds(tile.y(), tile.x());
    }

    public boolean isInsideWorld(Vector2i tile) {
        return !isOutsideWorld(tile);
    }

    public boolean containsPoint(double x, double y) {
        return 0 <= x && x <= map.terrain().numCols() * TS && 0 <= y && y <= map.terrain().numRows() * TS;
    }

    public Stream<Vector2i> energizerTiles() {
        return Arrays.stream(energizerTiles);
    }

    public Stream<Portal> portals() {
        return Arrays.stream(portals);
    }

    public boolean isPortalAt(Vector2i tile) {
        assertTileNotNull(tile);
        return portals().anyMatch(portal -> portal.contains(tile));
    }

    public boolean isDoorAt(Vector2i tile) {
        assertTileNotNull(tile);
        return tile.equals(leftDoorTile) || tile.equals(rightDoorTile);
    }

    public boolean isBlockedTile(Vector2i tile) {
        return !isOutsideWorld(tile) && isInaccessible(map.terrain().get(tile));
    }

    public boolean isTunnel(Vector2i tile) {
        return !isOutsideWorld(tile) && map.terrain().get(tile) == TUNNEL;
    }

    public boolean isIntersection(Vector2i tile) {
        if (isOutsideWorld(tile) || isPartOfHouse(tile)) {
            return false;
        }
        long numBlockedNeighbors = tile.neighbors().filter(this::isInsideWorld).filter(this::isBlockedTile).count();
        long numDoorNeighbors = tile.neighbors().filter(this::isInsideWorld).filter(this::isDoorAt).count();
        return numBlockedNeighbors + numDoorNeighbors < 2;
    }

    // House

    /**
     * @param minX tile-x of top left corner
     * @param minY tile-y of top left corner
     * @param maxX tile-x of bottom right corner
     * @param maxY tile-y of bottom right corner
     */
    public void createArcadeHouse(int minX, int minY, int maxX, int maxY) {
        houseTopLeftTile = vec_2i(minX, minY);
        houseSize = vec_2i(maxX - minX + 1, maxY - minY + 1);
        leftDoorTile = vec_2i(minX + 3, minY);
        rightDoorTile = vec_2i(minX + 4, minY);
        setGhostDirection(RED_GHOST, Direction.LEFT);
        setGhostDirection(PINK_GHOST, Direction.DOWN);
        setGhostDirection(CYAN_GHOST, Direction.UP);
        setGhostDirection(ORANGE_GHOST, Direction.UP);

        // Create an obstacle for the house!
        //TODO change attributes to min_tile and max_tiles
        for (int y = minY; y <= maxY; ++y) {
            for (int x = minX; x <= maxX; ++x) {
                byte value = EMPTY;
                if      (x == minX && y == minY) value = CORNER_NW;
                else if (x == minX && y == maxY) value = CORNER_SW;
                else if (x == maxX && y == minY) value = CORNER_NE;
                else if (x == maxX && y == maxY) value = CORNER_SE;
                else if (y == minY && (x == minX + 3 || x == minX + 4)) value = DOOR;
                else if (x == minX || x == maxX) value = WALL_V;
                else if (y == minY || y == maxY) value = WALL_H;
                map.terrain().set(vec_2i(x, y), value);
            }
        }
    }

    public Vector2i houseTopLeftTile() {
        return houseTopLeftTile;
    }

    public Vector2i houseSize() {
        return houseSize;
    }

    public Vector2i houseLeftDoorTile() {
        return leftDoorTile;
    }

    public Vector2i houseRightDoorTile() {
        return rightDoorTile;
    }

    /**
     * @return position where ghost can enter the door
     */
    public Vector2f houseEntryPosition() {
        return vec_2f(TS * rightDoorTile.x() - HTS, TS * (rightDoorTile.y() - 1));
    }

    public Vector2f houseCenter() {
        return houseTopLeftTile.toVector2f().scaled(TS).plus(houseSize.toVector2f().scaled(HTS));
    }

    public float houseCeilingY() {
        return (houseTopLeftTile.y() + 1 ) * TS;
    }

    public float houseFloorY() {
        return (houseTopLeftTile.y() + houseSize().y() - 2) * TS;
    }

    /**
     * @param tile some tile
     * @return tells if the given tile is part of the ghost house
     */
    public boolean isPartOfHouse(Vector2i tile) {
        assertTileNotNull(tile);
        Vector2i max = houseTopLeftTile.plus(houseSize().minus(1, 1));
        return tile.x() >= houseTopLeftTile.x() && tile.x() <= max.x() //
            && tile.y() >= houseTopLeftTile.y() && tile.y() <= max.y();
    }

    public Vector2f pacPosition() {
        return pacPosition;
    }

    public void setGhostPosition(byte ghostID, Vector2f position) {
        assertLegalGhostID(ghostID);
        ghostPositions[ghostID] = position;
    }

    public Vector2f ghostPosition(byte ghostID) {
        assertLegalGhostID(ghostID);
        return ghostPositions[ghostID];
    }

    public void setGhostDirection(byte ghostID, Direction dir) {
        assertLegalGhostID(ghostID);
        assertNotNull(dir);
        ghostDirections[ghostID] = dir;
    }

    public Direction ghostDirection(byte ghostID) {
        assertLegalGhostID(ghostID);
        return ghostDirections[ghostID];
    }

    // Food

    public int totalFoodCount() {
        return totalFoodCount;
    }

    public int uneatenFoodCount() {
        return uneatenFoodCount;
    }

    public int eatenFoodCount() {
        return totalFoodCount - uneatenFoodCount;
    }

    public void registerFoodEatenAt(Vector2i tile) {
        if (hasFoodAt(tile)) {
            eatenFood.set(map.food().index(tile));
            --uneatenFoodCount;
        } else {
            Logger.warn("Attempt to eat foot at tile {} that has none", tile);
        }
    }

    public boolean isFoodPosition(Vector2i tile) {
        return !isOutsideWorld(tile) && map.food().get(tile) != EMPTY;
    }

    public boolean isEnergizerPosition(Vector2i tile) {
        return !isOutsideWorld(tile) && map.food().get(tile) == FoodTiles.ENERGIZER;
    }

    public boolean hasFoodAt(Vector2i tile) {
        return isFoodPosition(tile) && !hasEatenFoodAt(tile);
    }

    public boolean hasEatenFoodAt(Vector2i tile) {
        return !isOutsideWorld(tile) && eatenFood.get(map.food().index(tile));
    }
}