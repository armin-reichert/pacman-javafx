/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.tilemap.editor;

import de.amr.pacmanfx.uilib.tilemap.FoodMapRenderer;
import de.amr.pacmanfx.uilib.tilemap.TerrainMapRenderer;
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

public class Palette {

    public static final Color BG_COLOR = Color.WHITE;

    private final byte id;
    private final int toolSize;
    private final int numRows;
    private final int numCols;
    private final GraphicsContext ctx;
    private final List<TileMapEditorTool> tools;

    private TileRenderer renderer;
    private TileMapEditorTool selectedTool;
    private Tooltip tooltip;
    private int selectedRow;
    private int selectedCol;

    private Palette(byte id, int toolSize, int numRows, int numCols) {
        var canvas = new Canvas(numCols * toolSize, numRows * toolSize);
        ctx = canvas.getGraphicsContext2D();
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

    public Palette(byte id, int toolSize, int numRows, int numCols, TerrainMapRenderer terrainMapRenderer) {
        this(id, toolSize, numRows, numCols);
        TerrainTileMapRenderer copy = new TerrainTileMapRenderer(ctx.getCanvas());
        copy.backgroundColorProperty().bind(terrainMapRenderer.backgroundColorProperty());
        copy.colorSchemeProperty().bind(terrainMapRenderer.colorSchemeProperty());
        renderer = copy;
    }

    public Palette(byte id, int toolSize, int numRows, int numCols, FoodMapRenderer foodMapRenderer) {
        this(id, toolSize, numRows, numCols);
        FoodMapRenderer copy = new FoodMapRenderer(ctx.getCanvas());
        copy.backgroundColorProperty().bind(foodMapRenderer.backgroundColorProperty());
        copy.energizerColorProperty().bind(foodMapRenderer.energizerColorProperty());
        copy.pelletColorProperty().bind(foodMapRenderer.pelletColorProperty());
        renderer = copy;
    }

    public byte id() {
        return id;
    }

    public TileValueEditorTool newTileTool(TileMapEditor editController, byte value, String description) {
        return new TileValueEditorTool(renderer, toolSize, value, description);
    }

    public PropertyValueEditorTool newPropertyTool(String propertyName, String description) {
        return new PropertyValueEditorTool(renderer, toolSize, propertyName, description);
    }

    public void addTileTool(TileMapEditor editor, byte value, String description) {
        tools.add(newTileTool(editor, value, description));
    }

    public void addPropertyTool(String propertyName, String description) {
        tools.add(newPropertyTool(propertyName, description));
    }

    public Node root() {
        return ctx.getCanvas();
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

    public TileMapEditorTool selectedTool() {
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

    private TileMapEditorTool getToolOrNull(int index) {
        if (index < tools.size()) {
            return tools.get(index);
        }
        return null;
    }

    private void handleMouseClick(MouseEvent e) {
        int row = (int) e.getY() / toolSize;
        int col = (int) e.getX() / toolSize;
        int i = row * numCols + col;
        TileMapEditorTool tool = getToolOrNull(i);
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
        TileMapEditorTool tool = getToolOrNull(i);
        if (tool != null) {
            String text = tool.description();
            tooltip.setText(text.isEmpty() ? "?" : text);
        } else {
            tooltip.setText("No selection");
        }
    }

    public void draw() {
        double width = ctx.getCanvas().getWidth(), height = ctx.getCanvas().getHeight();
        ctx.save();

        ctx.setFill(BG_COLOR);
        ctx.fillRect(0, 0, width, height);

        if (renderer != null) {
            renderer.setScaling(toolSize / 8.0);
            for (int i = 0; i < numRows * numCols; ++i) {
                TileMapEditorTool tool = getToolOrNull(i);
                if (tool != null) {
                    tool.draw(i / numCols, i % numCols);
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
