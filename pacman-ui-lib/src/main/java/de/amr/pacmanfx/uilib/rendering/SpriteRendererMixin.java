package de.amr.pacmanfx.uilib.rendering;

import de.amr.pacmanfx.lib.RectShort;
import de.amr.pacmanfx.lib.Vector2f;
import de.amr.pacmanfx.uilib.assets.SpriteSheet;

import static java.util.Objects.requireNonNull;

public interface SpriteRendererMixin extends CanvasRenderer {

    SpriteSheet<?> spriteSheet();

    /**
     * Draws a sprite (region inside sprite sheet) unscaled at the given position.
     *
     * @param sprite      the sprite to draw
     * @param x           x-coordinate of left-upper corner
     * @param y           y-coordinate of left-upper corner
     * @param scaled      tells is the destination rectangle's position and size is scaled using the current scaling value
     */
    default void drawSprite(RectShort sprite, double x, double y, boolean scaled) {
        requireNonNull(sprite);
        double s = scaled ? scaling() : 1;
        ctx().drawImage(spriteSheet().sourceImage(),
            sprite.x(), sprite.y(), sprite.width(), sprite.height(),
            s * x, s * y, s * sprite.width(), s * sprite.height());
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

    default void drawSpriteCentered(double centerX, double centerY, RectShort sprite) {
        drawSprite(sprite, centerX - 0.5 * sprite.width(), centerY - 0.5 * sprite.height(), true);
    }
}