/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.mapeditor;

import de.amr.games.pacman.lib.Tiles;
import de.amr.games.pacman.lib.Vector2i;
import de.amr.games.pacman.ui2d.tilemap.TileMapRenderer;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.util.Duration;

/**
 * @author Armin Reichert
 */
public class Palette extends Canvas {

    public record EditorTool(byte value, String description) {}

    final int cellSize;
    final int numRows;
    final int numCols;
    final TileMapRenderer renderer;
    final EditorTool[] editorTools;
    final GraphicsContext g;
    byte selectedValue;
    int selectedValueRow;
    int selectedValueCol;
    Tooltip tooltip;

    public Palette(int cellSize, int numRows, int numCols, TileMapRenderer renderer) {
        this.cellSize = cellSize;
        this.numRows = numRows;
        this.numCols = numCols;
        this.renderer = renderer;
        editorTools = new EditorTool[numRows * numCols];
        g = getGraphicsContext2D();
        selectedValue = 0;
        selectedValueRow = -1;
        selectedValueCol = -1;
        setWidth(numCols * cellSize);
        setHeight(numRows * cellSize);
        setOnMouseClicked(this::pickValue);
        // Tooltip for tool :-)
        tooltip = new Tooltip("This tool does...");
        tooltip.setShowDelay(Duration.seconds(0.1));
        tooltip.setHideDelay(Duration.seconds(0.5));
        tooltip.setFont(Font.font("Sans", 12));
        Tooltip.install(this, tooltip);
        //TODO Can tooltip text be computed by current tooltip position?
        setOnMouseMoved(e -> {
            int row = (int) e.getY() / cellSize;
            int col = (int) e.getX() / cellSize;
            int i = row*numCols + col;
            if (editorTools[i] != null) {
                String text = editorTools[i].description();
                tooltip.setText(text.isBlank() ? "Tool #" + (i+1) : text);
            }
        });
    }

    public void setTools(EditorTool... someEditorTools) {
        for (int i = 0; i < someEditorTools.length; ++i) {
            if (i < editorTools.length) {
                editorTools[i] = someEditorTools[i];
            }
        }
    }

    private void pickValue(MouseEvent e) {
        selectedValueRow = (int) e.getY() / cellSize;
        selectedValueCol = (int) e.getX() / cellSize;
        int i = selectedValueRow * numCols + selectedValueCol;
        selectedValue = editorTools[i].value();
    }

    public void draw() {
        g.setFill(Color.BLACK);
        g.fillRect(0, 0, getWidth(), getHeight());
        if (renderer != null) {
            renderer.setScaling(cellSize / 8.0);
            for (int i = 0; i < numRows * numCols; ++i) {
                int row = i / numCols, col = i % numCols;
                if (editorTools[i] != null) {
                    renderer.drawTile(g, new Vector2i(col, row), editorTools[i].value());
                } else {
                    renderer.drawTile(g, new Vector2i(col, row), Tiles.EMPTY);
                }
            }
        }
        // Grid lines
        g.setStroke(Color.LIGHTGRAY);
        g.setLineWidth(1);
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
