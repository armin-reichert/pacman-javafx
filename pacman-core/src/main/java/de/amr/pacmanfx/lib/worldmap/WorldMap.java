/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.lib.worldmap;

import de.amr.pacmanfx.lib.Vector2i;
import de.amr.pacmanfx.model.DefaultWorldMapPropertyName;
import org.tinylog.Logger;

import java.io.*;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static de.amr.pacmanfx.Globals.HTS;
import static de.amr.pacmanfx.Globals.TS;
import static de.amr.pacmanfx.Validations.requireNonNegativeInt;
import static java.util.Objects.requireNonNull;

public class WorldMap {

    public static final String MARKER_BEGIN_TERRAIN_LAYER = "!terrain";
    public static final String MARKER_BEGIN_FOOD_LAYER = "!food";
    public static final String MARKER_BEGIN_DATA_SECTION = "!data";

    public static boolean isValidTerrainCode(byte code) {
        return Stream.of(TerrainTile.values()).anyMatch(tile -> tile.$ == code);
    }

    public static boolean isValidFoodCode(byte code) {
        return Stream.of(FoodTile.values()).anyMatch(tile -> tile.$ == code);
    }

    private static void assertValidLayerID(LayerID id) {
        requireNonNull(id);
        if (Stream.of(LayerID.values()).noneMatch(layerID -> layerID == id)) {
            throw new IllegalArgumentException("Illegal map layer ID: '%s'".formatted(id));
        }
    }

    public static WorldMap emptyMap(int tilesX, int tilesY) {
        var empty = new WorldMap();
        empty.numRows = requireNonNegativeInt(tilesY);
        empty.numCols = requireNonNegativeInt(tilesX);
        empty.terrainLayer = new WorldMapLayer(tilesY, tilesX);
        empty.foodLayer = new WorldMapLayer(tilesY, tilesX);
        return empty;
    }

    public static WorldMap copyOfMap(WorldMap original) {
        requireNonNull(original);
        var copy = new WorldMap();
        copy.numRows = original.numRows;
        copy.numCols = original.numCols;
        copy.terrainLayer = new WorldMapLayer(original.terrainLayer);
        copy.foodLayer = new WorldMapLayer(original.foodLayer);
        copy.obstacles = original.obstacles != null ? new HashSet<>(original.obstacles) : null;
        copy.configMap = new HashMap<>(original.configMap);
        copy.url = original.url;
        return copy;
    }

