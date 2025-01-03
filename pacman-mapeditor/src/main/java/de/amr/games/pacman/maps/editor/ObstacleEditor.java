/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.maps.editor;

import de.amr.games.pacman.lib.Globals;
import de.amr.games.pacman.lib.Vector2i;
import de.amr.games.pacman.lib.tilemap.TileMap;
import de.amr.games.pacman.lib.tilemap.TileEncoding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import org.tinylog.Logger;

import static de.amr.games.pacman.maps.editor.TileMapEditorViewModel.PALETTE_ID_TERRAIN;

public class ObstacleEditor {

    final BooleanProperty enabledPy = new SimpleBooleanProperty();

    private final EditController editController;
    private final TileMapEditorViewModel viewModel;

    private Vector2i anchor;
    private Vector2i frontier;
    private Vector2i minTile; // top left corner
    private Vector2i maxTile; // bottom right corner
    private boolean join = true;

    public ObstacleEditor(EditController editController, TileMapEditorViewModel viewModel) {
        this.editController = editController;
        this.viewModel = viewModel;
    }

    public Vector2i minTile() {
        return minTile;
    }

    public Vector2i maxTile() {
        return maxTile;
    }

    public boolean isDisabled() {
        Palette selectedPalette = viewModel.selectedPalette();
        boolean emptyTileEntrySelected =
            selectedPalette.id() == PALETTE_ID_TERRAIN &&
            selectedPalette.getSelectedEntryRow() == 0 && selectedPalette.getSelectedEntryCol() == 0;
        return !(enabledPy.get() && emptyTileEntrySelected);
    }

    public void startEditing(Vector2i tile) {
        if (isDisabled()) {
            return;
        }
        Logger.debug("Start inserting obstacle at tile {}", tile);
        minTile = maxTile = anchor = frontier = tile;
    }

