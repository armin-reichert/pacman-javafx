/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.lib.worldmap;

import de.amr.pacmanfx.lib.Vector2i;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

public class WorldMapLayer implements RectangularTileRegion {

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

    @Override
    public int numRows() { return tileContent.length; }

    @Override
    public int numCols() { return tileContent[0].length; }

    public byte get(int row, int col) {
        assertInsideWorld(row, col);
        return tileContent[row][col];
    }

    public byte get(Vector2i tile) {
        return get(tile.y(), tile.x()); // Note order y=row, x=col
    }

    public void set(int row, int col, byte code) {
        assertInsideWorld(row, col);
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
     * @param content value to search for
     * @return stream of all tiles of this map with given content (row-by-row)
     */
    public Stream<Vector2i> tilesContaining(byte content) {
        return tiles().filter(tile -> get(tile) == content);
    }
}