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
import de.amr.games.pacman.model.actors.Bonus;
import de.amr.games.pacman.model.actors.Creature;
import de.amr.games.pacman.model.actors.MovingBonus;
import de.amr.games.pacman.ui2d.GameContext;
import de.amr.games.pacman.ui2d.util.SpriteAnimationCollection;
import de.amr.games.pacman.ui2d.variant.ms_pacman.ClapperboardAnimation;
import javafx.beans.property.ObjectProperty;
import javafx.scene.image.Image;
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

    static ImageArea imageArea(Image sourceImage, int x, int y, int width, int height) {
        return new ImageArea(sourceImage, new RectArea(x, y, width, height));
    }

    ObjectProperty<Color> backgroundColorProperty();

    void selectMap(WorldMap worldMap, int mapNumber, GameSpriteSheet spriteSheet);

    void drawWorld(GameSpriteSheet spriteSheet, GameContext context, GameWorld world);

    void setFlashMode(boolean on);

    void setBlinkingOn(boolean on);

    default void clearCanvas() {
        ctx().setFill(backgroundColorProperty().get());
        ctx().fillRect(0, 0, canvas().getWidth(), canvas().getHeight());
    }

    /**
     * Over-paints all eaten pellet tiles.
     * Assumes to be called in scaled graphics context!
     */
    default void overPaintEatenPellets(GameWorld world) {
        world.map().food().tiles()
            .filter(not(world::isEnergizerPosition))
            .filter(world::hasEatenFoodAt).forEach(tile -> overPaint(tile, 4));
    }

    /**
     * Over-pains all eaten energizer tiles.
     * Assumes to be called in scaled graphics context!
     */
    default void overPaintEnergizers(GameWorld world, Predicate<Vector2i> condition) {
        world.energizerTiles().filter(condition).forEach(tile -> overPaint(tile, 9.5));
    }

    /**
     * Draws a square of the given size in background color over the tile. Used to hide eaten food and energizers.
     * Assumes to be called in scaled graphics context!
     */
    private void overPaint(Vector2i tile, double squareSize) {
        double centerX = tile.x() * TS + HTS, centerY = tile.y() * TS + HTS;
        ctx().setFill(backgroundColorProperty().get());
        ctx().fillRect(centerX - 0.5 * squareSize, centerY - 0.5 * squareSize, squareSize, squareSize);
    }

    default void drawAnimatedCreatureInfo(AnimatedEntity animatedCreature) {
        if (animatedCreature.animations().isPresent() && animatedCreature.animations().get() instanceof SpriteAnimationCollection sa) {
            Creature guy = (Creature) animatedCreature.entity();
            String animationName = sa.currentAnimationName();
            if (animationName != null) {
                String text = animationName + " " + sa.currentAnimation().frameIndex();
                ctx().setFill(Color.WHITE);
                ctx().setFont(Font.font("Monospaced", scaled(6)));
                ctx().fillText(text, scaled(guy.posX() - 4), scaled(guy.posY() - 4));
            }
            if (guy.wishDir() != null) {
                float scaling = (float) scalingProperty().get();
                Vector2f arrowHead = guy.center().plus(guy.wishDir().vector().scaled(12f)).scaled(scaling);
                Vector2f guyCenter = guy.center().scaled(scaling);
                float radius = scaling * 2, diameter = 2 * radius;

                ctx().setStroke(Color.WHITE);
                ctx().setLineWidth(0.5);
                ctx().strokeLine(guyCenter.x(), guyCenter.y(), arrowHead.x(), arrowHead.y());

                ctx().setFill(guy.isNewTileEntered() ? Color.YELLOW : Color.GREEN);
                ctx().fillOval(arrowHead.x() - radius, arrowHead.y() - radius, diameter, diameter);
            }
        }
    }

    /**
     * Draws a text with the given style at the given (unscaled) position.
     *
     * @param text  text
     * @param color text color
     * @param font  text font
     * @param x     unscaled x-position
     * @param y     unscaled y-position (baseline)
     */
    default void drawText(String text, Color color, Font font, double x, double y) {
        ctx().setFont(font);
        ctx().setFill(color);
        ctx().fillText(text, scaled(x), scaled(y));
    }

    default void drawLivesCounter(GameSpriteSheet spriteSheet, int numLivesDisplayed, int y) {
        if (numLivesDisplayed == 0) {
            return;
        }
        int maxSymbols = 5;
        var x = TS * 2;
        for (int i = 0; i < Math.min(numLivesDisplayed, maxSymbols); ++i) {
            drawSpriteScaled(spriteSheet, spriteSheet.livesCounterSprite(), x + TS * (2 * i), y);
        }
        // show text indicating that more lives are available than symbols displayed (can happen when lives are added via cheat)
        int moreLivesThanSymbols = numLivesDisplayed - maxSymbols;
        if (moreLivesThanSymbols > 0) {
            Font font = Font.font("Serif", FontWeight.BOLD, scaled(8));
            drawText("+" + moreLivesThanSymbols, Color.YELLOW, font, x + TS * 10, y + TS);
        }
    }

    default void drawLevelCounter(GameSpriteSheet spriteSheet, List<Byte> symbols, double x, double y) {
        double currentX = x;
        for (byte symbol : symbols) {
            drawSpriteScaled(spriteSheet, spriteSheet.bonusSymbolSprite(symbol), currentX, y);
            currentX -= TS * 2;
        }
    }

    default void drawScore(Score score, String title, double x, double y, Font font, Color color) {
        var pointsText = String.format("%02d", score.points());
        drawText(title, color, font, x, y);
        drawText(String.format("%7s", pointsText), color, font, x, y + TS + 1);
        if (score.points() != 0) {
            drawText("L" + score.levelNumber(), color, font, x + t(8), y + TS + 1);
        }
    }

    default void drawTileGrid(int numWorldTilesX, int numWorldTilesY  ) {
        ctx().setStroke(Color.LIGHTGRAY);
        ctx().setLineWidth(0.2);
        for (int row = 0; row <= numWorldTilesY; ++row) {
            ctx().strokeLine(0, scaled(TS * row), scaled(numWorldTilesX * TS), scaled(TS * row));
        }
        for (int col = 0; col <= numWorldTilesX; ++col) {
            ctx().strokeLine(scaled(TS * col), 0, scaled(TS * col), scaled(numWorldTilesY * TS));
        }
    }

    default void drawMovingBonus(GameSpriteSheet spriteSheet, MovingBonus bonus) {
        ctx().save();
        ctx().translate(0, bonus.elongationY());
        switch (bonus.state()) {
            case Bonus.STATE_EDIBLE -> drawEntitySprite(bonus.entity(), spriteSheet, spriteSheet.bonusSymbolSprite(bonus.symbol()));
            case Bonus.STATE_EATEN  -> drawEntitySprite(bonus.entity(), spriteSheet, spriteSheet.bonusValueSprite(bonus.symbol()));
            default -> {}
        }
        ctx().restore();
    }

    default void drawClapperBoard(GameSpriteSheet spriteSheet, Font font, Color textColor, ClapperboardAnimation animation, double x, double y) {
        var sprite = animation.currentSprite(spriteSheet.clapperboardSprites());
        if (sprite != RectArea.PIXEL) {
            drawSpriteCenteredOverBox(spriteSheet, sprite, x, y);
            ctx().setFont(font);
            ctx().setFill(textColor.darker());
            var numberX = scaled(x + sprite.width() - 25);
            var numberY = scaled(y + 18);
            ctx().setFill(textColor);
            ctx().fillText(animation.number(), numberX, numberY);
            var textX = scaled(x + sprite.width());
            ctx().fillText(animation.text(), textX, numberY);
        }
    }

    default void drawMsPacManMidwayCopyright(Image image, double x, double y, Color color, Font font) {
        drawImageScaled(image, x, y + 2, t(4) - 2, t(4));
        ctx().setFont(font);
        ctx().setFill(color);
        ctx().fillText("©", scaled(x + TS * 5), scaled(y + TS * 2 + 2));
        ctx().fillText("MIDWAY MFG CO", scaled(x + TS * 7), scaled(y + TS * 2));
        ctx().fillText("1980/1981", scaled(x + TS * 8), scaled(y + TS * 4));
    }
}