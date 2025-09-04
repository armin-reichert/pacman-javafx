/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.tilemap.editor.palette;

import de.amr.pacmanfx.lib.RectShort;
import de.amr.pacmanfx.tilemap.editor.EditorUI;
import de.amr.pacmanfx.tilemap.editor.actions.Action_SetTerrainProperty;
import de.amr.pacmanfx.uilib.rendering.CanvasRenderer;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import static de.amr.pacmanfx.Globals.TS;
import static de.amr.pacmanfx.lib.tilemap.WorldMapFormatter.formatTile;
import static de.amr.pacmanfx.tilemap.editor.EditorGlobals.TOOL_SIZE;
import static de.amr.pacmanfx.tilemap.editor.rendering.ArcadeSprites.SPRITE_SHEET;
import static java.util.Objects.requireNonNull;

public class ActorTool extends PropertyValuePaletteTool {

    private final RectShort sprite;

    public ActorTool(EditorUI ui, String propertyName, String description, RectShort sprite) {
        super(propertyName, description);
        this.sprite = requireNonNull(sprite);
        editor = (layerID, tile) -> new Action_SetTerrainProperty(ui.editor(), propertyName, formatTile(tile)).execute();
    }

    @Override
    public void draw(CanvasRenderer renderer, int row, int col) {
        GraphicsContext g = renderer.ctx();
        g.setFill(Color.BLACK);
        g.fillRect(col * TOOL_SIZE, row * TOOL_SIZE, TOOL_SIZE, TOOL_SIZE);
        g.save();
        g.setImageSmoothing(true);
        g.scale(TOOL_SIZE / (double) TS, TOOL_SIZE / (double) TS);
        double x = col * TS, y = row * TS;
        drawSprite(g, x, y, sprite);
        g.restore();
    }

    private void drawSprite(GraphicsContext g, double x, double y, RectShort sprite) {
        g.drawImage(SPRITE_SHEET,
                sprite.x(), sprite.y(), sprite.width(), sprite.height(),
                x + 1, y + 1, TS - 2, TS - 2);
    }
}
