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
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import org.tinylog.Logger;

import java.util.function.BiConsumer;

public class ObstacleEditor {

    private final ObjectProperty<WorldMap> worldMapPy = new SimpleObjectProperty<>();
    private final BooleanProperty doubleStrokePy = new SimpleBooleanProperty(false);

    private BiConsumer<Vector2i, Byte> editCallback;
    private boolean enabled;
    private Vector2i anchor;
    private Vector2i frontier;
    private Vector2i minTile; // top left corner
    private Vector2i maxTile; // bottom right corner
    private boolean join = true;

    public ObjectProperty<WorldMap> worldMapProperty() { return worldMapPy; }

    public BooleanProperty doubleStrokeProperty() { return doubleStrokePy; }

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

    public void endEditing(Vector2i tile) {
        if (!enabled) {
            return;
        }
        Logger.debug("End inserting obstacle at tile {}", tile);
        commit();
    }

    private void commit() {
        byte[][] editedContent = editedContent();
        if (editedContent != null) {
            if (join) {
                editedContent = joinedContent(editedContent, editedContent.length, editedContent[0].length);
            }
            copy(editedContent);
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

            case TerrainTiles.DCORNER_NE -> TerrainTiles.DWALL_H;
            case TerrainTiles.DCORNER_SW -> TerrainTiles.DWALL_V;
            case TerrainTiles.DWALL_V    -> TerrainTiles.DCORNER_SW;
            case TerrainTiles.DWALL_H    -> TerrainTiles.DCORNER_NE;

            default -> joinedContent[0][0];
        };

        Vector2i lowerLeftCorner = new Vector2i(minTile.x(), maxTile.y());
        joinedContent[numRows-1][0] = switch (original.get(lowerLeftCorner)) {
            case TerrainTiles.WALL_H    -> TerrainTiles.CORNER_SE;
            case TerrainTiles.WALL_V    -> TerrainTiles.CORNER_NW;
            case TerrainTiles.CORNER_SE -> TerrainTiles.WALL_H;
            case TerrainTiles.CORNER_NW -> TerrainTiles.WALL_V;

            case TerrainTiles.DWALL_H    -> TerrainTiles.DCORNER_SE;
            case TerrainTiles.DWALL_V    -> TerrainTiles.DCORNER_NW;
            case TerrainTiles.DCORNER_SE -> TerrainTiles.DWALL_H;
            case TerrainTiles.DCORNER_NW -> TerrainTiles.DWALL_V;

            default -> joinedContent[numRows-1][0];
        };

        Vector2i upperRightCorner = new Vector2i(maxTile.x(), minTile.y());
        joinedContent[0][numCols-1] = switch (original.get(upperRightCorner)) {
            case TerrainTiles.WALL_V    -> TerrainTiles.CORNER_SE;
            case TerrainTiles.WALL_H    -> TerrainTiles.CORNER_NW;
            case TerrainTiles.CORNER_SE -> TerrainTiles.WALL_V;
            case TerrainTiles.CORNER_NW -> TerrainTiles.WALL_H;

            case TerrainTiles.DWALL_V    -> TerrainTiles.DCORNER_SE;
            case TerrainTiles.DWALL_H    -> TerrainTiles.DCORNER_NW;
            case TerrainTiles.DCORNER_SE -> TerrainTiles.DWALL_V;
            case TerrainTiles.DCORNER_NW -> TerrainTiles.DWALL_H;

            default -> joinedContent[0][numCols-1];
        };

        joinedContent[numRows-1][numCols-1] = switch (original.get(maxTile)) {
            case TerrainTiles.WALL_V    -> TerrainTiles.CORNER_NE;
            case TerrainTiles.WALL_H    -> TerrainTiles.CORNER_SW;
            case TerrainTiles.CORNER_SW -> TerrainTiles.WALL_H;
            case TerrainTiles.CORNER_NE -> TerrainTiles.WALL_V;

            case TerrainTiles.DWALL_V    -> TerrainTiles.DCORNER_NE;
            case TerrainTiles.DWALL_H    -> TerrainTiles.DCORNER_SW;
            case TerrainTiles.DCORNER_SW -> TerrainTiles.DWALL_H;
            case TerrainTiles.DCORNER_NE -> TerrainTiles.DWALL_V;

            default -> joinedContent[numRows-1][numCols-1];
        };

        crossings = 0;
        int leftBorder = minTile.x();
        for (int row = minTile.y(); row < maxTile.y(); ++row) {
            int x = 0, y = row - minTile.y();
            if (doubleStrokePy.get()) {
                if (editedContent[y][x] == TerrainTiles.DWALL_V && original.get(row, leftBorder) == TerrainTiles.DWALL_H) {
                    joinedContent[y][x] = Globals.isEven(crossings) ? TerrainTiles.DCORNER_SE : TerrainTiles.DCORNER_NE;
                }
            }
            else {
                if (editedContent[y][x] == TerrainTiles.WALL_V && original.get(row, leftBorder) == TerrainTiles.WALL_H) {
                    joinedContent[y][x] = Globals.isEven(crossings) ? TerrainTiles.CORNER_SE : TerrainTiles.CORNER_NE;
                }
            }
            ++crossings;
        }

        crossings = 0;
        int rightBorder = maxTile.x();
        for (int row = minTile.y(); row < maxTile.y(); ++row) {
            int x = rightBorder - minTile.x(), y = row - minTile.y();
            if (doubleStrokePy.get()) {
                if (editedContent[y][x] == TerrainTiles.DWALL_V && original.get(row, leftBorder) == TerrainTiles.DWALL_H) {
                    joinedContent[y][x] = Globals.isEven(crossings) ? TerrainTiles.DCORNER_SW : TerrainTiles.DCORNER_NW;
                }
            }
            else {
                if (editedContent[y][x] == TerrainTiles.WALL_V && original.get(row, leftBorder) == TerrainTiles.WALL_H) {
                    joinedContent[y][x] = Globals.isEven(crossings) ? TerrainTiles.CORNER_SW : TerrainTiles.CORNER_NW;
                }
            }
            ++crossings;
        }

        crossings = 0;
        int upperBorder = minTile.y(); // upper border
        for (int col = minTile.x(); col < maxTile.x(); ++col) {
            int x = col - minTile.x(), y = upperBorder - minTile.y();
            if (doubleStrokePy.get()) {
                if (editedContent[y][x] == TerrainTiles.DWALL_H && original.get(upperBorder, col) == TerrainTiles.DWALL_V) {
                    joinedContent[y][x] = Globals.isEven(crossings) ? TerrainTiles.DCORNER_SE : TerrainTiles.DCORNER_SW;
                }
            }
            else {
                if (editedContent[y][x] == TerrainTiles.WALL_H && original.get(upperBorder, col) == TerrainTiles.WALL_V) {
                    joinedContent[y][x] = Globals.isEven(crossings) ? TerrainTiles.CORNER_SE : TerrainTiles.CORNER_SW;
                }
            }
            ++crossings;
        }

        crossings = 0;
        int lowerBorder = maxTile.y(); // lower border
        for (int col = minTile.x(); col < maxTile.x(); ++col) {
            int x = col - minTile.x(), y = lowerBorder - minTile.y();
            if (doubleStrokePy.get()) {
                if (editedContent[y][x] == TerrainTiles.DWALL_H && original.get(lowerBorder, col) == TerrainTiles.DWALL_V) {
                    joinedContent[y][x] = Globals.isEven(crossings) ? TerrainTiles.DCORNER_NE : TerrainTiles.DCORNER_NW;
                }
            }
            else {
                if (editedContent[y][x] == TerrainTiles.WALL_H && original.get(lowerBorder, col) == TerrainTiles.WALL_V) {
                    joinedContent[y][x] = Globals.isEven(crossings) ? TerrainTiles.CORNER_NE : TerrainTiles.CORNER_NW;
                }
            }
            ++crossings;
        }

        return joinedContent;
    }

