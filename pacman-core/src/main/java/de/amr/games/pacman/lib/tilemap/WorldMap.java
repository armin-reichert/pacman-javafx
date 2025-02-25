/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.lib.tilemap;

import de.amr.games.pacman.lib.Vector2i;
import org.tinylog.Logger;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static de.amr.games.pacman.lib.Globals.*;
import static de.amr.games.pacman.lib.tilemap.LayerID.FOOD;
import static de.amr.games.pacman.lib.tilemap.LayerID.TERRAIN;

/**
 * @author Armin Reichert
 */
public class WorldMap {

    public static class Layer {
        private final Map<String, String> properties = new HashMap<>();
        private byte[][] values;

        public Layer() {}

        public Layer(Layer other) {
            int numRows = other.values.length, numCols = other.values[0].length;
            properties.putAll(other.properties);
            values = new byte[numRows][];
            for (int row = 0; row < numRows; ++row) {
                values[row] = Arrays.copyOf(other.values[row], numCols);
            }
        }

        public void replaceProperties(Map<String, String> otherProperties) {
            properties.clear();
            properties.putAll(otherProperties);
        }
    }

    private static final Pattern TILE_PATTERN = Pattern.compile("\\((\\d+),(\\d+)\\)");
    private static final String TILE_FORMAT = "(%d,%d)";

    public static final String PROPERTY_POS_BONUS                = "pos_bonus";
    public static final String PROPERTY_POS_PAC                  = "pos_pac";
    public static final String PROPERTY_POS_RED_GHOST            = "pos_ghost_1_red";
    public static final String PROPERTY_POS_PINK_GHOST           = "pos_ghost_2_pink";
    public static final String PROPERTY_POS_CYAN_GHOST           = "pos_ghost_3_cyan";
    public static final String PROPERTY_POS_ORANGE_GHOST         = "pos_ghost_4_orange";
    public static final String PROPERTY_POS_SCATTER_RED_GHOST    = "pos_scatter_ghost_1_red";
    public static final String PROPERTY_POS_SCATTER_PINK_GHOST   = "pos_scatter_ghost_2_pink";
    public static final String PROPERTY_POS_SCATTER_CYAN_GHOST   = "pos_scatter_ghost_3_cyan";
    public static final String PROPERTY_POS_SCATTER_ORANGE_GHOST = "pos_scatter_ghost_4_orange";
    public static final String PROPERTY_POS_HOUSE_MIN_TILE       = "pos_house_min";
    public static final String PROPERTY_POS_HOUSE_MAX_TILE       = "pos_house_max";

    public static final String PROPERTY_COLOR_FOOD               = "color_food";
    public static final String PROPERTY_COLOR_WALL_STROKE        = "color_wall_stroke";
    public static final String PROPERTY_COLOR_WALL_FILL          = "color_wall_fill";
    public static final String PROPERTY_COLOR_DOOR               = "color_door";

    private static final String BEGIN_TERRAIN_LAYER = "!terrain";
    private static final String BEGIN_FOOD_LAYER    = "!food";
    private static final String BEGIN_DATA_SECTION  = "!data";

    public static Optional<Vector2i> parseTile(String text) {
        assertNotNull(text);
        Matcher m = TILE_PATTERN.matcher(text);
        if (!m.matches()) {
            return Optional.empty();
        }
        try {
            int x = Integer.parseInt(m.group(1));
            int y = Integer.parseInt(m.group(2));
            return Optional.of(new Vector2i(x, y));
        } catch (NumberFormatException x) {
            return Optional.empty();
        }
    }

    public static String formatTile(Vector2i tile) {
        assertNotNull(tile);
        return TILE_FORMAT.formatted(tile.x(), tile.y());
    }

    private final URL url;
    private int numRows;
    private int numCols;
    private Layer terrainLayer;
    private Layer foodLayer;
    private Set<Obstacle> obstacles = Collections.emptySet();
    private final Map<String, Object> config = new HashMap<>();

    public WorldMap(WorldMap other) {
        assertNotNull(other);
        numRows = other.numRows;
        numCols = other.numCols;
        url = other.url;
        terrainLayer = new Layer(other.terrainLayer);
        foodLayer = new Layer(other.foodLayer);
        obstacles = new HashSet<>(other.obstacles);
    }

    public WorldMap(int numRows, int numCols) {
        this.numRows = numRows;
        this.numCols = numCols;
        url = null;
        terrainLayer = new Layer();
        terrainLayer.values = new byte[numRows][numCols];
        foodLayer = new Layer();
        foodLayer.values = new byte[numRows][numCols];
    }

