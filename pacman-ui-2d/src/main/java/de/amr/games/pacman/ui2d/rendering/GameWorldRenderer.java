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
import de.amr.games.pacman.ui2d.util.SpriteAnimations;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.List;

import static de.amr.games.pacman.lib.Globals.*;

/**
 * @author Armin Reichert
 */
public interface GameWorldRenderer {

    DoubleProperty scalingProperty();

    default double scaled(double factor) {
        return scalingProperty().get() * factor;
    }

    ObjectProperty<Color> backgroundColorProperty();

    SpriteRenderer spriteRenderer();

    void selectMap(WorldMap worldMap, int mapNumber);

    void drawWorld(GraphicsContext g, GameContext context, GameWorld world);

    void setFlashMode(boolean on);

    void setBlinkingOn(boolean on);

    default void overPaintFood(GraphicsContext g, GameWorld world, Vector2i tile) {
        double cx = t(tile.x()) + HTS;
        double cy = t(tile.y()) + HTS;
        //TODO check if this crap is still needed
        double r = world.isEnergizerPosition(tile) ? 4.5 : 2;
        g.setFill(backgroundColorProperty().get());
        g.fillRect(scaled(cx - r), scaled(cy - r), scaled(2 * r), scaled(2 * r));
    }

    /**
     * Draws animated entity (Pac-Man, ghost, moving bonus) if entity is visible.
     *
     * @param g graphics context
     * @param guy the animated entity
     */
    default void drawAnimatedEntity(GraphicsContext g, AnimatedEntity guy) {
        if (guy.isVisible() && guy.animations().isPresent()) {
            if (guy.animations().get() instanceof SpriteAnimations spriteAnimations) {
                spriteRenderer().drawEntitySprite(g, guy.entity(), spriteAnimations.currentSprite());
            }
        }
    }

    default void drawAnimatedCreatureInfo(GraphicsContext g, AnimatedEntity animatedCreature) {
        if (animatedCreature.animations().isPresent() && animatedCreature.animations().get() instanceof SpriteAnimations sa) {
            Creature guy = (Creature) animatedCreature.entity();
            String animationName = sa.currentAnimationName();
            if (animationName != null) {
                String text = animationName + " " + sa.currentAnimation().frameIndex();
                g.setFill(Color.WHITE);
                g.setFont(Font.font("Monospaced", scaled(6)));
                g.fillText(text, scaled(guy.posX() - 4), scaled(guy.posY() - 4));
            }
            if (guy.wishDir() != null) {
                float r = 2;
                Vector2f dirVector = guy.wishDir().vector().toVector2f();
                Vector2f guyCenter = guy.center();
                Vector2f indicatorCenter = guyCenter.plus(dirVector.scaled(1.5f * TS));
                Vector2f indicatorTopLeft = indicatorCenter.minus(r, r);
                g.setStroke(Color.WHITE);
                g.strokeLine(scaled(guyCenter.x()), scaled(guyCenter.y()), scaled(indicatorCenter.x()), scaled(indicatorCenter.y()));
                g.setFill(guy.isNewTileEntered() ? Color.YELLOW : Color.GREEN);
                g.fillOval(scaled(indicatorTopLeft.x()), scaled(indicatorTopLeft.y()), scaled(2 * r), scaled(2 * r));
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
            spriteRenderer().drawSpriteScaled(g, spriteRenderer().spriteSheet().livesCounterSprite(), x + TS * (2 * i), y);
        }
        // show text indicating that more lives are available than symbols displayed (can happen when lives are added via cheat)
        int moreLivesThanSymbols = numLivesDisplayed - maxSymbols;
        if (moreLivesThanSymbols > 0) {
            Font font = Font.font("Serif", FontWeight.BOLD, scaled(8));
            drawText(g, "+" + moreLivesThanSymbols, Color.YELLOW, font, x + TS * 10, y + TS);
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