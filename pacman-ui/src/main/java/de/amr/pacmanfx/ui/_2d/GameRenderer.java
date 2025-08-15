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
import de.amr.pacmanfx.model.actors.MovingActor;
import de.amr.pacmanfx.model.actors.Pac;
import de.amr.pacmanfx.ui.GameAssets;
import de.amr.pacmanfx.uilib.animation.SingleSpriteNoAnimation;
import de.amr.pacmanfx.uilib.animation.SpriteAnimationManager;
import de.amr.pacmanfx.uilib.assets.SpriteSheet;
import de.amr.pacmanfx.uilib.rendering.BaseRenderer;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import org.tinylog.Logger;

import java.util.Optional;

import static de.amr.pacmanfx.Globals.HTS;
import static java.util.Objects.requireNonNull;

/**
 * Common base class of all 2D game renderers.
 */
public abstract class GameRenderer extends BaseRenderer {

    protected final GameAssets assets;

    protected GameRenderer(GameAssets assets) {
        this.assets = requireNonNull(assets);
    }

    public GameAssets assets() { return assets; }

    public abstract Optional<SpriteSheet<?>> optSpriteSheet();

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
     */
    public void drawActor(Actor actor) {
        requireNonNull(actor);
        if (!actor.isVisible()) return;
        if (actor.animations().isEmpty()) {
            Logger.error("Actor {} has no animations", actor);
        }
        actor.animations().ifPresent(am -> {
            switch (am) {
                case SingleSpriteNoAnimation singleSprite -> drawActorSpriteCentered(actor, singleSprite.sprite());
                case SpriteAnimationManager<?> spriteAnimations -> {
                    if (spriteAnimations.current() != null) {
                        drawActorSpriteCentered(actor, spriteAnimations.currentSprite(actor));
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
     * @param sprite      the sprite to draw
     * @param x           x-coordinate of left-upper corner
     * @param y           y-coordinate of left-upper corner
     */
    public void drawSprite(RectShort sprite, double x, double y) {
        requireNonNull(sprite);
        optSpriteSheet().ifPresent(spriteSheet -> ctx().drawImage(
                spriteSheet.sourceImage(),
                sprite.x(), sprite.y(), sprite.width(), sprite.height(),
                x, y, sprite.width(), sprite.height()));
    }

    /**
     * Draws a sprite (region inside sprite sheet) the given position. The sprite size and the position are scaled using
     * the current scale factor.
     *
     * @param sprite        the sprite to draw (can be null)
     * @param x             x-coordinate of left-upper corner (unscaled)
     * @param y             y-coordinate of left-upper corner (unscaled)
     */
    public void drawSpriteScaled(RectShort sprite, double x, double y) {
        requireNonNull(sprite);
        optSpriteSheet().ifPresent(spriteSheet -> drawSpriteScaled(spriteSheet.sourceImage(), sprite, x, y));
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
    public void drawSpriteScaled(Image image, RectShort sprite, double x, double y) {
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
    public void drawSpriteScaledCenteredAt(RectShort sprite, double cx, double cy) {
        requireNonNull(sprite);
        drawSpriteScaled(sprite, cx - 0.5 * sprite.width(), cy - 0.5 * sprite.height());
    }

    /**
     * Draws the sprite centered over the "collision box" (one-tile square) of the given actor (if visible).
     *
     * @param actor an actor
     * @param sprite actor sprite
     */
    public void drawActorSpriteCentered(Actor actor, RectShort sprite) {
        float centerX = actor.x() + HTS, centerY = actor.y() + HTS;
        drawSpriteScaledCenteredAt(sprite, centerX, centerY);
    }

    public void drawMovingActorInfo(MovingActor movingActor) {
        if (!movingActor.isVisible()) {
            return;
        }
        if (movingActor instanceof Pac pac) {
            String autopilot = pac.isUsingAutopilot() ? "autopilot" : "";
            String immune = pac.isImmune() ? "immune" : "";
            String text = "%s\n%s".formatted(autopilot, immune).trim();
            ctx().setFill(Color.WHITE);
            ctx().setFont(Font.font("Monospaced", scaled(6)));
            ctx().fillText(text, scaled(pac.x() - 4), scaled(pac.y() + 16));
        }
        movingActor.animations()
            .filter(SpriteAnimationManager.class::isInstance)
            .map(SpriteAnimationManager.class::cast)
            .ifPresent(spriteAnimationMap -> {
                String selectedID = spriteAnimationMap.selectedID();
                if (selectedID != null) {
                    drawAnimationInfo(movingActor, spriteAnimationMap, selectedID);
                }
                if (movingActor.wishDir() != null) {
                    drawDirectionIndicator(movingActor);
                }
            });
    }

    private void drawAnimationInfo(Actor actor, SpriteAnimationManager<?> spriteAnimationMap, String selectedID) {
        ctx().save();
        String text = "[%s:%d]".formatted(selectedID, spriteAnimationMap.current().frameIndex());
        double x = scaled(actor.x() - 4), y = scaled(actor.y() - 4);
        ctx().setFill(Color.WHITE);
        ctx().setFont(Font.font("Sans", scaled(7)));
        ctx().fillText(text, x, y);
        ctx().setStroke(Color.GRAY);
        ctx().strokeText(text, x, y);
        ctx().restore();
    }

    private void drawDirectionIndicator(MovingActor movingActor) {
        ctx().save();
        double scaling = scaling();
        Vector2f center = movingActor.center();
        Vector2f arrowHead = center.plus(movingActor.wishDir().vector().scaled(12f)).scaled(scaling);
        Vector2f guyCenter = center.scaled(scaling);
        double radius = scaling * 2, diameter = 2 * radius;
        ctx().setStroke(Color.WHITE);
        ctx().setLineWidth(0.5);
        ctx().strokeLine(guyCenter.x(), guyCenter.y(), arrowHead.x(), arrowHead.y());
        ctx().setFill(movingActor.isNewTileEntered() ? Color.YELLOW : Color.GREEN);
        ctx().fillOval(arrowHead.x() - radius, arrowHead.y() - radius, diameter, diameter);
        ctx().restore();
    }
}