    public static WorldMap loadFromURL(URL url) throws IOException {
        requireNonNull(url);
        try (var br = new BufferedReader(new InputStreamReader(url.openStream(), StandardCharsets.UTF_8))) {
            WorldMap worldMap = WorldMapParser.parse(br.lines(), WorldMap::isValidTerrainCode, WorldMap::isValidFoodCode);
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
            pw.print(sourceCode());
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
    WorldMapLayer terrainLayer;
    WorldMapLayer foodLayer;
    Set<Obstacle> obstacles; // uninitialized!
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
        WorldMap newMap = WorldMap.emptyMap(numCols, numRows + 1);
        newMap.terrainLayer.replacePropertyMap(terrainLayer.propertyMap());
        newMap.foodLayer.replacePropertyMap(foodLayer.propertyMap());
        for (int row = 0; row < newMap.numRows; ++row) {
            for (int col = 0; col < newMap.numCols; ++col) {
                byte terrainValue = TerrainTile.EMPTY.$;
                byte foodValue = FoodTile.EMPTY.$;
                if (row < rowIndex) {
                    terrainValue = content(LayerID.TERRAIN, row, col);
                    foodValue = content(LayerID.FOOD, row, col);
                } else if (row > rowIndex) {
                    terrainValue = content(LayerID.TERRAIN, row - 1, col);
                    foodValue = content(LayerID.FOOD, row - 1, col);
                } else {
                    if ((col == 0 || col == numCols - 1)
                        && content(LayerID.TERRAIN, row, col) == TerrainTile.WALL_V.$) {
                        terrainValue = TerrainTile.WALL_V.$; // keep vertical border wall
                    }
                }
                newMap.setContent(LayerID.TERRAIN, row, col, terrainValue);
                newMap.setContent(LayerID.FOOD, row, col, foodValue);
            }
        }
        return newMap;
    }

    public WorldMap deleteRowAtIndex(int rowIndexToDelete) {
        if (rowIndexToDelete < 0 || rowIndexToDelete > numRows - 1) {
            throw new IllegalArgumentException("Illegal row index for deleting row: " + rowIndexToDelete);
        }
        WorldMap newMap = WorldMap.emptyMap(numCols, numRows - 1);
        newMap.terrainLayer.replacePropertyMap(terrainLayer.propertyMap());
        newMap.foodLayer.replacePropertyMap(foodLayer.propertyMap());
        for (int row = 0; row < newMap.numRows; ++row) {
            for (int col = 0; col < newMap.numCols; ++col) {
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

        Vector2i houseMinTile = getTerrainTileProperty(DefaultWorldMapPropertyName.POS_HOUSE_MIN_TILE);
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

    public WorldMapLayer terrainLayer() {
        return terrainLayer;
    }

    public WorldMapLayer foodLayer() {
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

    public boolean outOfWorld(Vector2i tile) {
        return outOfWorld(tile.y(), tile.x());
    }

    public boolean outOfWorld(int row, int col) {
        return row < 0 || row >= numRows || col < 0 || col >= numCols;
    }

    public void assertInsideWorld(Vector2i tile) {
        requireNonNull(tile);
        if (outOfWorld(tile)) {
            throw new IllegalArgumentException("Tile %s is not inside world".formatted(tile));
        }
    }

    /**
     * @param tile tile inside map bounds
     * @return index in row-by-row order
     */
    public int indexInRowWiseOrder(Vector2i tile) {
        return numCols * tile.y() + tile.x();
    }

    /**
     * @return stream of all tiles of this map (row-by-row)
     */
    public Stream<Vector2i> tiles() {
        return IntStream.range(0, numCols * numRows).mapToObj(this::tile);
    }

    /**
     * @param index tile index in order top-to-bottom, left-to-right
     * @return tile with given index
     */
    public Vector2i tile(int index) {
        return Vector2i.of(index % numCols, index / numCols);
    }

    /**
     * @param tile some tile
     * @return The tile at the mirrored position wrt vertical mirror axis.
     */
    public Vector2i mirrorPosition(Vector2i tile) {
        assertInsideWorld(tile);
        return Vector2i.of(numCols - 1 - tile.x(), tile.y());
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

    public Stream<String> propertyNames(LayerID layerID) {
        assertValidLayerID(layerID);
        return layer(layerID).propertyMap().keySet().stream();
    }

    /**
     * @param propertyName property name
     * @param defaultTile tile returned if property map does not contain property name (can be null)
     * @return tile value of property in terrain layer or default value
     */
    public Vector2i getTerrainTileProperty(String propertyName, Vector2i defaultTile) {
        requireNonNull(propertyName);
        String value = terrainLayer.propertyMap().get(propertyName);
        return value != null
            ? WorldMapParser.parseTile(value).orElse(defaultTile)
            : defaultTile;
    }

    /**
     * @param propertyName property name
     * @return tile value of property in terrain layer or <code>null</code>
     */
    public Vector2i getTerrainTileProperty(String propertyName) {
        return getTerrainTileProperty(propertyName, null);
    }

    /**
     * @param layerID Layer ID
     * @param row row inside map bounds
     * @param col column inside map bounds
     * @return map data at position
     * @throws IllegalArgumentException if tile outside map bounds
     */
    public byte content(LayerID layerID, int row, int col) {
        assertValidLayerID(layerID);
        if (outOfWorld(row, col)) {
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
     * @param code map value
     * @throws IllegalArgumentException if tile outside map bounds
     */
    public void setContent(LayerID layerID, int row, int col, byte code) {
        assertValidLayerID(layerID);
        if (outOfWorld(row, col)) {
            throw new IllegalArgumentException(String.format("Illegal map coordinate row=%d col=%d", row, col));
        }
        layer(layerID).set(row, col, code);
    }

    /**
     * Sets map data at position inside map bounds
     *
     * @param layerID Layer ID
     * @param tile tile inside map bounds
     * @param code map value
     * @throws IllegalArgumentException if tile outside map bounds
     */
    public void setContent(LayerID layerID, Vector2i tile, byte code) {
        setContent(layerID, tile.y(), tile.x(), code);
    }

    /**
     * Sets map data for a rectangular region.
     *
     * @param layerID Layer ID
     * @param origin top-left tile of region
     * @param content content of region
     */
    public void setContent(LayerID layerID, Vector2i origin, byte[][] content) {
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

    private void printLayer(PrintWriter pw, WorldMapLayer layer) {
        layer.propertyMap().entrySet().stream()
            .sorted(Map.Entry.comparingByKey())
            .map(entry -> "%s=%s".formatted(entry.getKey(), entry.getValue()))
            .forEach(pw::println);
        pw.println(WorldMap.MARKER_BEGIN_DATA_SECTION);
        for (int row = 0; row < layer.numRows(); ++row) {
            for (int col = 0; col < layer.numCols(); ++col) {
                byte value = layer.get(row, col);
                pw.printf("#%02X", value);
                if (col < layer.numCols() - 1) {
                    pw.print(",");
                }
            }
            pw.println();
        }
    }

    public String sourceCode() {
        var sw = new StringWriter();
        var pw = new PrintWriter(sw);
        pw.println(WorldMap.MARKER_BEGIN_TERRAIN_LAYER);
        printLayer(pw, terrainLayer);
        pw.println(WorldMap.MARKER_BEGIN_FOOD_LAYER);
        printLayer(pw, foodLayer);
        pw.flush();
        return sw.toString();
    }

    public String sourceCodeWithLineNumbers() {
        StringBuilder sb = new StringBuilder();
        String[] lines = sourceCode().split("\n");
        for (int i = 0; i < lines.length; ++i) {
            sb.append("%5d: ".formatted(i + 1)).append(lines[i]).append("\n");
        }
        return sb.toString();
    }
}