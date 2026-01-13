/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.model.world;

import org.tinylog.Logger;

import java.io.*;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import static de.amr.pacmanfx.Validations.requireNonNegativeInt;
import static java.util.Objects.requireNonNull;

public class WorldMap {

    public static final String COMMENT_PREFIX = "#";

    public static final String MARKER_BEGIN_TERRAIN_LAYER = "!terrain";
    public static final String MARKER_BEGIN_FOOD_LAYER = "!food";
    public static final String MARKER_BEGIN_DATA_SECTION = "!data";

    public static WorldMap loadFromURL(URL url) throws WorldMapParseException, IOException {
        requireNonNull(url);
        try {
            return WorldMapParser.parse(url);
        }
        catch (IOException x) {
            Logger.error("Error loading world map from URL '{}'", url);
            Logger.error(x);
            throw new RuntimeException(x);
        }
        catch (WorldMapParseException x) {
            Logger.error("Error parsing world map from URL '{}'", url);
            Logger.error(x);
            throw new RuntimeException(x);
        }
    }

    public static WorldMap loadFromFile(File file) throws WorldMapParseException, IOException {
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
    Map<Object, Object> configMap = new HashMap<>();

    WorldMap() {}

    public WorldMap(int tilesX, int tilesY) {
        numRows = requireNonNegativeInt(tilesY);
        numCols = requireNonNegativeInt(tilesX);
        terrainLayer = new TerrainLayer(tilesY, tilesX);
        foodLayer = new FoodLayer(tilesY, tilesX);
    }

    public WorldMap(WorldMap template) {
        numRows = template.numRows;
        numCols = template.numCols;
        url = template.url;
        terrainLayer = new TerrainLayer(template.terrainLayer);
        foodLayer = new FoodLayer(template.foodLayer);
        configMap = new HashMap<>(template.configMap);
    }

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
                    terrainValue = terrainLayer.content(row, col);
                    foodValue = foodLayer.content(row, col);
                } else if (row > rowIndex) {
                    terrainValue = terrainLayer.content(row - 1, col);
                    foodValue = foodLayer.content(row - 1, col);
                } else {
                    if ((col == 0 || col == numCols - 1)
                        && terrainLayer.content(row, col) == TerrainTile.WALL_V.$) {
                        terrainValue = TerrainTile.WALL_V.$; // keep vertical border wall
                    }
                }
                newMap.terrainLayer.setContent(row, col, terrainValue);
                newMap.foodLayer.setContent(row, col, foodValue);
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
                    newMap.terrainLayer.setContent(row, col, terrainLayer.content(row, col));
                    newMap.foodLayer.setContent(row, col, foodLayer.content(row, col));
                } else {
                    newMap.terrainLayer.setContent(row, col, terrainLayer.content(row + 1, col));
                    newMap.foodLayer.setContent(row, col, foodLayer.content(row + 1, col));
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

    public WorldMapLayer layer(WorldMapLayerID id) {
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

    public void setConfigValue(Object key, Object value) {
        requireNonNull(key);
        requireNonNull(value);
        configMapCreateIfNull().put(key, value);
    }

    @SuppressWarnings("unchecked")
    public <T> T getConfigValue(Object key) {
        requireNonNull(key);
        return (T) configMapCreateIfNull().get(key);
    }

    public boolean hasConfigValue(Object key) {
        requireNonNull(key);
        return configMap != null && configMap.containsKey(key);
    }

    private Map<Object, Object> configMapCreateIfNull() {
        if (configMap == null) {
            configMap = new HashMap<>();
        }
        return configMap;
    }

    private void printDataSection(PrintWriter pw, WorldMapLayer layer) {
        pw.println(MARKER_BEGIN_DATA_SECTION);
        for (int row = 0; row < layer.numRows(); ++row) {
            for (int col = 0; col < layer.numCols(); ++col) {
                pw.printf("#%02X", layer.content(row, col));
                if (col < layer.numCols() - 1) {
                    pw.print(",");
                }
            }
            pw.println();
        }
    }

    private void printCommentLine(PrintWriter pw, String comment) {
        pw.println(COMMENT_PREFIX + comment);
    }

    private void printLayerProperties(PrintWriter pw, WorldMapLayer layer) {
        layer.propertiesSortedByName()
            .map(entry -> "%s=%s".formatted(entry.getKey(), entry.getValue()))
            .forEach(pw::println);
    }

    public String sourceCode(boolean lineNumbers) {
        final var sw = new StringWriter();
        final var pw = new PrintWriter(sw);

        pw.println(MARKER_BEGIN_TERRAIN_LAYER);
        printLayerProperties(pw, terrainLayer);
        printDataSection(pw, terrainLayer);

        pw.println(MARKER_BEGIN_FOOD_LAYER);
        printCommentLine(pw, " Pellets (total): %d".formatted(foodLayer.totalFoodCount()));
        printCommentLine(pw, " Energizers: %d".formatted(foodLayer.energizerTiles().size()));
        printLayerProperties(pw, foodLayer);
        printDataSection(pw, foodLayer);

        final String source = sw.toString();
        if (lineNumbers) {
            final var sb = new StringBuilder();
            final String[] lines = source.split("\\R");
            for (int lineNum = 1; lineNum <= lines.length; ++lineNum) {
                sb.append("%5d: %s\n".formatted(lineNum, lines[lineNum-1]));
            }
            return sb.toString();
        }
        else {
            return source;
        }
    }
}