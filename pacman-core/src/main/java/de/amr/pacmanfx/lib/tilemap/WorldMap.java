/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.lib.tilemap;

import de.amr.pacmanfx.lib.Vector2i;
import de.amr.pacmanfx.model.WorldMapProperty;
import org.tinylog.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static de.amr.pacmanfx.Globals.HTS;
import static de.amr.pacmanfx.Globals.TS;
import static de.amr.pacmanfx.Validations.requireNonNegative;
import static de.amr.pacmanfx.lib.tilemap.TerrainTile.WALL_V;
import static java.util.Objects.requireNonNull;

public class WorldMap {

    public static final String MARKER_BEGIN_TERRAIN_LAYER = "!terrain";
    public static final String MARKER_BEGIN_FOOD_LAYER = "!food";
    public static final String MARKER_BEGIN_DATA_SECTION = "!data";

    private static boolean isValidTerrainValue(byte value) {
        return Stream.of(TerrainTile.values()).anyMatch(tileID -> TerrainTile.byteValue(tileID) == value);
    }

    private static boolean isValidFoodValue(byte value) {
        return Stream.of(FoodTile.values()).anyMatch(tile -> FoodTile.byteValue(tile) == value);
    }

    public static WorldMap emptyMap(int numRows, int numCols) {
        requireNonNegative(numRows);
        requireNonNegative(numCols);
        WorldMap worldMap = new WorldMap();
        worldMap.terrainLayer = new WorldMapLayer(numRows, numCols);
        worldMap.foodLayer = new WorldMapLayer(numRows, numCols);
        return worldMap;
    }

    public static WorldMap copyMap(WorldMap original) {
        requireNonNull(original);
        WorldMap copy = new WorldMap();
        copy.terrainLayer = new WorldMapLayer(original.terrainLayer);
        copy.foodLayer = new WorldMapLayer(original.foodLayer);
        copy.obstacles = original.obstacles != null ? new HashSet<>(original.obstacles) : null;
        copy.configMap = new HashMap<>(original.configMap);
        copy.url = original.url;
        return copy;
    }

    public static WorldMap fromURL(URL url) throws IOException {
        var reader = new BufferedReader(new InputStreamReader(url.openStream(), StandardCharsets.UTF_8));
        WorldMap worldMap = WorldMapParser.parse(reader.lines(), WorldMap::isValidTerrainValue, WorldMap::isValidFoodValue);
        worldMap.url = url;
        return worldMap;
    }

    public static WorldMap fromFile(File file) throws IOException {
        return fromURL(file.toURI().toURL());
    }

    // Package access for parser

    URL url;
    WorldMapLayer terrainLayer;
    WorldMapLayer foodLayer;
    Set<Obstacle> obstacles = null; // uninitialized!
    Map<String, Object> configMap = new HashMap<>();

