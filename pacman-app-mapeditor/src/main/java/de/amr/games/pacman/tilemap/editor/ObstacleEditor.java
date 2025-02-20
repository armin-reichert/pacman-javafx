/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.tilemap.editor;

import de.amr.games.pacman.lib.Globals;
import de.amr.games.pacman.lib.Vector2i;
import de.amr.games.pacman.lib.tilemap.TerrainTiles;
import de.amr.games.pacman.lib.tilemap.TileMap;
import de.amr.games.pacman.lib.tilemap.WorldMap;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.canvas.GraphicsContext;
import org.tinylog.Logger;

import java.util.function.BiConsumer;

public class ObstacleEditor {

    private final ObjectProperty<WorldMap> worldMapPy = new SimpleObjectProperty<>();

    private BiConsumer<Vector2i, Byte> editCallback;
    private boolean enabled;
    private Vector2i anchor;
    private Vector2i frontier;
    private Vector2i minTile; // top left corner
    private Vector2i maxTile; // bottom right corner
    private boolean join = true;

    public ObjectProperty<WorldMap> worldMapProperty() { return worldMapPy; }

    public void setEditCallback(BiConsumer<Vector2i, Byte> callback) {
        editCallback = callback;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public Vector2i minTile() {
        return minTile;
    }

    public Vector2i maxTile() {
        return maxTile;
    }

    public void startEditing(Vector2i tile) {
        if (!enabled) {
            return;
        }
        Logger.debug("Start inserting obstacle at tile {}", tile);
        minTile = maxTile = anchor = frontier = tile;
    }

    public void continueEditing(Vector2i tile) {
        if (!enabled) {
            return;
        }
        if (tile.equals(frontier)) {
            return;
        }
        Logger.debug("Continue inserting obstacle at tile {}", tile);
        frontier = tile;
        int dx = frontier.x() - anchor.x(), dy = frontier.y() - anchor.y();
        if (dx >= 0) {
            if (dy >= 0) {
                // frontier right-down
                minTile = anchor;
                maxTile = frontier;
            } else {
                // frontier right-up
                minTile = new Vector2i(anchor.x(), frontier.y());
                maxTile = new Vector2i(frontier.x(), anchor.y());
            }
        } else {
            if (dy >= 0) {
                // frontier left-down
                minTile = new Vector2i(frontier.x(), anchor.y());
                maxTile = new Vector2i(anchor.x(), frontier.y());
            } else {
                // frontier left-up
                minTile = frontier;
                maxTile = anchor;
            }
        }
        Logger.debug("Min tile {} max tile {}", minTile, maxTile);
    }

    public void endEditing() {
        if (enabled && editCallback != null) {
            commit();
        }
    }

    public void draw(GraphicsContext g, TerrainRendererInEditor renderer) {
        byte[][] content = editedContent();
        if (content != null) {
            for (int row = 0; row < content.length; ++row) {
                for (int col = 0; col < content[0].length; ++col) {
                    Vector2i tile = minTile.plus(col, row);
                    renderer.drawTile(g, tile, content[row][col]);
                }
            }
        }
    }

    private void commit() {
        byte[][] content = editedContent();
        if (content != null) {
            if (join) {
                content = joinedContent(content, content.length, content[0].length);
            }
            int numRows = content.length, numCols = content[0].length;
            for (int row = 0; row < numRows; ++row) {
                for (int col = 0; col < numCols; ++col) {
                    var tile = new Vector2i(minTile.x() + col, minTile.y() + row);
                    editCallback.accept(tile, content[row][col]);
                }
            }
        }
        anchor = frontier = minTile = maxTile = null;
    }

    private byte[][] joinedContent(byte[][] editedContent, int numRows, int numCols) {
        byte[][] joinedContent = new byte[numRows][numCols];
        for (int row = 0; row < numRows; ++row) {
            System.arraycopy(editedContent[row], 0, joinedContent[row], 0, numCols);
        }
        int crossings;

        TileMap original = worldMapPy.get().terrain();

        joinedContent[0][0] = switch (original.get(minTile)) {
            case TerrainTiles.CORNER_NE -> TerrainTiles.WALL_H;
            case TerrainTiles.CORNER_SW -> TerrainTiles.WALL_V;
            case TerrainTiles.WALL_V    -> TerrainTiles.CORNER_SW;
            case TerrainTiles.WALL_H    -> TerrainTiles.CORNER_NE;
            default -> joinedContent[0][0];
        };

        Vector2i lowerLeftCorner = new Vector2i(minTile.x(), maxTile.y());
        joinedContent[numRows-1][0] = switch (original.get(lowerLeftCorner)) {
            case TerrainTiles.WALL_H    -> TerrainTiles.CORNER_SE;
            case TerrainTiles.WALL_V    -> TerrainTiles.CORNER_NW;
            case TerrainTiles.CORNER_SE -> TerrainTiles.WALL_H;
            case TerrainTiles.CORNER_NW -> TerrainTiles.WALL_V;
            default -> joinedContent[numRows-1][0];
        };

        Vector2i upperRightCorner = new Vector2i(maxTile.x(), minTile.y());
        joinedContent[0][numCols-1] = switch (original.get(upperRightCorner)) {
            case TerrainTiles.WALL_V    -> TerrainTiles.CORNER_SE;
            case TerrainTiles.WALL_H    -> TerrainTiles.CORNER_NW;
            case TerrainTiles.CORNER_SE -> TerrainTiles.WALL_V;
            case TerrainTiles.CORNER_NW -> TerrainTiles.WALL_H;
            default -> joinedContent[0][numCols-1];
        };

        joinedContent[numRows-1][numCols-1] = switch (original.get(maxTile)) {
            case TerrainTiles.WALL_V    -> TerrainTiles.CORNER_NE;
            case TerrainTiles.WALL_H    -> TerrainTiles.CORNER_SW;
            case TerrainTiles.CORNER_SW -> TerrainTiles.WALL_H;
            case TerrainTiles.CORNER_NE -> TerrainTiles.WALL_V;
            default -> joinedContent[numRows-1][numCols-1];
        };

        crossings = 0;
        int leftBorder = minTile.x();
        for (int row = minTile.y(); row < maxTile.y(); ++row) {
            int x = 0, y = row - minTile.y();
            if (editedContent[y][x] == TerrainTiles.WALL_V && original.get(row, leftBorder) == TerrainTiles.WALL_H) {
                joinedContent[y][x] = Globals.isEven(crossings) ? TerrainTiles.CORNER_SE : TerrainTiles.CORNER_NE;
            }
            ++crossings;
        }

        crossings = 0;
        int rightBorder = maxTile.x();
        for (int row = minTile.y(); row < maxTile.y(); ++row) {
            int x = rightBorder - minTile.x(), y = row - minTile.y();
            if (editedContent[y][x] == TerrainTiles.WALL_V && original.get(row, leftBorder) == TerrainTiles.WALL_H) {
                joinedContent[y][x] = Globals.isEven(crossings) ? TerrainTiles.CORNER_SW : TerrainTiles.CORNER_NW;
            }
            ++crossings;
        }

        crossings = 0;
        int upperBorder = minTile.y(); // upper border
        for (int col = minTile.x(); col < maxTile.x(); ++col) {
            int x = col - minTile.x(), y = upperBorder - minTile.y();
            if (editedContent[y][x] == TerrainTiles.WALL_H && original.get(upperBorder, col) == TerrainTiles.WALL_V) {
                joinedContent[y][x] = Globals.isEven(crossings) ? TerrainTiles.CORNER_SE : TerrainTiles.CORNER_SW;
            }
            ++crossings;
        }

        crossings = 0;
        int lowerBorder = maxTile.y(); // lower border
        for (int col = minTile.x(); col < maxTile.x(); ++col) {
            int x = col - minTile.x(), y = lowerBorder - minTile.y();
            if (editedContent[y][x] == TerrainTiles.WALL_H && original.get(lowerBorder, col) == TerrainTiles.WALL_V) {
                joinedContent[y][x] = Globals.isEven(crossings) ? TerrainTiles.CORNER_NE : TerrainTiles.CORNER_NW;
            }
            ++crossings;
        }

        return joinedContent;
    }

    private byte[][] editedContent() {
        if (minTile == null || maxTile == null) {
            return null;
        }
        int numRows = maxTile.y() - minTile.y() + 1;
        int numCols = maxTile.x() - minTile.x() + 1;
        if (numRows <= 1 || numCols <= 1) {
            return null;
        }
        byte[][] content = new byte[numRows][numCols];
        for (int row = minTile.y(); row <= maxTile.y(); ++row) {
            for (int col = minTile.x(); col <= maxTile.x(); ++col) {
                byte value = TerrainTiles.EMPTY;
                if (row == minTile.y() && col == minTile.x()) {
                    value = TerrainTiles.CORNER_NW;
                } else if (row == minTile.y() && col == maxTile.x()) {
                    value = TerrainTiles.CORNER_NE;
                } else if (row == maxTile.y() && col == minTile.x()) {
                    value = TerrainTiles.CORNER_SW;
                } else if (row == maxTile.y() && col == maxTile.x()) {
                    value = TerrainTiles.CORNER_SE;
                } else if (row == minTile.y() || row == maxTile.y()) {
                    value = TerrainTiles.WALL_H;
                } else if (col == minTile.x() || col == maxTile.x()) {
                    value = TerrainTiles.WALL_V;
                }
                content[row - minTile.y()][col - minTile.x()] = value;
            }
        }
        return content;
    }
}
