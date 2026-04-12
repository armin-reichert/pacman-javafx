/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.uilib.rendering;

import de.amr.pacmanfx.lib.math.Direction;
import de.amr.pacmanfx.lib.math.RectShort;
import de.amr.pacmanfx.lib.math.Vector2f;
import de.amr.pacmanfx.uilib.assets.SpriteSheet;

import static java.util.Objects.requireNonNull;

/**
 * Mix-in interface providing sprite rendering functionality.
 */
public interface SpriteRendererMixin extends Renderer {

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
        final double s = scaled ? scaling() : 1;
        ctx().setImageSmoothing(imageSmoothing());
        ctx().drawImage(spriteSheet().sourceImage(),
            sprite.x(), sprite.y(),
            sprite.width(), sprite.height(),
            s * x, s * y,
            s * sprite.width(), s * sprite.height());
    }

    /**
     * Draws the sprite centered over the given position. The target position is scaled using the current scaling value.
     *
     * @param sprite the actor sprite
     * @param centerX x-position over which sprite gets drawn
     * @param centerY y-position over which sprite gets drawn
     */
    default void drawSpriteCentered(RectShort sprite, double centerX, double centerY) {
        drawSprite(sprite, centerX - 0.5 * sprite.width(), centerY - 0.5 * sprite.height(), true);
    }

    /**
     * Draws the sprite centered over the given position. The target position is scaled using the current scaling value.
     *
     * @param sprite the actor sprite
     * @param center position over which sprite gets drawn
     */
    default void drawSpriteCentered(RectShort sprite, Vector2f center) {
        drawSpriteCentered(sprite, center.x(), center.y());
    }

    /**
     * Draws the sprite centered and rotated towards the given direction over the given (already scaled) position.
     * It is assumed that the sprite is pointing to the left in its default orientation (rotation = 0).
     *
     * @param sprite the actor sprite
     * @param center position over which sprite gets drawn
     * @param dir the direction the sprite is facing
     */
    default void drawSpriteCenteredFacingAt(RectShort sprite, Vector2f center, Direction dir) {
        requireNonNull(sprite);
        ctx().save();
        ctx().translate(center.x(), center.y());
        switch (dir) {
            case LEFT  -> {}
            case UP    -> ctx().rotate(90);
            case RIGHT -> ctx().scale(-1, 1);
            case DOWN  -> {
                ctx().scale(-1, 1);
                ctx().rotate(-90);
            }
        }
        drawSpriteCentered(sprite, 0, 0);
        ctx().restore();
    }
}