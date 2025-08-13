/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.ui._2d;

import de.amr.pacmanfx.GameContext;
import de.amr.pacmanfx.lib.Disposable;
import de.amr.pacmanfx.lib.RectShort;
import de.amr.pacmanfx.lib.Vector2f;
import de.amr.pacmanfx.lib.Vector2i;
import de.amr.pacmanfx.model.GameData;
import de.amr.pacmanfx.model.GameLevel;
import de.amr.pacmanfx.model.actors.*;
import de.amr.pacmanfx.ui.GameAssets;
import de.amr.pacmanfx.uilib.animation.SingleSpriteWithoutAnimation;
import de.amr.pacmanfx.uilib.animation.SpriteAnimationMap;
import de.amr.pacmanfx.uilib.assets.SpriteSheet;
import javafx.beans.property.FloatProperty;
import javafx.beans.property.SimpleFloatProperty;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import org.tinylog.Logger;

import java.util.List;
import java.util.Optional;

import static de.amr.pacmanfx.Globals.HTS;
import static de.amr.pacmanfx.Globals.TS;
import static java.util.Objects.requireNonNull;

/**
 * Common base class of all 2D game renderers.
 */
public abstract class GameRenderer implements Disposable {

    public static void fillCanvas(Canvas canvas, Color color) {
        requireNonNull(canvas);
        requireNonNull(color);
        GraphicsContext ctx = canvas.getGraphicsContext2D();
        ctx.setFill(color);
        ctx.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
    }

    protected final GameAssets assets;
    protected final FloatProperty scalingProperty = new SimpleFloatProperty(1);
    protected GraphicsContext ctx;

    protected GameRenderer(GameAssets assets) {
        this.assets = requireNonNull(assets);
    }

    public GameAssets assets() { return assets; }

    public abstract Optional<SpriteSheet<?>> optSpriteSheet();

    public GraphicsContext ctx() { return ctx; }

    public FloatProperty scalingProperty() { return scalingProperty; }

    public void setScaling(double value) {
        if (value <= 0) {
            throw new IllegalArgumentException("Scaling value must be positive but is %.2f".formatted(value));
        }
        scalingProperty().set((float)value);
    }

    public float scaling() { return scalingProperty().get(); }

    public float scaled(double value) { return scaling() * (float) value; }

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
    public abstract void drawHUD(GameContext gameContext, GameData data, Vector2f sceneSize, long tick);

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
     * Draws the specified actors in sequence.
     *
     * @param actors list of actors
     */
    public <T extends Actor> void drawActors(List<T> actors) {
        for (Actor actor : actors) {
            drawActor(actor);
        }
    }

    /**
     * Draws an actor (Pac-Man, ghost, moving bonus, etc.) if it is visible.
     *
     * @param actor the actor to draw
     */
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
                default -> Logger.error("Cannot render animated actor with animation map of type {}", animationMap.getClass().getSimpleName());
            }
        });
    }

    /**
     * Fills a square at the center of the given tile with the current fill color. Used to hide pellets, energizers
     * or sprites that are part of a map image.
     *
     * @param tile a tile
     * @param sideLength side length of the square
     */
    public void fillSquareAtTileCenter(Vector2i tile, int sideLength) {
        requireNonNull(tile);
        double centerX = tile.x() * TS + HTS, centerY = tile.y() * TS + HTS;
        float halfSideLength = 0.5f * sideLength;
        ctx().fillRect(centerX - halfSideLength, centerY - halfSideLength, sideLength, sideLength);
    }

    /**
     * Draws text at the given tile position (scaled by the current scaling value).
     *
     * @param text  text
     * @param color text color
     * @param font  text font
     * @param tileX unscaled tile x-position
     * @param tileY unscaled tile y-position (baseline)
     */
    public void fillTextAtScaledTilePosition(String text, Color color, Font font, int tileX, int tileY) {
        fillTextAtScaledPosition(text, color, font, TS(tileX), TS(tileY));
    }

    /**
     * Draws text left-aligned at the given position (scaled by the current scaling value).
     *
     * @param text  text
     * @param color text color
     * @param font  text font
     * @param x     unscaled x-position
     * @param y     unscaled y-position (baseline)
     */
    public void fillTextAtScaledPosition(String text, Color color, Font font, double x, double y) {
        ctx().setFont(font);
        ctx().setFill(color);
        ctx().fillText(text, scaled(x), scaled(y));
    }

    /**
     * Draws text left-aligned at the given position (scaled by the current scaling value).
     *
     * @param text  text
     * @param color text color
     * @param x     unscaled x-position
     * @param y     unscaled y-position (baseline)
     */
    public void fillTextAtScaledPosition(String text, Color color, double x, double y) {
        ctx().setFill(color);
        ctx().fillText(text, scaled(x), scaled(y));
    }

    /**
     * Draws text left-aligned at the given position (scaled by the current scaling value).
     *
     * @param text  text
     * @param font  text font
     * @param x     unscaled x-position
     * @param y     unscaled y-position (baseline)
     */
    public void fillTextAtScaledPosition(String text, Font font, double x, double y) {
        ctx().setFont(font);
        ctx().fillText(text, scaled(x), scaled(y));
    }

    /**
     * Draws text left-aligned at the given position (scaled by the current scaling value).
     *
     * @param text  text
     * @param x     unscaled x-position
     * @param y     unscaled y-position (baseline)
     */
    public void fillTextAtScaledPosition(String text, double x, double y) {
        ctx().fillText(text, scaled(x), scaled(y));
    }

    /**
     * Draws text center-aligned at the given x position (scaled by the current scaling value).
     *
     * @param text  text
     * @param color text color
     * @param font  text font
     * @param x     unscaled x-position
     * @param y     unscaled y-position (baseline)
     */
    public void fillTextAtScaledCenter(String text, Color color, Font font, double x, double y) {
        ctx().save();
        ctx().setTextAlign(TextAlignment.CENTER);
        fillTextAtScaledPosition(text, color, font, x, y);
        ctx().restore();
    }

    public void drawTileGrid(double sizeX, double sizeY, Color gridColor) {
        double thin = 0.2, medium = 0.4, thick = 0.8;
        int numCols = (int) (sizeX / TS), numRows = (int) (sizeY / TS);
        double width = scaled(numCols * TS), height = scaled(numRows * TS);
        ctx().save();
        ctx().setStroke(gridColor);
        for (int row = 0; row <= numRows; ++row) {
            double y = scaled(row * TS);
            ctx().setLineWidth(row % 10 == 0 ? thick : row % 5 == 0 ? medium : thin);
            ctx().strokeLine(0, y, width, y);
        }
        for (int col = 0; col <= numCols; ++col) {
            double x = scaled(col * TS);
            ctx().setLineWidth(col % 10 == 0 ? thick : col % 5 == 0? medium : thin);
            ctx().strokeLine(x, 0, x, height);
        }
        ctx().restore();
    }

    // if sprite sheet is available:

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
            case Bonus bonus -> {
                //TODO
            }
            case Animated animated -> drawAnimatedMovingActorInfo(animated);
            default -> Logger.error("Cannot render moving actor info of class {}", movingActor.getClass().getSimpleName());
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