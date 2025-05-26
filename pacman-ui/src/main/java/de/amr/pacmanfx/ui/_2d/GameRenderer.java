/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.ui._2d;

import de.amr.pacmanfx.lib.RectArea;
import de.amr.pacmanfx.lib.Vector2f;
import de.amr.pacmanfx.lib.Vector2i;
import de.amr.pacmanfx.model.GameLevel;
import de.amr.pacmanfx.model.LevelCounter;
import de.amr.pacmanfx.model.Score;
import de.amr.pacmanfx.model.ScoreManager;
import de.amr.pacmanfx.model.actors.Actor;
import de.amr.pacmanfx.model.actors.Bonus;
import de.amr.pacmanfx.model.actors.Creature;
import de.amr.pacmanfx.model.actors.Pac;
import de.amr.pacmanfx.uilib.animation.SpriteAnimation;
import javafx.beans.property.FloatProperty;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import org.tinylog.Logger;

import java.util.function.Predicate;

import static de.amr.pacmanfx.Globals.HTS;
import static de.amr.pacmanfx.Globals.TS;
import static de.amr.pacmanfx.lib.UsefulFunctions.tiles_to_px;
import static java.util.Objects.requireNonNull;
import static java.util.function.Predicate.not;

/**
 * Common interface of all 2D game renderers.
 */
public interface GameRenderer {

    GraphicsContext ctx();

    default void fillCanvas(Color color) {
        requireNonNull(color);
        ctx().setFill(color);
        ctx().fillRect(0, 0, ctx().getCanvas().getWidth(), ctx().getCanvas().getHeight());
    }

    GameSpriteSheet spriteSheet();

    FloatProperty scalingProperty();

    default void setScaling(float value) {
        if (value <= 0) {
            throw new IllegalArgumentException("Scaling value must be positive value but is %f".formatted(value));
        }
        scalingProperty().set(value);
    }

    default float scaling() { return scalingProperty().get(); }

    default float scaled(double value) { return scaling() * (float) value; }

    /**
     * Applies the rendering hints from the world map to this renderer.
     *
     * @param level the game level that is rendered
     */
    void applyRenderingHints(GameLevel level);

