/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.lib.tilemap;

import de.amr.games.pacman.lib.Vector2i;
import org.tinylog.Logger;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static de.amr.games.pacman.lib.Globals.vec_2i;

/**
 * @author Armin Reichert
 */
public class TileMap {

    public static final String DATA_SECTION_START = "!data";

    public static Vector2i parseVector2i(String text) {
        Pattern pattern = Pattern.compile("\\((\\d+),(\\d+)\\)");
        Matcher m = pattern.matcher(text);
        if (m.matches()) {
            return new Vector2i(Integer.parseInt(m.group(1)), Integer.parseInt(m.group(2)));
        }
        return null;
    }

    public static String formatTile(Vector2i tile) {
        return "(%d,%d)".formatted(tile.x(), tile.y());
    }

    public static TileMap parseTileMap(List<String> lines, Predicate<Byte> valueAllowed) {
        // First pass: read property section and determine data section size
        int numDataRows = 0, numDataCols = -1;
        int dataSectionStartIndex = -1;
        StringBuilder propertySection = new StringBuilder();
        for (int lineIndex = 0; lineIndex < lines.size(); ++lineIndex) {
            String line = lines.get(lineIndex);
            if (DATA_SECTION_START.equals(line)) {
                dataSectionStartIndex = lineIndex + 1;
            }
            else if (dataSectionStartIndex == -1) {
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
        var tileMap = new TileMap(new byte[numDataRows][numDataCols]);
        tileMap.parseProperties(propertySection.toString());

        for (int lineIndex = dataSectionStartIndex; lineIndex < lines.size(); ++lineIndex) {
            String line = lines.get(lineIndex);
            int row = lineIndex -dataSectionStartIndex;
            String[] columns = line.split(",");
            for (int col = 0; col < columns.length; ++col) {
                String entry = columns[col].trim();
                try {
                    byte value = Byte.parseByte(entry);
                    if (valueAllowed.test(value)) {
                        tileMap.data[row][col] = value;
                    } else {
                        tileMap.data[row][col] = TileEncoding.EMPTY;
                        Logger.error("Invalid tile map value {} at row {}, col {}", value, row, col);
                    }
                } catch (NumberFormatException x) {
                    Logger.error("Invalid tile map entry {} at row {}, col {}", entry, row, col);
                }
            }
        }
        return tileMap;
    }

    private final Map<String, Object> properties = new HashMap<>();
    private final byte[][] data;


    public TileMap(TileMap other) {
        int numRows = other.numRows(), numCols = other.numCols();
        properties.putAll(other.properties);
        data = new byte[numRows][];
        for (int row = 0; row < numRows; ++row) {
            data[row] = Arrays.copyOf(other.data[row], numCols);
        }
    }

    public TileMap(int numRows, int numCols) {
        data = new byte[numRows][numCols];
    }

    private TileMap(byte[][] data) {
        this.data = data;
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
        return vec_2i(index % numCols(), index / numCols());
    }

    /**
     * @return stream of all tiles of this map with given content (row-by-row)
     */
    public Stream<Vector2i> tiles(byte content) {
        return tiles().filter(tile -> get(tile) == content);
    }

    /**
     * @param tile tile inside map bounds
     * @return index in row-by-row order
     */
    public int index(Vector2i tile) {
        return numCols() * tile.y() + tile.x();
    }

    public int numCols() {
        return data[0].length;
    }

    public int numRows() {
        return data.length;
    }

    public boolean outOfBounds(Vector2i tile) {
        return outOfBounds(tile.y(), tile.x());
    }

    public boolean outOfBounds(int row, int col) {
        return row < 0 || row >= numRows() || col < 0 || col >= numCols();
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

    public String getStringPropertyOrDefault(String name, String defaultValue) {
        return String.valueOf(properties.getOrDefault(name, defaultValue));
    }

    public boolean hasProperty(String name) {
        return properties.containsKey(name);
    }

    public Stream<String> stringPropertyNames() {
        return properties.keySet().stream().filter(name -> properties.get(name) instanceof String).sorted();
    }

    public Vector2i getTileProperty(String name, Vector2i defaultTile) {
        if (hasProperty(name)) {
            Vector2i tile = parseVector2i(getStringProperty(name));
            return tile != null ? tile : defaultTile;
        }
        return defaultTile;
    }

    private void parseProperties(String text) {
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
    }

    public byte[][] getData() {
        return data;
    }

    /**
     * @param row row inside map bounds
     * @param col column inside map bounds
     * @return map data at position
     * @throws IllegalArgumentException if tile outside map bounds
     */
    public byte get(int row, int col) {
        if (outOfBounds(row, col)) {
            throw new IllegalArgumentException(String.format("Illegal map coordinate row=%d col=%d", row, col));
        }
        return data[row][col];
    }

    /**
     * @param tile tile inside map bounds
     * @return map data at tile position
     * @throws IllegalArgumentException if tile outside map bounds
     */
    public byte get(Vector2i tile) {
        return get(tile.y(), tile.x());
    }

    /**
     * Sets map data at position inside map bounds
     * @param row row inside map bounds
     * @param col column inside map bounds
     * @param value map value
     * @throws IllegalArgumentException if tile outside map bounds
     */
    public void set(int row, int col, byte value) {
        if (outOfBounds(row, col)) {
            throw new IllegalArgumentException(String.format("Illegal map coordinate row=%d col=%d", row, col));
        }
        data[row][col] = value;
    }

    /**
     * Sets map data at position inside map bounds
     * @param tile tile inside map bounds
     * @param value map value
     * @throws IllegalArgumentException if tile outside map bounds
     */
    public void set(Vector2i tile, byte value) {
        set(tile.y(), tile.x(), value);
    }

    /**
     * Sets all map data to {@link TileEncoding#EMPTY}
     */
    public void clear() {
        for (byte[] row : data) {
            Arrays.fill(row, TileEncoding.EMPTY);
        }
    }

    public void print(Writer w) {
        var pw = new PrintWriter(w);
        stringPropertyNames().map(name -> name + "=" + getStringProperty(name)).forEach(pw::println);
        pw.println(DATA_SECTION_START);
        for (int row = 0; row < numRows(); ++row) {
            for (int col = 0; col < numCols(); ++col) {
                pw.printf("%2d", data[row][col]);
                if (col < numCols() - 1) {
                    pw.print(",");
                }
            }
            pw.println();
        }
        pw.flush();
    }

    public String sourceCode() {
        var sw = new StringWriter();
        print(sw);
        return sw.toString();
    }
}