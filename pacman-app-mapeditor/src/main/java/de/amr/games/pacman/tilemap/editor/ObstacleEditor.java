/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.tilemap.editor;

import de.amr.games.pacman.lib.Vector2i;
import de.amr.games.pacman.lib.tilemap.LayerID;
import de.amr.games.pacman.lib.tilemap.TerrainTiles;
import de.amr.games.pacman.lib.tilemap.WorldMap;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.canvas.GraphicsContext;
import org.tinylog.Logger;

import static de.amr.games.pacman.Globals.isEven;
import static de.amr.games.pacman.tilemap.editor.TileMapEditorUtil.mirroredTileValue;
import static java.util.Objects.requireNonNull;

public class ObstacleEditor {

    private final ObjectProperty<WorldMap> worldMapPy = new SimpleObjectProperty<>();
    private final BooleanProperty joiningPy = new SimpleBooleanProperty();
    private final BooleanProperty symmetricEditPy = new SimpleBooleanProperty();

    private boolean enabled;
    private Vector2i anchor;
    private Vector2i frontier;
    private Vector2i minTile; // top left corner
    private Vector2i maxTile; // bottom right corner

    public ObstacleEditor() {}

    public void setValue(Vector2i tile, byte value) {
        Logger.info("Tile {} got value {}", tile, value);
    }

    public BooleanProperty joiningProperty() { return joiningPy; }

    public void setJoining(boolean join) {
        joiningPy.set(join);
    }

    public BooleanProperty symmetricEditProperty() { return symmetricEditPy; }

    public ObjectProperty<WorldMap> worldMapProperty() { return worldMapPy; }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void startEditing(Vector2i tile) {
        requireNonNull(tile);
        if (enabled) {
            minTile = maxTile = anchor = frontier = tile;
        }
    }

