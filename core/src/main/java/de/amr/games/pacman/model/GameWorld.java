/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.model;

import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.Vector2f;
import de.amr.games.pacman.lib.Vector2i;
import de.amr.games.pacman.lib.tilemap.WorldMap;
import org.tinylog.Logger;

import java.util.*;
import java.util.stream.Stream;

import static de.amr.games.pacman.lib.Globals.*;
import static de.amr.games.pacman.lib.tilemap.Tiles.*;
import static de.amr.games.pacman.model.GameModel.*;

/**
 * @author Armin Reichert
 */
public class GameWorld {

    public static final String PROPERTY_COLOR_FOOD               = "color_food";
    public static final String PROPERTY_COLOR_WALL_STROKE        = "color_wall_stroke";
    public static final String PROPERTY_COLOR_WALL_FILL          = "color_wall_fill";
    public static final String PROPERTY_COLOR_DOOR               = "color_door";
    public static final String PROPERTY_POS_BONUS                = "pos_bonus";
    public static final String PROPERTY_POS_PAC                  = "pos_pac";
    public static final String PROPERTY_POS_RED_GHOST            = "pos_ghost_1_red";
    public static final String PROPERTY_POS_PINK_GHOST           = "pos_ghost_2_pink";
    public static final String PROPERTY_POS_CYAN_GHOST           = "pos_ghost_3_cyan";
    public static final String PROPERTY_POS_ORANGE_GHOST         = "pos_ghost_4_orange";
    public static final String PROPERTY_POS_SCATTER_RED_GHOST    = "pos_scatter_ghost_1_red";
    public static final String PROPERTY_POS_SCATTER_PINK_GHOST   = "pos_scatter_ghost_2_pink";
    public static final String PROPERTY_POS_SCATTER_CYAN_GHOST   = "pos_scatter_ghost_3_cyan";
    public static final String PROPERTY_POS_SCATTER_ORANGE_GHOST = "pos_scatter_ghost_4_orange";

    private final WorldMap map;
    private final Vector2f pacPosition;
    private final Vector2f[] ghostPositions = new Vector2f[4];
    private final Direction[] ghostDirections = new Direction[4];
    private final Vector2i[] energizerTiles;
    private final Portal[] portals;
    private final Map<Vector2i, List<Direction>> forbiddenPassages = new HashMap<>(4);

    private Vector2i houseTopLeftTile;
    private Vector2i houseSize;
    private Vector2i leftDoorTile;
    private Vector2i rightDoorTile;

    // instead of Set<Vector2i> we use a bitset indexed by top-down-left-to-right tile index
    private final BitSet eatenFood;
    private final int totalFoodCount;
    private int uneatenFoodCount;

    public GameWorld(WorldMap map) {
        this.map = checkNotNull(map);
        var portalList = new ArrayList<Portal>();
        int firstColumn = 0, lastColumn = map.terrain().numCols() - 1;
        for (int row = 0; row < map.terrain().numRows(); ++row) {
            Vector2i leftBorderTile = v2i(firstColumn, row), rightBorderTile = v2i(lastColumn, row);
            if (map.terrain().get(row, firstColumn) == TUNNEL && map.terrain().get(row, lastColumn) == TUNNEL) {
                portalList.add(new Portal(leftBorderTile, rightBorderTile, 2));
            }
        }
        portals = portalList.toArray(new Portal[0]);

        map.terrain().computeTerrainPaths(); // not sure where to do this

        pacPosition                  = map.terrain().getTileProperty(PROPERTY_POS_PAC,          v2i(13,26)).scaled(TS).plus(4f, 0);
        ghostPositions[RED_GHOST]    = map.terrain().getTileProperty(PROPERTY_POS_RED_GHOST,    v2i(13,14)).scaled(TS).plus(4f, 0);
        ghostPositions[PINK_GHOST]   = map.terrain().getTileProperty(PROPERTY_POS_PINK_GHOST,   v2i(13,17)).scaled(TS).plus(4f, 0);
        ghostPositions[CYAN_GHOST]   = map.terrain().getTileProperty(PROPERTY_POS_CYAN_GHOST,   v2i(11,17)).scaled(TS).plus(4f, 0);
        ghostPositions[ORANGE_GHOST] = map.terrain().getTileProperty(PROPERTY_POS_ORANGE_GHOST, v2i(15,17)).scaled(TS).plus(4f, 0);

        energizerTiles = map.food().tiles(ENERGIZER).toArray(Vector2i[]::new);
        eatenFood = new BitSet(map.food().numCols() * map.food().numRows());
        uneatenFoodCount = totalFoodCount = (int) map.food().tiles().filter(tile -> map.food().get(tile) != EMPTY).count();
    }

    /**
     * @param topLeftX tile-x of top left corner
     * @param topLeftY tile-y of top left corner
     */
    public void createArcadeHouse(int topLeftX, int topLeftY) {
        setHouseArea(topLeftX, topLeftY, 8, 5);
        setDoorTiles(v2i(topLeftX + 3, topLeftY), v2i(topLeftX + 4, topLeftY));
        setGhostDirection(RED_GHOST, Direction.LEFT);
        setGhostDirection(PINK_GHOST, Direction.DOWN);
        setGhostDirection(CYAN_GHOST, Direction.UP);
        setGhostDirection(ORANGE_GHOST, Direction.UP);
    }

