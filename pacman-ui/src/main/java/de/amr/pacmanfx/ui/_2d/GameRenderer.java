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

import static java.util.Objects.requireNonNull;

/**
 * Common base class of all 2D game renderers.
 */
public abstract class GameRenderer extends BaseRenderer implements DebugInfoRenderer {

    // -- Sprite rendering helpers

    /**
     * Draws a sprite (region inside sprite sheet) unscaled at the given position.
     *
     * @param spriteSheetImage the sprite sheet image
     * @param sprite      the sprite to draw
     * @param x           x-coordinate of left-upper corner
     * @param y           y-coordinate of left-upper corner
     * @param scaled      tells is the destination rectangle's position and size is scaled using the current scaling value
     */
    public void drawSprite(Image spriteSheetImage, RectShort sprite, double x, double y, boolean scaled) {
        requireNonNull(spriteSheetImage);
        requireNonNull(sprite);
        double s = scaled ? scaling() : 1;
        ctx().drawImage(spriteSheetImage,
            sprite.x(), sprite.y(), sprite.width(), sprite.height(),
            s * x, s * y, s * sprite.width(), s * sprite.height());
    }

    /**
     * Draws the sprite centered over the given position. The target position is scaled using the current scaling value.
     *
     * @param center position over which sprite gets drawn
     * @param spriteSheetImage the sprite sheet image
     * @param sprite the actor sprite
     */
    public void drawSpriteCentered(Vector2f center, Image spriteSheetImage, RectShort sprite) {
        drawSpriteCentered(center.x(), center.y(), spriteSheetImage, sprite);
    }

    public void drawSpriteCentered(double centerX, double centerY, Image spriteSheetImage, RectShort sprite) {
        drawSprite(spriteSheetImage, sprite, centerX - 0.5 * sprite.width(), centerY - 0.5 * sprite.height(), true);
    }

    // -- Game-specific methods

    /**
     * Applies settings specific to the given game level to this renderer. This can be for example
     * the selection of a different color scheme which is specified in the level map. The default
     * implementation is empty such that subclasses that have no such hints can silently ignore it.
     *
     * @param level the game level that is rendered
     */
    public void applyLevelSettings(GameLevel level) {}

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
     * @param backgroundColor level background color
     * @param mazeHighlighted if the maze is drawn as highlighted (flashing)
     * @param energizerHighlighted if the blinking energizers are in their highlighted state
     * @param tick current clock tick
     */
    public abstract void drawLevel(
        GameContext gameContext,
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

        actor.animations().ifPresent(animations -> {
            RectShort sprite = switch (animations) {
                case SingleSpriteNoAnimation singleSprite -> singleSprite.sprite();
                case SpriteAnimationManager<?> spriteAnimations
                    -> spriteAnimations.currentAnimation() != null ? spriteAnimations.currentSprite(actor) : null;
                default -> null;
            };
            if (sprite != null) {
                drawSpriteCentered(actor.center(), spriteSheetImage, sprite);
            }
        });
    }
}