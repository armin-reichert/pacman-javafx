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
import java.io.Writer;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static de.amr.games.pacman.lib.Direction.*;
import static de.amr.games.pacman.lib.Globals.checkNotNull;
import static de.amr.games.pacman.lib.Globals.v2i;
import static java.util.function.Predicate.not;

/**
 * @author Armin Reichert
 */
public class TileMap {

    public static final String DATA_SECTION_START = "!data";

    // only used in terrain maps for computing and storing contours
    private static class TerrainMapData {
        private final BitSet exploredSet = new BitSet();
        private List<TileMapPath> singleStrokePaths = new ArrayList<>();
        private List<TileMapPath> doubleStrokePaths = new ArrayList<>();
        private List<TileMapPath> fillerPaths = new ArrayList<>();

        TerrainMapData() {}

        TerrainMapData(TerrainMapData other) {
            singleStrokePaths = new ArrayList<>(other.singleStrokePaths);
            doubleStrokePaths = new ArrayList<>(other.doubleStrokePaths);
            fillerPaths = new ArrayList<>(other.fillerPaths);
        }

        void clearExploredSet() {
            exploredSet.clear();
        }
    }

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
        tileMap.loadPropertiesFromText(propertySection.toString());

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
                        tileMap.data[row][col] = Tiles.EMPTY;
                        Logger.info("Invalid tile map value {} at row {}, col {}", value, row, col);
                    }
                } catch (NumberFormatException x) {
                    Logger.error("Invalid tile map entry {} at row {}, col {}", entry, row, col);
                }
            }
        }
        return tileMap;
    }

    private final Properties properties = new Properties();
    private final byte[][] data;
    private TerrainMapData terrainMapData;

    public TileMap(TileMap other) {
        int numRows = other.numRows(), numCols = other.numCols();
        properties.putAll(other.properties);
        data = new byte[numRows][];
        for (int row = 0; row < numRows; ++row) {
            data[row] = Arrays.copyOf(other.data[row], numCols);
        }
        if (other.terrainMapData != null) {
            terrainMapData = new TerrainMapData(other.terrainMapData);
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

    public String getProperty(String key) {
        return properties.getProperty(key);
    }

    public String getPropertyOrDefault(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
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

    public void print(Writer w) throws IOException {
        var pw = new PrintWriter(w);
        properties.store(w, "");
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

    // How the move direction changes when "traversing" a tile
    private static Direction exitDirection(Direction incomingDir, byte tileType) {
        return switch (tileType) {
            case Tiles.CORNER_NW, Tiles.DCORNER_NW, Tiles.DCORNER_ANGULAR_NW -> incomingDir == LEFT  ? DOWN  : RIGHT;
            case Tiles.CORNER_NE, Tiles.DCORNER_NE, Tiles.DCORNER_ANGULAR_NE -> incomingDir == RIGHT ? DOWN  : LEFT;
            case Tiles.CORNER_SE, Tiles.DCORNER_SE, Tiles.DCORNER_ANGULAR_SE -> incomingDir == DOWN  ? LEFT  : UP;
            case Tiles.CORNER_SW, Tiles.DCORNER_SW, Tiles.DCORNER_ANGULAR_SW -> incomingDir == DOWN  ? RIGHT : UP;
            default -> incomingDir;
        };
    }

    /**
     * Computes the contour paths created by terrain tiles which are then used by the renderer. Outer paths like the
     * maze border are double-stroked, inner paths like normal obstacles are single-stroked. The ghost house is
     * double-stroked and uses angular corner tiles.
     */
    public void computeTerrainPaths() {
        terrainMapData = new TerrainMapData();

        int firstCol = 0, lastCol = numCols() - 1;
        Direction startDir;
        // Paths starting at left maze border leading inside maze
        for (int row = 0; row < numRows(); ++row) {
            byte firstColContent = get(row, firstCol);
            startDir = switch (firstColContent) {
                case Tiles.DWALL_H -> RIGHT;
                case Tiles.DCORNER_SE, Tiles.DCORNER_ANGULAR_SE -> UP; // tunnel entry, path continues upwards
                case Tiles.DCORNER_NE, Tiles.DCORNER_ANGULAR_NE -> RIGHT; // ??? why not down?
                default -> null; // should never happen
            };
            if (startDir != null) {
                addDoubleStrokePath(new Vector2i(firstCol, row), startDir);
            }
        }
        // Paths starting at right maze border leading inside maze
        for (int row = 0; row < numRows(); ++row) {
            byte lastColContent = get(row, lastCol);
            startDir = switch (lastColContent) {
                case Tiles.DWALL_H -> LEFT;
                case Tiles.DCORNER_SW, Tiles.DCORNER_ANGULAR_SW -> UP; // tunnel entry, path continues upwards
                case Tiles.DCORNER_NW, Tiles.DCORNER_ANGULAR_NW -> DOWN; // tunnel entry, path continues downwards
                default -> null; // should never happen
            };
            if (startDir != null) {
                addDoubleStrokePath(new Vector2i(lastCol, row), startDir);
            }
        }

        // Other closed double-stroke paths
        for (int row = 0; row < numRows(); ++row) {
            for (int col = 0; col < numCols(); ++col) {
                if (get(row, col) == Tiles.DWALL_V) {
                    addDoubleStrokePath(new Vector2i(col, row), DOWN);
                }
            }
        }

        // find ghost house, doors are included as walls!
        tiles(Tiles.DCORNER_ANGULAR_NW)
            .filter(not(this::isExplored))
            .filter(tile -> tile.x() > firstCol && tile.x() < lastCol)
            .map(corner -> computePath(corner, LEFT))
            .forEach(terrainMapData.doubleStrokePaths::add);

        // add paths for obstacles inside maze, start with top-left corner of obstacle
        tiles(Tiles.CORNER_NW).filter(not(this::isExplored))
            .map(corner -> computePath(corner, LEFT))
            .forEach(terrainMapData.singleStrokePaths::add);


        // create filler paths for concavities from maze border inside maze
        terrainMapData.clearExploredSet();
        int topRow = 3, bottomRow = numRows() - 3; // TODO make this more general

        for (int col = 0; col < numCols() - 1; ++col) {
            if (get(topRow, col) == Tiles.DCORNER_NE && get(topRow, col + 1) == Tiles.DCORNER_NW) {
                Logger.info("Found concavity entry at top row {} col {}", topRow, col);
                Vector2i pathStartTile = new Vector2i(col, topRow + 1);
                TileMapPath path = computePath(pathStartTile, DOWN, tile -> outOfBounds(tile) || tile.equals(pathStartTile.plus(1, -1)));
                path.add(UP);
                path.add(LEFT);
                path.add(DOWN);
                terrainMapData.fillerPaths.add(path);
            }
        }
        for (int col = 0; col < numCols() - 1; ++col) {
            if (get(bottomRow, col) == Tiles.DCORNER_SE && get(bottomRow, col + 1) == Tiles.DCORNER_SW) {
                Logger.info("Found concavity entry at bottom row {} col {}", topRow, col);
                Vector2i pathStartTile = new Vector2i(col, bottomRow - 1);
                TileMapPath path = computePath(pathStartTile, UP, tile -> outOfBounds(tile) || tile.equals(pathStartTile.plus(1, 0)));
                path.add(DOWN);
                path.add(LEFT);
                path.add(UP);
                terrainMapData.fillerPaths.add(path);
            }
        }
        Logger.debug("Paths computed, {} single wall paths, {} double wall paths, {} filler paths",
            terrainMapData.singleStrokePaths.size(), terrainMapData.doubleStrokePaths.size(), terrainMapData.fillerPaths.size());
    }

    public Stream<TileMapPath> singleStrokePaths() {
        return terrainMapData != null ? terrainMapData.singleStrokePaths.stream() : Stream.empty();
    }

    public Stream<TileMapPath> doubleStrokePaths() {
        return terrainMapData != null ? terrainMapData.doubleStrokePaths.stream() : Stream.empty();
    }

    public Stream<TileMapPath> fillerPaths() {
        return terrainMapData != null ? terrainMapData.fillerPaths.stream() : Stream.empty();
    }

    private TileMapPath computePath(Vector2i startTile, Direction startDir) {
        return computePath(startTile, startDir, this::outOfBounds);
    }

    private TileMapPath computePath(Vector2i startTile, Direction startDir, Predicate<Vector2i> stopCondition) {
        checkNotNull(startTile);
        checkNotNull(startDir);
        if (outOfBounds(startTile)) {
            throw new IllegalArgumentException("Start tile of path must be inside map");
        }
        TileMapPath path = new TileMapPath(startTile);
        setExplored(startTile);
        var tile = startTile;
        var dir = startDir;
        while (true) {
            dir = exitDirection(dir, get(tile));
            tile = tile.plus(dir.vector());
            if (stopCondition.test(tile)) {
                break;
            }
            if (isExplored(tile)) {
                path.add(dir);
                break;
            }
            path.add(dir);
            setExplored(tile);
        }
        return path;
    }

    private void addDoubleStrokePath(Vector2i startTile, Direction startDir) {
        if (!isExplored(startTile)) {
            terrainMapData.doubleStrokePaths.add(computePath(startTile, startDir));
        }
    }

    private boolean isExplored(Vector2i tile) {
        return terrainMapData.exploredSet.get(index(tile));
    }

    private void setExplored(Vector2i tile) {
        terrainMapData.exploredSet.set(index(tile));
    }
}