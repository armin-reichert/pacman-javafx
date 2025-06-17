/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.arcade.rendering;

import de.amr.pacmanfx.lib.Vector2i;
import de.amr.pacmanfx.model.GameLevel;
import de.amr.pacmanfx.ui._2d.SpriteGameRenderer;
import javafx.beans.property.FloatProperty;
import javafx.beans.property.SimpleFloatProperty;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import java.util.function.Predicate;

import static de.amr.pacmanfx.Globals.HTS;
import static de.amr.pacmanfx.Globals.TS;
import static de.amr.pacmanfx.ui.PacManGames.theAssets;
import static java.util.Objects.requireNonNull;
import static java.util.function.Predicate.not;

public class ArcadePacMan_GameRenderer implements SpriteGameRenderer {

    private final ArcadePacMan_SpriteSheet spriteSheet;
    private final FloatProperty scalingPy = new SimpleFloatProperty(1.0f);
    private final GraphicsContext ctx;

    public ArcadePacMan_GameRenderer(ArcadePacMan_SpriteSheet spriteSheet, Canvas canvas) {
        this.spriteSheet = requireNonNull(spriteSheet);
        ctx = requireNonNull(canvas).getGraphicsContext2D();
    }

    @Override
    public ArcadePacMan_SpriteSheet spriteSheet() {
        return spriteSheet;
    }

    @Override
    public void applyRenderingHints(GameLevel level) {}

    @Override
    public GraphicsContext ctx() {
        return ctx;
    }

    @Override
    public FloatProperty scalingProperty() {
        return scalingPy;
    }

    @Override
    public void drawLevel(GameLevel level, Color backgroundColor, boolean mazeHighlighted, boolean energizerHighlighted) {
        double y = GameLevel.EMPTY_ROWS_OVER_MAZE * TS;
        ctx.save();
        ctx.scale(scaling(), scaling());
        if (mazeHighlighted) {
            ctx.drawImage(theAssets().image("pacman.flashing_maze"), 0, y);
        }
        else if (level.uneatenFoodCount() == 0) {
            drawSprite(spriteSheet.sprite(SpriteID.MAP_EMPTY), 0, y);
        }
        else {
            drawSprite(spriteSheet.sprite(SpriteID.MAP_FULL), 0, y);
            overPaintEatenPelletTiles(level, backgroundColor);
            overPaintEnergizerTiles(level, tile -> !energizerHighlighted || level.tileContainsEatenFood(tile), backgroundColor);
        }
        ctx.restore();
    }

    /**
     * Over-paints all eaten pellet tiles.
     * Assumes to be called in scaled graphics context!
     *
     * @param level the game level
     * @param color over-paint color (background color of level)
     */
    private void overPaintEatenPelletTiles(GameLevel level, Color color) {
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
    private void overPaintEnergizerTiles(GameLevel level, Predicate<Vector2i> overPaintCondition, Color color) {
        level.energizerTiles().filter(overPaintCondition).forEach(tile -> paintSquareInsideTile(tile, 10, color));
    }

    /**
     * Draws a square of the given size in background color over the tile. Used to hide eaten food and energizers.
     * Assumes to be called in scaled graphics context!
     */
    private void paintSquareInsideTile(Vector2i tile, double squareSize, Color color) {
        double centerX = tile.x() * TS + HTS, centerY = tile.y() * TS + HTS;
        ctx().setFill(color);
        ctx().fillRect(centerX - 0.5 * squareSize, centerY - 0.5 * squareSize, squareSize, squareSize);
    }
}