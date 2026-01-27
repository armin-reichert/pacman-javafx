/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.mapeditor.palette;

import de.amr.pacmanfx.uilib.rendering.TileRenderer;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static de.amr.pacmanfx.mapeditor.EditorGlobals.FONT_TOOL_TIPS;
import static de.amr.pacmanfx.mapeditor.EditorGlobals.TOOL_SIZE;
import static java.util.Objects.requireNonNull;

public class Palette extends Canvas {

    private static final String CHECK_MARK_SYMBOL = "\u2713";
    private static final Font CHECK_MARK_FONT = Font.font(16);
    private static final Color CHECK_MARK_FILL = Color.grayRgb(42);

    private static final int BOTTOM_HEIGHT = 25;

    private final PaletteID id;
    private final List<PaletteTool> tools = new ArrayList<>();
    private final Tooltip tooltip;

    private TileRenderer renderer;
    private final IntegerProperty selectedToolIndex = new SimpleIntegerProperty(-1);

    public Palette(PaletteID id, int maxEntries) {
        super(maxEntries * TOOL_SIZE, TOOL_SIZE + BOTTOM_HEIGHT);

        this.id = requireNonNull(id);

        // Tooltip for tool :-)
        tooltip = new Tooltip("This tool does...");
        tooltip.setShowDelay(Duration.seconds(0.1));
        tooltip.setHideDelay(Duration.seconds(0.5));
        tooltip.setFont(FONT_TOOL_TIPS);
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

    public void addTool(PaletteTool tool) {
        tools.add(tool);
    }

    public Optional<PaletteTool> selectedTool() {
        return selectedToolIndex() != -1 ? Optional.of(tools.get(selectedToolIndex())) : Optional.empty();
    }

    public int selectedToolIndex() {
        return selectedToolIndex.get();
    }

    public void setSelectedToolIndex(int index) {
        if (index >= 0 && index < tools.size()) {
            selectedToolIndex.set(index);
        }
    }

    public int numTools() {
        return tools.size();
    }

    private PaletteTool getToolOrNull(int index) {
        if (tools.isEmpty()) return null;
        return 0 <= index && index < tools.size() ? tools.get(index) : null;
    }

    private int indexFromPosition(double x, double y) {
        return (int) x / TOOL_SIZE;
    }

    private void handleMouseClick(MouseEvent e) {
        int index = indexFromPosition(e.getX(), e.getY());
        if (getToolOrNull(index) != null) {
            selectedToolIndex.set(index);
        }
    }

    private void handleMouseMove(MouseEvent e) {
        int index = indexFromPosition(e.getX(), e.getY());
        PaletteTool tool = getToolOrNull(index);
        if (tool != null) {
            String text = tool.description();
            tooltip.setText(text.isEmpty() ? "?" : text);
        } else {
            tooltip.setText("");
        }
    }

    public void draw() {
        GraphicsContext ctx = getGraphicsContext2D();
        double width = getWidth(), height = getHeight();
        double separatorY = TOOL_SIZE + 1;

        ctx.save();
        ctx.setFill(Color.BLACK);
        ctx.fillRect(0, 0, width, TOOL_SIZE + 1);
        // bottom area with checkmarks
        ctx.setFill(Color.LIGHTGRAY);
        ctx.fillRect(0, separatorY, width, BOTTOM_HEIGHT);

        if (renderer != null) {
            renderer.setScaling(TOOL_SIZE / 8.0);
            for (int i = 0; i < numTools(); ++i) {
                PaletteTool tool = getToolOrNull(i);
                if (tool != null) {
                    tool.draw(renderer, 0, i % numTools());
                }
            }
        }

        // mark selected tool
        if (selectedToolIndex() != -1) {
            ctx.setFont(CHECK_MARK_FONT);
            ctx.setFill(CHECK_MARK_FILL);
            ctx.fillText(CHECK_MARK_SYMBOL, selectedToolIndex() * TOOL_SIZE + 0.5 * TOOL_SIZE - 8, TOOL_SIZE + 0.6 * BOTTOM_HEIGHT);
        }

        // Separators
        ctx.setLineWidth(0.25);
        for (int col = 1; col < numTools(); ++col) {
            int x = col * TOOL_SIZE;
            ctx.setStroke(Color.WHITE);
            ctx.strokeLine(x, 0, x, TOOL_SIZE);
            ctx.setStroke(Color.BLACK);
            ctx.strokeLine(x, TOOL_SIZE, x, height);
        }

        ctx.restore();
    }
}