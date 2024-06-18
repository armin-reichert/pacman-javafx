/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.mapeditor;

import de.amr.games.pacman.lib.Vector2i;
import de.amr.games.pacman.lib.tilemap.TileMap;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.util.Duration;

import static de.amr.games.pacman.lib.Globals.TS;

/**
 * @author Armin Reichert
 */
public class Palette extends Canvas {

    public static final Color BG_COLOR = Color.WHITE;

    public interface Tool {
        String description();
        void apply(TileMap tileMap, Vector2i tile);
        void draw(GraphicsContext g, int row, int col);
    }

    public class ChangeTileValueTool implements Tool {
        private final byte value;
        private final String description;

        public ChangeTileValueTool(byte value, String description) {
            this.value = value;
            this.description = description;
        }

        @Override
        public String description() {
            return description;
        }

        @Override
        public void apply(TileMap tileMap, Vector2i tile) {
            tileMap.set(tile, value);
        }

        @Override
        public void draw(GraphicsContext g, int row, int col) {
            g.setFill(Color.BLACK);
            g.fillRect(col * cellSize, row * cellSize, cellSize, cellSize);
            renderer.drawTile(g, new Vector2i(col, row), value);
        }
    }

    public class ChangePropertyValueTool implements Tool {
        private final String propertyName;
        private final String description;

        public ChangePropertyValueTool(String propertyName, String description) {
            this.propertyName = propertyName;
            this.description = description;
        }

        @Override
        public String description() {
            return description;
        }

        @Override
        public void apply(TileMap tileMap, Vector2i tile) {
            tileMap.setProperty(propertyName, TileMapUtil.formatTile(tile));
        }

        @Override
        public void draw(GraphicsContext g, int row, int col) {
            g.setFill(Color.BLACK);
            g.fillRect(col * cellSize, row * cellSize, cellSize, cellSize);
            if (renderer instanceof TileMapEditorTerrainRenderer tr) {
                g.save();
                g.scale(cellSize / (double)TS, cellSize / (double)TS);
                tr.drawSpecialTile(g, propertyName, new Vector2i(col, row));
                g.restore();
            }
        }
    }

    final int cellSize;
    final int numRows;
    final int numCols;
    final TileMapRenderer renderer;
    final Tool[] tools;
    final GraphicsContext g;
    Tool selectedTool;
    int selectedRow;
    int selectedCol;
    Tooltip tooltip;

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

    public Palette.ChangeTileValueTool changeTileValueTool(byte value, String description) {
        return new ChangeTileValueTool(value, description);
    }

    public ChangePropertyValueTool changePropertyValueTool(String propertyName, String description) {
        return new ChangePropertyValueTool(propertyName, description);
    }

    public void setTools(Tool... someEditorTools) {
        for (int i = 0; i < someEditorTools.length; ++i) {
            if (i < tools.length) {
                tools[i] = someEditorTools[i];
            }
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
        g.setStroke(Color.YELLOW);
        g.setLineWidth(1);
        if (selectedRow != -1 && selectedCol != -1) {
            g.strokeRect(selectedCol * cellSize, selectedRow * cellSize, cellSize, cellSize);
        }
        g.restore();
    }
}
