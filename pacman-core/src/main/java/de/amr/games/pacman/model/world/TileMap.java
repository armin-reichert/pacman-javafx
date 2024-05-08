/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.model.world;

import de.amr.games.pacman.lib.Vector2i;
import org.tinylog.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static de.amr.games.pacman.lib.Globals.v2i;

/**
 * @author Armin Reichert
 */
public class TileMap {

    public static TileMap fromURL(URL url, byte valueLimit) {
        try (BufferedReader r = new BufferedReader(new InputStreamReader(url.openStream(), StandardCharsets.UTF_8))) {
            return new TileMap(r.lines().toList(), valueLimit);
        } catch (IOException x) {
            Logger.error(x);
            throw new IllegalArgumentException("Cannot create tile map from URL " + url);
        }
    }

    private final byte[][] data;
    private final String commentSection;
    private final Map<String, String> properties = new HashMap<>();

    private TileMap(List<String> lines, byte valueLimit) {
        int numRows = 0, numCols = -1;
        StringBuilder sb = new StringBuilder();
        for (String line : lines) {
            if (line.startsWith("#")) {
                sb.append(line).append("\n");
            } else {
                numRows += 1;
                if (numCols == -1) {
                    String[] values = line.split(",");
                    numCols = values.length;
                }
            }
        }
        commentSection = sb.toString();
        this.data = new byte[numRows][numCols];
        int row = 0;
        for (String line : lines) {
            if (line.startsWith("#")) {
                String rest = line.substring(1);
                var assignment = rest.split("=");
                if (assignment.length == 2) {
                    var lhs = assignment[0].trim();
                    var rhs = assignment[1].trim();
                    properties.put(lhs, rhs);
                }
                continue;
            }
            String[] values = line.split(",");
            if (values.length != numCols) {
                throw new IllegalArgumentException("Inconsistent map data");
            }
            for (int col = 0; col < values.length; ++col) {
                byte value = Byte.parseByte(values[col].trim());
                if (value >= valueLimit) {
                    Logger.error("Invalid tile map value {} at row {}, col {}", value, row, col);
                } else {
                    data[row][col] = value;
                }
            }
            ++row;
        }
    }

    public TileMap(TileMap other) {
        data = new byte[other.numRows()][];
        for (int row = 0; row < other.numRows(); ++row) {
            data[row] = Arrays.copyOf(other.data[row], other.numCols());
        }
        properties.putAll(other.properties);
        commentSection = other.commentSection;
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

    public String getCommentSection() {
        return commentSection;
    }

    public String getProperty(String key) {
        return properties.get(key);
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
}