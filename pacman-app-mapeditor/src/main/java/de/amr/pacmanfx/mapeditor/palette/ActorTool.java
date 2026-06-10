/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.mapeditor.palette;

import de.amr.basics.math.RectShort;
import de.amr.basics.math.Vector2i;
import de.amr.pacmanfx.mapeditor.TileMapEditorUI;
import de.amr.pacmanfx.mapeditor.actions.Action_SetTerrainProperty;
import de.amr.pacmanfx.model.world.WorldMap;
import de.amr.pacmanfx.model.world.WorldMapPropertyName;
import de.amr.pacmanfx.uilib.rendering.Renderer;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import java.util.function.Consumer;

import static de.amr.pacmanfx.mapeditor.Globals_MapEditor.TOOL_SIZE;
import static de.amr.pacmanfx.mapeditor.rendering.ArcadeSprites.SPRITE_SHEET;
import static java.util.Objects.requireNonNull;

public class ActorTool extends PropertyValueEditorTool {

    private final Consumer<Vector2i> editor;
    private final RectShort sprite;

    public ActorTool(TileMapEditorUI ui, String propertyName, String description, RectShort sprite) {
        super(propertyName, description);
        this.sprite = requireNonNull(sprite);
        editor = tile -> new Action_SetTerrainProperty(ui.editor(), propertyName, String.valueOf(tile)).execute();
    }

    @Override
    public Consumer<Vector2i> editor() {
        return editor;
    }

    @Override
    public void draw(Renderer renderer, int row, int col) {
        GraphicsContext ctx = renderer.ctx();
        ctx.save();
        ctx.setImageSmoothing(true);
        ctx.scale(TOOL_SIZE / (double) WorldMap.TS, TOOL_SIZE / (double) WorldMap.TS);
        Vector2i tile = new Vector2i(col, row);
        switch (propertyName) {
            case WorldMapPropertyName.POS_SCATTER_RED_GHOST -> drawScatterTarget(ctx, tile, Color.RED);
            case WorldMapPropertyName.POS_SCATTER_PINK_GHOST -> drawScatterTarget(ctx, tile, Color.PINK);
            case WorldMapPropertyName.POS_SCATTER_CYAN_GHOST -> drawScatterTarget(ctx, tile, Color.CYAN);
            case WorldMapPropertyName.POS_SCATTER_ORANGE_GHOST -> drawScatterTarget(ctx, tile, Color.ORANGE);
            default -> {
                double x = col * WorldMap.TS, y = row * WorldMap.TS;
                drawSprite(ctx, x, y, sprite);
            }
        }
        ctx.restore();
    }

    private void drawScatterTarget(GraphicsContext ctx, Vector2i tile, Color color) {
        double x = tile.x() * WorldMap.TS, y = tile.y() * WorldMap.TS;
        ctx.setFill(color);
        ctx.fillOval(x + 2, y + 2, WorldMap.TS - 4, WorldMap.TS - 4);
        ctx.setStroke(Color.WHITE);
        ctx.setLineWidth(0.4);
        ctx.strokeLine(x + 0.5 * WorldMap.TS, y, x + 0.5 * WorldMap.TS, y + WorldMap.TS);
        ctx.strokeLine(x, y + 0.5 * WorldMap.TS, x + WorldMap.TS, y + 0.5 * WorldMap.TS);
    }

    private void drawSprite(GraphicsContext g, double x, double y, RectShort sprite) {
        g.drawImage(SPRITE_SHEET,
                sprite.x(), sprite.y(), sprite.width(), sprite.height(),
                x + 1, y + 1, WorldMap.TS - 2, WorldMap.TS - 2);
    }
}