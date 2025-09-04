/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.tilemap.editor.palette;

import de.amr.pacmanfx.uilib.tilemap.TileRenderer;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;

import static de.amr.pacmanfx.tilemap.editor.EditorGlobals.TOOL_SIZE;
import static java.util.Objects.requireNonNull;

public class Palette extends Canvas {

    private final PaletteID id;
    private final int numRows;
    private final int numCols;
    private final List<EditorTool> tools = new ArrayList<>();
    private final Tooltip tooltip;

    private TileRenderer renderer;
    private int selectedToolIndex;

    public Palette(PaletteID id, int numRows, int numCols) {
        super(numCols * TOOL_SIZE, numRows * TOOL_SIZE);

        this.id = requireNonNull(id);
        this.numRows = numRows;
        this.numCols = numCols;
        selectedToolIndex = -1;

        // Tooltip for tool :-)
        tooltip = new Tooltip("This tool does...");
        tooltip.setShowDelay(Duration.seconds(0.1));
        tooltip.setHideDelay(Duration.seconds(0.5));
        tooltip.setFont(Font.font("Sans", 12));
        Tooltip.install(this, tooltip);

        setOnMouseClicked(this::handleMouseClick);
        setOnMouseMoved(this::handleMouseMove);
    }

    public void setRenderer(TileRenderer renderer) {
        this.renderer = renderer;
    }

    public PaletteID id() {
        return id;
    }

    public void addTool(EditorTool tool) {
        tools.add(tool);
    }

    public boolean isToolSelected() {
        return selectedToolIndex != -1;
    }

    public int selectedIndex() {
        return selectedToolIndex;
    }

    public int selectedRowIndex() {
        return selectedToolIndex != -1 ? selectedToolIndex / numCols : -1;
    }

    public int selectedColIndex() {
        return selectedToolIndex != -1 ? selectedToolIndex % numCols : -1;
    }

    public EditorTool selectedTool() {
        return selectedToolIndex != -1 ? tools.get(selectedToolIndex) : null;
    }

    public void selectTool(int index) {
        if (index >= 0 && index < tools.size()) {
            selectedToolIndex = index;
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
        int row = (int) e.getY() / TOOL_SIZE;
        int col = (int) e.getX() / TOOL_SIZE;
        int index = row * numCols + col;
        EditorTool tool = getToolOrNull(index);
        if (tool != null) {
            selectedToolIndex = index;
        }
    }

    private void handleMouseMove(MouseEvent e) {
        int row = (int) e.getY() / TOOL_SIZE;
        int col = (int) e.getX() / TOOL_SIZE;
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
        double width = getWidth(), height = getHeight();
        GraphicsContext ctx = getGraphicsContext2D();

        ctx.save();
        ctx.clearRect(0, 0, width, height);

        if (renderer != null) {
            renderer.setScaling(TOOL_SIZE / 8.0);
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
            ctx.strokeLine(0, row * TOOL_SIZE, width, row * TOOL_SIZE);
        }
        for (int col = 1; col < numCols; ++col) {
            ctx.strokeLine(col * TOOL_SIZE, 0, col * TOOL_SIZE, height);
        }

        // mark selected tool
        if (selectedToolIndex != -1) {
            ctx.setStroke(Color.RED);
            ctx.setLineWidth(2);
            ctx.strokeRect(selectedColIndex() * TOOL_SIZE + 1, selectedRowIndex() * TOOL_SIZE + 1, TOOL_SIZE - 2, TOOL_SIZE - 2);
        }

        ctx.restore();
    }
}