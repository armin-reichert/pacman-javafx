/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.model.world;

import de.amr.basics.math.Vector2f;
import de.amr.basics.math.Vector2i;
import org.tinylog.Logger;

import java.io.*;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

import static de.amr.basics.math.Vector2f.vec2_float;
import static de.amr.pacmanfx.core.Validations.requireNonNegativeInt;
import static java.util.Objects.requireNonNull;

public class WorldMap {

    // Tile coordinates

    /** Tile size: 8px */
    public static final byte TS = 8;

    /** Half tile size: 4px */
    public static final byte HTS = 4;

    public static Vector2i tile(int x, int y) {
        return new Vector2i(x, y);
    }

    /**
     * @param numTiles number of tiles
     * @return number of pixels corresponding to given number of tiles
     */
    public static float TS(double numTiles) { return (float) numTiles * TS; }

    /**
     * @param position a position
     * @return tile containing given position
     */
    public static Vector2i computeTileAt(Vector2f position) {
        requireNonNull(position);
        return computeTileAt(position.x(), position.y());
    }

    /**
     * @param x x position
     * @param y y position
     * @return tile containing given position
     */
    public static Vector2i computeTileAt(float x, float y) {
        float tx = x >= 0 ? x / TS : (x - TS) / TS;
        float ty = y >= 0 ? y / TS : (y - TS) / TS;
        return new Vector2i((int) tx, (int) ty);
    }

    /**
     * @param tileX tile x coordinate
     * @param tileY tile y coordinate
     * @return position (scaled by tile size) half tile right of tile origin
     */
    public static Vector2f halfTileRightOf(int tileX, int tileY) {
        return vec2_float(TS * tileX + HTS, TS * tileY);
    }

    /**
     * @param tile some tile
     * @return position (scaled by tile size) half tile right of tile origin
     */
    public static Vector2f halfTileRightOf(Vector2i tile) {
        return halfTileRightOf(tile.x(), tile.y());
    }

    /**
     * Arcade maps have a size of 28x36 tiles (28 cols, 36 rows, including the empty rows over and under the maze).
     * The tile size is 8px which gives a map size of 224x288px.
     */
    public static final Vector2i ARCADE_MAP_SIZE_IN_PIXELS = new Vector2i(28 * TS, 36 * TS);

    // Map creation

    public static final Charset MAP_FILE_CHARSET = StandardCharsets.UTF_8;

    /** Tiles are store inside map files like {@code (12,29)} */
    public static final Pattern TILE_PATTERN = Pattern.compile("\\((\\d+),(\\d+)\\)");

    public enum Marker {
        COMMENT("#"),
        BEGIN_TERRAIN_LAYER("!terrain"),
        BEGIN_FOOD_LAYER("!food"),
        BEGIN_DATA_SECTION("!data");

        Marker(String literal) {
            this.literal = literal;
        }

        public String literal() {
            return literal;
        }

        @Override
        public String toString() {
            return literal;
        }

        private final String literal;
    }

    public static Optional<WorldMap> fromURL(URL url) {
        requireNonNull(url);
        try {
            final Optional<WorldMap> optWorldMap = fromStream(url.openStream());
            optWorldMap.ifPresent(worldMap -> worldMap.url = url.toExternalForm());
            return optWorldMap;

        } catch (IOException x) {
            Logger.error(x, "Error opening url " + url);
            return Optional.empty();
        }
    }

    public static Optional<WorldMap> fromFile(File file) {
        requireNonNull(file);
        try {
            // Use fromURL such that URL is stored inside map! This is needed to load map via link in UI!
            return fromURL(file.toURI().toURL());
        } catch (IOException x) {
            Logger.error(x, "Error opening file: " + file.getAbsolutePath());
            return Optional.empty();
        }
    }

    private static Optional<WorldMap> fromStream(InputStream is) {
        requireNonNull(is);
        final WorldMapParser parser = new WorldMapParser();
        try (var rdr = new BufferedReader(new InputStreamReader(is, MAP_FILE_CHARSET))) {
            final WorldMap worldMap = parser.parse(rdr.lines(), TerrainTile::isValidCode, FoodTile::isValidCode);
            return Optional.of(worldMap);
        } catch (Exception x) {
            Logger.error(x, "Could not parse world map file");
            return Optional.empty();
        }
    }

    /**
     * Saves this map to given file (UTF-8 character encoding).
     *
     * @param file file to save to
     */
    public void saveToFile(File file) throws IOException {
        requireNonNull(file);
        final String source = WorldMapWriter.createSourceCode(this, false);
        try (var fileWriter = new PrintWriter(file, MAP_FILE_CHARSET)) {
            fileWriter.println(source);
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

    /**
     * @param tilesX number of tiles horizontally (columns)
     * @param tilesY number of tiles vertically (rows)
     */
    public WorldMap(int tilesX, int tilesY) {
        numRows = requireNonNegativeInt(tilesY);
        numCols = requireNonNegativeInt(tilesX);
        terrainLayer = new TerrainLayer(numRows, numCols);
        foodLayer = new FoodLayer(numRows, numCols);
    }

    public WorldMap(WorldMap prototype) {
        requireNonNull(prototype);
        numRows = prototype.numRows;
        numCols = prototype.numCols;
        url = prototype.url;
        terrainLayer = new TerrainLayer(prototype.terrainLayer);
        foodLayer = new FoodLayer(prototype.foodLayer);
        configMap = new HashMap<>(prototype.configMap);
    }

    //TODO What happens with the entries in the config map if the map coordinates change?
    public WorldMap insertRowBeforeIndex(int rowIndex) {
        if (rowIndex < 0 || rowIndex > numRows) {
            throw new IllegalArgumentException("Illegal row index for inserting row: " + rowIndex);
        }
        final WorldMap newMap = new WorldMap(numCols, numRows + 1);
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
        newMap.configMap = new HashMap<>(configMap);
        return newMap;
    }

    //TODO What happens with the entries in the config map if the map coordinates change?
    public WorldMap deleteRowAtIndex(int rowIndexToDelete) {
        if (rowIndexToDelete < 0 || rowIndexToDelete > numRows - 1) {
            throw new IllegalArgumentException("Illegal row index for deleting row: " + rowIndexToDelete);
        }
        final WorldMap newMap = new WorldMap(numCols, numRows - 1);
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
        newMap.configMap = new HashMap<>(configMap);
        return newMap;
    }

    public TerrainLayer terrainLayer() {
        return terrainLayer;
    }

    public FoodLayer foodLayer() {
        return foodLayer;
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
        configMap.put(key, value);
    }

    @SuppressWarnings("unchecked")
    public <T> T getConfigValue(Object key) {
        requireNonNull(key);
        return (T) configMap.get(key);
    }

    public boolean hasConfigValue(Object key) {
        requireNonNull(key);
        return configMap != null && configMap.containsKey(key);
    }
}