/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.tilemap.editor.palette;

import de.amr.pacmanfx.lib.Vector2i;
import de.amr.pacmanfx.lib.tilemap.LayerID;
import de.amr.pacmanfx.model.WorldMapProperty;
import de.amr.pacmanfx.tilemap.editor.EditorUI;
import de.amr.pacmanfx.tilemap.editor.actions.Action_SetTerrainProperty;
import de.amr.pacmanfx.uilib.rendering.CanvasRenderer;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import java.util.function.BiConsumer;

import static de.amr.pacmanfx.Globals.TS;
import static de.amr.pacmanfx.lib.tilemap.WorldMapFormatter.formatTile;
import static de.amr.pacmanfx.tilemap.editor.EditorGlobals.TOOL_SIZE;

public class ScatterTileTool extends PropertyValuePaletteTool {

    protected BiConsumer<LayerID, Vector2i> editor;

    public ScatterTileTool(EditorUI ui, String propertyName, String description) {
        super(propertyName, description);
        editor = (layerID, tile) -> new Action_SetTerrainProperty(ui.editor(), propertyName, formatTile(tile)).execute();
    }

    @Override
    public BiConsumer<LayerID, Vector2i> editor() {
        return editor;
    }

    @Override
    public void draw(CanvasRenderer renderer, int row, int col) {
        GraphicsContext ctx = renderer.ctx();
        ctx.setFill(Color.BLACK);
        ctx.fillRect(col * TOOL_SIZE, row * TOOL_SIZE, TOOL_SIZE, TOOL_SIZE);
        ctx.save();
        ctx.setImageSmoothing(true);
        ctx.scale(TOOL_SIZE / (double) TS, TOOL_SIZE / (double) TS);
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
