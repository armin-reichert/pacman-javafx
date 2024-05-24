/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.tilemapeditor;

import de.amr.games.pacman.lib.Tiles;
import de.amr.games.pacman.lib.Vector2i;
import de.amr.games.pacman.ui.fx.tilemap.TileMapRenderer;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;

/**
 * @author Armin Reichert
 */
public class Palette extends Canvas {

    final int cellSize;
    final int numRows;
    final int numCols;
    final TileMapRenderer renderer;
    final byte[] cellValues;
    final GraphicsContext g;
    byte selectedValue;
    int selectedValueRow;
    int selectedValueCol;

    public Palette(int cellSize, int numRows, int numCols, byte valueEnd, TileMapRenderer renderer) {
        this.cellSize = cellSize;
        this.numRows = numRows;
        this.numCols = numCols;
        this.renderer = renderer;
        cellValues = new byte[numRows * numCols];
        for (int i = 0; i < cellValues.length; ++i) {
            cellValues[i] = i < valueEnd ? (byte) i : Tiles.EMPTY;
        }
        g = getGraphicsContext2D();
        selectedValue = 0;
        selectedValueRow = -1;
        selectedValueCol = -1;
        setWidth(numCols * cellSize);
        setHeight(numRows * cellSize);
        setOnMouseClicked(this::pickValue);
    }

    public void setValues(byte... values) {
        for (int i = 0; i < values.length; ++i) {
            if (i < cellValues.length) {
                cellValues[i] = values[i];
            }
        }
    }

    private void pickValue(MouseEvent e) {
        selectedValueRow = (int) e.getY() / cellSize;
        selectedValueCol = (int) e.getX() / cellSize;
        selectedValue = cellValues[selectedValueRow * numCols + selectedValueCol];
    }

    public void draw() {
        g.setFill(Color.BLACK);
        g.fillRect(0, 0, getWidth(), getHeight());
        if (renderer != null) {
            renderer.setScaling(cellSize / 8.0);
            for (int i = 0; i < numRows * numCols; ++i) {
                int row = i / numCols, col = i % numCols;
                renderer.drawTile(g, new Vector2i(col, row), cellValues[i]);
            }
        }
        // Grid lines
        g.setStroke(Color.LIGHTGRAY);
        g.setLineWidth(0.75);
        for (int row = 1; row < numRows; ++row) {
            g.strokeLine(0, row * cellSize, getWidth(), row * cellSize);
        }
        for (int col = 1; col < numCols; ++col) {
            g.strokeLine(col * cellSize, 0, col * cellSize, getHeight());
        }
        // mark selected cell
        g.setStroke(Color.YELLOW);
        g.setLineWidth(1);
        if (selectedValueRow != -1 && selectedValueCol != -1) {
            g.strokeRect(selectedValueCol * cellSize, selectedValueRow * cellSize, cellSize, cellSize);
        }
    }
}
