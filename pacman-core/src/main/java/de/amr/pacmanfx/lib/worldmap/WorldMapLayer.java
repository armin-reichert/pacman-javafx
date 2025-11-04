/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.lib.worldmap;

import de.amr.pacmanfx.lib.math.Vector2i;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

public class WorldMapLayer {

    private int emptyRowsOverMaze = 3;
    private int emptyRowsBelowMaze = 2;

    private final Map<String, String> propertyMap = new HashMap<>();
    private final byte[][] tileContent;

    public WorldMapLayer(int numRows, int numCols) {
        tileContent = new byte[numRows][numCols];
    }

    public WorldMapLayer(WorldMapLayer other) {
        emptyRowsOverMaze = other.emptyRowsOverMaze;
        emptyRowsBelowMaze = other.emptyRowsBelowMaze;
        propertyMap.putAll(other.propertyMap);
        tileContent = new byte[other.numRows()][];
        for (int row = 0; row < other.numRows(); ++row) {
            tileContent[row] = Arrays.copyOf(other.tileContent[row], other.numCols());
        }
    }

    public int numRows() { return tileContent.length; }

    public int numCols() { return tileContent[0].length; }

    public void setEmptyRowsOverMaze(int numRows) {
        emptyRowsOverMaze = numRows;
    }

    public int emptyRowsOverMaze() {
        return emptyRowsOverMaze;
    }

    public void setEmptyRowsBelowMaze(int numRows) {
        emptyRowsBelowMaze = numRows;
    }

    public int emptyRowsBelowMaze() {
        return emptyRowsBelowMaze;
    }

    public boolean outOfBounds(Vector2i tile) {
        return outOfBounds(tile.y(), tile.x());
    }

    public boolean outOfBounds(int row, int col) {
        return row < 0 || row >= numRows() || col < 0 || col >= numCols();
    }

    public void assertInsideWorld(Vector2i tile) {
        requireNonNull(tile);
        if (outOfBounds(tile)) {
            throw new IllegalArgumentException("Tile %s is outside world".formatted(tile));
        }
    }

    public void assertInsideWorld(int row, int col) {
        if (outOfBounds(row, col)) {
            throw new IllegalArgumentException("Coordinate (row=%d, col=%d) is outside world".formatted(row, col));
        }
    }

    /**
     * @param tile tile inside map bounds
     * @return index in row-by-row order
     */
    public int indexInRowWiseOrder(Vector2i tile) {
        return numCols() * tile.y() + tile.x();
    }

    /**
     * @return stream of all tiles of this map (row-by-row)
     */
    public Stream<Vector2i> tiles() {
        return IntStream.range(0, numCols() * numRows()).mapToObj(this::tileAtIndex);
    }

    /**
     * @param index tile index in order top-to-bottom, left-to-right
     * @return tile with given index
     */
    public Vector2i tileAtIndex(int index) {
        return Vector2i.of(index % numCols(), index / numCols());
    }

    /**
     * @param tile some tile
     * @return The tile at the mirrored position wrt vertical mirror axis.
     */
    public Vector2i mirrorPosition(Vector2i tile) {
        assertInsideWorld(tile);
        return Vector2i.of(numCols() - 1 - tile.x(), tile.y());
    }

    public byte content(int row, int col) {
        assertInsideWorld(row, col);
        return tileContent[row][col];
    }

    public byte content(Vector2i tile) {
        return content(tile.y(), tile.x()); // Note order y=row, x=col
    }

    public void setContent(int row, int col, byte code) {
        assertInsideWorld(row, col);
        tileContent[row][col] = code;
    }

    public void setContent(Vector2i tile, byte code) {
        setContent(tile.y(), tile.x(), code);
    }

    public void setAll(byte code) {
        for (byte[] row : tileContent) {
            Arrays.fill(row, code);
        }
    }

    public Map<String, String> propertyMap() {
        return propertyMap;
    }

    public Stream<Map.Entry<String, String>> propertiesSortedByName() {
        return propertyMap.entrySet().stream().sorted(Map.Entry.comparingByKey());
    }

    public void replacePropertyMap(Map<String, String> other) {
        propertyMap.clear();
        propertyMap.putAll(other);
    }

    /**
     * @param content value to search for
     * @return stream of all tiles of this map with given content (row-by-row)
     */
    public Stream<Vector2i> tilesContaining(byte content) {
        return tiles().filter(tile -> content(tile) == content);
    }
}