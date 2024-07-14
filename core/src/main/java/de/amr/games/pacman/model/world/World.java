/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.model.world;

import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.Vector2i;
import de.amr.games.pacman.lib.tilemap.Tiles;
import de.amr.games.pacman.lib.tilemap.WorldMap;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static de.amr.games.pacman.lib.Globals.*;
import static de.amr.games.pacman.lib.tilemap.Tiles.*;
import static de.amr.games.pacman.model.GameModel.*;
import static java.util.Collections.unmodifiableList;

/**
 * @author Armin Reichert
 */
public class World {

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

    private final BitSet eatenFood;
    private final int totalFoodCount;
    private int uneatenFoodCount;

    private final List<Vector2i> energizerTiles;
    private List<Portal> portals;
    private Map<Vector2i, List<Direction>> forbiddenPassages = Map.of();
    private House house;

    public World(WorldMap map) {
        this.map = checkNotNull(map);
        findPortals();
        map.terrain().computeTerrainPaths();
        energizerTiles = map.food().tiles(ENERGIZER).toList();
        eatenFood = new BitSet(map.food().numCols() * map.food().numRows());
        uneatenFoodCount = totalFoodCount
            = (int) map.food().tiles().filter(tile -> map.food().get(tile) != EMPTY).count();
    }

    public Vector2i ghostScatterTile(byte ghostID) {
        checkGhostID(ghostID);
        return switch (ghostID) {
            case RED_GHOST -> map.terrain().getTileProperty(
                PROPERTY_POS_SCATTER_RED_GHOST, v2i(0, numCols() - 3));
            case PINK_GHOST -> map.terrain().getTileProperty(
                PROPERTY_POS_SCATTER_PINK_GHOST, v2i(0, 3));
            case CYAN_GHOST -> map.terrain().getTileProperty(
                PROPERTY_POS_SCATTER_CYAN_GHOST, new Vector2i(numRows()-1, numCols()-1));
            case ORANGE_GHOST -> map.terrain().getTileProperty(
                PROPERTY_POS_SCATTER_ORANGE_GHOST, new Vector2i(numRows()-1, 0));
            default -> throw new IllegalArgumentException("Illegal ghost ID: " + ghostID);
        };
    }

    private void findPortals() {
        portals = new ArrayList<>();
        int lastColumn = numCols() - 1;
        for (int row = 0; row < numRows(); ++row) {
            var leftBorderTile = v2i(0, row);
            var rightBorderTile = v2i(lastColumn, row);
            if (map.terrain().get(row, 0) == TUNNEL && map.terrain().get(row, lastColumn) == TUNNEL) {
                portals.add(new Portal(leftBorderTile, rightBorderTile, 2));
            }
        }
    }

    public void addHouse(House house) {
        this.house = checkNotNull(house);
    }

    public House house() {
        return house;
    }

    public int numCols() {
        return map.numCols();
    }

    public int numRows() {
        return map.numRows();
    }

    public Stream<Vector2i> tiles() {
        return map.terrain().tiles();
    }

    public WorldMap map() {
        return map;
    }

    public boolean insideBounds(Vector2i tile) {
        checkTileNotNull(tile);
        return !map.terrain().outOfBounds(tile.y(), tile.x());
    }

    public boolean containsPoint(double x, double y) {
        return 0 <= x && x <= numCols() * TS && 0 <= y && y <= numRows() * TS;
    }

    public void setForbiddenPassages(Map<Vector2i, List<Direction>> forbiddenPassages) {
        this.forbiddenPassages = forbiddenPassages;
    }

    public Map<Vector2i, List<Direction>> forbiddenPassages() {
        return forbiddenPassages;
    }

    /**
     * @return tiles in order top-to-bottom, left-to-right
     */
    public Stream<Vector2i> energizerTiles() {
        return energizerTiles.stream();
    }

    /**
     * @return Unmodifiable list of portals
     */
    public List<Portal> portals() {
        return unmodifiableList(portals);
    }

    public boolean belongsToPortal(Vector2i tile) {
        checkTileNotNull(tile);
        return portals.stream().anyMatch(portal -> portal.contains(tile));
    }

    public boolean isBlockedTile(Vector2i tile) {
        return insideBounds(tile) && isBlockedTerrain(map.terrain().get(tile));
    }

    private boolean isBlockedTerrain(byte content) {
        return content == WALL_H  || content == WALL_V
            || content == DWALL_H || content == DWALL_V
            || content == CORNER_NE  || content == CORNER_NW  || content == CORNER_SE  || content == CORNER_SW
            || content == DCORNER_NE || content == DCORNER_NW || content == DCORNER_SE || content == DCORNER_SW;
    }

    public boolean isTunnel(Vector2i tile) {
        if (!insideBounds(tile)) {
            return false;
        }
        return map.terrain().get(tile) == TUNNEL;
    }

    public boolean isIntersection(Vector2i tile) {
        checkTileNotNull(tile);
        if (!insideBounds(tile) || house.contains(tile)) {
            return false;
        }
        long numBlockedNeighbors = tile.neighbors().filter(this::insideBounds).filter(this::isBlockedTile).count();
        long numDoorNeighbors = tile.neighbors().filter(this::insideBounds).filter(house.door()::occupies).count();
        return numBlockedNeighbors + numDoorNeighbors < 2;
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
        if (!insideBounds(tile)) {
            return; // raise error?
        }
        if (hasFoodAt(tile)) {
            eatenFood.set(map.food().index(tile));
            --uneatenFoodCount;
        }
    }

    public boolean isFoodTile(Vector2i tile) {
        if (!insideBounds(tile)) {
            return false;
        }
        return map.food().get(tile) != EMPTY;
    }

    public boolean isEnergizerTile(Vector2i tile) {
        if (!insideBounds(tile)) {
            return false;
        }
        return map.food().get(tile) == ENERGIZER;
    }

    public boolean hasFoodAt(Vector2i tile) {
        if (!insideBounds(tile)) {
            return false;
        }
        return map.food().get(tile) != EMPTY && !eatenFood.get(map.food().index(tile));
    }

    public boolean hasEatenFoodAt(Vector2i tile) {
        if (!insideBounds(tile)) {
            return false;
        }
        return eatenFood.get(map.food().index(tile));
    }
}