    public Vector2i ghostScatterTile(byte ghostID) {
        checkGhostID(ghostID);
        return switch (ghostID) {
            case RED_GHOST -> map.terrain().getTileProperty(
                PROPERTY_POS_SCATTER_RED_GHOST, v2i(0, map.terrain().numCols() - 3));
            case PINK_GHOST -> map.terrain().getTileProperty(
                PROPERTY_POS_SCATTER_PINK_GHOST, v2i(0, 3));
            case CYAN_GHOST -> map.terrain().getTileProperty(
                PROPERTY_POS_SCATTER_CYAN_GHOST, v2i(map.terrain().numRows() - 1, map.terrain().numCols() - 1));
            case ORANGE_GHOST -> map.terrain().getTileProperty(
                PROPERTY_POS_SCATTER_ORANGE_GHOST, v2i(map.terrain().numRows() - 1, 0));
            default -> throw new IllegalArgumentException("Illegal ghost ID: " + ghostID);
        };
    }

    public WorldMap map() {
        return map;
    }

    public boolean isOutsideWorld(Vector2i tile) {
        checkTileNotNull(tile);
        return map.terrain().outOfBounds(tile.y(), tile.x());
    }

    public boolean isInsideWorld(Vector2i tile) {
        return !isOutsideWorld(tile);
    }

    public boolean containsPoint(double x, double y) {
        return 0 <= x && x <= map.terrain().numCols() * TS && 0 <= y && y <= map.terrain().numRows() * TS;
    }

    public void setForbiddenPassages(Map<Vector2i, List<Direction>> map) {
        forbiddenPassages.clear();
        forbiddenPassages.putAll(map);
    }

    public Map<Vector2i, List<Direction>> forbiddenPassages() {
        return forbiddenPassages;
    }

    public Stream<Vector2i> energizerTiles() {
        return Arrays.stream(energizerTiles);
    }

    public Stream<Portal> portals() {
        return Arrays.stream(portals);
    }

    public boolean isPortalAt(Vector2i tile) {
        checkTileNotNull(tile);
        return portals().anyMatch(portal -> portal.contains(tile));
    }

    public boolean isDoorAt(Vector2i tile) {
        checkTileNotNull(tile);
        return tile.equals(leftDoorTile) || tile.equals(rightDoorTile);
    }

    public boolean isBlockedTile(Vector2i tile) {
        return !isOutsideWorld(tile) && isBlockedTerrain(map.terrain().get(tile));
    }

    private boolean isBlockedTerrain(byte content) {
        return content == WALL_H  || content == WALL_V
            || content == DWALL_H || content == DWALL_V
            || content == CORNER_NE  || content == CORNER_NW  || content == CORNER_SE  || content == CORNER_SW
            || content == DCORNER_NE || content == DCORNER_NW || content == DCORNER_SE || content == DCORNER_SW;
    }

    public boolean isTunnel(Vector2i tile) {
        if (isOutsideWorld(tile)) {
            return false;
        }
        return map.terrain().get(tile) == TUNNEL;
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

    public void setHouseArea(int topLeftX, int topLeftY, int numTilesX, int numTilesY)  {
        houseTopLeftTile = v2i(topLeftX, topLeftY);
        houseSize = v2i(numTilesX, numTilesY);
    }

    public void setDoorTiles(Vector2i leftTile, Vector2i rightTile) {
        checkNotNull(leftTile);
        checkNotNull(rightTile);
        leftDoorTile = leftTile;
        rightDoorTile = rightTile;
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
        return v2f(TS * rightDoorTile.x() - HTS, TS * (rightDoorTile.y() - 1));
    }

    public Vector2f houseCenter() {
        return houseTopLeftTile.toVector2f().scaled(TS).plus(houseSize.toVector2f().scaled(HTS));
    }

    /**
     * @param tile some tile
     * @return tells if the given tile is part of the ghost house
     */
    public boolean isPartOfHouse(Vector2i tile) {
        Vector2i max = houseTopLeftTile.plus(houseSize().minus(1, 1));
        return tile.x() >= houseTopLeftTile.x() && tile.x() <= max.x() //
            && tile.y() >= houseTopLeftTile.y() && tile.y() <= max.y();
    }

    public Vector2f pacPosition() {
        return pacPosition;
    }

    public Vector2f ghostPosition(byte ghostID) {
        checkGhostID(ghostID);
        return ghostPositions[ghostID];
    }

    public void setGhostDirection(byte ghostID, Direction dir) {
        checkGhostID(ghostID);
        checkNotNull(dir);
        ghostDirections[ghostID] = dir;
    }

    public Direction ghostDirection(byte ghostID) {
        checkGhostID(ghostID);
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

    public void eatFoodAt(Vector2i tile) {
        if (isOutsideWorld(tile)) {
            Logger.error("Attempt to eat food from non-food tile");
            return;
        }
        if (hasFoodAt(tile)) {
            eatenFood.set(map.food().index(tile));
            --uneatenFoodCount;
        }
    }

    public boolean isFoodPosition(Vector2i tile) {
        return !isOutsideWorld(tile) && map.food().get(tile) != EMPTY;
    }

    public boolean isEnergizerPosition(Vector2i tile) {
        return !isOutsideWorld(tile) && map.food().get(tile) == ENERGIZER;
    }

    public boolean hasFoodAt(Vector2i tile) {
        return isFoodPosition(tile) && !hasEatenFoodAt(tile);
    }

    public boolean hasEatenFoodAt(Vector2i tile) {
        return !isOutsideWorld(tile) && eatenFood.get(map.food().index(tile));
    }
}