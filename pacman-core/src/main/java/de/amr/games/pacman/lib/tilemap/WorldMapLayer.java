/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.lib.tilemap;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class WorldMapLayer {
    private final Map<String, String> properties = new HashMap<>();
    private final byte[][] values;

    public WorldMapLayer(int numRows, int numCols) {
        values = new byte[numRows][numCols];
    }

    public WorldMapLayer(WorldMapLayer other) {
        int numRows = other.values.length, numCols = other.values[0].length;
        properties.putAll(other.properties);
        values = new byte[numRows][];
        for (int row = 0; row < numRows; ++row) {
            values[row] = Arrays.copyOf(other.values[row], numCols);
        }
    }

    public int numRows() { return values.length; }
    public int numCols() { return values[0].length; }

    public byte get(int row, int col) {
        return values[row][col];
    }

    public void set(int row, int col, byte value) {
        values[row][col] = value;
    }

    public void setAll(byte value) {
        for (byte[] row : values) {
            Arrays.fill(row, value);
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
