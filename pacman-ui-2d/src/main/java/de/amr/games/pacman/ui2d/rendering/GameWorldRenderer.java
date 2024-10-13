/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d.rendering;

import de.amr.games.pacman.lib.RectArea;
import de.amr.games.pacman.lib.Vector2f;
import de.amr.games.pacman.lib.Vector2i;
import de.amr.games.pacman.lib.tilemap.WorldMap;
import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.model.GameWorld;
import de.amr.games.pacman.model.Score;
import de.amr.games.pacman.model.actors.*;
import de.amr.games.pacman.ui2d.GameContext;
import de.amr.games.pacman.ui2d.scene.ms_pacman.ClapperboardAnimation;
import de.amr.games.pacman.ui2d.util.AssetStorage;
import de.amr.games.pacman.ui2d.util.SpriteAnimation;
import de.amr.games.pacman.ui2d.util.SpriteAnimationCollection;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import org.tinylog.Logger;

import java.util.List;
import java.util.function.Predicate;

import static de.amr.games.pacman.lib.Globals.*;
import static de.amr.games.pacman.ui2d.GameAssets2D.ARCADE_PALE;
import static java.util.function.Predicate.not;

/**
 * @author Armin Reichert
 */
public interface GameWorldRenderer {

    void configure(GameModel game, GameSpriteSheet spriteSheet);

    static ImageArea imageArea(Image sourceImage, int x, int y, int width, int height) {
        return new ImageArea(sourceImage, new RectArea(x, y, width, height));
    }

    GameWorldRenderer copy();

    AssetStorage assets();

    Canvas canvas();

    void setCanvas(Canvas canvas);

    default GraphicsContext ctx() { return canvas().getGraphicsContext2D(); }

    DoubleProperty scalingProperty();

    default double scaling() { return scalingProperty().get(); }

    default double scaled(double factor) {
        return scaling() * factor;
    }

    default Font scaledArcadeFont(double size) {
        return assets().font("font.arcade", scaled(size));
    }

    /**
     * Draws the given source scaled by the current scaling value.
     *
     * @param image     source
     * @param x         unscaled x-coordinate
     * @param y         unscaled y-coordinate
     * @param width     unscaled width
     * @param height    unscaled height
     */
    default void drawImageScaled(Image image, double x, double y, double width, double height) {
        ctx().drawImage(image, scaled(x), scaled(y), scaled(width), scaled(height));
    }

    /**
     * Draws a sprite (section of the sprite sheet source) at the given (scaled) position.
     *
     * @param spriteSheet   the sprite sheet
     * @param sprite        the sprite sheet section to draw
     * @param x             scaled x-coordinate
     * @param y             scaled y-coordinate
     */
    default void drawSpriteUnscaled(GameSpriteSheet spriteSheet, RectArea sprite, double x, double y) {
        drawSubImage(spriteSheet.sourceImage(), sprite, x, y);
    }

    /**
     * Draws a sprite using the current scene scaling.
     *
     * @param spriteSheet the sprite sheet from which the sprite is drawn
     * @param sprite    sprite sheet region ("sprite")
     * @param x         UNSCALED x position
     * @param y         UNSCALED y position
     */
    default void drawSpriteScaled(GameSpriteSheet spriteSheet, RectArea sprite, double x, double y) {
        drawSubImageScaled(spriteSheet.sourceImage(), sprite, x, y);
    }

    /**
     * Draws a section of an source at the given (scaled) position.
     *
     * @param sourceImage   the source image
     * @param sprite        the source image area to draw
     * @param x             scaled x-coordinate
     * @param y             scaled y-coordinate
     */
    default void drawSubImage(Image sourceImage, RectArea sprite, double x, double y) {
        if (sprite != null) {
            ctx().drawImage(sourceImage,
                sprite.x(), sprite.y(), sprite.width(), sprite.height(),
                x, y, sprite.width(), sprite.height());
        }
    }

    default void drawSubImageScaled(Image sourceImage, RectArea sprite, double x, double y) {
        if (sprite != null) {
            ctx().drawImage(sourceImage,
                sprite.x(), sprite.y(), sprite.width(), sprite.height(),
                scaled(x), scaled(y), scaled(sprite.width()), scaled(sprite.height()));
        }
    }

    /**
     * Draws a sprite centered over a one "square tile" large box (bounding box of creature). The position specifies the
     * left-upper corner of the bounding box. Note that the sprites for Pac-Man and the ghosts are 16 pixels wide but the
     * bounding box is only 8 pixels (one square tile) wide.
     *
     * @param spriteSheet the sprite sheet from which the sprite is drawn
     * @param sprite    sprite sheet region (can be null)
     * @param x         x-coordinate of left-upper corner of bounding box
     * @param y         y-coordinate of left-upper corner of bounding box
     */
    default void drawSpriteCenteredOverBox(GameSpriteSheet spriteSheet, RectArea sprite, double x, double y) {
        drawSpriteCenteredOverPosition(spriteSheet, sprite, x + HTS, y + HTS);
    }

    default void drawSpriteCenteredOverPosition(GameSpriteSheet spriteSheet, RectArea sprite, double x, double y) {
        drawSpriteScaled(spriteSheet, sprite, x - 0.5 * sprite.width(), y - 0.5 * sprite.height());
    }

