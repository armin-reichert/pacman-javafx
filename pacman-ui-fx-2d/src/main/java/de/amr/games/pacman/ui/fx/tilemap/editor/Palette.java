/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui.fx.tilemap.editor;

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

    final int gridSize;
    final int numRows;
    final int numCols;
    final TileMapRenderer renderer;
    final byte[] valueAtIndex;
    final GraphicsContext g;
    byte selectedValue;
    int selectedValueRow;
    int selectedValueCol;

    public Palette(int gridSize, int numRows, int numCols, byte valueEnd, TileMapRenderer renderer) {
        this.gridSize = gridSize;
        this.numRows = numRows;
        this.numCols = numCols;
        this.renderer = renderer;
        valueAtIndex = new byte[numRows * numCols];
        for (int i = 0; i < valueAtIndex.length; ++i) {
            valueAtIndex[i] = i < valueEnd ? (byte) i : Tiles.EMPTY;
        }
        g = getGraphicsContext2D();
        selectedValue = 0;
        selectedValueRow = -1;
        selectedValueCol = -1;
        setWidth(numCols * gridSize);
        setHeight(numRows * gridSize);
        setOnMouseClicked(this::pickValue);
    }

    public void setValues(byte... values) {
        for (int i = 0; i < values.length; ++i) {
            if (i < valueAtIndex.length) {
                valueAtIndex[i] = values[i];
            }
        }
    }

    private void pickValue(MouseEvent e) {
        selectedValueRow = (int) e.getY() / gridSize;
        selectedValueCol = (int) e.getX() / gridSize;
        selectedValue = valueAtIndex[selectedValueRow * numCols + selectedValueCol];
    }

    public void draw() {
        g.setFill(Color.BLACK);
        g.fillRect(0, 0, getWidth(), getHeight());
        if (renderer != null) {
            renderer.setScaling((float) gridSize / 8f);
            for (int i = 0; i < numRows * numCols; ++i) {
                int row = i / numCols, col = i % numCols;
                renderer.drawTile(g, new Vector2i(col, row), valueAtIndex[i]);
            }
        }
        // Grid lines
        g.setStroke(Color.LIGHTGRAY);
        g.setLineWidth(0.75);
        for (int row = 1; row < numRows; ++row) {
            g.strokeLine(0, row * gridSize, getWidth(), row * gridSize);
        }
        for (int col = 1; col < numCols; ++col) {
            g.strokeLine(col * gridSize, 0, col * gridSize, getHeight());
        }
        // mark selected cell
        g.setStroke(Color.YELLOW);
        g.setLineWidth(1);
        if (selectedValueRow != -1 && selectedValueCol != -1) {
            g.strokeRect(selectedValueCol * gridSize, selectedValueRow * gridSize, gridSize, gridSize);
        }
    }
}
