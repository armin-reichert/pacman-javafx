package de.amr.pacmanfx.lib.worldmap;

import de.amr.pacmanfx.lib.Vector2i;

import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

public interface RectangularTileRegion {

    int numRows();

    int numCols();

    default boolean outOfBounds(Vector2i tile) {
        return outOfBounds(tile.y(), tile.x());
    }

    default boolean outOfBounds(int row, int col) {
        return row < 0 || row >= numRows() || col < 0 || col >= numCols();
    }

    default void assertInsideWorld(Vector2i tile) {
        requireNonNull(tile);
        if (outOfBounds(tile)) {
            throw new IllegalArgumentException("Tile %s is outside world".formatted(tile));
        }
    }

    default void assertInsideWorld(int row, int col) {
        if (outOfBounds(row, col)) {
            throw new IllegalArgumentException("Coordinate (row=%d, col=%d) is outside world".formatted(row, col));
        }
    }

    /**
     * @param tile tile inside map bounds
     * @return index in row-by-row order
     */
    default int indexInRowWiseOrder(Vector2i tile) {
        return numCols() * tile.y() + tile.x();
    }

    /**
     * @return stream of all tiles of this map (row-by-row)
     */
    default Stream<Vector2i> tiles() {
        return IntStream.range(0, numCols() * numRows()).mapToObj(this::tile);
    }

    /**
     * @param index tile index in order top-to-bottom, left-to-right
     * @return tile with given index
     */
    default Vector2i tile(int index) {
        return Vector2i.of(index % numCols(), index / numCols());
    }

    /**
     * @param tile some tile
     * @return The tile at the mirrored position wrt vertical mirror axis.
     */
    default Vector2i mirrorPosition(Vector2i tile) {
        assertInsideWorld(tile);
        return Vector2i.of(numCols() - 1 - tile.x(), tile.y());
    }
}
