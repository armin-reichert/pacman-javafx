/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.tilemap.editor;

import de.amr.pacmanfx.lib.Vector2i;
import de.amr.pacmanfx.lib.tilemap.LayerID;
import de.amr.pacmanfx.lib.tilemap.WorldMap;
import de.amr.pacmanfx.tilemap.editor.rendering.TerrainTileMapRenderer;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;

import java.util.function.BiConsumer;

import static de.amr.pacmanfx.lib.UsefulFunctions.isEven;
import static de.amr.pacmanfx.lib.tilemap.TerrainTile.*;
import static de.amr.pacmanfx.tilemap.editor.TileMapEditorUtil.mirroredTileCode;
import static java.util.Objects.requireNonNull;

public class ObstacleEditor {

    private BiConsumer<Vector2i, Byte> onEditTile;

    private final BooleanProperty joining = new SimpleBooleanProperty();
    private final BooleanProperty symmetricEdit = new SimpleBooleanProperty();
    private final ObjectProperty<WorldMap> worldMap = new SimpleObjectProperty<>();

    private boolean enabled;
    private Vector2i anchor;
    private Vector2i frontier;
    private Vector2i minTile; // top left corner
    private Vector2i maxTile; // bottom right corner

    private final TileMapEditor.ChangeManager changeManager;

    public ObstacleEditor(TileMapEditor.ChangeManager changeManager) {
        this.changeManager = changeManager;
    }

    public void setOnEditTile(BiConsumer<Vector2i, Byte> onEditTile) {
        this.onEditTile = onEditTile;
    }

    public void editTile(Vector2i tile, byte value) {
        if (onEditTile != null) {
            onEditTile.accept(tile, value);
        }
    }

    public BooleanProperty joiningProperty() { return joining; }

    public BooleanProperty symmetricEditModeProperty() { return symmetricEdit; }

