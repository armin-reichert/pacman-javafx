/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.lib.tilemap;

import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.Vector2i;
import org.tinylog.Logger;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.util.*;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static de.amr.games.pacman.lib.Direction.*;
import static de.amr.games.pacman.lib.Globals.v2i;

/**
 * @author Armin Reichert
 */
public class TileMap {

    static final String DATA_SECTION_START = "!data";

    private final Properties properties = new Properties();
    private final byte[][] data;
    private final List<TileMapPath> wallPaths = new ArrayList<>();
    private final List<TileMapPath> dwallPaths = new ArrayList<>();

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

    public static TileMap parse(List<String> lines, byte valueLimit) {
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
        tileMap.loadPropertiesFromText(propertySection.toString());

        for (int lineIndex = dataSectionStartIndex; lineIndex < lines.size(); ++lineIndex) {
            String line = lines.get(lineIndex);
            int row = lineIndex -dataSectionStartIndex;
            String[] columns = line.split(",");
            for (int col = 0; col < columns.length; ++col) {
                String entry = columns[col].trim();
                try {
                    byte value = Byte.parseByte(entry);
                    if (value >= valueLimit) {
                        tileMap.data[row][col] = Tiles.EMPTY;
                        Logger.info("Invalid tile map value {} at row {}, col {}", value, row, col);
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

    public static TileMap copyOf(TileMap other) {
        var copy = new TileMap(other.numRows(), other.numCols());
        for (int row = 0; row < other.numRows(); ++row) {
            copy.data[row] = Arrays.copyOf(other.data[row], other.numCols());
        }
        copy.properties.putAll(other.properties);
        return copy;
    }

    public TileMap(int numRows, int numCols) {
        data = new byte[numRows][numCols];
    }

    private TileMap(byte[][] data) {
        this.data = data;
    }

    public void computePaths() {
        Logger.debug("Compute paths for {}", this);
        wallPaths.clear();
        dwallPaths.clear();

        var explored = new BitSet();
        Predicate<Vector2i> isUnexplored = tile -> !explored.get(index(tile));

        tiles(Tiles.CORNER_NW).filter(isUnexplored)
            .map(corner -> new TileMapPath(this, explored, corner, LEFT))
            .forEach(wallPaths::add);

        // Paths starting at left and right maze border leading inside maze
        var handlesLeft = new HashMap<Vector2i, Direction>();
        var handlesRight = new HashMap<Vector2i, Direction>();
        for (int row = 0; row < numRows(); ++row) {
            if (get(row, 0) == Tiles.DWALL_H) {
                handlesLeft.put(new Vector2i(0, row), RIGHT);
            }
            if (get(row, 0) == Tiles.DCORNER_SE) {
                handlesLeft.put(new Vector2i(0, row), UP);
            }
            if (get(row, 0) == Tiles.DCORNER_NE) {
                handlesLeft.put(new Vector2i(0, row), DOWN);
            }
            if (get(row, numCols() - 1) == Tiles.DWALL_H) {
                handlesRight.put(new Vector2i(numCols() - 1, row), LEFT);
            }
            if (get(row, numCols() - 1) == Tiles.DCORNER_SW) {
                handlesLeft.put(new Vector2i(0, row), UP);
            }
            if (get(row, numCols() - 1) == Tiles.DCORNER_NW) {
                handlesLeft.put(new Vector2i(0, row), DOWN);
            }
        }

        handlesLeft.entrySet().stream()
            .filter(entry -> isUnexplored.test(entry.getKey()))
            .map(entry -> new TileMapPath(this, explored, entry.getKey(), entry.getValue()))
            .forEach(dwallPaths::add);

        handlesRight.entrySet().stream()
            .filter(entry -> isUnexplored.test(entry.getKey()))
            .map(entry -> new TileMapPath(this, explored, entry.getKey(), entry.getValue()))
            .forEach(dwallPaths::add);

        // find ghost house, doors are included as walls!
        tiles(Tiles.DCORNER_NW).filter(isUnexplored)
            .map(corner -> new TileMapPath(this, explored, corner, LEFT))
            .forEach(dwallPaths::add);

        Logger.debug("Paths computed, {} single wall paths, {} double wall paths", wallPaths.size(), dwallPaths.size());
    }

    public Stream<TileMapPath> wallPaths() {
        return wallPaths.stream();
    }

    public Stream<TileMapPath> dwallPaths() {
        return dwallPaths.stream();
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
        return v2i(index % numCols(), index / numCols());
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

    public void setProperty(String name, String value) {
        properties.put(name, value);
    }

    @SuppressWarnings("unchecked")
    public <T> T getProperty(String key) {
        return (T) properties.get(key);
    }

    public boolean hasProperty(String name) {
        return properties.containsKey(name);
    }

    public Properties getProperties() {
        return properties;
    }

    public Vector2i getTileProperty(String key, Vector2i defaultTile) {
        if (hasProperty(key)) {
            Vector2i tile = parseVector2i(getProperty(key));
            return tile != null ? tile : defaultTile;
        }
        return defaultTile;
    }

    public void loadPropertiesFromText(String text) {
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
     * Sets all map data to {@link Tiles#EMPTY}
     */
    public void clear() {
        for (byte[] row : data) {
            Arrays.fill(row, Tiles.EMPTY);
        }
    }

    public void print(PrintWriter w) throws IOException {
        properties.store(w, "");
        w.println(DATA_SECTION_START);
        for (int row = 0; row < numRows(); ++row) {
            for (int col = 0; col < numCols(); ++col) {
                w.printf("%2d", data[row][col]);
                if (col < numCols() - 1) {
                    w.print(",");
                }
            }
            w.println();
        }
    }
}