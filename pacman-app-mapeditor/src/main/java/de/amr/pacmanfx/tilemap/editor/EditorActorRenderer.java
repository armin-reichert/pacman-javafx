/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.tilemap.editor;

import de.amr.pacmanfx.lib.RectShort;
import de.amr.pacmanfx.lib.Vector2i;
import de.amr.pacmanfx.uilib.rendering.BaseSpriteRenderer;
import javafx.scene.canvas.Canvas;

import static de.amr.pacmanfx.tilemap.editor.ArcadeMap.SPRITE_SHEET;

public class EditorActorRenderer extends BaseSpriteRenderer {

    public EditorActorRenderer(Canvas canvas) {
        super(canvas);
    }

    public void drawActorSprite(Vector2i leftTile, RectShort sprite, double gridSize) {
        if (leftTile != null) {
            double spriteSize = 1.75 * gridSize;
            drawSpriteImage(sprite, gridSize,
                (leftTile.x() + 0.5) * gridSize, // sprite image is draw between left tile and right neighbor tile!
                leftTile.y() * gridSize,
                spriteSize, spriteSize);
        }
    }
    private void drawSpriteImage(RectShort sprite, double gridSize, double x, double y, double w, double h) {
        double ox = 0.5 * (w - gridSize), oy = 0.5 * (h - gridSize);
        ctx().drawImage(SPRITE_SHEET, sprite.x(), sprite.y(), sprite.width(), sprite.height(), x - ox, y - oy, w, h);
    }
}
