/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.model.world;

import de.amr.games.pacman.lib.Vector2i;
import org.tinylog.Logger;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static de.amr.games.pacman.lib.Globals.*;

/**
 * @author Armin Reichert
 */
@SuppressWarnings("unchecked")
public class TileMap {

    /**
     * @param url URL of tile map file
     * @param valueLimit upper bound (exclusive) of allowed tile values
     * @return tile map loaded from given URL or {@code NULL}
     */
    public static TileMap fromURL(URL url, byte valueLimit) {
        try (BufferedReader r = new BufferedReader(new InputStreamReader(url.openStream(), StandardCharsets.UTF_8))) {
            return parse(r.lines().toList(), valueLimit);
        } catch (IOException x) {
            throw new IllegalArgumentException("Cannot load tile map from URL" + url);
        }
    }

    private final Properties properties = new Properties();
    private final byte[][] data;

    public static TileMap parse(List<String> lines, byte valueLimit) {

        // First pass: read property section and determine data section size
        int numDataRows = 0, numDataCols = -1;
        int dataSectionStart = -1;
        StringBuilder propertySection = new StringBuilder();
        for (int lineIndex = 0; lineIndex < lines.size(); ++lineIndex) {
            String line = lines.get(lineIndex);
            if (line.startsWith("!data")) {
                dataSectionStart = lineIndex + 1;
            }
            else if (dataSectionStart == -1) {
                propertySection.append(line).append("\n");
            } else {
                numDataRows++;
                String[] columns = line.split(",");
                if (numDataCols == -1) {
                    numDataCols = columns.length;
                } else if (numDataCols != columns.length) {
                    Logger.error("Tile map is inconsistent, row={}", lineIndex);
                }
            }
        }
        if (numDataRows == 0) {
            Logger.error("No data section found");
        }

        // Second pass: read data
        var tileMap = new TileMap(new byte[numDataRows][numDataCols]);
        tileMap.setPropertiesFromText(propertySection.toString());
        // keep size up-to-date
        tileMap.properties.setProperty("num_rows", String.valueOf(numDataRows));
        tileMap.properties.setProperty("num_cols", String.valueOf(numDataCols));

        for (int lineIndex = dataSectionStart; lineIndex < lines.size(); ++lineIndex) {
            String line = lines.get(lineIndex);
            int row = lineIndex -dataSectionStart;
            String[] columns = line.split(",");
            for (int col = 0; col < columns.length; ++col) {
                String entry = columns[col].trim();
                try {
                    byte value = Byte.parseByte(entry);
                    if (value >= valueLimit) {
                        tileMap.data[row][col] = Tiles.EMPTY;
                        Logger.error("Invalid tile map value {} at row {}, col {}", value, row, col);
                    } else {
                        tileMap.data[row][col] = value;
                    }
                } catch (NumberFormatException x) {
                    Logger.error("Invalid tile map entry {} at row {}, col {}", entry, row, col);
                }
            }
        }
        return tileMap;
    }

    public static TileMap copy(TileMap other) {
        var copy = new TileMap(other.numRows(), other.numCols());
        for (int row = 0; row < other.numRows(); ++row) {
            copy.data[row] = Arrays.copyOf(other.data[row], other.numCols());
        }
        copy.properties.putAll(other.properties);
        return copy;
    }

    public TileMap(int numRows, int numCols) {
        data = new byte[numRows][numCols];
        properties.setProperty("num_rows", String.valueOf(numRows));
        properties.setProperty("num_cols", String.valueOf(numCols));
    }

    private TileMap(byte[][] data) {
        this.data = data;
    }

    /**
     * @return stream of all tiles of this map row-by-row
     */
    public Stream<Vector2i> tiles() {
        return IntStream.range(0, numCols() * numRows()).mapToObj(this::tile);
    }

    /**
     * @param index tile index in order top-to-bottom, left-to-right
     * @return tile with given index
     */
    public Vector2i tile(int index) {
        return v2i(index % numCols(), index / numCols());
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

    public boolean insideBounds(int row, int col) {
        return 0 <= row && row < numRows() && 0 <= col && col < numCols();
    }

    public boolean insideBounds(Vector2i tile) {
        checkTileNotNull(tile);
        return insideBounds(tile.y(), tile.x());
    }

    @SuppressWarnings("unchecked")
    public <T> T getProperty(String key) {
        return (T) properties.get(key);
    }

    public <T> T getPropertyOrDefault(String key, T defaultValue) {
        return hasProperty(key) ?  (T) properties.get(key) : defaultValue;
    }

    public boolean hasProperty(String name) {
        return properties.containsKey(name);
    }

    public Properties getProperties() {
        return properties;
    }

    public String getPropertiesAsText() {
        StringWriter w = new StringWriter();
        try {
            properties.store(w, "");
            return w.toString();
        } catch (IOException x) {
            Logger.error(x);
            return "";
        }
    }

    public void setPropertiesFromText(String text) {
        StringReader r = new StringReader(text);
        try {
            properties.load(r);
        } catch (IOException x) {
            Logger.error("Could not read properties from text {}", text);
            Logger.error(x);
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
        if (insideBounds(row, col)) {
            return data[row][col];
        }
        throw new IllegalArgumentException(String.format("Illegal map coordinate row=%d col=%d", row, col));
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
        if (!insideBounds(row, col)) {
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
     * Sets all map data to {@link Tiles#EMPTY}
     */
    public void clear() {
        for (byte[] row : data) {
            Arrays.fill(row, Tiles.EMPTY);
        }
    }

    public void write(Writer w) {
        try {
            writeProperties(w);
            writeData(w);
        } catch (IOException x) {
            Logger.error("Could not save tile map");
            Logger.error(x);
        }
    }

    private void writeProperties(Writer w) throws IOException {
        properties.store(w, "");
    }

    private void writeData(Writer w) throws IOException {
        w.write("!data\r\n");
        for (int row = 0; row < numRows(); ++row) {
            for (int col = 0; col < numCols(); ++col) {
                String valueTxt = String.valueOf(get(row, col));
                w.write(String.format("%2s", valueTxt));
                if (col < numCols() - 1) {
                    w.write(",");
                }
            }
            w.write("\r\n");
        }
    }
}