/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.lib.tilemap;

import de.amr.pacmanfx.lib.Vector2i;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class WorldMapLayer {

    private final Map<String, String> properties = new HashMap<>();
    private final byte[][] codes;

    public WorldMapLayer(int numRows, int numCols) {
        codes = new byte[numRows][numCols];
    }

    public WorldMapLayer(WorldMapLayer other) {
        int numRows = other.codes.length, numCols = other.codes[0].length;
        properties.putAll(other.properties);
        codes = new byte[numRows][];
        for (int row = 0; row < numRows; ++row) {
            codes[row] = Arrays.copyOf(other.codes[row], numCols);
        }
    }

    public int numRows() { return codes.length; }
    public int numCols() { return codes[0].length; }

    public byte get(int row, int col) {
        return codes[row][col];
    }

    public byte get(Vector2i tile) {
        return get(tile.y(), tile.x()); // Note order y=row, x=col
    }

    public void set(int row, int col, byte code) {
        codes[row][col] = code;
    }

    public void set(Vector2i tile, byte code) {
        set(tile.y(), tile.x(), code);
    }

    public void setAll(byte code) {
        for (byte[] row : codes) {
            Arrays.fill(row, code);
        }
    }

    public Map<String, String> properties() {
        return properties;
    }

    public void replaceProperties(Map<String, String> otherProperties) {
        properties.clear();
        properties.putAll(otherProperties);
    }
}
