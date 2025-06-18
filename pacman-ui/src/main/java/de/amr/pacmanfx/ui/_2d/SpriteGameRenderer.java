package de.amr.pacmanfx.ui._2d;

import de.amr.pacmanfx.lib.Sprite;
import de.amr.pacmanfx.lib.Vector2f;
import de.amr.pacmanfx.model.LevelCounter;
import de.amr.pacmanfx.model.LivesCounter;
import de.amr.pacmanfx.model.actors.*;
import de.amr.pacmanfx.uilib.animation.SingleSpriteAnimationMap;
import de.amr.pacmanfx.uilib.animation.SpriteAnimationMap;
import de.amr.pacmanfx.uilib.assets.SpriteSheet;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import org.tinylog.Logger;

import static de.amr.pacmanfx.Globals.HTS;
import static de.amr.pacmanfx.Globals.TS;
import static de.amr.pacmanfx.ui.PacManGames.theUI;
import static java.util.Objects.requireNonNull;

public interface SpriteGameRenderer extends GameRenderer {

    SpriteSheet<?> spriteSheet();

    /**
     * Draws a sprite (region inside sprite sheet) unscaled at the given position.
     *
     * @param sprite      the sprite to draw (can be null)
     * @param x           x-coordinate of left-upper corner
     * @param y           y-coordinate of left-upper corner
     */
    default void drawSprite(Sprite sprite, double x, double y) {
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
    default void drawSpriteScaled(Sprite sprite, double x, double y) {
        if (sprite != null) {
            drawSpriteScaled(spriteSheet().sourceImage(), sprite, x, y);
        }
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
    default void drawSpriteScaled(Image spriteSheetImage, Sprite sprite, double x, double y) {
        requireNonNull(spriteSheetImage);
        requireNonNull(sprite);
        ctx().drawImage(spriteSheetImage,
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
    default void drawSpriteScaledCenteredAt(Sprite sprite, double cx, double cy) {
        if (sprite != null) {
            drawSpriteScaled(sprite, cx - 0.5 * sprite.width(), cy - 0.5 * sprite.height());
        }
    }

    /**
     * Draws the sprite centered over the "collision box" (one-tile square) of the given actor (if visible).
     *
     * @param actor an actor
     * @param sprite actor sprite
     */
    private void drawActorSpriteCentered(Actor actor, Sprite sprite) {
        float centerX = actor.x() + HTS, centerY = actor.y() + HTS;
        drawSpriteScaledCenteredAt(sprite, centerX, centerY);
    }

    /**
     * Draws an actor (Pac-Man, ghost, moving bonus, etc.) if it is visible.
     *
     * @param actor the actor to draw
     */
    default void drawActor(Actor actor) {
        requireNonNull(actor);
        if (!actor.isVisible()) {
            return;
        }
        switch (actor) {
            case StaticBonus staticBonus     -> drawStaticBonus(staticBonus);
            case MovingBonus movingBonus     -> drawMovingBonus(movingBonus);
            case LivesCounter livesCounter   -> drawLivesCounter(livesCounter);
            case LevelCounter levelCounter   -> drawLevelCounter(levelCounter);
            case AnimatedActor animatedActor -> drawAnimatedActor(animatedActor);
            default -> {}
        }
    }

    private void drawAnimatedActor(AnimatedActor animatedActor) {
        animatedActor.animations().ifPresent(animationMap -> {
            // assume interface is only implemented by Actor (sub-)classes
            Actor actor = (Actor) animatedActor;
            switch (animationMap) {
                case SingleSpriteAnimationMap singleSpriteAnimationMap ->
                        drawActorSpriteCentered(actor, singleSpriteAnimationMap.singleSprite());
                case SpriteAnimationMap spriteAnimationMap -> {
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

    private void drawMovingBonus(MovingBonus bonus) {
        if (bonus.state() == Bonus.STATE_INACTIVE) return;
        ctx().save();
        ctx().translate(0, bonus.elongationY());
        switch (bonus.state()) {
            case Bonus.STATE_EDIBLE -> drawActorSpriteCentered(bonus, theUI().configuration().createBonusSymbolSprite(bonus.symbol()));
            case Bonus.STATE_EATEN  -> drawActorSpriteCentered(bonus, theUI().configuration().createBonusValueSprite(bonus.symbol()));
        }
        ctx().restore();
    }

    private void drawStaticBonus(StaticBonus bonus) {
        switch (bonus.state()) {
            case Bonus.STATE_INACTIVE -> {}
            case Bonus.STATE_EDIBLE -> drawActorSpriteCentered(bonus, theUI().configuration().createBonusSymbolSprite(bonus.symbol()));
            case Bonus.STATE_EATEN  -> drawActorSpriteCentered(bonus, theUI().configuration().createBonusValueSprite(bonus.symbol()));
        }
    }

    default void drawLevelCounter(LevelCounter levelCounter) {
        float x = levelCounter.x(), y = levelCounter.y();
        for (byte symbol : levelCounter.symbols()) {
            Sprite sprite = theUI().configuration().createBonusSymbolSprite(symbol);
            drawSpriteScaled(sprite, x, y);
            x -= TS * 2;
        }
    }

    default void drawLivesCounter(LivesCounter livesCounter) {
        Sprite sprite = theUI().configuration().createLivesCounterSprite();
        for (int i = 0; i < livesCounter.visibleLifeCount(); ++i) {
            drawSpriteScaled(sprite, livesCounter.x() + TS * (2 * i), livesCounter.y());
        }
        if (livesCounter.lifeCount() > livesCounter.maxLivesDisplayed()) {
            // show text indicating that more lives are available than symbols displayed (cheating may cause this)
            Font font = Font.font("Serif", FontWeight.BOLD, scaled(8));
            fillText("(%d)".formatted(livesCounter.lifeCount()), Color.YELLOW, font,
                livesCounter.x() + TS * 10, livesCounter.y() + TS);
        }
    }

    default void drawMovingActorInfo(MovingActor movingActor) {
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
            case AnimatedActor animatedActor -> drawAnimatedMovingActorInfo(animatedActor);
            default -> {}
        }
    }

    default void drawAnimatedMovingActorInfo(AnimatedActor animatedMovingActor) {
        if (animatedMovingActor instanceof MovingActor movingActor) {
            animatedMovingActor.animations()
                    .filter(SpriteAnimationMap.class::isInstance)
                    .map(SpriteAnimationMap.class::cast)
                    .ifPresent(animations -> {
                        String animID = animations.selectedAnimationID();
                        if (animID != null) {
                            String text = animID + " " + animations.currentAnimation().frameIndex();
                            ctx().setFill(Color.WHITE);
                            ctx().setFont(Font.font("Monospaced", scaled(6)));
                            ctx().fillText(text, scaled(movingActor.x() - 4), scaled(movingActor.y() - 4));
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
                    });
        }
    }
}