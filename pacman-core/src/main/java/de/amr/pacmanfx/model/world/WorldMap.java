/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.model.world;

import de.amr.pacmanfx.lib.math.Vector2i;
import org.tinylog.Logger;

import java.io.*;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static de.amr.pacmanfx.Validations.requireNonNegativeInt;
import static java.util.Objects.requireNonNull;

public class WorldMap {

    private static final Charset CHARSET = StandardCharsets.UTF_8;

    private static final Pattern TILE_PATTERN = Pattern.compile("\\((\\d+),(\\d+)\\)");
    private static final String COMMENT_PREFIX = "#";

    private static final String MARKER_BEGIN_TERRAIN_LAYER = "!terrain";
    private static final String MARKER_BEGIN_FOOD_LAYER = "!food";
    private static final String MARKER_BEGIN_DATA_SECTION = "!data";

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

    private static WorldMap parse(
        Stream<String> linesStream,
        Predicate<Byte> validTerrainValueTest,
        Predicate<Byte> validFoodValueTest) throws WorldMapParseException
    {
        final var lines = new ArrayList<>(linesStream.toList()); // modifiable list!
        final WorldMap worldMap = new WorldMap();

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
            if (WorldMap.MARKER_BEGIN_TERRAIN_LAYER.equals(line)) {
                insideTerrainLayer = true;
            } else if (WorldMap.MARKER_BEGIN_FOOD_LAYER.equals(line)) {
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
        worldMap.terrainLayer = new TerrainLayer(parseLayer(terrainLayerRows, validTerrainValueTest));
        worldMap.foodLayer = new FoodLayer(parseLayer(foodLayerRows, validFoodValueTest));

        //TODO
        worldMap.numRows = worldMap.terrainLayer.numRows();
        worldMap.numCols = worldMap.terrainLayer.numCols();

        return worldMap;
    }

    private static WorldMapLayer parseLayer(List<String> lines, Predicate<Byte> valueAllowed) throws WorldMapParseException {
        // First pass: read property section and determine data section size
        int numDataRows = 0, numDataCols = -1;
        int dataStartIndex = -1;
        StringBuilder propertySection = new StringBuilder();
        for (int lineIndex = 0; lineIndex < lines.size(); ++lineIndex) {
            String line = lines.get(lineIndex);
            if (WorldMap.MARKER_BEGIN_DATA_SECTION.equals(line)) {
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
                    final String msg = "Inconsistent tile map data: found %d column(s), expected %d".formatted(columns.length, numDataCols);
                    throw new WorldMapParseException(msg, null, lineIndex, line);
                }
            }
        }
        if (numDataRows == 0) {
            final String msg = ("Inconsistent tile map data: No data section found");
            throw new WorldMapParseException(msg, null, 0, "");
        }

        // Second pass: read data and build new tile map
        var tileMap = new WorldMapLayer(numDataRows, numDataCols);
        tileMap.propertyMap().putAll(parseProperties(propertySection.toString()));

        for (int lineIndex = dataStartIndex; lineIndex < lines.size(); ++lineIndex) {
            String line = lines.get(lineIndex);
            int row = lineIndex -dataStartIndex;
            String[] columns = line.split(",");
            for (int col = 0; col < columns.length; ++col) {
                String entry = columns[col].trim();
                try {
                    byte value = Byte.decode(entry);
                    if (valueAllowed.test(value)) {
                        tileMap.setContent(row, col, value);
                    } else {
                        tileMap.setContent(row, col, (byte) 0);
                        Logger.error("Invalid tile map value {} at row {}, col {}", value, row, col);
                    }
                } catch (NumberFormatException x) {
                    Logger.error("Invalid tile map entry {} at row {}, col {}", entry, row, col);
                }
            }
        }
        return tileMap;
    }

    private static Map<String, String> parseProperties(String text) {
        var properties = new HashMap<String, String>();
        String[] lines = text.split("\n");
        for (String line : lines) {
            if (line.startsWith(WorldMap.COMMENT_PREFIX))
                continue;
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

    public static WorldMap create(URL url) throws IOException, WorldMapParseException {
        requireNonNull(url);
        try (var br = new BufferedReader(new InputStreamReader(url.openStream(), CHARSET))) {
            WorldMap worldMap = parse(br.lines(), TerrainTile::isValidCode, FoodTile::isValidCode);
            worldMap.url = url.toExternalForm();
            return worldMap;
        }
    }

    public static WorldMap loadFromFile(File file) throws WorldMapParseException, IOException {
        requireNonNull(file);
        return create(file.toURI().toURL());
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

    private int numCols;
    private int numRows;
    private String url;
    private TerrainLayer terrainLayer;
    private FoodLayer foodLayer;
    private Map<Object, Object> configMap = new HashMap<>();

    private WorldMap() {}

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