    WorldMap() {}

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder();
        s.append("WorldMap{");
        if (terrainLayer != null) {
            s.append("numRows=").append(numRows());
            s.append(", numCols=").append(numCols());
        }
        s.append(", url=").append(url);
        s.append("}");
        return s.toString();
    }

    public WorldMap insertRowBeforeIndex(int rowIndex) {
        if (rowIndex < 0 || rowIndex > numRows()) {
            throw new IllegalArgumentException("Illegal row index for inserting row: " + rowIndex);
        }
        WorldMap newMap = emptyMap(numRows() + 1, numCols());
        newMap.terrainLayer.replaceProperties(terrainLayer.properties());
        newMap.foodLayer.replaceProperties(foodLayer.properties());
        for (int row = 0; row < newMap.numRows(); ++row) {
            for (int col = 0; col < newMap.numCols(); ++col) {
                byte terrainValue = TerrainTile.emptyTileValue();
                byte foodValue = FoodTile.emptyTileValue();
                if (row < rowIndex) {
                    terrainValue = content(LayerID.TERRAIN, row, col);
                    foodValue = content(LayerID.FOOD, row, col);
                } else if (row > rowIndex) {
                    terrainValue = content(LayerID.TERRAIN, row - 1, col);
                    foodValue = content(LayerID.FOOD, row - 1, col);
                } else {
                    if ((col == 0 || col == numCols() - 1)
                            && content(LayerID.TERRAIN, row, col) == TerrainTile.byteValue(WALL_V)) {
                        terrainValue = TerrainTile.byteValue(WALL_V); // keep vertical border wall
                    }
                }
                newMap.setContent(LayerID.TERRAIN, row, col, terrainValue);
                newMap.setContent(LayerID.FOOD, row, col, foodValue);
            }
        }
        return newMap;
    }

    public WorldMap deleteRowAtIndex(int rowIndexToDelete) {
        if (rowIndexToDelete < 0 || rowIndexToDelete > numRows() - 1) {
            throw new IllegalArgumentException("Illegal row index for deleting row: " + rowIndexToDelete);
        }
        WorldMap newMap = emptyMap(numRows() - 1, numCols());
        newMap.terrainLayer.replaceProperties(terrainLayer.properties());
        newMap.foodLayer.replaceProperties(foodLayer.properties());
        for (int row = 0; row < newMap.numRows(); ++row) {
            for (int col = 0; col < newMap.numCols(); ++col) {
                if (row < rowIndexToDelete) {
                    newMap.setContent(LayerID.TERRAIN, row, col, content(LayerID.TERRAIN, row, col));
                    newMap.setContent(LayerID.FOOD, row, col, content(LayerID.FOOD, row, col));
                } else {
                    newMap.setContent(LayerID.TERRAIN, row, col, content(LayerID.TERRAIN, row + 1, col));
                    newMap.setContent(LayerID.FOOD, row, col, content(LayerID.FOOD, row + 1, col));
                }
            }
        }
        return newMap;
    }


    public List<Vector2i> buildObstacleList() {
        List<Vector2i> tilesWithErrors = new ArrayList<>();
        obstacles = ObstacleBuilder.buildObstacles(this, tilesWithErrors);

        // remove house obstacle
        Vector2i houseMinTile = getTerrainTileProperty(WorldMapProperty.POS_HOUSE_MIN_TILE);
        if (houseMinTile == null) {
            Logger.info("Could not remove house placeholder from obstacle list, house min tile not set");
        } else {
            Vector2i houseStartPoint = houseMinTile.scaled(TS).plus(TS, HTS);
            obstacles.stream()
                .filter(obstacle -> obstacle.startPoint().equals(houseStartPoint))
                .findFirst().ifPresent(houseObstacle -> {
                    Logger.debug("Removing house placeholder-obstacle starting at tile {}, point {}", houseMinTile, houseStartPoint);
                    obstacles.remove(houseObstacle);
                });
        }
        Logger.info("{} obstacles found in map ", obstacles.size(), this);
        return tilesWithErrors;
    }

    public Set<Obstacle> obstacles() {
        if (obstacles == null) { // first access
            buildObstacleList();
        }
        return Collections.unmodifiableSet(obstacles);
    }

    public WorldMapLayer layer(LayerID id) {
        requireNonNull(id);
        return switch (id) {
            case TERRAIN -> terrainLayer;
            case FOOD -> foodLayer;
        };
    }

    public int numCols() {
        return terrainLayer.numCols();
    }

    public int numRows() {
        return terrainLayer.numRows();
    }

    public URL url() {
        return url;
    }

    /**
     * @param tile tile inside map bounds
     * @return index in row-by-row order
     */
    public int index(Vector2i tile) {
        return numCols() * tile.y() + tile.x();
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
        return Vector2i.of(index % numCols(), index / numCols());
    }

    /**
     * @param layerID the layer ID
     * @param content value to search for
     * @return stream of all tiles of this map with given content (row-by-row)
     */
    public Stream<Vector2i> tilesContaining(LayerID layerID, byte content) {
        return tiles().filter(tile -> content(layerID, tile) == content);
    }

    /**
     * @param tile some tile
     * @return The tile at the mirrored position wrt vertical mirror axis.
     */
    public Vector2i mirroredTile(Vector2i tile) {
        return Vector2i.of(numCols() - 1 - tile.x(), tile.y());
    }

    public boolean outOfBounds(Vector2i tile) {
        return outOfBounds(tile.y(), tile.x());
    }

    public boolean outOfBounds(int row, int col) {
        return row < 0 || row >= numRows() || col < 0 || col >= numCols();
    }

    // Configuration map, can store values of any data type. Keys and values must not be null.

    public void setConfigValue(String key, Object value) {
        requireNonNull(key);
        requireNonNull(value);
        configMap().put(key, value);
    }

    @SuppressWarnings("unchecked")
    public <T> T getConfigValue(String key) {
        requireNonNull(key);
        return (T) configMap().get(key);
    }

    public boolean hasConfigValue(String key) {
        requireNonNull(key);
        return configMap != null && configMap.containsKey(key);
    }

    private Map<String, Object> configMap() {
        if (configMap == null) {
            configMap = new HashMap<>();
        }
        return configMap;
    }

    // Properties map by layer

    public Map<String, String> properties(LayerID layerID) {
        return layer(layerID).properties();
    }

    public Stream<String> propertyNames(LayerID layerID) {
        return properties(layerID).keySet().stream().sorted();
    }

    public Vector2i getTileProperty(LayerID layerID, String propertyName, Vector2i defaultTile) {
        requireNonNull(propertyName);
        if (properties(layerID).containsKey(propertyName)) {
            String value = properties(layerID).get(propertyName);
            return WorldMapParser.parseTile(value).orElse(defaultTile);
        }
        return defaultTile;
    }

    /**
     * @param propertyName property name
     * @return tile value of property in terrain layer or default value
     */
    public Vector2i getTerrainTileProperty(String propertyName, Vector2i defaultTile) {
        return getTileProperty(LayerID.TERRAIN, propertyName, defaultTile);
    }

    /**
     * @param propertyName property name
     * @return tile value of property in terrain layer or <code>null</code>
     */
    public Vector2i getTerrainTileProperty(String propertyName) {
        return getTileProperty(LayerID.TERRAIN, propertyName, null);
    }

    /**
     * @param layerID Layer ID
     * @param row row inside map bounds
     * @param col column inside map bounds
     * @return map data at position
     * @throws IllegalArgumentException if tile outside map bounds
     */
    public byte content(LayerID layerID, int row, int col) {
        requireNonNull(layerID);
        if (outOfBounds(row, col)) {
            throw new IllegalArgumentException(String.format("Illegal map coordinate row=%d col=%d", row, col));
        }
        return layer(layerID).get(row, col);
    }

    /**
     * @param layerID Layer ID
     * @param tile tile inside map bounds
     * @return map data at tile position
     * @throws IllegalArgumentException if tile outside map bounds
     */
    public byte content(LayerID layerID, Vector2i tile) {
        return content(layerID, tile.y(), tile.x());
    }

    /**
     * Sets map layer data at position inside map bounds.
     *
     * @param layerID Layer ID
     * @param row row inside map bounds
     * @param col column inside map bounds
     * @param value map value
     * @throws IllegalArgumentException if tile outside map bounds
     */
    public void setContent(LayerID layerID, int row, int col, byte value) {
        requireNonNull(layerID);
        if (outOfBounds(row, col)) {
            throw new IllegalArgumentException(String.format("Illegal map coordinate row=%d col=%d", row, col));
        }
        layer(layerID).set(row, col, value);
    }

    /**
     * Sets map data at position inside map bounds
     *
     * @param layerID Layer ID
     * @param tile tile inside map bounds
     * @param value map value
     * @throws IllegalArgumentException if tile outside map bounds
     */
    public void setContent(LayerID layerID, Vector2i tile, byte value) {
        setContent(layerID, tile.y(), tile.x(), value);
    }
}