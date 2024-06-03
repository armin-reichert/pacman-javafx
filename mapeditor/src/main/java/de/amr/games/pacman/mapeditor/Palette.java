/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.mapeditor;

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

    public static final Color BG_COLOR = Color.WHITE;

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
        setOnMouseMoved(e -> {
            int row = (int) e.getY() / cellSize;
            int col = (int) e.getX() / cellSize;
            int i = row*numCols + col;
            if (editorTools[i] != null) {
                String text = editorTools[i].description();
                tooltip.setText(text.isEmpty() ? "?" : text);
            } else {
                tooltip.setText("No selection");
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
        int pickRow = (int) e.getY() / cellSize;
        int pickCol = (int) e.getX() / cellSize;
        int i = pickRow * numCols + pickCol;
        if (editorTools[i] != null) {
            selectedValueRow = pickRow;
            selectedValueCol = pickCol;
            selectedValue = editorTools[i].value();
        }
    }

    public void draw() {
        g.setFill(BG_COLOR);
        g.fillRect(0, 0, getWidth(), getHeight());
        if (renderer != null) {
            renderer.setScaling(cellSize / 8.0);
            for (int i = 0; i < numRows * numCols; ++i) {
                int row = i / numCols, col = i % numCols;
                if (editorTools[i] != null) {
                    g.setFill(Color.BLACK);
                    g.fillRect(col * cellSize, row * cellSize, cellSize, cellSize);
                    renderer.drawTile(g, new Vector2i(col, row), editorTools[i].value());
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
