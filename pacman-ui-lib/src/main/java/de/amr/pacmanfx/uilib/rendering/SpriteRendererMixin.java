/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.uilib.rendering;

import de.amr.basics.math.Direction;
import de.amr.basics.math.RectShort;
import de.amr.basics.math.Vector2f;
import de.amr.pacmanfx.uilib.assets.SpriteSheet;

import static java.util.Objects.requireNonNull;

/**
 * Mix-in interface providing sprite rendering functionality.
 */
public interface SpriteRendererMixin extends Renderer {

    record FacingSprite(RectShort sprite, Direction facingDirection) {}

    SpriteSheet<?> spriteSheet();

    /**
     * Draws a sprite (region inside sprite sheet) at the given position.
     *
     * @param sprite      the sprite to draw
     * @param x           x-coordinate of left-upper corner
     * @param y           y-coordinate of left-upper corner
     * @param scaled      tells is the destination rectangle's position and size will be scaled using the current scaling value
     */
    default void drawSprite(RectShort sprite, double x, double y, boolean scaled) {
        requireNonNull(sprite);
        final double s = scaled ? scaling() : 1;
        ctx().setImageSmoothing(imageSmoothing());
        ctx().drawImage(spriteSheet().sourceImage(),
            sprite.x(), sprite.y(), sprite.width(), sprite.height(),
            s * x, s * y, s * sprite.width(), s * sprite.height());
    }

    /**
     * Draws the sprite centered over the given position. The target position will be scaled using the current scaling value.
     *
     * @param sprite the actor sprite
     * @param unscaledX unscaled x-position over which sprite gets drawn
     * @param unscaledY unscaled y-position over which sprite gets drawn
     */
    default void drawSpriteCentered(RectShort sprite, double unscaledX, double unscaledY) {
        drawSprite(sprite, unscaledX - 0.5 * sprite.width(), unscaledY - 0.5 * sprite.height(), true);
    }

    /**
     * Draws the sprite centered over the given position. The target position will be scaled using the current scaling value.
     *
     * @param sprite the actor sprite
     * @param centerUnscaled position over which sprite gets drawn
     */
    default void drawSpriteCentered(RectShort sprite, Vector2f centerUnscaled) {
        drawSpriteCentered(sprite, centerUnscaled.x(), centerUnscaled.y());
    }
}