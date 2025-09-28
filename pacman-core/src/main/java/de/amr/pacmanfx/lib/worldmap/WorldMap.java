/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.lib.worldmap;

import de.amr.pacmanfx.lib.Vector2i;
import org.tinylog.Logger;

import java.io.*;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import static de.amr.pacmanfx.Validations.requireNonNegativeInt;
import static java.util.Objects.requireNonNull;

public class WorldMap {

    public static final String MARKER_BEGIN_TERRAIN_LAYER = "!terrain";
    public static final String MARKER_BEGIN_FOOD_LAYER = "!food";
    public static final String MARKER_BEGIN_DATA_SECTION = "!data";

    private static void assertValidLayerID(LayerID id) {
        requireNonNull(id);
        if (Stream.of(LayerID.values()).noneMatch(layerID -> layerID == id)) {
            throw new IllegalArgumentException("Illegal map layer ID: '%s'".formatted(id));
        }
    }

    public WorldMap(int tilesX, int tilesY) {
        numRows = requireNonNegativeInt(tilesY);
        numCols = requireNonNegativeInt(tilesX);
        terrainLayer = new TerrainLayer(tilesY, tilesX);
        foodLayer = new FoodLayer(tilesY, tilesX);
    }

    public static WorldMap copyOf(WorldMap original) {
        requireNonNull(original);
        var copy = new WorldMap();
        copy.numRows = original.numRows;
        copy.numCols = original.numCols;
        copy.terrainLayer = new TerrainLayer(original.terrainLayer);
        copy.foodLayer = new FoodLayer(original.foodLayer);
        copy.configMap = new HashMap<>(original.configMap);
        copy.url = original.url;
        return copy;
    }

    public static WorldMap loadFromURL(URL url) throws IOException {
        requireNonNull(url);
        try (var br = new BufferedReader(new InputStreamReader(url.openStream(), StandardCharsets.UTF_8))) {
            WorldMap worldMap = WorldMapParser.parse(br.lines(), TerrainLayer::isValidTerrainCode, FoodLayer::isValidFoodCode);
            worldMap.url = URLDecoder.decode(url.toExternalForm(), StandardCharsets.UTF_8);
            return worldMap;
        }
    }

    public static WorldMap loadFromFile(File file) throws IOException {
        requireNonNull(file);
        return loadFromURL(file.toURI().toURL());
    }

    /**
     * Saves this map to given file (UTF-8 character encoding).
     *
     * @param file file to save to
     * @return {@code true} if saving succeeded
     */
    public boolean saveToFile(File file) {
        try (var pw = new PrintWriter(file, StandardCharsets.UTF_8)) {
            pw.print(sourceCode(false));
            return true;
        } catch (IOException x) {
            Logger.error(x);
            return false;
        }
    }

    // Package-visible for access by parser methods

    int numCols;
    int numRows;
    String url;
    TerrainLayer terrainLayer;
    FoodLayer foodLayer;
    Map<String, Object> configMap = new HashMap<>();

