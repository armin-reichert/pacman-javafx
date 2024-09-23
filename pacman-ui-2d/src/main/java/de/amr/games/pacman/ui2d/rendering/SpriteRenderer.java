/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d.rendering;

import de.amr.games.pacman.model.actors.AnimatedEntity;
import de.amr.games.pacman.model.actors.Entity;
import de.amr.games.pacman.ui2d.util.SpriteAnimationCollection;
import javafx.beans.property.DoubleProperty;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;

import static de.amr.games.pacman.lib.Globals.HTS;

/**
 * @author Armin Reichert
 */
public interface SpriteRenderer {

    GameSpriteSheet spriteSheet();

    DoubleProperty scalingProperty();

    default double scaling() { return scalingProperty().get(); }

    default double scaled(double factor) {
        return scaling() * factor;
    }

    /**
     * Draws the given source scaled by the current scaling value.
     *
     * @param g         graphics context
     * @param image     source
     * @param x         unscaled x-coordinate
     * @param y         unscaled y-coordinate
     * @param width     unscaled width
     * @param height    unscaled height
     */
    default void drawImageScaled(GraphicsContext g, Image image, double x, double y, double width, double height) {
        g.drawImage(image, scaled(x), scaled(y), scaled(width), scaled(height));
    }

    /**
     * Draws a sprite (section of the sprite sheet source) at the given (scaled) position.
     *
     * @param g             graphics context
     * @param sprite        the sprite sheet section to draw
     * @param x             scaled x-coordinate
     * @param y             scaled y-coordinate
     */
    default void drawSpriteUnscaled(GraphicsContext g, RectArea sprite, double x, double y) {
        drawSubImage(g, spriteSheet().sourceImage(), sprite, x, y);
    }

    /**
     * Draws a sprite using the current scene scaling.
     *
     * @param g         graphics context
     * @param sprite    sprite sheet region ("sprite")
     * @param x         UNSCALED x position
     * @param y         UNSCALED y position
     */
    default void drawSpriteScaled(GraphicsContext g, RectArea sprite, double x, double y) {
        drawSubImageScaled(g, spriteSheet().sourceImage(), sprite, x, y);
    }

    /**
     * Draws a sprite using the current scene scaling.
     *
     * @param g         graphics context
     * @param spriteSheet the sprite sheet from which the sprite is drawn
     * @param sprite    sprite sheet region ("sprite")
     * @param x         UNSCALED x position
     * @param y         UNSCALED y position
     */
    default void drawSpriteScaled(GraphicsContext g, GameSpriteSheet spriteSheet, RectArea sprite, double x, double y) {
        drawSubImageScaled(g, spriteSheet.sourceImage(), sprite, x, y);
    }

    /**
     * Draws a section of an source at the given (scaled) position.
     *
     * @param g             graphics context
     * @param sourceImage   the source image
     * @param sprite        the source image area to draw
     * @param x             scaled x-coordinate
     * @param y             scaled y-coordinate
     */
    default void drawSubImage(GraphicsContext g, Image sourceImage, RectArea sprite, double x, double y) {
        if (sprite != null) {
            g.drawImage(sourceImage,
                sprite.x(), sprite.y(), sprite.width(), sprite.height(),
                x, y, sprite.width(), sprite.height());
        }
    }

    default void drawSubImageScaled(GraphicsContext g, Image sourceImage, RectArea sprite, double x, double y) {
        if (sprite != null) {
            g.drawImage(sourceImage,
                    sprite.x(), sprite.y(), sprite.width(), sprite.height(),
                    scaled(x), scaled(y), scaled(sprite.width()), scaled(sprite.height()));
        }
    }

    /**
     * Draws a sprite centered over a one "square tile" large box (bounding box of creature). The position specifies the
     * left-upper corner of the bounding box. Note that the sprites for Pac-Man and the ghosts are 16 pixels wide but the
     * bounding box is only 8 pixels (one square tile) wide.
     *
     * @param g         graphics context
     * @param sprite    sprite sheet region (can be null)
     * @param x         x-coordinate of left-upper corner of bounding box
     * @param y         y-coordinate of left-upper corner of bounding box
     */
    default void drawSpriteCenteredOverBox(GraphicsContext g, RectArea sprite, double x, double y) {
        drawSpriteScaled(g, sprite, x + HTS - 0.5 * sprite.width(), y + HTS - 0.5 * sprite.height());
    }

    /**
     * Draws a sprite centered over a one "square tile" large box (bounding box of creature). The position specifies the
     * left-upper corner of the bounding box. Note that the sprites for Pac-Man and the ghosts are 16 pixels wide but the
     * bounding box is only 8 pixels (one square tile) wide.
     *
     * @param g         graphics context
     * @param spriteSheet the sprite sheet from which the sprite is drawn
     * @param sprite    sprite sheet region (can be null)
     * @param x         x-coordinate of left-upper corner of bounding box
     * @param y         y-coordinate of left-upper corner of bounding box
     */
    default void drawSpriteCenteredOverBox(GraphicsContext g, GameSpriteSheet spriteSheet, RectArea sprite, double x, double y) {
        drawSpriteScaled(g, spriteSheet, sprite, x + HTS - 0.5 * sprite.width(), y + HTS - 0.5 * sprite.height());
    }

    /**
     * Draws the sprite over the bounding box of the given entity (if visible).
     *
     * @param g         graphics context
     * @param entity    an entity like Pac-Man or a ghost
     * @param sprite    sprite sheet region (can be null)
     */
    default void drawEntitySprite(GraphicsContext g, Entity entity, RectArea sprite) {
        if (entity.isVisible()) {
            drawSpriteCenteredOverBox(g, sprite, entity.posX(), entity.posY());
        }
    }

    /**
     * Draws the sprite over the bounding box of the given entity (if visible).
     *
     * @param g         graphics context
     * @param entity    an entity like Pac-Man or a ghost
     * @param spriteSheet the sprite sheet from which the sprite is drwan
     * @param sprite    sprite sheet region (can be null)
     */
    default void drawEntitySprite(GraphicsContext g, Entity entity, GameSpriteSheet spriteSheet, RectArea sprite) {
        if (entity.isVisible()) {
            drawSpriteCenteredOverBox(g, spriteSheet, sprite, entity.posX(), entity.posY());
        }
    }

    /**
     * Draws animated entity (Pac-Man, ghost, moving bonus) if entity is visible.
     *
     * @param g graphics context
     * @param guy the animated entity
     */
    default void drawAnimatedEntity(GraphicsContext g, AnimatedEntity guy) {
        if (guy.isVisible() && guy.animations().isPresent()) {
            if (guy.animations().get() instanceof SpriteAnimationCollection spriteAnimations) {
                GameSpriteSheet spriteSheet = spriteAnimations.currentAnimation().spriteSheet();
                RectArea sprite = spriteAnimations.currentSprite(guy);
                drawEntitySprite(g, guy.entity(), spriteSheet, sprite);
            }
        }
    }

}