/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.ui._2d;

import de.amr.pacmanfx.GameContext;
import de.amr.pacmanfx.lib.Vector2f;
import de.amr.pacmanfx.lib.Vector2i;
import de.amr.pacmanfx.model.GameLevel;
import de.amr.pacmanfx.model.HUD;
import de.amr.pacmanfx.model.actors.Actor;
import de.amr.pacmanfx.uilib.assets.AssetStorage;
import de.amr.pacmanfx.uilib.model3D.Destroyable;
import javafx.beans.property.FloatProperty;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;

import java.util.List;

import static de.amr.pacmanfx.Globals.HTS;
import static de.amr.pacmanfx.Globals.TS;
import static de.amr.pacmanfx.lib.UsefulFunctions.tiles_to_px;
import static java.util.Objects.requireNonNull;

/**
 * Common interface of all 2D game renderers.
 */
public interface GameRenderer extends Destroyable {

    static void fillCanvas(Canvas canvas, Color color) {
        requireNonNull(canvas);
        requireNonNull(color);
        GraphicsContext ctx = canvas.getGraphicsContext2D();
        ctx.setFill(color);
        ctx.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
    }

    AssetStorage assets();

    GraphicsContext ctx();

    FloatProperty scalingProperty();

    default void setScaling(double value) {
        if (value <= 0) {
            throw new IllegalArgumentException("Scaling value must be positive but is %.2f".formatted(value));
        }
        scalingProperty().set((float)value);
    }

    default float scaling() { return scalingProperty().get(); }

    default float scaled(double value) { return scaling() * (float) value; }

    /**
     * Applies rendering hints for the given game level to this renderer.
     *
     * @param level the game level that is rendered
     */
    default void applyRenderingHints(GameLevel level) {}

    /**
     * Draws the Head-Up Display (score, live counter, level counter, coins inserted)
     *
     * @param gameContext the game context
     * @param hud the Head-Up Display
     * @param sceneSize scene size in pixels
     * @param tick current clock tick
     */
    void drawHUD(GameContext gameContext, HUD hud, Vector2f sceneSize, long tick);

    /**
     * @param gameContext the game context
     * @param level the game level to be drawn
     * @param backgroundColor level background color
     * @param mazeHighlighted if the maze is drawn as highlighted (flashing)
     * @param energizerHighlighted if the blinking energizers are in their highlighted state
     * @param tick current clock tick
     */
    void drawLevel(
        GameContext gameContext,
        GameLevel level,
        Color backgroundColor,
        boolean mazeHighlighted,
        boolean energizerHighlighted,
        long tick);

    /**
     * Draws the specified actor.
     *
     * @param actor any actor
     */
    void drawActor(Actor actor);

    /**
     * Draws the specified actors in sequence.
     *
     * @param actors list of actors
     */
    @SuppressWarnings("unchecked")
    default <T extends Actor> void drawActors(T... actors) {
        for (Actor actor : actors) {
            drawActor(actor);
        }
    }

    /**
     * Draws the specified actors in sequence.
     *
     * @param actors list of actors
     */
    default <T extends Actor> void drawActors(List<T> actors) {
        for (Actor actor : actors) {
            drawActor(actor);
        }
    }

    /**
     * Fills a square at the center of the given tile with the current fill color. Used to hide pellets, energizers
     * or sprites that are part of a map image.
     *
     * @param tile a tile
     * @param sideLength side length of the square
     */
    default void fillSquareAtTileCenter(Vector2i tile, int sideLength) {
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
    default void fillTextAtScaledTilePosition(String text, Color color, Font font, int tileX, int tileY) {
        fillTextAtScaledPosition(text, color, font, tiles_to_px(tileX), tiles_to_px(tileY));
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
    default void fillTextAtScaledPosition(String text, Color color, Font font, double x, double y) {
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
    default void fillTextAtScaledPosition(String text, Color color, double x, double y) {
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
    default void fillTextAtScaledPosition(String text, Font font, double x, double y) {
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
    default void fillTextAtScaledPosition(String text, double x, double y) {
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
    default void fillTextAtScaledCenter(String text, Color color, Font font, double x, double y) {
        ctx().save();
        ctx().setTextAlign(TextAlignment.CENTER);
        fillTextAtScaledPosition(text, color, font, x, y);
        ctx().restore();
    }

    default void drawTileGrid(double sizeX, double sizeY, Color gridColor) {
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
}