    public void continueEditing(Vector2i tile) {
        requireNonNull(tile);
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

    public void draw(GraphicsContext g, TerrainTileMapRenderer renderer) {
        byte[][] content = editedContent();
        if (content != null) {
            for (int row = 0; row < content.length; ++row) {
                for (int col = 0; col < content[0].length; ++col) {
                    Vector2i tile = minTile.plus(col, row);
                    renderer.drawTile(g, tile, content[row][col]);
                    if (symmetricEditProperty().get()) {
                        Vector2i mirroredTile = worldMapPy.get().mirroredTile(tile);
                        renderer.drawTile(g, mirroredTile, mirroredTileValue(content[row][col]));
                    }
                }
            }
        }
    }

    private void commit() {
        byte[][] content = editedContent();
        if (content != null) {
            int numRows = content.length, numCols = content[0].length;
            if (joiningPy.get()) {
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

        joinedContent[0][0] = switch (worldMap.get(LayerID.TERRAIN, minTile)) {
            case TerrainTiles.ARC_NE -> TerrainTiles.WALL_H;
            case TerrainTiles.ARC_SW -> TerrainTiles.WALL_V;
            case TerrainTiles.WALL_V    -> TerrainTiles.ARC_SW;
            case TerrainTiles.WALL_H    -> TerrainTiles.ARC_NE;
            default -> joinedContent[0][0];
        };

        Vector2i lowerLeftCorner = new Vector2i(minTile.x(), maxTile.y());
        joinedContent[numRows-1][0] = switch (worldMap.get(LayerID.TERRAIN, lowerLeftCorner)) {
            case TerrainTiles.WALL_H    -> TerrainTiles.ARC_SE;
            case TerrainTiles.WALL_V    -> TerrainTiles.ARC_NW;
            case TerrainTiles.ARC_SE -> TerrainTiles.WALL_H;
            case TerrainTiles.ARC_NW -> TerrainTiles.WALL_V;
            default -> joinedContent[numRows-1][0];
        };

        Vector2i upperRightCorner = new Vector2i(maxTile.x(), minTile.y());
        joinedContent[0][numCols-1] = switch (worldMap.get(LayerID.TERRAIN, upperRightCorner)) {
            case TerrainTiles.WALL_V    -> TerrainTiles.ARC_SE;
            case TerrainTiles.WALL_H    -> TerrainTiles.ARC_NW;
            case TerrainTiles.ARC_SE -> TerrainTiles.WALL_V;
            case TerrainTiles.ARC_NW -> TerrainTiles.WALL_H;
            default -> joinedContent[0][numCols-1];
        };

        joinedContent[numRows-1][numCols-1] = switch (worldMap.get(LayerID.TERRAIN, maxTile)) {
            case TerrainTiles.WALL_V    -> TerrainTiles.ARC_NE;
            case TerrainTiles.WALL_H    -> TerrainTiles.ARC_SW;
            case TerrainTiles.ARC_SW -> TerrainTiles.WALL_H;
            case TerrainTiles.ARC_NE -> TerrainTiles.WALL_V;
            default -> joinedContent[numRows-1][numCols-1];
        };

        crossings = 0;
        int leftBorder = minTile.x();
        for (int row = minTile.y(); row < maxTile.y(); ++row) {
            int x = 0, y = row - minTile.y();
            if (editedContent[y][x] == TerrainTiles.WALL_V
                    && worldMap.get(LayerID.TERRAIN, row, leftBorder) == TerrainTiles.WALL_H) {
                joinedContent[y][x] = isEven(crossings) ? TerrainTiles.ARC_SE : TerrainTiles.ARC_NE;
            }
            ++crossings;
        }

        crossings = 0;
        int rightBorder = maxTile.x();
        for (int row = minTile.y(); row < maxTile.y(); ++row) {
            int x = rightBorder - minTile.x(), y = row - minTile.y();
            if (editedContent[y][x] == TerrainTiles.WALL_V
                    && worldMap.get(LayerID.TERRAIN, row, leftBorder) == TerrainTiles.WALL_H) {
                joinedContent[y][x] = isEven(crossings) ? TerrainTiles.ARC_SW : TerrainTiles.ARC_NW;
            }
            ++crossings;
        }

        crossings = 0;
        int upperBorder = minTile.y(); // upper border
        for (int col = minTile.x(); col < maxTile.x(); ++col) {
            int x = col - minTile.x(), y = upperBorder - minTile.y();
            if (editedContent[y][x] == TerrainTiles.WALL_H
                    && worldMap.get(LayerID.TERRAIN, upperBorder, col) == TerrainTiles.WALL_V) {
                joinedContent[y][x] = isEven(crossings) ? TerrainTiles.ARC_SE : TerrainTiles.ARC_SW;
            }
            ++crossings;
        }

        crossings = 0;
        int lowerBorder = maxTile.y(); // lower border
        for (int col = minTile.x(); col < maxTile.x(); ++col) {
            int x = col - minTile.x(), y = lowerBorder - minTile.y();
            if (editedContent[y][x] == TerrainTiles.WALL_H
                    && worldMap.get(LayerID.TERRAIN, lowerBorder, col) == TerrainTiles.WALL_V) {
                joinedContent[y][x] = isEven(crossings) ? TerrainTiles.ARC_NE : TerrainTiles.ARC_NW;
            }
            ++crossings;
        }

        return joinedContent;
    }

    private byte[][] editedContent() {
        if (minTile == null || maxTile == null) {
            return null;
        }
        int sizeY = maxTile.y() - minTile.y() + 1;
        int sizeX = maxTile.x() - minTile.x() + 1;
        if (sizeY <= 1 || sizeX <= 1) {
            return null;
        }
        byte[][] content = new byte[sizeY][sizeX];
        for (int y = minTile.y(); y <= maxTile.y(); ++y) {
            for (int x = minTile.x(); x <= maxTile.x(); ++x) {
                content[y - minTile.y()][x - minTile.x()] = computeTileValue(y, x);
            }
        }
        return content;
    }

    private byte computeTileValue(int y, int x) {
        if (y == minTile.y() && x == minTile.x()) return TerrainTiles.ARC_NW;
        if (y == minTile.y() && x == maxTile.x()) return TerrainTiles.ARC_NE;
        if (y == maxTile.y() && x == minTile.x()) return TerrainTiles.ARC_SW;
        if (y == maxTile.y() && x == maxTile.x()) return TerrainTiles.ARC_SE;
        if (y == minTile.y() || y == maxTile.y()) return TerrainTiles.WALL_H;
        if (x == minTile.x() || x == maxTile.x()) return TerrainTiles.WALL_V;
        return TerrainTiles.EMPTY;
    }
}
