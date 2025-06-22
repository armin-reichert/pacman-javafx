package de.amr.pacmanfx.ui._2d;

import de.amr.pacmanfx.lib.Sprite;
import de.amr.pacmanfx.lib.Vector2f;
import de.amr.pacmanfx.model.actors.Actor;
import de.amr.pacmanfx.model.actors.Animated;
import de.amr.pacmanfx.model.actors.MovingActor;
import de.amr.pacmanfx.model.actors.Pac;
import de.amr.pacmanfx.uilib.animation.SingleSpriteWithoutAnimation;
import de.amr.pacmanfx.uilib.animation.SpriteAnimationMap;
import de.amr.pacmanfx.uilib.assets.SpriteSheet;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import org.tinylog.Logger;

import static de.amr.pacmanfx.Globals.HTS;
import static java.util.Objects.requireNonNull;

public abstract class SpriteGameRenderer implements GameRenderer {

    public abstract SpriteSheet<?> spriteSheet();

    /**
     * Draws a sprite (region inside sprite sheet) unscaled at the given position.
     *
     * @param sprite      the sprite to draw
     * @param x           x-coordinate of left-upper corner
     * @param y           y-coordinate of left-upper corner
     */
    public void drawSprite(Sprite sprite, double x, double y) {
        requireNonNull(sprite);
        ctx().drawImage(spriteSheet().sourceImage(),
                sprite.x(), sprite.y(), sprite.width(), sprite.height(),
                x, y, sprite.width(), sprite.height());
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
        requireNonNull(sprite);
        drawSpriteScaled(spriteSheet().sourceImage(), sprite, x, y);
    }

    /**
     * Draws the given sprite from the given sprite sheet image at the given position (left-upper corner).
     * The position and the sprite size are scaled by the current scaling of the renderer.
     *
     * @param image the sprite sheet image
     * @param sprite a sprite
     * @param x unscaled x-coordinate of left-upper corner
     * @param y unscaled y-coordinate of left-upper corner
     */
    public void drawSpriteScaled(Image image, Sprite sprite, double x, double y) {
        requireNonNull(image);
        requireNonNull(sprite);
        ctx().drawImage(image,
            sprite.x(), sprite.y(), sprite.width(), sprite.height(),
            scaled(x), scaled(y), scaled(sprite.width()), scaled(sprite.height()));
    }

    /**
     * Draws a sprite centered over a position.
     *
     * @param sprite sprite (region in sprite sheet, may be null)
     * @param cx  x-coordinate of the center position
     * @param cy  y-coordinate of the center position
     */
    public void drawSpriteScaledCenteredAt(Sprite sprite, double cx, double cy) {
        requireNonNull(sprite);
        drawSpriteScaled(sprite, cx - 0.5 * sprite.width(), cy - 0.5 * sprite.height());
    }

    /**
     * Draws the sprite centered over the "collision box" (one-tile square) of the given actor (if visible).
     *
     * @param actor an actor
     * @param sprite actor sprite
     */
    public void drawActorSpriteCentered(Actor actor, Sprite sprite) {
        float centerX = actor.x() + HTS, centerY = actor.y() + HTS;
        drawSpriteScaledCenteredAt(sprite, centerX, centerY);
    }

    /**
     * Draws an actor (Pac-Man, ghost, moving bonus, etc.) if it is visible.
     *
     * @param actor the actor to draw
     */
    @Override
    public void drawActor(Actor actor) {
        requireNonNull(actor);
        if (actor.isVisible()) {
            if (actor instanceof Animated animated) {
                drawAnimatedActor(animated);
            }
            else throw new IllegalArgumentException("%s: Cannot draw actor of class %s".formatted(
                    getClass().getSimpleName(), actor.getClass().getSimpleName()));
        }
    }

    public void drawAnimatedActor(Animated animated) {
        animated.animationMap().ifPresent(animationMap -> {
            // assume interface is only implemented by Actor (sub-)classes
            Actor actor = (Actor) animated;
            switch (animationMap) {
                case SingleSpriteWithoutAnimation singleSpriteWithoutAnimation ->
                        drawActorSpriteCentered(actor, singleSpriteWithoutAnimation.singleSprite());
                case SpriteAnimationMap<?> spriteAnimationMap -> {
                    if (spriteAnimationMap.currentAnimation() != null) {
                        drawActorSpriteCentered(actor, spriteAnimationMap.currentSprite(actor));
                    } else {
                        Logger.error("No current animation for actor {}", actor);
                    }
                }
                default -> Logger.error("Cannot render animated actor with animation map of type {}", animationMap.getClass());
            }
        });
    }

    public void drawMovingActorInfo(MovingActor movingActor) {
        if (!movingActor.isVisible()) {
            return;
        }
        switch (movingActor) {
            case Pac pac -> {
                drawAnimatedMovingActorInfo(pac);
                String autopilot = pac.isUsingAutopilot() ? "autopilot" : "";
                String immune = pac.isImmune() ? "immune" : "";
                String text = "%s\n%s".formatted(autopilot, immune).trim();
                ctx().setFill(Color.WHITE);
                ctx().setFont(Font.font("Monospaced", scaled(6)));
                ctx().fillText(text, scaled(pac.x() - 4), scaled(pac.y() + 16));
            }
            case Animated animated -> drawAnimatedMovingActorInfo(animated);
            default -> {}
        }
    }

public void drawAnimatedMovingActorInfo(Animated animated) {
    if (!(animated instanceof MovingActor movingActor)) return;

    animated.animationMap()
        .filter(SpriteAnimationMap.class::isInstance)
        .map(SpriteAnimationMap.class::cast)
        .ifPresent(spriteAnimationMap -> {
            ctx().save();
            String selectedID = spriteAnimationMap.selectedAnimationID();
            if (selectedID != null) {
                String text = "[%s:%d]".formatted(selectedID, spriteAnimationMap.currentAnimation().frameIndex());
                double x = scaled(movingActor.x() - 4), y = scaled(movingActor.y() - 4);
                ctx().setFill(Color.WHITE);
                ctx().setFont(Font.font("Sans", scaled(7)));
                ctx().fillText(text, x, y);
                ctx().setStroke(Color.GRAY);
                ctx().strokeText(text, x, y);
            }
            if (movingActor.wishDir() != null) {
                float scaling = scaling();
                Vector2f center = movingActor.center();
                Vector2f arrowHead = center.plus(movingActor.wishDir().vector().scaled(12f)).scaled(scaling);
                Vector2f guyCenter = center.scaled(scaling);
                float radius = scaling * 2, diameter = 2 * radius;
                ctx().setStroke(Color.WHITE);
                ctx().setLineWidth(0.5);
                ctx().strokeLine(guyCenter.x(), guyCenter.y(), arrowHead.x(), arrowHead.y());
                ctx().setFill(movingActor.isNewTileEntered() ? Color.YELLOW : Color.GREEN);
                ctx().fillOval(arrowHead.x() - radius, arrowHead.y() - radius, diameter, diameter);
            }
            ctx().restore();
        });
    }
}