    /**
     * Draws the sprite over the bounding box of the given entity (if visible).
     *
     * @param entity    an entity like Pac-Man or a ghost
     * @param spriteSheet the sprite sheet from which the sprite is drwan
     * @param sprite    sprite sheet region (can be null)
     */
    default void drawSprite(Entity entity, GameSpriteSheet spriteSheet, RectArea sprite) {
        if (entity.isVisible()) {
            drawSpriteCenteredOverBox(spriteSheet, sprite, entity.posX(), entity.posY());
        }
    }

    /**
     * Draws animated entity (Pac-Man, ghost, moving bonus) if entity is visible.
     *
     * @param character the animated entity
     */
    default void drawAnimatedEntity(AnimatedEntity character) {
        character.animations().ifPresent(animations -> {
            if (character.isVisible() && animations instanceof SpriteAnimationCollection spriteAnimations) {
                SpriteAnimation currentAnimation = spriteAnimations.current();
                if (currentAnimation != null) {
                    drawSprite(character.entity(), currentAnimation.spriteSheet(), spriteAnimations.currentSprite(character));
                } else {
                    Logger.error("No current animation for character {}", character);
                }
            }
        });
    }

    ObjectProperty<Color> backgroundColorProperty();

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
                String text = animationName + " " + sa.current().frameIndex();
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

    default void drawLivesCounter(GameSpriteSheet spriteSheet, int numLives, int maxLives, Vector2i worldSize) {
        if (numLives == 0) {
            return;
        }
        double x = TS * 2;
        double y = TS * (worldSize.y() - 2);
        for (int i = 0; i < Math.min(numLives, maxLives); ++i) {
            drawSpriteScaled(spriteSheet, spriteSheet.livesCounterSprite(), x + TS * (2 * i), y);
        }
        // show text indicating that more lives are available than symbols displayed (can happen when lives are added via cheat)
        int moreLivesThanSymbols = numLives - maxLives;
        if (moreLivesThanSymbols > 0) {
            Font font = Font.font("Serif", FontWeight.BOLD, scaled(8));
            drawText("+" + moreLivesThanSymbols, Color.YELLOW, font, x + TS * 10, y + TS);
        }
    }

    default void drawLevelCounter(GameSpriteSheet spriteSheet, int levelNumber, List<Byte> symbols, Vector2i worldSize) {
        double x = TS * (worldSize.x() - 4), y = TS * (worldSize.y() - 2);
        for (byte symbol : symbols) {
            drawSpriteScaled(spriteSheet, spriteSheet.bonusSymbolSprite(symbol), x, y);
            x -= TS * 2;
        }
    }

    default void drawScores(GameContext context) {
        drawScore(context.game().score(),     "SCORE",      t(1),  t(1), scaledArcadeFont(TS), ARCADE_PALE);
        drawScore(context.game().highScore(), "HIGH SCORE", t(14), t(1), scaledArcadeFont(TS), ARCADE_PALE);
    }

    default void drawScore(Score score, String title, double x, double y, Font font, Color color) {
        var pointsText = String.format("%02d", score.points());
        drawText(title, color, font, x, y);
        drawText(String.format("%7s", pointsText), color, font, x, y + TS + 1);
        if (score.points() != 0) {
            drawText("L" + score.levelNumber(), color, font, x + t(8), y + TS + 1);
        }
    }

    default void drawTileGrid(Vector2i gridSizeTiles) {
        ctx().setStroke(Color.LIGHTGRAY);
        ctx().setLineWidth(0.2);
        for (int row = 0; row <= gridSizeTiles.y(); ++row) {
            ctx().strokeLine(0, scaled(TS * row), scaled(gridSizeTiles.y() * TS), scaled(TS * row));
        }
        for (int col = 0; col <= gridSizeTiles.x(); ++col) {
            ctx().strokeLine(scaled(TS * col), 0, scaled(TS * col), scaled(gridSizeTiles.y() * TS));
        }
    }

    default void drawMovingBonus(GameSpriteSheet spriteSheet, MovingBonus bonus) {
        ctx().save();
        ctx().translate(0, bonus.elongationY());
        switch (bonus.state()) {
            case Bonus.STATE_EDIBLE -> drawSprite(bonus.entity(), spriteSheet, spriteSheet.bonusSymbolSprite(bonus.symbol()));
            case Bonus.STATE_EATEN  -> drawSprite(bonus.entity(), spriteSheet, spriteSheet.bonusValueSprite(bonus.symbol()));
            default -> {}
        }
        ctx().restore();
    }

    default void drawClapperBoard(GameSpriteSheet spriteSheet,
        Font font, Color textColor,
        ClapperboardAnimation animation,
        double x, double y) {}

    default void drawMsPacManMidwayCopyright(Image image, double x, double y, Color color, Font font) {
        drawImageScaled(image, x, y + 2, t(4) - 2, t(4));
        ctx().setFont(font);
        ctx().setFill(color);
        ctx().fillText("©", scaled(x + TS * 5), scaled(y + TS * 2 + 2));
        ctx().fillText("MIDWAY MFG CO", scaled(x + TS * 7), scaled(y + TS * 2));
        ctx().fillText("1980/1981", scaled(x + TS * 8), scaled(y + TS * 4));
    }
}