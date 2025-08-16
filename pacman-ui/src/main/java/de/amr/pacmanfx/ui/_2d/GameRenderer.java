/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.ui._2d;

import de.amr.pacmanfx.GameContext;
import de.amr.pacmanfx.lib.RectShort;
import de.amr.pacmanfx.lib.Vector2f;
import de.amr.pacmanfx.model.GameLevel;
import de.amr.pacmanfx.model.HUDData;
import de.amr.pacmanfx.model.actors.Actor;
import de.amr.pacmanfx.uilib.animation.SingleSpriteNoAnimation;
import de.amr.pacmanfx.uilib.animation.SpriteAnimationManager;
import de.amr.pacmanfx.uilib.rendering.BaseRenderer;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import org.tinylog.Logger;

import static de.amr.pacmanfx.Globals.HTS;
import static java.util.Objects.requireNonNull;

/**
 * Common base class of all 2D game renderers.
 */
public abstract class GameRenderer extends BaseRenderer implements DebugInfoRenderer {

    /**
     * Applies rendering hints for the given game level to this renderer.
     *
     * @param level the game level that is rendered
     */
    public void applyRenderingHints(GameLevel level) {}

    /**
     * Draws the Head-Up Display (score, live counter, level counter, coins inserted)
     *
     * @param gameContext the game context
     * @param data the data displayed in the Head-Up Display
     * @param sceneSize scene size in pixels
     * @param tick current clock tick
     */
    public abstract void drawHUD(GameContext gameContext, HUDData data, Vector2f sceneSize, long tick);

    /**
     * @param gameContext the game context
     * @param level the game level to be drawn
     * @param backgroundColor level background color
     * @param mazeHighlighted if the maze is drawn as highlighted (flashing)
     * @param energizerHighlighted if the blinking energizers are in their highlighted state
     * @param tick current clock tick
     */
    public abstract void drawLevel(
        GameContext gameContext,
        GameLevel level,
        Color backgroundColor,
        boolean mazeHighlighted,
        boolean energizerHighlighted,
        long tick);

    /**
     * Draws an actor (Pac-Man, ghost, moving bonus, etc.) if it is visible.
     *
     * @param actor the actor to draw
     * @param spriteSheetImage sprite sheet image
     */
    public void drawActor(Actor actor, Image spriteSheetImage) {
        requireNonNull(actor);

        if (!actor.isVisible()) return;

        if (actor.animations().isEmpty()) {
            Logger.error("Actor {} has no animations", actor);
        }

        actor.animations().ifPresent(am -> {
            switch (am) {
                case SingleSpriteNoAnimation singleSprite -> drawActorSpriteCentered(actor, spriteSheetImage, singleSprite.sprite());
                case SpriteAnimationManager<?> spriteAnimations -> {
                    if (spriteAnimations.current() != null) {
                        drawActorSpriteCentered(actor, spriteSheetImage, spriteAnimations.currentSprite(actor));
                    } else {
                        Logger.error("Cannot draw actor {}: No animation selected", actor);
                    }
                }
                default -> Logger.error("Unsupported animation type: {}", am.getClass().getSimpleName());
            }
        });
    }

    /**
     * Draws a sprite (region inside sprite sheet) unscaled at the given position.
     *
     * @param spriteSheetImage the sprite sheet image
     * @param sprite      the sprite to draw
     * @param x           x-coordinate of left-upper corner
     * @param y           y-coordinate of left-upper corner
     */
    public void drawSprite(Image spriteSheetImage, RectShort sprite, double x, double y) {
        requireNonNull(spriteSheetImage);
        requireNonNull(sprite);
        ctx().drawImage(spriteSheetImage,
            sprite.x(), sprite.y(), sprite.width(), sprite.height(),
            x, y, sprite.width(), sprite.height());
    }

    /**
     * Draws the given sprite from the given sprite sheet image at the given position (left-upper corner).
     * The position and the sprite size are scaled by the current scaling of the renderer.
     *
     * @param spriteSheetImage the sprite sheet image
     * @param sprite a sprite
     * @param x unscaled x-coordinate of left-upper corner
     * @param y unscaled y-coordinate of left-upper corner
     */
    public void drawSpriteScaled(Image spriteSheetImage, RectShort sprite, double x, double y) {
        requireNonNull(spriteSheetImage);
        requireNonNull(sprite);
        ctx().drawImage(spriteSheetImage,
                sprite.x(), sprite.y(), sprite.width(), sprite.height(),
                scaled(x), scaled(y), scaled(sprite.width()), scaled(sprite.height()));
    }

    /**
     * Draws a sprite centered over a position.
     *
     * @param spriteSheetImage the sprite sheet image
     * @param sprite sprite (region in sprite sheet, may be null)
     * @param cx  x-coordinate of the center position
     * @param cy  y-coordinate of the center position
     */
    public void drawSpriteScaledCenteredAt(Image spriteSheetImage, RectShort sprite, double cx, double cy) {
        drawSpriteScaled(spriteSheetImage, sprite, cx - 0.5 * sprite.width(), cy - 0.5 * sprite.height());
    }

    /**
     * Draws the sprite centered over the "collision box" (one-tile square) of the given actor (if visible).
     *
     * @param actor an actor
     * @param spriteSheetImage the sprite sheet image
     * @param sprite actor sprite
     */
    public void drawActorSpriteCentered(Actor actor, Image spriteSheetImage, RectShort sprite) {
        requireNonNull(actor);
        drawSpriteScaledCenteredAt(spriteSheetImage, sprite, actor.x() + HTS, actor.y() + HTS);
    }
}