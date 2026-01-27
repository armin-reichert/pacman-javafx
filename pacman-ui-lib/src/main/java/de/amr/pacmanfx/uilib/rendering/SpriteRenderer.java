/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.uilib.rendering;

import de.amr.pacmanfx.lib.math.RectShort;
import de.amr.pacmanfx.lib.math.Vector2f;
import de.amr.pacmanfx.uilib.assets.SpriteSheet;

import static java.util.Objects.requireNonNull;

public interface SpriteRenderer extends Renderer {

    SpriteSheet<?> spriteSheet();

    /**
     * Draws a sprite (region inside sprite sheet) at the given position.
     *
     * @param sprite      the sprite to draw
     * @param x           x-coordinate of left-upper corner
     * @param y           y-coordinate of left-upper corner
     * @param scaled      tells is the destination rectangle's position and size is scaled using the current scaling value
     */
    default void drawSprite(RectShort sprite, double x, double y, boolean scaled) {
        requireNonNull(sprite);
        final double scalingValue = scaled ? scaling() : 1;
        ctx().setImageSmoothing(imageSmoothing());
        ctx().drawImage(spriteSheet().sourceImage(),
            sprite.x(), sprite.y(),
            sprite.width(), sprite.height(),
            scalingValue * x, scalingValue * y,
            scalingValue * sprite.width(), scalingValue * sprite.height());
    }

    /**
     * Draws the sprite centered over the given position. The target position is scaled using the current scaling value.
     *
     * @param center position over which sprite gets drawn
     * @param sprite the actor sprite
     */
    default void drawSpriteCentered(Vector2f center, RectShort sprite) {
        drawSpriteCentered(center.x(), center.y(), sprite);
    }

    /**
     * Draws the sprite centered over the given position. The target position is scaled using the current scaling value.
     *
     * @param centerX x-position over which sprite gets drawn
     * @param centerY y-position over which sprite gets drawn
     * @param sprite the actor sprite
     */
    default void drawSpriteCentered(double centerX, double centerY, RectShort sprite) {
        drawSprite(sprite, centerX - 0.5 * sprite.width(), centerY - 0.5 * sprite.height(), true);
    }
}