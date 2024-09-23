/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d.rendering;

import de.amr.games.pacman.lib.Vector2f;
import de.amr.games.pacman.lib.Vector2i;
import de.amr.games.pacman.lib.tilemap.WorldMap;
import de.amr.games.pacman.model.GameWorld;
import de.amr.games.pacman.model.Score;
import de.amr.games.pacman.model.actors.AnimatedEntity;
import de.amr.games.pacman.model.actors.Creature;
import de.amr.games.pacman.ui2d.GameContext;
import de.amr.games.pacman.ui2d.util.SpriteAnimationCollection;
import javafx.beans.property.ObjectProperty;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.List;
import java.util.function.Predicate;

import static de.amr.games.pacman.lib.Globals.*;
import static java.util.function.Predicate.not;

/**
 * @author Armin Reichert
 */
public interface GameWorldRenderer extends SpriteRenderer {

    ObjectProperty<Color> backgroundColorProperty();

    void selectMap(WorldMap worldMap, int mapNumber);

    void drawWorld(GraphicsContext g, GameContext context, GameWorld world);

    void setFlashMode(boolean on);

    void setBlinkingOn(boolean on);

    /**
     * Over-paints all eaten pellet tiles.
     * Assumes to be called in scaled graphics context!
     */
    default void overPaintEatenPellets(GraphicsContext g, GameWorld world) {
        world.map().food().tiles()
            .filter(not(world::isEnergizerPosition))
            .filter(world::hasEatenFoodAt).forEach(tile -> overPaint(g, tile, 4));
    }

    /**
     * Over-pains all eaten energizer tiles.
     * Assumes to be called in scaled graphics context!
     */
    default void overPaintEnergizers(GraphicsContext g, GameWorld world, Predicate<Vector2i> condition) {
        world.energizerTiles().filter(condition).forEach(tile -> overPaint(g, tile, 9.5));
    }

    /**
     * Draws a square of the given size in background color over the tile. Used to hide eaten food and energizers.
     * Assumes to be called in scaled graphics context!
     */
    private void overPaint(GraphicsContext g, Vector2i tile, double squareSize) {
        double centerX = tile.x() * TS + HTS, centerY = tile.y() * TS + HTS;
        g.setFill(backgroundColorProperty().get());
        g.fillRect(centerX - 0.5 * squareSize, centerY - 0.5 * squareSize, squareSize, squareSize);
    }

    default void drawGhostFacingRight(GraphicsContext g, byte ghostID, double x, double y) {
        drawSpriteCenteredOverBox(g, spriteSheet().ghostFacingRight(ghostID), x, y);
    }

    default void drawAnimatedCreatureInfo(GraphicsContext g, AnimatedEntity animatedCreature) {
        if (animatedCreature.animations().isPresent() && animatedCreature.animations().get() instanceof SpriteAnimationCollection sa) {
            Creature guy = (Creature) animatedCreature.entity();
            String animationName = sa.currentAnimationName();
            if (animationName != null) {
                String text = animationName + " " + sa.currentAnimation().frameIndex();
                g.setFill(Color.WHITE);
                g.setFont(Font.font("Monospaced", scaled(6)));
                g.fillText(text, scaled(guy.posX() - 4), scaled(guy.posY() - 4));
            }
            if (guy.wishDir() != null) {
                float scaling = (float) scalingProperty().get();
                Vector2f arrowHead = guy.center().plus(guy.wishDir().vector().scaled(12f)).scaled(scaling);
                Vector2f guyCenter = guy.center().scaled(scaling);
                float radius = scaling * 2, diameter = 2 * radius;

                g.setStroke(Color.WHITE);
                g.setLineWidth(0.5);
                g.strokeLine(guyCenter.x(), guyCenter.y(), arrowHead.x(), arrowHead.y());

                g.setFill(guy.isNewTileEntered() ? Color.YELLOW : Color.GREEN);
                g.fillOval(arrowHead.x() - radius, arrowHead.y() - radius, diameter, diameter);
            }
        }
    }

    /**
     * Draws a text with the given style at the given (unscaled) position.
     *
     * @param g     graphics context
     * @param text  text
     * @param color text color
     * @param font  text font
     * @param x     unscaled x-position
     * @param y     unscaled y-position (baseline)
     */
    default void drawText(GraphicsContext g, String text, Color color, Font font, double x, double y) {
        g.setFont(font);
        g.setFill(color);
        g.fillText(text, scaled(x), scaled(y));
    }

    default void drawLivesCounter(GraphicsContext g, int numLivesDisplayed, int tileY) {
        if (numLivesDisplayed == 0) {
            return;
        }
        int maxSymbols = 5;
        var x = TS * 2;
        var y = TS * tileY;
        for (int i = 0; i < Math.min(numLivesDisplayed, maxSymbols); ++i) {
            drawSpriteScaled(g, spriteSheet().livesCounterSprite(), x + TS * (2 * i), y);
        }
        // show text indicating that more lives are available than symbols displayed (can happen when lives are added via cheat)
        int moreLivesThanSymbols = numLivesDisplayed - maxSymbols;
        if (moreLivesThanSymbols > 0) {
            Font font = Font.font("Serif", FontWeight.BOLD, scaled(8));
            drawText(g, "+" + moreLivesThanSymbols, Color.YELLOW, font, x + TS * 10, y + TS);
        }
    }

    default void drawLevelCounter(GraphicsContext g, List<Byte> symbols, double x, double y) {
        double currentX = x;
        for (byte symbol : symbols) {
            RectArea sprite = spriteSheet().bonusSymbolSprite(symbol);
            drawSpriteScaled(g, sprite, currentX, y);
            currentX -= TS * 2;
        }
    }

    default void drawScore(GraphicsContext g, Score score, String title, double x, double y, Font font, Color color) {
        var pointsText = String.format("%02d", score.points());
        drawText(g, title, color, font, x, y);
        drawText(g, String.format("%7s", pointsText), color, font, x, y + TS + 1);
        if (score.points() != 0) {
            drawText(g, "L" + score.levelNumber(), color, font, x + t(8), y + TS + 1);
        }
    }

    default void drawTileGrid(GraphicsContext g, int numWorldTilesX, int numWorldTilesY  ) {
        g.setStroke(Color.LIGHTGRAY);
        g.setLineWidth(0.2);
        for (int row = 0; row <= numWorldTilesY; ++row) {
            g.strokeLine(0, scaled(TS * row), scaled(numWorldTilesX * TS), scaled(TS * row));
        }
        for (int col = 0; col <= numWorldTilesX; ++col) {
            g.strokeLine(scaled(TS * col), 0, scaled(TS * col), scaled(numWorldTilesY * TS));
        }
    }
}