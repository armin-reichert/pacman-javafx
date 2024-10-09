/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.maps.editor;

import de.amr.games.pacman.maps.rendering.TileMapRenderer;
import javafx.scene.Node;
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
public class Palette {

    public static final Color BG_COLOR = Color.WHITE;

    private final int toolSize;
    private final int numRows;
    private final int numCols;
    private final TileMapRenderer renderer;
    private final Tool[] tools;
    private final GraphicsContext g;
    private Tool selectedTool;
    private int selectedRow;
    private int selectedCol;
    private final Tooltip tooltip;

    public Palette(int toolSize, int numRows, int numCols, TileMapRenderer renderer) {
        this.toolSize = toolSize;
        this.numRows = numRows;
        this.numCols = numCols;
        this.renderer = renderer;
        tools = new Tool[numRows * numCols];

        selectedTool = null;
        selectedRow = -1;
        selectedCol = -1;

        // Tooltip for tool :-)
        tooltip = new Tooltip("This tool does...");
        tooltip.setShowDelay(Duration.seconds(0.1));
        tooltip.setHideDelay(Duration.seconds(0.5));
        tooltip.setFont(Font.font("Sans", 12));

        var canvas = new Canvas(numCols * toolSize, numRows * toolSize);
        canvas.setOnMouseClicked(this::handleMouseClick);
        canvas.setOnMouseMoved(this::handleMouseMove);

        g = canvas.getGraphicsContext2D();
        Tooltip.install(canvas, tooltip);
    }

    public TileValueEditorTool createTileValueEditorTool(EditController editor, byte value, String description) {
        return new TileValueEditorTool(editor, renderer, toolSize, value, description);
    }

    public PropertyValueEditorTool createPropertyValueEditorTool(String propertyName, String description) {
        return new PropertyValueEditorTool(renderer, toolSize, propertyName, description);
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

    public Node root() {
        return g.getCanvas();
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
            selectedRow = index / numCols;
            selectedCol = index % numCols;
        }
    }

    private void handleMouseClick(MouseEvent e) {
        int row = (int) e.getY() / toolSize;
        int col = (int) e.getX() / toolSize;
        int i = row * numCols + col;
        if (tools[i] != null) {
            selectedRow = row;
            selectedCol = col;
            selectedTool = tools[i];
        }
    }

    private void handleMouseMove(MouseEvent e) {
        int row = (int) e.getY() / toolSize;
        int col = (int) e.getX() / toolSize;
        int i = row * numCols + col;
        if (tools[i] != null) {
            String text = tools[i].description();
            tooltip.setText(text.isEmpty() ? "?" : text);
        } else {
            tooltip.setText("No selection");
        }
    }

    public void draw() {
        double width = g.getCanvas().getWidth(), height = g.getCanvas().getHeight();
        g.save();

        g.setFill(BG_COLOR);
        g.fillRect(0, 0, width, height);

        if (renderer != null) {
            renderer.setScaling(toolSize / 8.0);
            for (int i = 0; i < numRows * numCols; ++i) {
                if (tools[i] != null) {
                    tools[i].draw(g, i / numCols, i % numCols);
                }
            }
        }

        // Grid lines
        g.setStroke(Color.LIGHTGRAY);
        g.setLineWidth(1);
        for (int row = 1; row < numRows; ++row) {
            g.strokeLine(0, row * toolSize, width, row * toolSize);
        }
        for (int col = 1; col < numCols; ++col) {
            g.strokeLine(col * toolSize, 0, col * toolSize, height);
        }

        // mark selected tool
        if (selectedRow != -1 && selectedCol != -1) {
            g.setStroke(Color.RED);
            g.setLineWidth(2);
            g.strokeRect(selectedCol * toolSize + 1, selectedRow * toolSize + 1, toolSize - 2, toolSize - 2);
        }

        g.restore();
    }
}
