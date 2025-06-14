package de.amr.pacmanfx.ui._2d;

import de.amr.pacmanfx.lib.Sprite;
import de.amr.pacmanfx.model.actors.Actor;
import de.amr.pacmanfx.model.actors.AnimatedActor;
import de.amr.pacmanfx.uilib.animation.SingleSpriteAnimationMap;
import de.amr.pacmanfx.uilib.animation.SpriteAnimation;
import de.amr.pacmanfx.uilib.animation.SpriteAnimationMap;
import de.amr.pacmanfx.uilib.assets.SpriteSheet;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import org.tinylog.Logger;

import static de.amr.pacmanfx.Globals.HTS;
import static de.amr.pacmanfx.Globals.TS;
import static java.util.Objects.requireNonNull;

public abstract class SpriteGameRenderer implements GameRenderer {

    public abstract SpriteSheet<?> spriteSheet();

    /**
     * Draws a sprite (region inside sprite sheet) unscaled at the given position.
     *
     * @param sprite      the sprite to draw (can be null)
     * @param x           x-coordinate of left-upper corner
     * @param y           y-coordinate of left-upper corner
     */
    public void drawSprite(Sprite sprite, double x, double y) {
        if (sprite != null) {
            ctx().drawImage(spriteSheet().sourceImage(),
                    sprite.x(), sprite.y(), sprite.width(), sprite.height(),
                    x, y, sprite.width(), sprite.height());
        }
    }

    /**
     * Draws a sprite (region inside sprite sheet) the given position. The sprite size and the position are scaled using
     * the current scale factor.
     *
     * @param sprite        the sprite to draw (may be null)
     * @param x             x-coordinate of left-upper corner (unscaled)
     * @param y             y-coordinate of left-upper corner (unscaled)
     */
    public void drawSpriteScaled(Sprite sprite, double x, double y) {
        if (sprite != null) {
            drawSpriteScaled(spriteSheet().sourceImage(), sprite, x, y);
        }
    }

    /**
     * Draws a sprite centered over a position.
     *
     * @param sprite sprite (region in sprite sheet, may be null)
     * @param cx  x-coordinate of the center position
     * @param cy  y-coordinate of the center position
     */
    public void drawSpriteScaledCenteredAt(Sprite sprite, double cx, double cy) {
        if (sprite != null) {
            drawSpriteScaled(sprite, cx - 0.5 * sprite.width(), cy - 0.5 * sprite.height());
        }
    }

    /**
     * Draws the sprite over the "collision box" (one-tile square) of the given actor (if visible).
     *
     * @param actor an actor
     * @param sprite sprite sheet region (can be null)
     */
    public void drawActorSprite(Actor actor, Sprite sprite) {
        requireNonNull(actor);
        if (actor.isVisible() && sprite != null) {
            drawSpriteScaledCenteredAt(sprite, actor.x() + HTS, actor.y() + HTS);
        }
    }

    /**
     * Draws (animated) actor (Pac-Man, ghost, moving bonus) if visible.
     *
     * @param actor the actor to draw
     */
    public void drawActor(Actor actor) {
        requireNonNull(actor);
        if (!actor.isVisible()) {
            return;
        }
        if (actor instanceof AnimatedActor animatedActor) {
            animatedActor.animations().ifPresent(animations -> {
                if (animations instanceof SpriteAnimationMap spriteAnimationMap) {
                    SpriteAnimation currentAnimation = spriteAnimationMap.currentAnimation();
                    if (currentAnimation != null) {
                        drawActorSprite(actor, spriteAnimationMap.currentSprite(actor));
                    } else {
                        Logger.error("No current animation for actor {}", actor);
                    }
                }
                else if (animations instanceof SingleSpriteAnimationMap single) {
                    drawActorSprite(actor, single.currentSprite());
                }
            });
        }
    }

    public void drawLivesCounter(int numLives, int maxLives, double x, double y, Sprite livesCounterSprite) {
        if (numLives == 0) {
            return;
        }
        for (int i = 0; i < Math.min(numLives, maxLives); ++i) {
            drawSpriteScaled(livesCounterSprite, x + TS * (2 * i), y);
        }
        // show text indicating that more lives are available than symbols displayed (can happen when lives are added via cheat)
        int moreLivesThanSymbols = numLives - maxLives;
        if (moreLivesThanSymbols > 0) {
            Font font = Font.font("Serif", FontWeight.BOLD, scaled(8));
            fillText("+" + moreLivesThanSymbols, Color.YELLOW, font, x + TS * 10, y + TS);
        }
    }
}