    WorldMap() {}

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder();
        s.append("WorldMap{");
        if (terrainLayer != null) {
            s.append("numRows=").append(numRows);
            s.append(", numCols=").append(numCols);
        }
        s.append(", url=").append(url);
        s.append("}");
        return s.toString();
    }

    public WorldMap insertRowBeforeIndex(int rowIndex) {
        if (rowIndex < 0 || rowIndex > numRows) {
            throw new IllegalArgumentException("Illegal row index for inserting row: " + rowIndex);
        }
        WorldMap newMap = new WorldMap(numCols, numRows + 1);
        newMap.terrainLayer.replacePropertyMap(terrainLayer.propertyMap());
        newMap.foodLayer.replacePropertyMap(foodLayer.propertyMap());
        for (int row = 0; row < newMap.numRows; ++row) {
            for (int col = 0; col < newMap.numCols; ++col) {
                byte terrainValue = TerrainTile.EMPTY.$;
                byte foodValue = FoodTile.EMPTY.$;
                if (row < rowIndex) {
                    terrainValue = terrainLayer.get(row, col);
                    foodValue = foodLayer.get(row, col);
                } else if (row > rowIndex) {
                    terrainValue = terrainLayer.get(row - 1, col);
                    foodValue = foodLayer.get(row - 1, col);
                } else {
                    if ((col == 0 || col == numCols - 1)
                        && terrainLayer.get(row, col) == TerrainTile.WALL_V.$) {
                        terrainValue = TerrainTile.WALL_V.$; // keep vertical border wall
                    }
                }
                newMap.terrainLayer.set(row, col, terrainValue);
                newMap.foodLayer.set(row, col, foodValue);
            }
        }
        return newMap;
    }

    public WorldMap deleteRowAtIndex(int rowIndexToDelete) {
        if (rowIndexToDelete < 0 || rowIndexToDelete > numRows - 1) {
            throw new IllegalArgumentException("Illegal row index for deleting row: " + rowIndexToDelete);
        }
        WorldMap newMap = new WorldMap(numCols, numRows - 1);
        newMap.terrainLayer.replacePropertyMap(terrainLayer.propertyMap());
        newMap.foodLayer.replacePropertyMap(foodLayer.propertyMap());
        for (int row = 0; row < newMap.numRows; ++row) {
            for (int col = 0; col < newMap.numCols; ++col) {
                if (row < rowIndexToDelete) {
                    newMap.terrainLayer.set(row, col, terrainLayer.get(row, col));
                    newMap.foodLayer.set(row, col, foodLayer.get(row, col));
                } else {
                    newMap.terrainLayer.set(row, col, terrainLayer.get(row + 1, col));
                    newMap.foodLayer.set(row, col, foodLayer.get(row + 1, col));
                }
            }
        }
        return newMap;
    }

    public TerrainLayer terrainLayer() {
        return terrainLayer;
    }

    public FoodLayer foodLayer() {
        return foodLayer;
    }

    public WorldMapLayer layer(LayerID id) {
        assertValidLayerID(id);
        return switch (id) {
            case TERRAIN -> terrainLayer;
            case FOOD -> foodLayer;
        };
    }

    public int numCols() {
        return numCols;
    }

    public int numRows() {
        return numRows;
    }

    public String url() {
        return url;
    }

    // Configuration map, can store values of any data type. Keys and values must not be null.

    public void setConfigValue(String key, Object value) {
        requireNonNull(key);
        requireNonNull(value);
        configMapIfNullCreate().put(key, value);
    }

    @SuppressWarnings("unchecked")
    public <T> T getConfigValue(String key) {
        requireNonNull(key);
        return (T) configMapIfNullCreate().get(key);
    }

    public boolean hasConfigValue(String key) {
        requireNonNull(key);
        return configMap != null && configMap.containsKey(key);
    }

    private Map<String, Object> configMapIfNullCreate() {
        if (configMap == null) {
            configMap = new HashMap<>();
        }
        return configMap;
    }

    /**
     * Sets map data for a rectangular region.
     *
     * @param layerID Layer ID
     * @param origin top-left tile of region
     * @param content content of region
     */
    public void setContentArea(LayerID layerID, Vector2i origin, byte[][] content) {
        requireNonNull(layerID);
        requireNonNull(origin);
        requireNonNull(content);
        int numCols = content[0].length, numRows = content.length;
        for (int x = 0; x < numCols; ++x) {
            for (int y = 0; y < numRows; ++y) {
                layer(layerID).set(origin.y() + y, origin.x() + x, content[y][x]);
            }
        }
    }

    private void printLayer(PrintWriter pw, WorldMapLayer layer, String layerMarker) {
        pw.println(layerMarker);
        layer.propertyMap().entrySet().stream()
            .sorted(Map.Entry.comparingByKey())
            .map(entry -> "%s=%s".formatted(entry.getKey(), entry.getValue()))
            .forEach(pw::println);
        pw.println(MARKER_BEGIN_DATA_SECTION);
        for (int row = 0; row < layer.numRows(); ++row) {
            for (int col = 0; col < layer.numCols(); ++col) {
                pw.printf("#%02X", layer.get(row, col));
                if (col < layer.numCols() - 1) {
                    pw.print(",");
                }
            }
            pw.println();
        }
    }

    public String sourceCode(boolean lineNumbers) {
        var sw = new StringWriter();
        var pw = new PrintWriter(sw);
        printLayer(pw, terrainLayer, MARKER_BEGIN_TERRAIN_LAYER);
        printLayer(pw, foodLayer, MARKER_BEGIN_FOOD_LAYER);
        String source = sw.toString();
        if (lineNumbers) {
            StringBuilder sb = new StringBuilder();
            String[] lines = source.split("\n");
            for (int lineNum = 1; lineNum <= lines.length; ++lineNum) {
                sb.append("%5d: %s\n".formatted(lineNum, lines[lineNum-1]));
            }
            return sb.toString();
        } else {
            return source;
        }
    }
}