    /**
     * Draws a sprite (region inside sprite sheet) unscaled at the given position.
     *
     * @param sprite        the sprite to draw (may be null)
     * @param x             x-coordinate of left-upper corner
     * @param y             y-coordinate of left-upper corner
     */
    default void drawSprite(RectArea sprite, double x, double y) {
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
    default void drawSpriteScaled(RectArea sprite, double x, double y) {
        if (sprite != null) {
            drawImageRegionScaled(spriteSheet().sourceImage(), sprite, x, y);
        }
    }

    /**
     * Draws the given region from the given image to the given position (left-upper corner). The position and
     * the region size are scaled by the current scaling factor.
     *
     * @param image an image
     * @param region a region inside the image
     * @param x unscaled x-coordinate of left-upper corner
     * @param y unscaled y-coordinate of left-upper corner
     */
    default void drawImageRegionScaled(Image image, RectArea region, double x, double y) {
        requireNonNull(image);
        requireNonNull(region);
        ctx().drawImage(image,
            region.x(), region.y(), region.width(), region.height(),
            scaled(x), scaled(y), scaled(region.width()), scaled(region.height()));
    }

    /**
     * Draws a sprite centered over a position.
     *
     * @param sprite sprite (region in sprite sheet, may be null)
     * @param cx  x-coordinate of the center position
     * @param cy  y-coordinate of the center position
     */
    default void drawSpriteScaledWithCenter(RectArea sprite, double cx, double cy) {
        drawSpriteScaled(sprite, cx - 0.5 * sprite.width(), cy - 0.5 * sprite.height());
    }

    /**
     * Draws the sprite over the "collision box" (one-tile square) of the given actor (if visible).
     *
     * @param actor an actor
     * @param sprite sprite sheet region (can be null)
     */
    default void drawActorSprite(Actor actor, RectArea sprite) {
        requireNonNull(actor);
        if (actor.isVisible() && sprite != null) {
            drawSpriteScaledWithCenter(sprite, actor.x() + HTS, actor.y() + HTS);
        }
    }

    /**
     * Draws (animated) actor (Pac-Man, ghost, moving bonus) if entity is visible.
     *
     * @param actor the actor to draw
     */
    default void drawActor(Actor actor) {
        requireNonNull(actor);
        if (!actor.isVisible()) {
            return;
        }
        actor.animations().ifPresent(animations -> {
            if (animations instanceof SpriteAnimationMap spriteAnimations) {
                SpriteAnimation currentAnimation = spriteAnimations.currentAnimation();
                if (currentAnimation != null) {
                    drawActorSprite(actor, spriteAnimations.currentSprite(actor));
                } else {
                    Logger.error("No current animation for actor {}", actor);
                }
            }
        });
    }

    default void drawBonus(Bonus bonus) {
        if (bonus.state() == Bonus.STATE_EDIBLE) {
            drawActorSprite(bonus.actor(), spriteSheet().bonusSymbolSprite(bonus.symbol()));
        } else if (bonus.state() == Bonus.STATE_EATEN) {
            drawActorSprite(bonus.actor(), spriteSheet().bonusValueSprite(bonus.symbol()));
        }
    }

    void drawLevelCounter(LevelCounter levelCounter, Vector2f sceneSizeInPixels);

    /**
     *
     * @param level the game level to be drawn
     * @param x x position of left-upper corner
     * @param y y position of left-upper corner
     * @param backgroundColor level background color
     * @param mazeHighlighted if the maze is drawn as highlighted (flashing)
     * @param energizerHighlighted if the blinking energizers are in their highlighted state
     */
    void drawLevel(GameLevel level, double x, double y, Color backgroundColor, boolean mazeHighlighted, boolean energizerHighlighted);

    /**
     * Over-paints all eaten pellet tiles.
     * Assumes to be called in scaled graphics context!
     *
     * @param level the game level
     * @param color over-paint color (background color of level)
     */
    default void overPaintEatenPelletTiles(GameLevel level, Color color) {
        level.worldMap().tiles()
            .filter(not(level::isEnergizerPosition))
            .filter(level::tileContainsEatenFood)
            .forEach(tile -> paintSquareInsideTile(tile, 4, color));
    }

    /**
     * Over-paints all eaten energizer tiles.
     * Assumes to be called in scaled graphics context!
     *
     * @param level the game level
     * @param overPaintCondition when {@code true} energizer tile is over-painted
     * @param color over-paint color (background color of level)
     */
    default void overPaintEnergizerTiles(GameLevel level, Predicate<Vector2i> overPaintCondition, Color color) {
        level.energizerTiles().filter(overPaintCondition)
            .forEach(tile -> paintSquareInsideTile(tile, 10, color));
    }

    /**
     * Draws a square of the given size in background color over the tile. Used to hide eaten food and energizers.
     * Assumes to be called in scaled graphics context!
     */
    private void paintSquareInsideTile(Vector2i tile, double squareSize, Paint paint) {
        double centerX = tile.x() * TS + HTS, centerY = tile.y() * TS + HTS;
        ctx().setFill(paint);
        ctx().fillRect(centerX - 0.5 * squareSize, centerY - 0.5 * squareSize, squareSize, squareSize);
    }

    default void drawAnimatedCreatureInfo(Creature creature) {
        creature.animations()
            .filter(SpriteAnimationMap.class::isInstance)
            .map(SpriteAnimationMap.class::cast)
            .ifPresent(animations -> {
                String animID = animations.selectedAnimationID();
                if (animID != null) {
                    String text = animID + " " + animations.currentAnimation().frameIndex();
                    ctx().setFill(Color.WHITE);
                    ctx().setFont(Font.font("Monospaced", scaled(6)));
                    ctx().fillText(text, scaled(creature.x() - 4), scaled(creature.y() - 4));
                }
                if (creature.wishDir() != null) {
                    float scaling = scaling();
                    Vector2f center = creature.position().plus(HTS, HTS);
                    Vector2f arrowHead = center.plus(creature.wishDir().vector().scaled(12f)).scaled(scaling);
                    Vector2f guyCenter = center.scaled(scaling);
                    float radius = scaling * 2, diameter = 2 * radius;
                    ctx().setStroke(Color.WHITE);
                    ctx().setLineWidth(0.5);
                    ctx().strokeLine(guyCenter.x(), guyCenter.y(), arrowHead.x(), arrowHead.y());
                    ctx().setFill(creature.isNewTileEntered() ? Color.YELLOW : Color.GREEN);
                    ctx().fillOval(arrowHead.x() - radius, arrowHead.y() - radius, diameter, diameter);
                }
            });
        if (creature instanceof Pac pac) {
            String autopilot = pac.isUsingAutopilot() ? "autopilot" : "";
            String immune = pac.isImmune() ? "immune" : "";
            String text = "%s\n%s".formatted(autopilot, immune).trim();
            ctx().setFill(Color.WHITE);
            ctx().setFont(Font.font("Monospaced", scaled(6)));
            ctx().fillText(text, scaled(pac.x() - 4), scaled(pac.y() + 16));
        }
    }

    /**
     * Draws text at the given position (scaled by the current scaling value).
     *
     * @param text  text
     * @param color text color
     * @param font  text font
     * @param x     unscaled x-position
     * @param y     unscaled y-position (baseline)
     */
    default void fillTextAtScaledPosition(String text, Color color, Font font, double x, double y) {
        ctx().setFont(font);
        ctx().setFill(color);
        ctx().fillText(text, scaled(x), scaled(y));
    }

    default void centerTextAtScaledPosition(String text, Color color, Font font, double x, double y) {
        ctx().save();
        ctx().setFont(font);
        ctx().setFill(color);
        ctx().setTextAlign(TextAlignment.CENTER);
        ctx().fillText(text, scaled(x), scaled(y));
        ctx().restore();
    }

    default void fillTextAtScaledTilePosition(String text, Color color, Font font, int tileX, int tileY) {
        ctx().setFont(font);
        ctx().setFill(color);
        ctx().fillText(text, scaled(tiles_to_px(tileX)), scaled(tiles_to_px(tileY)));
    }

    default void drawLivesCounter(int numLives, int maxLives, double x, double y) {
        if (numLives == 0) {
            return;
        }
        for (int i = 0; i < Math.min(numLives, maxLives); ++i) {
            drawSpriteScaled(spriteSheet().livesCounterSprite(), x + TS * (2 * i), y);
        }
        // show text indicating that more lives are available than symbols displayed (can happen when lives are added via cheat)
        int moreLivesThanSymbols = numLives - maxLives;
        if (moreLivesThanSymbols > 0) {
            Font font = Font.font("Serif", FontWeight.BOLD, scaled(8));
            fillTextAtScaledPosition("+" + moreLivesThanSymbols, Color.YELLOW, font, x + TS * 10, y + TS);
        }
    }

    default void drawScores(ScoreManager scoreManager, Color color, Font font) {
        if (scoreManager.isScoreVisible()) {
            drawScore(scoreManager.score(), "SCORE", tiles_to_px(1), tiles_to_px(1), font, color);
            drawScore(scoreManager.highScore(), "HIGH SCORE", tiles_to_px(14), tiles_to_px(1), font, color);
        }
    }

    default void drawScore(Score score, String title, double x, double y, Font font, Color color) {
        var pointsText = String.format("%02d", score.points());
        fillTextAtScaledPosition(title, color, font, x, y);
        fillTextAtScaledPosition(String.format("%7s", pointsText), color, font, x, y + TS + 1);
        if (score.points() != 0) {
            fillTextAtScaledPosition("L" + score.levelNumber(), color, font, x + tiles_to_px(8), y + TS + 1);
        }
    }

    default void drawTileGrid(double sizeX, double sizeY, Color strokeColor) {
        double thin = 0.25, medium = 0.5, thick = 0.9;
        int numCols = (int) (sizeX / TS), numRows = (int) (sizeY / TS);
        double width = scaled(numCols * TS), height = scaled(numRows * TS);
        ctx().save();
        ctx().setStroke(strokeColor);
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
}