/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.tilemap.editor;

import de.amr.pacmanfx.lib.Vector2i;
import de.amr.pacmanfx.lib.tilemap.LayerID;
import de.amr.pacmanfx.lib.tilemap.TerrainTile;
import de.amr.pacmanfx.lib.tilemap.WorldMap;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.canvas.GraphicsContext;
import org.tinylog.Logger;

import static de.amr.pacmanfx.lib.UsefulFunctions.isEven;
import static de.amr.pacmanfx.lib.tilemap.TerrainTile.*;
import static de.amr.pacmanfx.tilemap.editor.TileMapEditorUtil.mirroredTileValue;
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

        byte minTileValue = worldMap.content(LayerID.TERRAIN, minTile);
        if       (minTileValue == ARC_NE.byteValue()) joinedContent[0][0] = WALL_H.byteValue();
        else if (minTileValue == ARC_SW.byteValue())  joinedContent[0][0] = WALL_V.byteValue();
        else if (minTileValue == WALL_V.byteValue())  joinedContent[0][0] = ARC_SW.byteValue();
        else if (minTileValue == WALL_H.byteValue())  joinedContent[0][0] = ARC_NE.byteValue();

        Vector2i lowerLeftCorner = new Vector2i(minTile.x(), maxTile.y());
        byte lowerLeftValue = worldMap.content(LayerID.TERRAIN, lowerLeftCorner);
        if      (lowerLeftValue == WALL_H.byteValue()) joinedContent[numRows-1][0] = ARC_SE.byteValue();
        else if (lowerLeftValue == WALL_V.byteValue()) joinedContent[numRows-1][0] = ARC_NW.byteValue();
        else if (lowerLeftValue == ARC_SE.byteValue()) joinedContent[numRows-1][0] = WALL_H.byteValue();
        else if (lowerLeftValue == ARC_NW.byteValue()) joinedContent[numRows-1][0] = WALL_V.byteValue();

        Vector2i upperRightCorner = new Vector2i(maxTile.x(), minTile.y());
        byte upperRightValue = worldMap.content(LayerID.TERRAIN, upperRightCorner);
        if      (upperRightValue == WALL_V.byteValue()) joinedContent[0][numCols-1] = ARC_SE.byteValue();
        else if (upperRightValue == WALL_H.byteValue()) joinedContent[0][numCols-1] = ARC_NW.byteValue();
        else if (upperRightValue == ARC_SE.byteValue()) joinedContent[0][numCols-1] = WALL_V.byteValue();
        else if (upperRightValue == ARC_NW.byteValue()) joinedContent[0][numCols-1] = WALL_H.byteValue();

        byte maxTileValue = worldMap.content(LayerID.TERRAIN, maxTile);
        if (maxTileValue == WALL_V.byteValue()) joinedContent[numRows-1][numCols-1] = ARC_NE.byteValue();
        if (maxTileValue == WALL_H.byteValue()) joinedContent[numRows-1][numCols-1] = ARC_SW.byteValue();
        if (maxTileValue == ARC_SW.byteValue()) joinedContent[numRows-1][numCols-1] = WALL_H.byteValue();
        if (maxTileValue == ARC_NE.byteValue()) joinedContent[numRows-1][numCols-1] = WALL_V.byteValue();

        crossings = 0;
        int leftBorder = minTile.x();
        for (int row = minTile.y(); row < maxTile.y(); ++row) {
            int x = 0, y = row - minTile.y();
            if (editedContent[y][x] == WALL_V.byteValue() && worldMap.content(LayerID.TERRAIN, row, leftBorder) == WALL_H.byteValue()) {
                joinedContent[y][x] = isEven(crossings) ? ARC_SE.byteValue() : ARC_NE.byteValue();
            }
            ++crossings;
        }

        crossings = 0;
        int rightBorder = maxTile.x();
        for (int row = minTile.y(); row < maxTile.y(); ++row) {
            int x = rightBorder - minTile.x(), y = row - minTile.y();
            if (editedContent[y][x] == WALL_V.byteValue() && worldMap.content(LayerID.TERRAIN, row, leftBorder) == WALL_H.byteValue()) {
                joinedContent[y][x] = isEven(crossings) ? ARC_SW.byteValue() : ARC_NW.byteValue();
            }
            ++crossings;
        }

        crossings = 0;
        int upperBorder = minTile.y(); // upper border
        for (int col = minTile.x(); col < maxTile.x(); ++col) {
            int x = col - minTile.x(), y = upperBorder - minTile.y();
            if (editedContent[y][x] == WALL_H.byteValue() && worldMap.content(LayerID.TERRAIN, upperBorder, col) == WALL_V.byteValue()) {
                joinedContent[y][x] = isEven(crossings) ? ARC_SE.byteValue() : ARC_SW.byteValue();
            }
            ++crossings;
        }

        crossings = 0;
        int lowerBorder = maxTile.y(); // lower border
        for (int col = minTile.x(); col < maxTile.x(); ++col) {
            int x = col - minTile.x(), y = lowerBorder - minTile.y();
            if (editedContent[y][x] == WALL_H.byteValue() && worldMap.content(LayerID.TERRAIN, lowerBorder, col) == WALL_V.byteValue()) {
                joinedContent[y][x] = isEven(crossings) ? ARC_NE.byteValue() : ARC_NW.byteValue();
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
        if (y == minTile.y() && x == minTile.x()) return ARC_NW.byteValue();
        if (y == minTile.y() && x == maxTile.x()) return ARC_NE.byteValue();
        if (y == maxTile.y() && x == minTile.x()) return ARC_SW.byteValue();
        if (y == maxTile.y() && x == maxTile.x()) return ARC_SE.byteValue();
        if (y == minTile.y() || y == maxTile.y()) return WALL_H.byteValue();
        if (x == minTile.x() || x == maxTile.x()) return WALL_V.byteValue();
        return TerrainTile.emptyTileValue();
    }
}
