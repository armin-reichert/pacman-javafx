/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.model.world;

import de.amr.games.pacman.lib.Vector2i;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static de.amr.games.pacman.lib.Globals.checkTileNotNull;
import static de.amr.games.pacman.lib.Globals.v2i;

/**
 * @author Armin Reichert
 */
public class TileMap {

    static byte[][] parse(List<String> lines) {
        int numRows = lines.size();
        String[] values = lines.getFirst().split(",");
        int numCols = values.length;
        byte[][] bytes = new byte[numRows][numCols];
        int row = 0;
        for (String line : lines) {
            values = line.split(",");
            if (values.length != numCols) {
                throw new IllegalArgumentException("Inconsistent map data");
            }
            for (int col = 0; col < values.length; ++col) {
                bytes[row][col] = Byte.parseByte(values[col].trim());
            }
            ++row;
        }
        return bytes;
    }

    public static TileMap fromURL(URL url, byte valueLimit) {
        try (BufferedReader r = new BufferedReader(
            new InputStreamReader(url.openStream(), StandardCharsets.UTF_8))) {
            var bytes = parse(r.lines().toList());
            return new TileMap(bytes, valueLimit);
        } catch (Exception x) {
            throw new IllegalArgumentException("Cannot create tile map from URL " + url);
        }
    }

    private final byte[][] data;

    public TileMap(byte[][] data, byte lastTileValue) {
        if (data == null) {
            throw new IllegalArgumentException("Map data missing");
        }
        if (data.length == 0) {
            throw new IllegalArgumentException("Map data empty");
        }
        var firstRow = data[0];
        if (firstRow.length == 0) {
            throw new IllegalArgumentException("Map data empty");
        }
        for (var row : data) {
            if (row.length != firstRow.length) {
                throw new IllegalArgumentException("Map has differently sized rows");
            }
        }
        for (int row = 0; row < data.length; ++row) {
            for (int col = 0; col < data[row].length; ++col) {
                byte d = data[row][col];
                if (d < 0 || d > lastTileValue) {
                    throw new IllegalArgumentException(String.format("Map data at row=%d, col=%d are illegal: %d", row, col, d));
                }
            }
        }
        this.data = data;
    }

    public TileMap(TileMap other) {
        data = new byte[other.numRows()][];
        for (int row = 0; row < other.numRows(); ++row) {
            data[row] = Arrays.copyOf(other.data[row], other.numCols());
        }
    }

    public byte[][] getData() {
        return data;
    }

    public boolean hasContentAt(Vector2i tile, byte tileContent) {
        checkTileNotNull(tile);
        return content(tile) == tileContent;
    }

    public byte content(Vector2i tile) {
        return content(tile.y(), tile.x());
    }

    public byte content(int row, int col) {
        return 0 <= row && row < numRows() && 0 <= col && col < numCols() ? data[row][col] : Tiles.EMPTY;
    }

    public void setContent(int row, int col, byte value) {
        if (0 <= row && row < numRows() && 0 <= col && col < numCols()) {
            data[row][col] = value;
        }
    }

    public void setContent(Vector2i tile, byte value) {
        setContent(tile.y(), tile.x(), value);
    }

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

    public int index(Vector2i tile) {
        return numCols() * tile.y() + tile.x();
    }

    public int numCols() {
        return data[0].length;
    }

    public int numRows() {
        return data.length;
    }

    public void clear() {
        tiles().forEach(tile -> setContent(tile, Tiles.EMPTY));
    }
}