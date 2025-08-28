/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.tilemap.editor;

import de.amr.pacmanfx.uilib.tilemap.TileRenderer;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;

import static java.util.Objects.requireNonNull;

public class Palette {

    public static final Color BG_COLOR = Color.WHITE;

    private final PaletteID id;
    private final int toolSize;
    private final int numRows;
    private final int numCols;
    private final Canvas canvas;
    private final List<EditorTool> tools;

    private final TileMapEditor editor;
    private TileRenderer renderer;
    private EditorTool selectedTool;
    private Tooltip tooltip;
    private int selectedRow;
    private int selectedCol;

    public Palette(TileMapEditor editor, PaletteID id, int toolSize, int numRows, int numCols) {
        this.editor = requireNonNull(editor);
        canvas = new Canvas(numCols * toolSize, numRows * toolSize);
        canvas.setOnMouseClicked(this::handleMouseClick);
        canvas.setOnMouseMoved(this::handleMouseMove);
        Tooltip.install(canvas, tooltip);

        this.id = id;
        this.toolSize = toolSize;
        this.numRows = numRows;
        this.numCols = numCols;
        tools = new ArrayList<>();

        selectedTool = null;
        selectedRow = -1;
        selectedCol = -1;

        // Tooltip for tool :-)
        tooltip = new Tooltip("This tool does...");
        tooltip.setShowDelay(Duration.seconds(0.1));
        tooltip.setHideDelay(Duration.seconds(0.5));
        tooltip.setFont(Font.font("Sans", 12));
    }

    public void setRenderer(TileRenderer renderer) {
        this.renderer = renderer;
    }

    public PaletteID id() {
        return id;
    }

    public Canvas canvas() {
        return canvas;
    }

    public void addTool(EditorTool tool) {
        tools.add(tool);
    }

    public Node root() {
        return canvas;
    }

    public boolean isToolSelected() {
        return selectedTool != null;
    }

    public int getSelectedEntryRow() {
        return selectedRow;
    }

    public int getSelectedEntryCol() {
        return selectedCol;
    }

    public int selectedIndex() {
        return selectedRow * numRows + selectedCol;
    }

    public EditorTool selectedTool() {
        return selectedTool;
    }

    public void selectTool(int index) {
        if (index >= 0 && index < tools.size()) {
            selectedTool = tools.get(index);
            selectedRow = index / numCols;
            selectedCol = index % numCols;
        }
    }

    public int numTools() {
        return tools.size();
    }

    private EditorTool getToolOrNull(int index) {
        if (index < tools.size()) {
            return tools.get(index);
        }
        return null;
    }

    private void handleMouseClick(MouseEvent e) {
        int row = (int) e.getY() / toolSize;
        int col = (int) e.getX() / toolSize;
        int i = row * numCols + col;
        EditorTool tool = getToolOrNull(i);
        if (tool != null) {
            selectedRow = row;
            selectedCol = col;
            selectedTool = tool;
        }
    }

    private void handleMouseMove(MouseEvent e) {
        int row = (int) e.getY() / toolSize;
        int col = (int) e.getX() / toolSize;
        int i = row * numCols + col;
        EditorTool tool = getToolOrNull(i);
        if (tool != null) {
            String text = tool.description();
            tooltip.setText(text.isEmpty() ? "?" : text);
        } else {
            tooltip.setText("No selection");
        }
    }

    public void draw() {
        double width = canvas.getWidth(), height = canvas.getHeight();

        GraphicsContext ctx = canvas.getGraphicsContext2D();
        ctx.save();
        ctx.setFill(BG_COLOR);
        ctx.fillRect(0, 0, width, height);

        if (renderer != null) {
            renderer.setScaling(toolSize / 8.0);
            for (int i = 0; i < numRows * numCols; ++i) {
                EditorTool tool = getToolOrNull(i);
                if (tool != null) {
                    tool.draw(renderer, i / numCols, i % numCols);
                }
            }
        }

        // Grid lines
        ctx.setStroke(Color.LIGHTGRAY);
        ctx.setLineWidth(1);
        for (int row = 1; row < numRows; ++row) {
            ctx.strokeLine(0, row * toolSize, width, row * toolSize);
        }
        for (int col = 1; col < numCols; ++col) {
            ctx.strokeLine(col * toolSize, 0, col * toolSize, height);
        }

        // mark selected tool
        if (selectedRow != -1 && selectedCol != -1) {
            ctx.setStroke(Color.RED);
            ctx.setLineWidth(2);
            ctx.strokeRect(selectedCol * toolSize + 1, selectedRow * toolSize + 1, toolSize - 2, toolSize - 2);
        }

        ctx.restore();
    }
}