    public WorldMap(URL url) throws IOException {
        this.url = assertNotNull(url);
        var r = new BufferedReader(new InputStreamReader(url.openStream(), StandardCharsets.UTF_8));
        parse(r.lines());
        updateObstacleList();
    }

    public WorldMap(File file) throws IOException {
        url = file.toURI().toURL();
        var r = new BufferedReader(new FileReader(file, StandardCharsets.UTF_8));
        parse(r.lines());
        updateObstacleList();
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

    public String sourceText() {
        var stringWriter = new StringWriter();
        var pw = new PrintWriter(stringWriter);
        pw.println(BEGIN_TERRAIN_LAYER);
        print(pw, terrainLayer);
        pw.println(BEGIN_FOOD_LAYER);
        print(pw, foodLayer);
        return stringWriter.toString();
    }

    public WorldMap insertRowBeforeIndex(int rowIndex) {
        if (rowIndex < 0 || rowIndex > numRows) {
            throw new IllegalArgumentException("Illegal row index for inserting row: " + rowIndex);
        }
        WorldMap newMap = new WorldMap(numRows + 1, numCols);
        newMap.terrainLayer.replaceProperties(terrainLayer.properties);
        newMap.foodLayer.replaceProperties(foodLayer.properties);
        for (int row = 0; row < newMap.numRows; ++row) {
            for (int col = 0; col < newMap.numCols; ++col) {
                byte terrainValue = TerrainTiles.EMPTY;
                byte foodValue = FoodTiles.EMPTY;
                if (row < rowIndex) {
                    terrainValue = get(TERRAIN, row, col);
                    foodValue = get(FOOD, row, col);
                } else if (row > rowIndex) {
                    terrainValue = get(TERRAIN, row - 1, col);
                    foodValue = get(FOOD, row - 1, col);
                } else {
                    if ((col == 0 || col == numCols - 1)
                            && get(TERRAIN, row, col) == TerrainTiles.WALL_V) {
                        terrainValue = TerrainTiles.WALL_V; // keep vertical border wall
                    }
                }
                newMap.set(TERRAIN, row, col, terrainValue);
                newMap.set(FOOD, row, col, foodValue);
            }
        }
        return newMap;
    }

    public WorldMap deleteRowAtIndex(int rowIndexToDelete) {
        if (rowIndexToDelete < 0 || rowIndexToDelete > numRows - 1) {
            throw new IllegalArgumentException("Illegal row index for deleting row: " + rowIndexToDelete);
        }
        WorldMap newMap = new WorldMap(numRows - 1, numCols);
        newMap.terrainLayer.replaceProperties(terrainLayer.properties);
        newMap.foodLayer.replaceProperties(foodLayer.properties);
        for (int row = 0; row < newMap.numRows; ++row) {
            for (int col = 0; col < newMap.numCols; ++col) {
                if (row < rowIndexToDelete) {
                    newMap.set(TERRAIN, row, col, get(TERRAIN, row, col));
                    newMap.set(FOOD, row, col, get(FOOD, row, col));
                } else {
                    newMap.set(TERRAIN, row, col, get(TERRAIN, row + 1, col));
                    newMap.set(FOOD, row, col, get(FOOD, row + 1, col));
                }
            }
        }
        return newMap;
    }

    private void print(PrintWriter pw, Layer layer) {
        layer.properties.keySet().stream()
                .sorted()
                .map(name -> name + "=" + layer.properties.get(name))
                .forEach(pw::println);
        pw.println(BEGIN_DATA_SECTION);
        for (int row = 0; row < numRows; ++row) {
            for (int col = 0; col < numCols; ++col) {
                byte value = layer.values[row][col];
                pw.printf("#%02X", value);
                if (col < numCols - 1) {
                    pw.print(",");
                }
            }
            pw.println();
        }
        pw.flush();
    }

    private void parse(Stream<String> rows) {
        var terrainLayerRows = new ArrayList<String>();
        var foodLayerRows = new ArrayList<String>();
        boolean insideTerrainLayer = false, insideFoodLayer = false;
        for (var row : rows.toList()) {
            if (BEGIN_TERRAIN_LAYER.equals(row)) {
                insideTerrainLayer = true;
            } else if (BEGIN_FOOD_LAYER.equals(row)) {
                insideTerrainLayer = false;
                insideFoodLayer = true;
            } else if (insideTerrainLayer) {
                terrainLayerRows.add(row);
            } else if (insideFoodLayer) {
                foodLayerRows.add(row);
            } else {
                Logger.error("Line skipped: '{}'", row);
            }
        }
        this.terrainLayer = parseTileMap(terrainLayerRows,
            value -> 0 <= value && value <= TerrainTiles.LAST_TERRAIN_VALUE, TerrainTiles.EMPTY);

        this.foodLayer = parseTileMap(foodLayerRows,
            value -> 0 <= value && value <= FoodTiles.ENERGIZER, FoodTiles.EMPTY);

        numRows = this.terrainLayer.values.length;
        numCols = this.terrainLayer.values[0].length;

        // Replace obsolete terrain tile values
        tiles().forEach(tile -> {
            byte content = get(TERRAIN, tile);
            byte newContent = switch (content) {
                case TerrainTiles.OBSOLETE_DWALL_H -> TerrainTiles.WALL_H;
                case TerrainTiles.OBSOLETE_DWALL_V -> TerrainTiles.WALL_V;
                case TerrainTiles.OBSOLETE_DCORNER_NW -> TerrainTiles.CORNER_NW;
                case TerrainTiles.OBSOLETE_DCORNER_SW -> TerrainTiles.CORNER_SW;
                case TerrainTiles.OBSOLETE_DCORNER_SE -> TerrainTiles.CORNER_SE;
                case TerrainTiles.OBSOLETE_DCORNER_NE -> TerrainTiles.CORNER_NE;
                default -> content;
            };
            set(TERRAIN, tile, newContent);
        });
    }

    private Layer parseTileMap(List<String> lines, Predicate<Byte> valueAllowed, byte emptyValue) {
        // First pass: read property section and determine data section size
        int numDataRows = 0, numDataCols = -1;
        int dataStartIndex = -1;
        StringBuilder propertySection = new StringBuilder();
        for (int lineIndex = 0; lineIndex < lines.size(); ++lineIndex) {
            String line = lines.get(lineIndex);
            if (BEGIN_DATA_SECTION.equals(line)) {
                dataStartIndex = lineIndex + 1;
            }
            else if (dataStartIndex == -1) {
                propertySection.append(line).append("\n");
            } else {
                numDataRows++;
                String[] columns = line.split(",");
                if (numDataCols == -1) {
                    numDataCols = columns.length;
                } else if (columns.length != numDataCols) {
                    Logger.error("Inconsistent tile map data: {} columns in line {}, expected {}",
                            columns.length, lineIndex, numDataCols);
                }
            }
        }
        if (numDataRows == 0) {
            Logger.error("Inconsistent tile map data: No data");
        }

        // Second pass: read data and build new tile map
        var tileMap = new Layer();
        tileMap.values = new byte[numDataRows][numDataCols];
        tileMap.properties.putAll(parseProperties(propertySection.toString()));

        for (int lineIndex = dataStartIndex; lineIndex < lines.size(); ++lineIndex) {
            String line = lines.get(lineIndex);
            int row = lineIndex -dataStartIndex;
            String[] columns = line.split(",");
            for (int col = 0; col < columns.length; ++col) {
                String entry = columns[col].trim();
                try {
                    byte value = Byte.decode(entry);
                    if (valueAllowed.test(value)) {
                        tileMap.values[row][col] = value;
                    } else {
                        tileMap.values[row][col] = emptyValue;
                        Logger.error("Invalid tile map value {} at row {}, col {}", value, row, col);
                    }
                } catch (NumberFormatException x) {
                    Logger.error("Invalid tile map entry {} at row {}, col {}", entry, row, col);
                }
            }
        }
        return tileMap;
    }

    private Map<String, String> parseProperties(String text) {
        var properties = new HashMap<String, String>();
        String[] lines = text.split("\n");
        for (String line : lines) {
            if (line.startsWith("#")) continue;
            String[] sides = line.split("=");
            if (sides.length != 2) {
                Logger.error("Invalid line inside property section: {}", line);
            } else {
                String lhs = sides[0].trim(), rhs = sides[1].trim();
                properties.put(lhs, rhs);
            }
        }
        return properties;
    }

    public List<Vector2i> updateObstacleList() {
        List<Vector2i> tilesWithErrors = new ArrayList<>();
        obstacles = ObstacleBuilder.buildObstacles(this, tilesWithErrors);

        // remove house obstacle
        Vector2i houseMinTile = getTerrainTileProperty(PROPERTY_POS_HOUSE_MIN_TILE, null);
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
        Logger.debug("Obstacle list updated for {}", this);
        return tilesWithErrors;
    }

    public Set<Obstacle> obstacles() {
        return Collections.unmodifiableSet(obstacles);
    }

    public boolean save(File file) {
        try (PrintWriter pw = new PrintWriter(file, StandardCharsets.UTF_8)) {
            pw.println(BEGIN_TERRAIN_LAYER);
            print(pw, terrainLayer);
            pw.println(BEGIN_FOOD_LAYER);
            print(pw, foodLayer);
            return true;
        } catch (IOException x) {
            Logger.error(x);
            return false;
        }
    }

    private Layer layer(LayerID id) {
        return switch (id) {
            case TERRAIN -> terrainLayer;
            case FOOD -> foodLayer;
        };
    }

    private Map<String, String> properties(LayerID layerID) {
        return layer(layerID).properties;
    }

    public int numCols() {
        return numCols;
    }

    public int numRows() {
        return numRows;
    }

    public URL url() {
        return url;
    }

    /**
     * @param tile tile inside map bounds
     * @return index in row-by-row order
     */
    public int index(Vector2i tile) {
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
        return vec_2i(index % numCols, index / numCols);
    }

    /**
     * @param layerID the layer ID
     * @param content value to search for
     * @return stream of all tiles of this map with given content (row-by-row)
     */
    public Stream<Vector2i> tilesContaining(LayerID layerID, byte content) {
        return tiles().filter(tile -> get(layerID, tile) == content);
    }

    public Vector2i vSymmetricTile(Vector2i tile) {
        return vec_2i(numCols - 1 - tile.x(), tile.y());
    }

    public boolean outOfBounds(Vector2i tile) {
        return outOfBounds(tile.y(), tile.x());
    }

    public boolean outOfBounds(int row, int col) {
        return row < 0 || row >= numRows || col < 0 || col >= numCols;
    }

    public void setConfigValue(String key, Object value) {
        config.put(key, value);
    }

    @SuppressWarnings("unchecked")
    public <T> T getConfigValue(String key) {
        return (T) config.get(key);
    }

    public boolean hasConfigValue(String key) {
        return config.containsKey(key);
    }

    public Stream<String> propertyNames(LayerID layerID) {
        return layer(layerID).properties.keySet().stream().sorted();
    }

    public boolean hasProperty(LayerID layerID, String propertyName) {
        return properties(layerID).containsKey(propertyName);
    }

    public String getProperty(LayerID layerID, String propertyName) {
        return properties(layerID).get(propertyName);
    }

    public String getPropertyOrDefault(LayerID layerID, String propertyName, String defaultValue) {
        return properties(layerID).getOrDefault(propertyName, defaultValue);
    }

    public void setProperty(LayerID layerID, String propertyName, String value) {
        properties(layerID).put(propertyName, value);
    }

    public Vector2i getTileProperty(LayerID layerID, String propertyName, Vector2i defaultTile) {
        if (hasProperty(layerID, propertyName)) {
            String propertyValue = getProperty(layerID, propertyName);
            return parseTile(propertyValue).orElse(defaultTile);
        }
        return defaultTile;
    }

    public Vector2i getTerrainTileProperty(String propertyName, Vector2i defaultTile) {
        return getTileProperty(TERRAIN, propertyName, defaultTile);
    }

    public void removeProperty(LayerID layerID, String propertyName) {
        properties(layerID).remove(propertyName);
    }

    /**
     * @param layerID Layer ID
     * @param row row inside map bounds
     * @param col column inside map bounds
     * @return map data at position
     * @throws IllegalArgumentException if tile outside map bounds
     */
    public byte get(LayerID layerID, int row, int col) {
        assertNotNull(layerID);
        if (outOfBounds(row, col)) {
            throw new IllegalArgumentException(String.format("Illegal map coordinate row=%d col=%d", row, col));
        }
        return layer(layerID).values[row][col];
    }

    /**
     * @param layerID Layer ID
     * @param tile tile inside map bounds
     * @return map data at tile position
     * @throws IllegalArgumentException if tile outside map bounds
     */
    public byte get(LayerID layerID, Vector2i tile) {
        return get(layerID, tile.y(), tile.x());
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
    public void set(LayerID layerID, int row, int col, byte value) {
        assertNotNull(layerID);
        if (outOfBounds(row, col)) {
            throw new IllegalArgumentException(String.format("Illegal map coordinate row=%d col=%d", row, col));
        }
        layer(layerID).values[row][col] = value;
    }

    /**
     * Sets map data at position inside map bounds
     *
     * @param layerID Layer ID
     * @param tile tile inside map bounds
     * @param value map value
     * @throws IllegalArgumentException if tile outside map bounds
     */
    public void set(LayerID layerID, Vector2i tile, byte value) {
        set(layerID, tile.y(), tile.x(), value);
    }

    public void setAll(LayerID layerID, byte value) {
        assertNotNull(layerID);
        for (byte[] row : layer(layerID).values) {
            Arrays.fill(row, value);
        }
    }
}