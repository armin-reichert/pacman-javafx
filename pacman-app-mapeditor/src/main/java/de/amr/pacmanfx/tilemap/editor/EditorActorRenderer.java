/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.tilemap.editor;

import de.amr.pacmanfx.lib.RectShort;
import de.amr.pacmanfx.lib.Vector2f;
import de.amr.pacmanfx.lib.Vector2i;
import de.amr.pacmanfx.uilib.rendering.BaseCanvasRenderer;
import javafx.scene.canvas.Canvas;

import static de.amr.pacmanfx.Globals.HTS;
import static de.amr.pacmanfx.Globals.TS;

public class EditorActorRenderer extends BaseCanvasRenderer {

    public EditorActorRenderer(Canvas canvas) {
        super(canvas);
    }

    /**
     * Draws the actor sprite half tile right of the given tile.
     */
    public void drawActor(Vector2i tile, RectShort sprite) {
        if (tile == null) return;
        Vector2f center = tile.toVector2f().scaled(TS).plus(TS, HTS);
        double x = center.x() - 0.5f * sprite.width();
        double y = center.y() - 0.5f * sprite.height();
        ctx.save();
        ctx.scale(scaling(), scaling());
        ctx.drawImage(ArcadeSprites.SPRITE_SHEET,
                sprite.x(), sprite.y(), sprite.width(), sprite.height(),
                x, y, sprite.width(), sprite.height()
        );
        ctx.restore();
    }
}