    public void continueEditing(Vector2i tile) {
        if (isDisabled()) {
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
        if (isDisabled()) {
            return;
        }
        Logger.debug("End inserting obstacle at tile {}", tile);
        commit();
    }

    private void commit() {
        byte[][] editedContent = editedContent();
        if (editedContent != null) {
            if (join) {
                TileMap originalTerrain = viewModel.worldMapProperty().get().terrain();
                editedContent = joinedContent(originalTerrain, editedContent, editedContent.length, editedContent[0].length);
            }
            copy(editedContent, viewModel.worldMapProperty().get().terrain());
        }
        anchor = frontier = minTile = maxTile = null;
    }

    private byte[][] joinedContent(TileMap original, byte[][] editedContent, int numRows, int numCols) {
        byte[][] joinedContent = new byte[numRows][numCols];
        for (int row = 0; row < numRows; ++row) {
            System.arraycopy(editedContent[row], 0, joinedContent[row], 0, numCols);
        }
        int crossings;

        joinedContent[0][0] = switch (original.get(minTile)) {
            case TileEncoding.CORNER_NE -> TileEncoding.WALL_H;
            case TileEncoding.CORNER_SW -> TileEncoding.WALL_V;
            case TileEncoding.WALL_V    -> TileEncoding.CORNER_SW;
            case TileEncoding.WALL_H    -> TileEncoding.CORNER_NE;
            default -> joinedContent[0][0];
        };

        Vector2i lowerLeftCorner = new Vector2i(minTile.x(), maxTile.y());
        joinedContent[numRows-1][0] = switch (original.get(lowerLeftCorner)) {
            case TileEncoding.WALL_H    -> TileEncoding.CORNER_SE;
            case TileEncoding.WALL_V    -> TileEncoding.CORNER_NW;
            case TileEncoding.CORNER_SE -> TileEncoding.WALL_H;
            case TileEncoding.CORNER_NW -> TileEncoding.WALL_V;
            default -> joinedContent[numRows-1][0];
        };

        Vector2i upperRightCorner = new Vector2i(maxTile.x(), minTile.y());
        joinedContent[0][numCols-1] = switch (original.get(upperRightCorner)) {
            case TileEncoding.WALL_V    -> TileEncoding.CORNER_SE;
            case TileEncoding.WALL_H    -> TileEncoding.CORNER_NW;
            case TileEncoding.CORNER_SE -> TileEncoding.WALL_V;
            case TileEncoding.CORNER_NW -> TileEncoding.WALL_H;
            default -> joinedContent[0][numCols-1];
        };

        joinedContent[numRows-1][numCols-1] = switch (original.get(maxTile)) {
            case TileEncoding.WALL_V    -> TileEncoding.CORNER_NE;
            case TileEncoding.WALL_H    -> TileEncoding.CORNER_SW;
            case TileEncoding.CORNER_SW -> TileEncoding.WALL_H;
            case TileEncoding.CORNER_NE -> TileEncoding.WALL_V;
            default -> joinedContent[numRows-1][numCols-1];
        };

        crossings = 0;
        int leftBorder = minTile.x();
        for (int row = minTile.y(); row < maxTile.y(); ++row) {
            int x = 0, y = row - minTile.y();
            if (editedContent[y][x] == TileEncoding.WALL_V && original.get(row, leftBorder) == TileEncoding.WALL_H) {
                joinedContent[y][x] = Globals.isEven(crossings) ? TileEncoding.CORNER_SE : TileEncoding.CORNER_NE;
                ++crossings;
            }
        }

        crossings = 0;
        int rightBorder = maxTile.x();
        for (int row = minTile.y(); row < maxTile.y(); ++row) {
            int x = rightBorder - minTile.x(), y = row - minTile.y();
            if (editedContent[y][x] == TileEncoding.WALL_V && original.get(row, leftBorder) == TileEncoding.WALL_H) {
                joinedContent[y][x] = Globals.isEven(crossings) ? TileEncoding.CORNER_SW : TileEncoding.CORNER_NW;
                ++crossings;
            }
        }

        crossings = 0;
        int upperBorder = minTile.y(); // upper border
        for (int col = minTile.x(); col < maxTile.x(); ++col) {
            int x = col - minTile.x(), y = upperBorder - minTile.y();
            if (editedContent[y][x] == TileEncoding.WALL_H && original.get(upperBorder, col) == TileEncoding.WALL_V) {
                joinedContent[y][x] = Globals.isEven(crossings) ? TileEncoding.CORNER_SE : TileEncoding.CORNER_SW;
                ++crossings;
            }
        }
        crossings = 0;
        int lowerBorder = maxTile.y(); // lower border
        for (int col = minTile.x(); col < maxTile.x(); ++col) {
            int x = col - minTile.x(), y = lowerBorder - minTile.y();
            if (editedContent[y][x] == TileEncoding.WALL_H && original.get(lowerBorder, col) == TileEncoding.WALL_V) {
                joinedContent[y][x] = Globals.isEven(crossings) ? TileEncoding.CORNER_NE : TileEncoding.CORNER_NW;
                ++crossings;
            }
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
        for (int row = minTile.y(); row <= maxTile.y(); ++row) {
            for (int col = minTile.x(); col <= maxTile.x(); ++col) {
                byte value = TileEncoding.EMPTY;
                if (row == minTile.y() && col == minTile.x()) {
                    value = TileEncoding.CORNER_NW;
                } else if (row == minTile.y() && col == maxTile.x()) {
                    value = TileEncoding.CORNER_NE;
                } else if (row == maxTile.y() && col == minTile.x()) {
                    value = TileEncoding.CORNER_SW;
                } else if (row == maxTile.y() && col == maxTile.x()) {
                    value = TileEncoding.CORNER_SE;
                } else if (row == minTile.y() || row == maxTile.y()) {
                    value = TileEncoding.WALL_H;
                } else if (col == minTile.x() || col == maxTile.x()) {
                    value = TileEncoding.WALL_V;
                }
                area[row - minTile.y()][col - minTile.x()] = value;
            }
        }
        return area;
    }

    private void copy(byte[][] values, TileMap tileMap) {
        int numRows = values.length;
        int numCols = values[0].length;
        for (int row = 0; row < numRows; ++row) {
            for (int col = 0; col < numCols; ++col) {
                Vector2i tile = new Vector2i(minTile.x() + col, minTile.y() + row);
                editController.setTileValue(tileMap, tile, values[row][col]);
            }
        }
    }
}
