/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.tilemap.editor;

import de.amr.games.pacman.lib.Globals;
import de.amr.games.pacman.lib.Vector2i;
import de.amr.games.pacman.lib.tilemap.TerrainTiles;
import de.amr.games.pacman.lib.tilemap.WorldMap;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.canvas.GraphicsContext;
import org.tinylog.Logger;

import static de.amr.games.pacman.lib.Globals.assertNotNull;

public class ObstacleEditor {

    private final ObjectProperty<WorldMap> worldMapPy = new SimpleObjectProperty<>();

    private boolean enabled;
    private Vector2i anchor;
    private Vector2i frontier;
    private Vector2i minTile; // top left corner
    private Vector2i maxTile; // bottom right corner
    private boolean join;

    public ObstacleEditor() {}

    public void setValue(Vector2i tile, byte value) {
        Logger.info("Tile {} git value {}", tile, value);
    }

    public void setJoin(boolean join) {
        this.join = join;
    }

    public ObjectProperty<WorldMap> worldMapProperty() { return worldMapPy; }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void startEditing(Vector2i tile) {
        assertNotNull(tile);
        if (enabled) {
            minTile = maxTile = anchor = frontier = tile;
        }
    }

    public void continueEditing(Vector2i tile) {
        assertNotNull(tile);
        if (!enabled || tile.equals(frontier)) {
            return;
        }
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
    }

    public void endEditing() {
        if (enabled) {
            commit();
        }
        anchor = frontier = minTile = maxTile = null;
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
            int numRows = content.length, numCols = content[0].length;
            if (join) {
                content = joinedContent(content, numRows, numCols);
            }
            for (int row = 0; row < numRows; ++row) {
                for (int col = 0; col < numCols; ++col) {
                    var tile = new Vector2i(minTile.x() + col, minTile.y() + row);
                    setValue(tile, content[row][col]);
                }
            }
        }
    }

    private byte[][] joinedContent(byte[][] editedContent, int numRows, int numCols) {
        byte[][] joinedContent = new byte[numRows][numCols];
        for (int row = 0; row < numRows; ++row) {
            System.arraycopy(editedContent[row], 0, joinedContent[row], 0, numCols);
        }
        int crossings;

        WorldMap worldMap = worldMapPy.get();

        joinedContent[0][0] = switch (worldMap.get(WorldMap.LayerID.TERRAIN, minTile)) {
            case TerrainTiles.CORNER_NE -> TerrainTiles.WALL_H;
            case TerrainTiles.CORNER_SW -> TerrainTiles.WALL_V;
            case TerrainTiles.WALL_V    -> TerrainTiles.CORNER_SW;
            case TerrainTiles.WALL_H    -> TerrainTiles.CORNER_NE;
            default -> joinedContent[0][0];
        };

        Vector2i lowerLeftCorner = new Vector2i(minTile.x(), maxTile.y());
        joinedContent[numRows-1][0] = switch (worldMap.get(WorldMap.LayerID.TERRAIN, lowerLeftCorner)) {
            case TerrainTiles.WALL_H    -> TerrainTiles.CORNER_SE;
            case TerrainTiles.WALL_V    -> TerrainTiles.CORNER_NW;
            case TerrainTiles.CORNER_SE -> TerrainTiles.WALL_H;
            case TerrainTiles.CORNER_NW -> TerrainTiles.WALL_V;
            default -> joinedContent[numRows-1][0];
        };

        Vector2i upperRightCorner = new Vector2i(maxTile.x(), minTile.y());
        joinedContent[0][numCols-1] = switch (worldMap.get(WorldMap.LayerID.TERRAIN, upperRightCorner)) {
            case TerrainTiles.WALL_V    -> TerrainTiles.CORNER_SE;
            case TerrainTiles.WALL_H    -> TerrainTiles.CORNER_NW;
            case TerrainTiles.CORNER_SE -> TerrainTiles.WALL_V;
            case TerrainTiles.CORNER_NW -> TerrainTiles.WALL_H;
            default -> joinedContent[0][numCols-1];
        };

        joinedContent[numRows-1][numCols-1] = switch (worldMap.get(WorldMap.LayerID.TERRAIN, maxTile)) {
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
            if (editedContent[y][x] == TerrainTiles.WALL_V
                    && worldMap.get(WorldMap.LayerID.TERRAIN, row, leftBorder) == TerrainTiles.WALL_H) {
                joinedContent[y][x] = Globals.isEven(crossings) ? TerrainTiles.CORNER_SE : TerrainTiles.CORNER_NE;
            }
            ++crossings;
        }

        crossings = 0;
        int rightBorder = maxTile.x();
        for (int row = minTile.y(); row < maxTile.y(); ++row) {
            int x = rightBorder - minTile.x(), y = row - minTile.y();
            if (editedContent[y][x] == TerrainTiles.WALL_V
                    && worldMap.get(WorldMap.LayerID.TERRAIN, row, leftBorder) == TerrainTiles.WALL_H) {
                joinedContent[y][x] = Globals.isEven(crossings) ? TerrainTiles.CORNER_SW : TerrainTiles.CORNER_NW;
            }
            ++crossings;
        }

        crossings = 0;
        int upperBorder = minTile.y(); // upper border
        for (int col = minTile.x(); col < maxTile.x(); ++col) {
            int x = col - minTile.x(), y = upperBorder - minTile.y();
            if (editedContent[y][x] == TerrainTiles.WALL_H
                    && worldMap.get(WorldMap.LayerID.TERRAIN, upperBorder, col) == TerrainTiles.WALL_V) {
                joinedContent[y][x] = Globals.isEven(crossings) ? TerrainTiles.CORNER_SE : TerrainTiles.CORNER_SW;
            }
            ++crossings;
        }

        crossings = 0;
        int lowerBorder = maxTile.y(); // lower border
        for (int col = minTile.x(); col < maxTile.x(); ++col) {
            int x = col - minTile.x(), y = lowerBorder - minTile.y();
            if (editedContent[y][x] == TerrainTiles.WALL_H
                    && worldMap.get(WorldMap.LayerID.TERRAIN, lowerBorder, col) == TerrainTiles.WALL_V) {
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
                content[row - minTile.y()][col - minTile.x()] = computeTileValue(row, col);
            }
        }
        return content;
    }

    private byte computeTileValue(int row, int col) {
        if (row == minTile.y() && col == minTile.x()) {
            return TerrainTiles.CORNER_NW;
        }
        if (row == minTile.y() && col == maxTile.x()) {
            return TerrainTiles.CORNER_NE;
        }
        if (row == maxTile.y() && col == minTile.x()) {
            return TerrainTiles.CORNER_SW;
        }
        if (row == maxTile.y() && col == maxTile.x()) {
            return TerrainTiles.CORNER_SE;
        }
        if (row == minTile.y() || row == maxTile.y()) {
            return TerrainTiles.WALL_H;
        }
        if (col == minTile.x() || col == maxTile.x()) {
            return TerrainTiles.WALL_V;
        }
        return TerrainTiles.EMPTY;
    }
}
