/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.mapeditor;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.util.Duration;
import org.tinylog.Logger;

/**
 * @author Armin Reichert
 */
public class Palette extends Canvas {

    public static final Color BG_COLOR = Color.WHITE;

    private final int cellSize;
    private final int numRows;
    private final int numCols;
    private final TileMapRenderer renderer;
    private final Tool[] tools;
    private final GraphicsContext g;
    private Tool selectedTool;
    private int selectedRow;
    private int selectedCol;
    private final Tooltip tooltip;

    public Palette(int cellSize, int numRows, int numCols, TileMapRenderer renderer) {
        this.cellSize = cellSize;
        this.numRows = numRows;
        this.numCols = numCols;
        this.renderer = renderer;
        tools = new Tool[numRows * numCols];
        g = getGraphicsContext2D();
        selectedTool = null;
        selectedRow = -1;
        selectedCol = -1;
        setWidth(numCols * cellSize);
        setHeight(numRows * cellSize);
        setOnMouseClicked(this::select);
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
            if (tools[i] != null) {
                String text = tools[i].description();
                tooltip.setText(text.isEmpty() ? "?" : text);
            } else {
                tooltip.setText("No selection");
            }
        });
    }

    public TileValueEditorTool createTileValueEditorTool(TileMapEditor editor, byte value, String description) {
        return new TileValueEditorTool(editor, renderer, cellSize, value, description);
    }

    public PropertyValueEditorTool createPropertyValueEditorTool(String propertyName, String description) {
        return new PropertyValueEditorTool(renderer, cellSize, propertyName, description);
    }

    public void setTools(Tool... editorTools) {
        for (int i = 0; i < editorTools.length; ++i) {
            if (i < tools.length) {
                tools[i] = editorTools[i];
            } else {
                Logger.error("Palette is full");
                break;
            }
        }
    }

    public boolean isToolSelected() {
        return selectedTool != null;
    }

    public Tool selectedTool() {
        return selectedTool;
    }

    public void selectTool(int index) {
        if (index >= 0 && index < tools.length) {
            selectedTool = tools[index];
        }
    }

    private void select(MouseEvent e) {
        int row = (int) e.getY() / cellSize;
        int col = (int) e.getX() / cellSize;
        int i = row * numCols + col;
        if (tools[i] != null) {
            selectedRow = row;
            selectedCol = col;
            selectedTool = tools[i];
        }
    }

    public void draw() {
        g.setFill(BG_COLOR);
        g.fillRect(0, 0, getWidth(), getHeight());
        if (renderer != null) {
            renderer.setScaling(cellSize / 8.0);
            for (int i = 0; i < numRows * numCols; ++i) {
                if (tools[i] != null) {
                    int row = i / numCols, col = i % numCols;
                    tools[i].draw(g, row, col);
                }
            }
        }
        g.save();
        // Grid lines
        g.setStroke(Color.LIGHTGRAY);
        g.setLineWidth(1);
        for (int row = 1; row < numRows; ++row) {
            g.strokeLine(0, row * cellSize, getWidth(), row * cellSize);
        }
        for (int col = 1; col < numCols; ++col) {
            g.strokeLine(col * cellSize, 0, col * cellSize, getHeight());
        }
        // mark selected tool
        g.setStroke(Color.RED);
        g.setLineWidth(2);
        if (selectedRow != -1 && selectedCol != -1) {
            g.strokeRect(selectedCol * cellSize + 1, selectedRow * cellSize + 1, cellSize - 2, cellSize - 2);
        }
        g.restore();
    }
}
