/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.lib.tilemap;

import de.amr.games.pacman.lib.Vector2i;
import de.amr.games.pacman.model.WorldMapProperty;
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

import static de.amr.games.pacman.Globals.HTS;
import static de.amr.games.pacman.Globals.TS;
import static de.amr.games.pacman.lib.tilemap.LayerID.FOOD;
import static de.amr.games.pacman.lib.tilemap.LayerID.TERRAIN;
import static java.util.Objects.requireNonNull;

public class WorldMap {

    private static final Pattern TILE_PATTERN = Pattern.compile("\\((\\d+),(\\d+)\\)");
    private static final String TILE_FORMAT = "(%d,%d)";

    private static final String BEGIN_TERRAIN_LAYER = "!terrain";
    private static final String BEGIN_FOOD_LAYER    = "!food";
    private static final String BEGIN_DATA_SECTION  = "!data";

    public static Optional<Vector2i> parseTile(String s) {
        requireNonNull(s);
        Matcher m = TILE_PATTERN.matcher(s);
        if (!m.matches()) {
            return Optional.empty();
        }
        try {
            int x = Integer.parseInt(m.group(1));
            int y = Integer.parseInt(m.group(2));
            return Optional.of(new Vector2i(x, y));
        } catch (NumberFormatException x) {
            Logger.error("Could not parse tile from text '{}'", s);
            return Optional.empty();
        }
    }

    public static String formatTile(Vector2i tile) {
        requireNonNull(tile);
        return String.format(TILE_FORMAT, tile.x(), tile.y());
    }

    private final URL url;
    private WorldMapLayer terrainLayer;
    private WorldMapLayer foodLayer;
    private Set<Obstacle> obstacles;
    private Map<String, Object> configMap;

    public WorldMap(WorldMap other) {
        requireNonNull(other);
        url = other.url;
        terrainLayer = new WorldMapLayer(other.terrainLayer);
        foodLayer = new WorldMapLayer(other.foodLayer);
        if (other.obstacles != null) {
            obstacles = new HashSet<>(other.obstacles);
        }
        if (other.configMap != null) {
            configMap = new HashMap<>(other.configMap);
        }
    }

    public WorldMap(int numRows, int numCols) {
        url = null;
        terrainLayer = new WorldMapLayer(numRows, numCols);
        foodLayer = new WorldMapLayer(numRows, numCols);
    }

    public WorldMap(URL url) throws IOException {
        this.url = requireNonNull(url);
        var reader = new BufferedReader(new InputStreamReader(url.openStream(), StandardCharsets.UTF_8));
        parse(reader.lines().toList());
    }

    public WorldMap(File file) throws IOException {
        this(file.toURI().toURL());
    }

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

    public String sourceText() {
        var sw = new StringWriter();
        var pw = new PrintWriter(sw);
        pw.println(BEGIN_TERRAIN_LAYER);
        print(pw, terrainLayer);
        pw.println(BEGIN_FOOD_LAYER);
        print(pw, foodLayer);
        return sw.toString();
    }

    public boolean save(File file) {
        try (PrintWriter pw = new PrintWriter(file, StandardCharsets.UTF_8)) {
            pw.print(sourceText());
            return true;
        } catch (IOException x) {
            Logger.error(x);
            return false;
        }
    }

    private void print(PrintWriter pw, WorldMapLayer layer) {
        Map<String, String> properties = layer.properties();
        properties.keySet().stream().sorted().map(name -> "%s=%s".formatted(name, properties.get(name))).forEach(pw::println);
        pw.println(BEGIN_DATA_SECTION);
        for (int row = 0; row < numRows(); ++row) {
            for (int col = 0; col < numCols(); ++col) {
                byte value = layer.get(row, col);
                pw.printf("#%02X", value);
                if (col < numCols() - 1) {
                    pw.print(",");
                }
            }
            pw.println();
        }
        pw.flush();
    }