    public ObjectProperty<WorldMap> worldMapProperty() { return worldMap; }

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
            editTouchedRegion();
        }
        anchor = frontier = minTile = maxTile = null;
    }

    private void editTouchedRegion() {
        byte[][] editedRect = editedRect();
        if (editedRect == null) return;
        int numRows = editedRect.length, numCols = editedRect[0].length;
        if (joining.get()) {
            editedRect = joinedContent(editedRect, numRows, numCols);
        }
        for (int row = 0; row < numRows; ++row) {
            for (int col = 0; col < numCols; ++col) {
                Vector2i tile = minTile.plus(col, row);
                editTile(tile, editedRect[row][col]);
            }
        }
    }

    public void draw(TerrainTileMapRenderer renderer) {
        byte[][] editedRect = editedRect();
        if (editedRect == null) return;
        for (int row = 0; row < editedRect.length; ++row) {
            for (int col = 0; col < editedRect[0].length; ++col) {
                Vector2i tile = minTile.plus(col, row);
                byte code = editedRect[row][col];
                renderer.drawTile(tile, code);
                if (symmetricEditModeProperty().get()) {
                    Vector2i mirroredTile = worldMap.get().mirroredTile(tile);
                    renderer.drawTile(mirroredTile, mirroredTileCode(code));
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

        WorldMap worldMap = this.worldMap.get();

        byte minTileValue = worldMap.content(LayerID.TERRAIN, minTile);
        if       (minTileValue == ARC_NE.$) joinedContent[0][0] = WALL_H.$;
        else if (minTileValue == ARC_SW.$)  joinedContent[0][0] = WALL_V.$;
        else if (minTileValue == WALL_V.$)  joinedContent[0][0] = ARC_SW.$;
        else if (minTileValue == WALL_H.$)  joinedContent[0][0] = ARC_NE.$;

        Vector2i lowerLeftCorner = new Vector2i(minTile.x(), maxTile.y());
        byte lowerLeftValue = worldMap.content(LayerID.TERRAIN, lowerLeftCorner);
        if      (lowerLeftValue == WALL_H.$) joinedContent[numRows-1][0] = ARC_SE.$;
        else if (lowerLeftValue == WALL_V.$) joinedContent[numRows-1][0] = ARC_NW.$;
        else if (lowerLeftValue == ARC_SE.$) joinedContent[numRows-1][0] = WALL_H.$;
        else if (lowerLeftValue == ARC_NW.$) joinedContent[numRows-1][0] = WALL_V.$;

        Vector2i upperRightCorner = new Vector2i(maxTile.x(), minTile.y());
        byte upperRightValue = worldMap.content(LayerID.TERRAIN, upperRightCorner);
        if      (upperRightValue == WALL_V.$) joinedContent[0][numCols-1] = ARC_SE.$;
        else if (upperRightValue == WALL_H.$) joinedContent[0][numCols-1] = ARC_NW.$;
        else if (upperRightValue == ARC_SE.$) joinedContent[0][numCols-1] = WALL_V.$;
        else if (upperRightValue == ARC_NW.$) joinedContent[0][numCols-1] = WALL_H.$;

        byte maxTileValue = worldMap.content(LayerID.TERRAIN, maxTile);
        if (maxTileValue == WALL_V.$) joinedContent[numRows-1][numCols-1] = ARC_NE.$;
        if (maxTileValue == WALL_H.$) joinedContent[numRows-1][numCols-1] = ARC_SW.$;
        if (maxTileValue == ARC_SW.$) joinedContent[numRows-1][numCols-1] = WALL_H.$;
        if (maxTileValue == ARC_NE.$) joinedContent[numRows-1][numCols-1] = WALL_V.$;

        crossings = 0;
        int leftBorder = minTile.x();
        for (int row = minTile.y(); row < maxTile.y(); ++row) {
            int x = 0, y = row - minTile.y();
            if (editedContent[y][x] == WALL_V.$ && worldMap.content(LayerID.TERRAIN, row, leftBorder) == WALL_H.$) {
                joinedContent[y][x] = isEven(crossings) ? ARC_SE.$ : ARC_NE.$;
            }
            ++crossings;
        }

        crossings = 0;
        int rightBorder = maxTile.x();
        for (int row = minTile.y(); row < maxTile.y(); ++row) {
            int x = rightBorder - minTile.x(), y = row - minTile.y();
            if (editedContent[y][x] == WALL_V.$ && worldMap.content(LayerID.TERRAIN, row, leftBorder) == WALL_H.$) {
                joinedContent[y][x] = isEven(crossings) ? ARC_SW.$ : ARC_NW.$;
            }
            ++crossings;
        }

        crossings = 0;
        int upperBorder = minTile.y(); // upper border
        for (int col = minTile.x(); col < maxTile.x(); ++col) {
            int x = col - minTile.x(), y = upperBorder - minTile.y();
            if (editedContent[y][x] == WALL_H.$ && worldMap.content(LayerID.TERRAIN, upperBorder, col) == WALL_V.$) {
                joinedContent[y][x] = isEven(crossings) ? ARC_SE.$ : ARC_SW.$;
            }
            ++crossings;
        }

        crossings = 0;
        int lowerBorder = maxTile.y(); // lower border
        for (int col = minTile.x(); col < maxTile.x(); ++col) {
            int x = col - minTile.x(), y = lowerBorder - minTile.y();
            if (editedContent[y][x] == WALL_H.$ && worldMap.content(LayerID.TERRAIN, lowerBorder, col) == WALL_V.$) {
                joinedContent[y][x] = isEven(crossings) ? ARC_NE.$ : ARC_NW.$;
            }
            ++crossings;
        }

        return joinedContent;
    }

    private byte[][] editedRect() {
        if (minTile == null || maxTile == null) {
            return null;
        }
        int sizeY = maxTile.y() - minTile.y() + 1;
        int sizeX = maxTile.x() - minTile.x() + 1;
        if (sizeY <= 1 || sizeX <= 1) {
            return null;
        }
        byte[][] rect = new byte[sizeY][sizeX];
        for (int y = minTile.y(); y <= maxTile.y(); ++y) {
            for (int x = minTile.x(); x <= maxTile.x(); ++x) {
                rect[y - minTile.y()][x - minTile.x()] = computeTileCode(y, x);
            }
        }
        return rect;
    }

    private byte computeTileCode(int row, int col) {
        if (row == minTile.y() && col == minTile.x()) return ARC_NW.$;
        if (row == minTile.y() && col == maxTile.x()) return ARC_NE.$;
        if (row == maxTile.y() && col == minTile.x()) return ARC_SW.$;
        if (row == maxTile.y() && col == maxTile.x()) return ARC_SE.$;
        if (row == minTile.y() || row == maxTile.y()) return WALL_H.$;
        if (col == minTile.x() || col == maxTile.x()) return WALL_V.$;
        return EMPTY.$;
    }
}