/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.model.world;

import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.Vector2f;
import de.amr.games.pacman.lib.Vector2i;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static de.amr.games.pacman.lib.Globals.*;
import static de.amr.games.pacman.model.world.Tiles.*;
import static java.util.Collections.unmodifiableList;

/**
 * @author Armin Reichert
 */
public class World {

    private final TileMap terrainMap;
    private final TileMap foodMap;
    private final List<Vector2i> energizerTiles;
    private final List<Portal> portals;
    private final House house;

    private final BitSet eaten;
    private final int totalFoodCount;
    private int uneatenFoodCount;

    private Vector2f pacPosition;
    private Vector2f[] ghostPositions;
    private Direction[] ghostDirections;
    private Vector2i[] ghostScatterTiles;
    private Vector2f bonusPosition;

    private Map<Vector2i, List<Direction>> forbiddenPassages = Map.of();

    /**
     * @param terrainMap terrain map
     * @param foodMap food map
     * @param house ghost house
     */
    public World(TileMap terrainMap, TileMap foodMap, House house) {
        this.terrainMap = checkNotNull(terrainMap);
        this.foodMap = checkNotNull(foodMap);
        this.house   = checkNotNull(house);

        // build portals
        var portalList = new ArrayList<Portal>();
        int lastColumn = terrainMap.numCols() - 1;
        for (int row = 0; row < terrainMap.numRows(); ++row) {
            var leftBorderTile = v2i(0, row);
            var rightBorderTile = v2i(lastColumn, row);
            if (terrainMap.content(row, 0) == Tiles.TUNNEL && terrainMap.content(row, lastColumn) == Tiles.TUNNEL) {
                portalList.add(new Portal(leftBorderTile, rightBorderTile, 2));
            }
        }
        portalList.trimToSize();
        portals = unmodifiableList(portalList);

        energizerTiles = foodMap.tiles().filter(this::isEnergizerTile).toList();
        eaten = new BitSet(foodMap.numCols() * foodMap.numRows());
        totalFoodCount = (int) foodMap.tiles().filter(this::isFoodTile).count();
        uneatenFoodCount = totalFoodCount;
    }

    public int numCols() {
        return terrainMap.numCols();
    }

    public int numRows() {
        return terrainMap.numRows();
    }

    public Stream<Vector2i> tiles() {
        return terrainMap.tiles();
    }

    public TileMap tileMap() {
        return terrainMap;
    }

    public TileMap foodMap() {
        return foodMap;
    }

    /**
     * @param tile a tile
     * @return if this tile is located inside the world bounds
     */
    public boolean insideBounds(Vector2i tile) {
        return 0 <= tile.x() && tile.x() < terrainMap.numCols() && 0 <= tile.y() && tile.y() < terrainMap.numRows();
    }

    /**
     * @return if this position is located inside the world bounds
     */
    public boolean insideBounds(double x, double y) {
        return 0 <= x && x < terrainMap.numCols() * TS && 0 <= y && y < terrainMap.numRows() * TS;
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

    public void setGhostScatterTiles(Vector2i[] tiles) {
        ghostScatterTiles = tiles;
    }

    public Vector2i ghostScatterTile(byte ghostID) {
        checkGhostID(ghostID);
        return ghostScatterTiles[ghostID];
    }

    public void setForbiddenPassages(Map<Vector2i, List<Direction>> forbiddenPassages) {
        this.forbiddenPassages = forbiddenPassages;
    }

    public Map<Vector2i, List<Direction>> forbiddenPassages() {
        return forbiddenPassages;
    }

    public void setBonusPosition(Vector2f position) {
        bonusPosition = position;
    }

    public Vector2f bonusPosition() {
        return bonusPosition;
    }

    public House house() {
        return house;
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
        return portals;
    }

    public boolean belongsToPortal(Vector2i tile) {
        checkTileNotNull(tile);
        return portals.stream().anyMatch(portal -> portal.contains(tile));
    }

    public boolean isBlockedTile(Vector2i tile) {
        byte content = terrainMap.content(tile);
        return content == WALL_H || content == WALL_V
            || content == DWALL_H || content == DWALL_V
            || content == CORNER_NE || content == CORNER_NW || content == CORNER_SE || content == CORNER_SW
            || content == DCORNER_NE || content == DCORNER_NW || content == DCORNER_SE || content == DCORNER_SW;
    }

    public boolean isTunnel(Vector2i tile) {
        return terrainMap.hasContentAt(tile, Tiles.TUNNEL);
    }

    public boolean isIntersection(Vector2i tile) {
        checkTileNotNull(tile);
        if (tile.x() <= 0 || tile.x() >= terrainMap.numCols() - 1) {
            return false; // exclude portal entries and tiles outside the map
        }
        if (house.contains(tile)) {
            return false;
        }
        long numWallNeighbors = tile.neighbors().filter(this::isBlockedTile).count();
        long numDoorNeighbors = tile.neighbors().filter(house.door()::occupies).count();
        return numWallNeighbors + numDoorNeighbors < 2;
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
        if (hasFoodAt(tile)) {
            eaten.set(foodMap.index(tile));
            --uneatenFoodCount;
        }
    }

    public boolean isFoodTile(Vector2i tile) {
        checkTileNotNull(tile);
        byte data = foodMap.content(tile);
        return data == Tiles.PELLET || data == Tiles.ENERGIZER;
    }

    public boolean isEnergizerTile(Vector2i tile) {
        checkTileNotNull(tile);
        return foodMap.content(tile) == Tiles.ENERGIZER;
    }

    public boolean hasFoodAt(Vector2i tile) {
        checkTileNotNull(tile);
        if (insideBounds(tile)) {
            byte data = foodMap.content(tile);
            return (data == Tiles.PELLET || data == Tiles.ENERGIZER) && !eaten.get(foodMap.index(tile));
        }
        return false;
    }

    public boolean hasEatenFoodAt(Vector2i tile) {
        checkTileNotNull(tile);
        return insideBounds(tile) && eaten.get(foodMap.index(tile));
    }
}