/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.lib.tilemap;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

public class TileMap {

    Map<String, Object> properties = new HashMap<>();
    byte[][] matrix;

    public TileMap() {}

    public TileMap(TileMap other) {
        int numRows = other.matrix.length, numCols = other.matrix[0].length;
        properties.putAll(other.properties);
        matrix = new byte[numRows][];
        for (int row = 0; row < numRows; ++row) {
            matrix[row] = Arrays.copyOf(other.matrix[row], numCols);
        }
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

    public String getStringPropertyOrElse(String name, String defaultValue) {
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
}