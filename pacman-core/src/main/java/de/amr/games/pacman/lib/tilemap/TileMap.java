/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.lib.tilemap;

import de.amr.games.pacman.lib.Vector2i;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static de.amr.games.pacman.lib.Globals.vec_2i;

public class TileMap {

    static final String MARKER_DATA_SECTION_START = "!data";


    final Map<String, Object> properties = new HashMap<>();
    final byte[][] matrix;

    public TileMap(int numRows, int numCols) {
        matrix = new byte[numRows][numCols];
    }

    TileMap(byte[][] matrix) {
        this.matrix = matrix;
    }

    public TileMap(TileMap other) {
        int numRows = other.numRows(), numCols = other.numCols();
        properties.putAll(other.properties);
        matrix = new byte[numRows][];
        for (int row = 0; row < numRows; ++row) {
            matrix[row] = Arrays.copyOf(other.matrix[row], numCols);
        }
    }

    /**
     * @return stream of all tiles of this map (row-by-row)
     */
    public Stream<Vector2i> tiles() {
        return IntStream.range(0, numCols() * numRows()).mapToObj(this::tile);
    }

    /**
     * @param index tile index in order top-to-bottom, left-to-right
     * @return tile with given index
     */
    public Vector2i tile(int index) {
        return vec_2i(index % numCols(), index / numCols());
    }

    /**
     * @return stream of all tiles of this map with given content (row-by-row)
     */
    public Stream<Vector2i> tiles(byte content) {
        return tiles().filter(tile -> get(tile) == content);
    }

    /**
     * @param tile tile inside map bounds
     * @return index in row-by-row order
     */
    public int index(Vector2i tile) {
        return numCols() * tile.y() + tile.x();
    }

    public int numCols() {
        return matrix[0].length;
    }

    public int numRows() {
        return matrix.length;
    }

    public boolean outOfBounds(Vector2i tile) {
        return outOfBounds(tile.y(), tile.x());
    }

    public boolean outOfBounds(int row, int col) {
        return row < 0 || row >= numRows() || col < 0 || col >= numCols();
    }

    public Map<String, Object> getProperties() {
        return properties;
    }

    public void replaceProperties(Map<String, Object> properties) {
        this.properties.clear();
        this.properties.putAll(properties);
    }

    public void setProperty(String name, Object value) {
        properties.put(name, value);
    }

    public void removeProperty(String name) {
        properties.remove(name);
    }

    public Object getProperty(String name) {
        return properties.get(name);
    }

    public String getStringProperty(String name) {
        return String.valueOf(properties.get(name));
    }

    public String getStringPropertyOrDefault(String name, String defaultValue) {
        return String.valueOf(properties.getOrDefault(name, defaultValue));
    }

    public boolean hasProperty(String name) {
        return properties.containsKey(name);
    }

    public Stream<String> stringPropertyNames() {
        return properties.keySet().stream().filter(name -> properties.get(name) instanceof String).sorted();
    }

    public byte[][] getData() {
        return matrix;
    }

    /**
     * @param row row inside map bounds
     * @param col column inside map bounds
     * @return map data at position
     * @throws IllegalArgumentException if tile outside map bounds
     */
    public byte get(int row, int col) {
        if (outOfBounds(row, col)) {
            throw new IllegalArgumentException(String.format("Illegal map coordinate row=%d col=%d", row, col));
        }
        return matrix[row][col];
    }

    /**
     * @param tile tile inside map bounds
     * @return map data at tile position
     * @throws IllegalArgumentException if tile outside map bounds
     */
    public byte get(Vector2i tile) {
        return get(tile.y(), tile.x());
    }

    /**
     * Sets map data at position inside map bounds
     * @param row row inside map bounds
     * @param col column inside map bounds
     * @param value map value
     * @throws IllegalArgumentException if tile outside map bounds
     */
    public void set(int row, int col, byte value) {
        if (outOfBounds(row, col)) {
            throw new IllegalArgumentException(String.format("Illegal map coordinate row=%d col=%d", row, col));
        }
        matrix[row][col] = value;
    }

    /**
     * Sets map data at position inside map bounds
     * @param tile tile inside map bounds
     * @param value map value
     * @throws IllegalArgumentException if tile outside map bounds
     */
    public void set(Vector2i tile, byte value) {
        set(tile.y(), tile.x(), value);
    }

    public void fill(byte fillValue) {
        for (byte[] row : matrix) {
            Arrays.fill(row, fillValue);
        }
    }

}