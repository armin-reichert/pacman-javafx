/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.mapeditor.rendering;

import de.amr.pacmanfx.lib.RectShort;
import de.amr.pacmanfx.lib.math.Vector2i;
import de.amr.pacmanfx.uilib.rendering.Renderer;

import static de.amr.pacmanfx.Globals.HTS;
import static de.amr.pacmanfx.Globals.TS;

public interface ActorSpriteRenderer extends Renderer {

    /**
     * Draws the actor sprite half tile right of the given tile.
     * @param tile actor tile, actor is drawn half tile right of this tile
     * @param sprite actor sprite
     */
    default void drawActorSprite(Vector2i tile, RectShort sprite) {
        Vector2i center = tile.scaled(TS).plus(TS, HTS);
        ctx().save();
        ctx().scale(scaling(), scaling());
        ctx().drawImage(ArcadeSprites.SPRITE_SHEET,
            sprite.x(), sprite.y(), sprite.width(), sprite.height(),
            center.x() - 0.5f * sprite.width(),
            center.y() - 0.5f * sprite.height(),
            sprite.width(),
            sprite.height()
        );
        ctx().restore();
    }
}