/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d.rendering;

import de.amr.games.pacman.lib.Vector2i;
import de.amr.games.pacman.model.GameWorld;
import de.amr.games.pacman.model.Score;
import de.amr.games.pacman.ui2d.GameContext;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.List;

import static de.amr.games.pacman.lib.Globals.*;

public interface GameWorldRenderer {

    DoubleProperty scalingProperty();

    default double scaled(double factor) {
        return scalingProperty().get() * factor;
    }

    ObjectProperty<Color> backgroundColorProperty();

    SpriteRenderer spriteRenderer();

    void drawWorld(GraphicsContext g, GameContext context, GameWorld world);

    void setFlashMode(boolean on);

    void setBlinkingOn(boolean on);

    default void overpaintFood(GraphicsContext g, GameWorld world, Vector2i tile) {
        double cx = t(tile.x()) + HTS;
        double cy = t(tile.y()) + HTS;
        double r = world.isEnergizerPosition(tile) ? 4.5 : 2;
        g.setFill(backgroundColorProperty().get());
        g.fillRect(scaled(cx - r), scaled(cy - r), scaled(2 * r), scaled(2 * r));
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
        int maxLives = 5;
        var x = TS * 2;
        var y = TS * tileY;
        for (int i = 0; i < Math.min(numLivesDisplayed, maxLives); ++i) {
            spriteRenderer().drawSpriteScaled(g,
                    spriteRenderer().spriteSheet().livesCounterSprite(),
                    x + TS * (2 * i), y);
        }
        // text indicating that more lives are available than displayed
        int excessLives = numLivesDisplayed - maxLives;
        if (excessLives > 0) {
            drawText(g, "+" + excessLives, Color.YELLOW,
                    Font.font("Serif", FontWeight.BOLD, scaled(8)), x + TS * 10, y + TS);
        }
    }

    default void drawLevelCounter(GraphicsContext g, List<Byte> symbols, double x, double y) {
        for (byte symbol : symbols) {
            spriteRenderer().drawSpriteScaled(g, spriteRenderer().spriteSheet().bonusSymbolSprite(symbol), x, y);
            x -= TS * 2;
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