/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.model.world;

import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.Vector2f;
import de.amr.games.pacman.lib.Vector2i;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static de.amr.games.pacman.lib.Globals.*;

/**
 * @author Armin Reichert
 */
public class World {

    public static final byte T_SPACE = 0;
    public static final byte T_WALL = 1;
    public static final byte T_TUNNEL = 2;
    public static final byte T_PELLET = 3;
    public static final byte T_ENERGIZER = 4;

    private static byte[][] validateTileMapData(byte[][] data) {
        if (data == null) {
            throw new IllegalArgumentException("Map data missing");
        }
        if (data.length == 0) {
            throw new IllegalArgumentException("Map data empty");
        }
        var firstRow = data[0];
        if (firstRow.length == 0) {
            throw new IllegalArgumentException("Map data empty");
        }
        for (var row : data) {
            if (row.length != firstRow.length) {
                throw new IllegalArgumentException("Map has differently sized rows");
            }
        }
        for (int row = 0; row < data.length; ++row) {
            for (int col = 0; col < data[row].length; ++col) {
                byte d = data[row][col];
                if (d < T_SPACE || d > T_ENERGIZER) {
                    throw new IllegalArgumentException(String.format("Map data at row=%d, col=%d are illegal: %d", row, col, d));
                }
            }
        }
        return data;
    }

    private final byte[][] tileMap;
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
     * @param tileMapData byte-array of tile map data
     * @param house ghost house
     */
    public World(byte[][] tileMapData, House house) {
        tileMap = validateTileMapData(tileMapData);
        checkNotNull(house);
        this.house = house;

        // build portals
        var portalList = new ArrayList<Portal>();
        int lastColumn = numCols() - 1;
        for (int row = 0; row < numRows(); ++row) {
            var leftBorderTile = v2i(0, row);
            var rightBorderTile = v2i(lastColumn, row);
            if (tileMap[row][0] == T_TUNNEL && tileMap[row][lastColumn] == T_TUNNEL) {
                portalList.add(new Portal(leftBorderTile, rightBorderTile, 2));
            }
        }
        portalList.trimToSize();
        portals = Collections.unmodifiableList(portalList);

        energizerTiles = tiles().filter(this::isEnergizerTile).collect(Collectors.toList());

        eaten = new BitSet(numCols() * numRows());
        totalFoodCount = (int) tiles().filter(this::isFoodTile).count();
        resetFood();
    }

    public void setPacPosition(Vector2f pacPosition) {
        this.pacPosition = pacPosition;
    }

    public Vector2f pacPosition() {
        return pacPosition;
    }

    public void setGhostPositions(Vector2f[] ghostPositions) {
        this.ghostPositions = ghostPositions;
    }

    public Vector2f ghostPosition(byte ghostID) {
        checkGhostID(ghostID);
        return ghostPositions[ghostID];
    }

    public void setGhostDirections(Direction[] ghostDirections) {
        this.ghostDirections = ghostDirections;
    }

    public Direction ghostDirection(byte ghostID) {
        checkGhostID(ghostID);
        return ghostDirections[ghostID];
    }

    public void setGhostScatterTiles(Vector2i[] ghostScatterTiles) {
        this.ghostScatterTiles = ghostScatterTiles;
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

    public void setBonusPosition(Vector2f bonusPosition) {
        this.bonusPosition = bonusPosition;
    }

    public Vector2f bonusPosition() {
        return bonusPosition;
    }

    public void resetFood() {
        eaten.clear();
        uneatenFoodCount = totalFoodCount;
    }

    public House house() {
        return house;
    }

    /**
     * @return tiles in order top-to-bottom, left-to-right
     */
    public Stream<Vector2i> tiles() {
        return IntStream.range(0, numCols() * numRows()).mapToObj(this::tile);
    }

    public Stream<Vector2i> energizerTiles() {
        return energizerTiles.stream();
    }

    /**
     * @param index tile index in order top-to-bottom, left-to-right
     * @return tile with given index
     */
    public Vector2i tile(int index) {
        return v2i(index % numCols(), index / numCols());
    }

    /**
     * @param tile a tile
     * @return if this tile is located inside the world bounds
     */
    public boolean insideBounds(Vector2i tile) {
        return 0 <= tile.x() && tile.x() < numCols() && 0 <= tile.y() && tile.y() < numRows();
    }

    /**
     * @return if this position is located inside the world bounds
     */
    public boolean insideBounds(double x, double y) {
        return 0 <= x && x < numCols() * TS && 0 <= y && y < numRows() * TS;
    }

    private int index(Vector2i tile) {
        return numCols() * tile.y() + tile.x();
    }

    public int numCols() {
        return tileMap[0].length;
    }

    public int numRows() {
        return tileMap.length;
    }

    public List<Portal> portals() {
        return portals;
    }

    public boolean belongsToPortal(Vector2i tile) {
        checkTileNotNull(tile);
        return portals.stream().anyMatch(portal -> portal.contains(tile));
    }

    private byte content(Vector2i tile) {
        return insideBounds(tile) ? tileMap[tile.y()][tile.x()] : T_SPACE;
    }

    public boolean isWall(Vector2i tile) {
        checkTileNotNull(tile);
        return content(tile) == T_WALL;
    }

    public boolean isTunnel(Vector2i tile) {
        checkTileNotNull(tile);
        return content(tile) == T_TUNNEL;
    }

    public boolean isFoodTile(Vector2i tile) {
        checkTileNotNull(tile);
        byte data = content(tile);
        return data == T_PELLET || data == T_ENERGIZER;
    }

    public boolean isEnergizerTile(Vector2i tile) {
        checkTileNotNull(tile);
        return content(tile) == T_ENERGIZER;
    }

    public boolean isIntersection(Vector2i tile) {
        checkTileNotNull(tile);
        if (tile.x() <= 0 || tile.x() >= numCols() - 1) {
            return false; // exclude portal entries and tiles outside the map
        }
        if (house.contains(tile)) {
            return false;
        }
        long numWallNeighbors = tile.neighbors().filter(this::isWall).count();
        long numDoorNeighbors = tile.neighbors().filter(house.door()::occupies).count();
        return numWallNeighbors + numDoorNeighbors < 2;
    }


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
            eaten.set(index(tile));
            --uneatenFoodCount;
        }
    }

    public boolean hasFoodAt(Vector2i tile) {
        checkTileNotNull(tile);
        if (insideBounds(tile)) {
            byte data = tileMap[tile.y()][tile.x()];
            return (data == T_PELLET || data == T_ENERGIZER) && !eaten.get(index(tile));
        }
        return false;
    }

    public boolean hasEatenFoodAt(Vector2i tile) {
        checkTileNotNull(tile);
        return insideBounds(tile) && eaten.get(index(tile));
    }
}