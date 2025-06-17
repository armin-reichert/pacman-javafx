/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.ui._2d;

import de.amr.pacmanfx.lib.Sprite;
import de.amr.pacmanfx.lib.Vector2f;
import de.amr.pacmanfx.model.GameLevel;
import de.amr.pacmanfx.model.Score;
import de.amr.pacmanfx.model.ScoreManager;
import de.amr.pacmanfx.model.actors.AnimatedActor;
import de.amr.pacmanfx.model.actors.MovingActor;
import de.amr.pacmanfx.model.actors.Pac;
import de.amr.pacmanfx.uilib.animation.SpriteAnimationMap;
import javafx.beans.property.FloatProperty;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;

import static de.amr.pacmanfx.Globals.TS;
import static de.amr.pacmanfx.lib.UsefulFunctions.tiles_to_px;
import static java.util.Objects.requireNonNull;

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
     *
     * @param level the game level to be drawn
     * @param x x position of left-upper corner
     * @param y y position of left-upper corner
     * @param backgroundColor level background color
     * @param mazeHighlighted if the maze is drawn as highlighted (flashing)
     * @param energizerHighlighted if the blinking energizers are in their highlighted state
     */
    void drawLevel(GameLevel level, double x, double y, Color backgroundColor, boolean mazeHighlighted, boolean energizerHighlighted);

    default void drawAnimatedActorInfo(MovingActor movingActor) {
        if (movingActor instanceof AnimatedActor animatedActor && movingActor.isVisible()) {
            animatedActor.animations()
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
        if (movingActor instanceof Pac pac) {
            String autopilot = pac.isUsingAutopilot() ? "autopilot" : "";
            String immune = pac.isImmune() ? "immune" : "";
            String text = "%s\n%s".formatted(autopilot, immune).trim();
            ctx().setFill(Color.WHITE);
            ctx().setFont(Font.font("Monospaced", scaled(6)));
            ctx().fillText(text, scaled(pac.x() - 4), scaled(pac.y() + 16));
        }
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
    default void fillTextAtTile(String text, Color color, Font font, int tileX, int tileY) {
        fillText(text, color, font, tiles_to_px(tileX), tiles_to_px(tileY));
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
    default void fillText(String text, Color color, Font font, double x, double y) {
        ctx().setFont(font);
        ctx().setFill(color);
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
    default void fillTextAtCenter(String text, Color color, Font font, double x, double y) {
        ctx().save();
        ctx().setTextAlign(TextAlignment.CENTER);
        fillText(text, color, font, x, y);
        ctx().restore();
    }

    default void drawScores(ScoreManager scoreManager, Color color, Font font) {
        if (scoreManager.isScoreVisible()) {
            drawScore(scoreManager.score(), "SCORE", tiles_to_px(1), tiles_to_px(1), font, color);
            drawScore(scoreManager.highScore(), "HIGH SCORE", tiles_to_px(14), tiles_to_px(1), font, color);
        }
    }

    default void drawScore(Score score, String title, double x, double y, Font font, Color color) {
        fillText(title, color, font, x, y);
        fillText("%7s".formatted("%02d".formatted(score.points())), color, font, x, y + TS + 1);
        if (score.points() != 0) {
            fillText("L" + score.levelNumber(), color, font, x + tiles_to_px(8), y + TS + 1);
        }
    }

    default void drawTileGrid(double sizeX, double sizeY, Color strokeColor) {
        double thin = 0.2, medium = 0.3, thick = 0.35;
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