/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.lib.worldmap;

import de.amr.pacmanfx.lib.Vector2i;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class WorldMapLayer {

    private final Map<String, String> propertyMap = new HashMap<>();
    private final byte[][] tileContent;

    public WorldMapLayer(int numRows, int numCols) {
        tileContent = new byte[numRows][numCols];
    }

    public WorldMapLayer(WorldMapLayer other) {
        propertyMap.putAll(other.propertyMap);
        tileContent = new byte[other.numRows()][];
        for (int row = 0; row < other.numRows(); ++row) {
            tileContent[row] = Arrays.copyOf(other.tileContent[row], other.numCols());
        }
    }

    public int numRows() { return tileContent.length; }

    public int numCols() { return tileContent[0].length; }

    public boolean outOfBounds(Vector2i tile) {
        return outOfBounds(tile.y(), tile.x());
    }

    public boolean outOfBounds(int row, int col) {
        return row < 0 || row >= numRows() || col < 0 || col >= numCols();
    }

    /**
     * @param tile tile inside map bounds
     * @return index in row-by-row order
     */
    public int indexInRowWiseOrder(Vector2i tile) {
        return numCols() * tile.y() + tile.x();
    }

    public byte get(int row, int col) {
        return tileContent[row][col];
    }

    public byte get(Vector2i tile) {
        return get(tile.y(), tile.x()); // Note order y=row, x=col
    }

    public void set(int row, int col, byte code) {
        tileContent[row][col] = code;
    }

    public void set(Vector2i tile, byte code) {
        set(tile.y(), tile.x(), code);
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
     * @param index tile index in order top-to-bottom, left-to-right
     * @return tile with given index
     */
    public Vector2i tile(int index) {
        return Vector2i.of(index % numCols(), index / numCols());
    }

    /**
     * @return stream of all tiles of this map (row-by-row)
     */
    public Stream<Vector2i> tiles() {
        return IntStream.range(0, numCols() * numRows()).mapToObj(this::tile);
    }

    /**
     * @param content value to search for
     * @return stream of all tiles of this map with given content (row-by-row)
     */
    public Stream<Vector2i> tilesContaining(byte content) {
        return tiles().filter(tile -> get(tile) == content);
    }
}