    public byte[][] editedContent() {
        if (minTile == null || maxTile == null) {
            return null;
        }
        int numRows = maxTile.y() - minTile.y() + 1;
        int numCols = maxTile.x() - minTile.x() + 1;
        if (numRows <= 1 || numCols <= 1) {
            return null;
        }
        byte[][] area = new byte[numRows][numCols];
        boolean doubleStroke = doubleStrokePy.get();
        for (int row = minTile.y(); row <= maxTile.y(); ++row) {
            for (int col = minTile.x(); col <= maxTile.x(); ++col) {
                byte value = TerrainTiles.EMPTY;
                if (row == minTile.y() && col == minTile.x()) {
                    value = doubleStroke ? TerrainTiles.DCORNER_NW : TerrainTiles.CORNER_NW;
                } else if (row == minTile.y() && col == maxTile.x()) {
                    value = doubleStroke ? TerrainTiles.DCORNER_NE : TerrainTiles.CORNER_NE;
                } else if (row == maxTile.y() && col == minTile.x()) {
                    value = doubleStroke ? TerrainTiles.DCORNER_SW : TerrainTiles.CORNER_SW;
                } else if (row == maxTile.y() && col == maxTile.x()) {
                    value = doubleStroke ? TerrainTiles.DCORNER_SE : TerrainTiles.CORNER_SE;
                } else if (row == minTile.y() || row == maxTile.y()) {
                    value = doubleStroke ?  TerrainTiles.DWALL_H : TerrainTiles.WALL_H;
                } else if (col == minTile.x() || col == maxTile.x()) {
                    value = doubleStroke ? TerrainTiles.DWALL_V : TerrainTiles.WALL_V;
                }
                area[row - minTile.y()][col - minTile.x()] = value;
            }
        }
        return area;
    }

    private void copy(byte[][] values) {
        int numRows = values.length;
        int numCols = values[0].length;
        for (int row = 0; row < numRows; ++row) {
            for (int col = 0; col < numCols; ++col) {
                Vector2i tile = new Vector2i(minTile.x() + col, minTile.y() + row);
                if (editCallback != null) {
                    editCallback.accept(tile, values[row][col]);
                }
            }
        }
    }
}
