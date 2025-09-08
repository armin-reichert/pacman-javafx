/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.mapeditor.palette;

import de.amr.pacmanfx.lib.Vector2i;
import de.amr.pacmanfx.mapeditor.EditorGlobals;
import de.amr.pacmanfx.mapeditor.EditorUI;
import de.amr.pacmanfx.mapeditor.actions.Action_SetTerrainProperty;
import de.amr.pacmanfx.model.WorldMapProperty;
import de.amr.pacmanfx.uilib.rendering.CanvasRenderer;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import java.util.function.Consumer;

import static de.amr.pacmanfx.Globals.TS;
import static de.amr.pacmanfx.lib.worldmap.WorldMapFormatter.formatTile;

public class ScatterTileEditorTool extends PropertyValueEditorTool {

    protected Consumer<Vector2i> editor;

    public ScatterTileEditorTool(EditorUI ui, String propertyName, String description) {
        super(propertyName, description);
        editor = tile -> new Action_SetTerrainProperty(ui.editor(), propertyName, formatTile(tile)).execute();
    }

    @Override
    public Consumer<Vector2i> editor() {
        return editor;
    }

    @Override
    public void draw(CanvasRenderer renderer, int row, int col) {
        GraphicsContext ctx = renderer.ctx();
        ctx.setFill(Color.BLACK);
        ctx.fillRect(col * EditorGlobals.TOOL_SIZE, row * EditorGlobals.TOOL_SIZE, EditorGlobals.TOOL_SIZE, EditorGlobals.TOOL_SIZE);
        ctx.save();
        ctx.setImageSmoothing(true);
        ctx.scale(EditorGlobals.TOOL_SIZE / (double) TS, EditorGlobals.TOOL_SIZE / (double) TS);
        Vector2i tile = new Vector2i(col, row);
        switch (propertyName) {
            case WorldMapProperty.POS_SCATTER_RED_GHOST -> drawScatterTarget(ctx, tile, Color.RED);
            case WorldMapProperty.POS_SCATTER_PINK_GHOST -> drawScatterTarget(ctx, tile, Color.PINK);
            case WorldMapProperty.POS_SCATTER_CYAN_GHOST -> drawScatterTarget(ctx, tile, Color.CYAN);
            case WorldMapProperty.POS_SCATTER_ORANGE_GHOST -> drawScatterTarget(ctx, tile, Color.ORANGE);
            default -> {}
        }
        ctx.restore();
    }

    private void drawScatterTarget(GraphicsContext ctx, Vector2i tile, Color color) {
        double x = tile.x() * TS, y = tile.y() * TS;
        ctx.setFill(color);
        ctx.fillRect(x, y, TS, TS);
        ctx.setStroke(Color.WHITE);
        ctx.setLineWidth(0.5);
        ctx.strokeOval(x + 2, y + 2, TS - 4, TS - 4);
        ctx.strokeLine(x + 0.5 * TS, y, x + 0.5 * TS, y + TS);
        ctx.strokeLine(x, y + 0.5 * TS, x + TS, y + 0.5 * TS);
    }
}
