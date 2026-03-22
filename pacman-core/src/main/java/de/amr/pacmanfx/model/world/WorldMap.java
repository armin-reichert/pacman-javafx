/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.model.world;

import de.amr.pacmanfx.lib.math.Vector2i;
import org.tinylog.Logger;

import java.io.*;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static de.amr.pacmanfx.Validations.requireNonNegativeInt;
import static java.util.Objects.requireNonNull;

public class WorldMap {

    public static final Charset CHARSET = StandardCharsets.UTF_8;

    public static final Pattern TILE_PATTERN = Pattern.compile("\\((\\d+),(\\d+)\\)");

    public static final String MARKER_COMMENT = "#";
    public static final String MARKER_BEGIN_TERRAIN_LAYER = "!terrain";
    public static final String MARKER_BEGIN_FOOD_LAYER = "!food";
    public static final String MARKER_BEGIN_DATA_SECTION = "!data";

    public enum LayerType { TERRAIN, FOOD }

    public static Vector2i parseTile(String s) {
        requireNonNull(s);
        Matcher m = TILE_PATTERN.matcher(s);
        if (!m.matches()) {
            throw new IllegalArgumentException("Cannot create tile from '%s'".formatted(s));
        }
        try {
            int x = Integer.parseInt(m.group(1));
            int y = Integer.parseInt(m.group(2));
            return new Vector2i(x, y);
        } catch (NumberFormatException x) {
            Logger.error(x, "Could not parse tile from text '{}'", s);
            throw new IllegalArgumentException("Cannot create tile from '%s'".formatted(s));
        }
    }

    public static Optional<WorldMap> fromURL(URL url) {
        requireNonNull(url);
        try {
            return fromStream(url.openStream());
        } catch (IOException x) {
            Logger.error(x);
            return Optional.empty();
        }
    }

    public static Optional<WorldMap> fromFile(File file) {
        requireNonNull(file);
        try {
            return fromStream(new FileInputStream(file));
        } catch (IOException x) {
            Logger.error(x);
            return Optional.empty();
        }
    }

    private static Optional<WorldMap> fromStream(InputStream is) throws IOException {
        requireNonNull(is);
        try (var rdr = new BufferedReader(new InputStreamReader(is, CHARSET))) {
            return WorldMapParser.parse(rdr.lines(), TerrainTile::isValidCode, FoodTile::isValidCode);
        }
    }

    /**
     * Saves this map to given file (UTF-8 character encoding).
     *
     * @param file file to save to
     * @return {@code true} if saving succeeded
     */
    public boolean saveToFile(File file) {
        try (var fileWriter = new PrintWriter(file, CHARSET)) {
            fileWriter.println(WorldMapWriter.createSourceCode(this, false));
            return true;
        } catch (IOException x) {
            Logger.error(x, "Could not save world map to file {}", file);
            return false;
        }
    }

    int numCols;
    int numRows;
    String url;
    TerrainLayer terrainLayer;
    FoodLayer foodLayer;
    Map<Object, Object> configMap = new HashMap<>();

    /** Make accessible for WorldMapParser */
    WorldMap() {}

    public WorldMap(int tilesX, int tilesY) {
        numRows = requireNonNegativeInt(tilesY);
        numCols = requireNonNegativeInt(tilesX);
        terrainLayer = new TerrainLayer(tilesY, tilesX);
        foodLayer = new FoodLayer(tilesY, tilesX);
    }

    public WorldMap(WorldMap prototype) {
        numRows = prototype.numRows;
        numCols = prototype.numCols;
        url = prototype.url;
        terrainLayer = new TerrainLayer(prototype.terrainLayer);
        foodLayer = new FoodLayer(prototype.foodLayer);
        configMap = new HashMap<>(prototype.configMap);
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
}