    public WorldMap insertRowBeforeIndex(int rowIndex) {
        if (rowIndex < 0 || rowIndex > numRows()) {
            throw new IllegalArgumentException("Illegal row index for inserting row: " + rowIndex);
        }
        WorldMap newMap = new WorldMap(numRows() + 1, numCols());
        newMap.terrainLayer.replaceProperties(terrainLayer.properties());
        newMap.foodLayer.replaceProperties(foodLayer.properties());
        for (int row = 0; row < newMap.numRows(); ++row) {
            for (int col = 0; col < newMap.numCols(); ++col) {
                byte terrainValue = TerrainTiles.EMPTY;
                byte foodValue = FoodTiles.EMPTY;
                if (row < rowIndex) {
                    terrainValue = get(TERRAIN, row, col);
                    foodValue = get(FOOD, row, col);
                } else if (row > rowIndex) {
                    terrainValue = get(TERRAIN, row - 1, col);
                    foodValue = get(FOOD, row - 1, col);
                } else {
                    if ((col == 0 || col == numCols() - 1)
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
        if (rowIndexToDelete < 0 || rowIndexToDelete > numRows() - 1) {
            throw new IllegalArgumentException("Illegal row index for deleting row: " + rowIndexToDelete);
        }
        WorldMap newMap = new WorldMap(numRows() - 1, numCols());
        newMap.terrainLayer.replaceProperties(terrainLayer.properties());
        newMap.foodLayer.replaceProperties(foodLayer.properties());
        for (int row = 0; row < newMap.numRows(); ++row) {
            for (int col = 0; col < newMap.numCols(); ++col) {
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

    private void parse(List<String> lines_) {
        // ensure lines can be modified
        List<String> lines = new ArrayList<>(lines_);
        // delete empty lines at end
        int i = lines.size() - 1;
        int count = 0;
        while (i >= 0 && lines.get(i).isBlank()) {
            lines.remove(i);
            ++count;
            --i;
        }
        if (count > 0) {
            Logger.info("{} empty line(s) at end of map file removed", count);
        }
        var terrainLayerRows = new ArrayList<String>();
        var foodLayerRows = new ArrayList<String>();
        boolean insideTerrainLayer = false, insideFoodLayer = false;
        for (String line : lines) {
            if (BEGIN_TERRAIN_LAYER.equals(line)) {
                insideTerrainLayer = true;
            } else if (BEGIN_FOOD_LAYER.equals(line)) {
                insideTerrainLayer = false;
                insideFoodLayer = true;
            } else if (insideTerrainLayer) {
                terrainLayerRows.add(line);
            } else if (insideFoodLayer) {
                foodLayerRows.add(line);
            } else {
                Logger.error("Line skipped: '{}'", line);
            }
        }
        this.terrainLayer = parseTileMap(terrainLayerRows,
            value -> 0 <= value && value <= TerrainTiles.MAX_VALUE, TerrainTiles.EMPTY);

        this.foodLayer = parseTileMap(foodLayerRows,
            value -> 0 <= value && value <= FoodTiles.ENERGIZER, FoodTiles.EMPTY);

        // Replace obsolete terrain tile values
        tiles().forEach(tile -> {
            byte content = get(TERRAIN, tile);
            byte newContent = switch (content) {
                case TerrainTiles.OBSOLETE_DWALL_H -> TerrainTiles.WALL_H;
                case TerrainTiles.OBSOLETE_DWALL_V -> TerrainTiles.WALL_V;
                case TerrainTiles.OBSOLETE_DCORNER_NW -> TerrainTiles.ARC_NW;
                case TerrainTiles.OBSOLETE_DCORNER_SW -> TerrainTiles.ARC_SW;
                case TerrainTiles.OBSOLETE_DCORNER_SE -> TerrainTiles.ARC_SE;
                case TerrainTiles.OBSOLETE_DCORNER_NE -> TerrainTiles.ARC_NE;
                default -> content;
            };
            set(TERRAIN, tile, newContent);
        });
    }

    private WorldMapLayer parseTileMap(List<String> lines, Predicate<Byte> valueAllowed, byte emptyValue) {
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
                    Logger.error("Inconsistent tile map data: found {} column(s) in line {}, expected {}",
                            columns.length, lineIndex, numDataCols);
                }
            }
        }
        if (numDataRows == 0) {
            Logger.error("Inconsistent tile map data: No data");
        }

        // Second pass: read data and build new tile map
        var tileMap = new WorldMapLayer(numDataRows, numDataCols);
        tileMap.properties().putAll(parseProperties(propertySection.toString()));

        for (int lineIndex = dataStartIndex; lineIndex < lines.size(); ++lineIndex) {
            String line = lines.get(lineIndex);
            int row = lineIndex -dataStartIndex;
            String[] columns = line.split(",");
            for (int col = 0; col < columns.length; ++col) {
                String entry = columns[col].trim();
                try {
                    byte value = Byte.decode(entry);
                    if (valueAllowed.test(value)) {
                        tileMap.set(row, col, value);
                    } else {
                        tileMap.set(row, col, emptyValue);
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

    public List<Vector2i> buildObstacleList() {
        List<Vector2i> tilesWithErrors = new ArrayList<>();
        obstacles = ObstacleBuilder.buildObstacles(this, tilesWithErrors);

        // remove house obstacle
        Vector2i houseMinTile = getTerrainTileProperty(WorldMapProperty.POS_HOUSE_MIN_TILE, null);
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

    private Map<String, String> properties(LayerID layerID) {
        return layer(layerID).properties();
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
        return tiles().filter(tile -> get(layerID, tile) == content);
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

    public void setConfigValue(String key, Object value) {
        getOrCreateConfigMap().put(key, value);
    }

    @SuppressWarnings("unchecked")
    public <T> T getConfigValue(String key) {
        return (T) getOrCreateConfigMap().get(key);
    }

    public boolean hasConfigValue(String key) {
        return getOrCreateConfigMap().containsKey(key);
    }

    private Map<String, Object> getOrCreateConfigMap() {
        if (configMap == null) {
            configMap = new HashMap<>();
        }
        return configMap;
    }

    public Stream<String> propertyNames(LayerID layerID) {
        return layer(layerID).properties().keySet().stream().sorted();
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
    public void set(LayerID layerID, Vector2i tile, byte value) {
        set(layerID, tile.y(), tile.x(), value);
    }
}