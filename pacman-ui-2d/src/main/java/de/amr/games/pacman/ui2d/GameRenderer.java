/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d;

import de.amr.games.pacman.lib.RectArea;
import de.amr.games.pacman.lib.Vector2f;
import de.amr.games.pacman.lib.Vector2i;
import de.amr.games.pacman.lib.arcade.Arcade;
import de.amr.games.pacman.lib.tilemap.WorldMap;
import de.amr.games.pacman.model.GameWorld;
import de.amr.games.pacman.model.Score;
import de.amr.games.pacman.model.actors.AnimatedEntity;
import de.amr.games.pacman.model.actors.Bonus;
import de.amr.games.pacman.model.actors.Creature;
import de.amr.games.pacman.model.actors.Entity;
import de.amr.games.pacman.ui2d.assets.AssetStorage;
import de.amr.games.pacman.ui2d.assets.GameSpriteSheet;
import de.amr.games.pacman.ui2d.assets.SpriteAnimation;
import de.amr.games.pacman.ui2d.assets.SpriteAnimationCollection;
import javafx.beans.property.DoubleProperty;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import org.tinylog.Logger;

import java.util.function.Predicate;

import static de.amr.games.pacman.lib.Globals.*;
import static java.util.function.Predicate.not;

/**
 * @author Armin Reichert
 */
public interface GameRenderer {

    default void setWorldMap(WorldMap worldMap) {}
    AssetStorage assets();
    Color backgroundColor();
    void setBackgroundColor(Color color);
    GameSpriteSheet spriteSheet();
    Canvas canvas();
    default GraphicsContext ctx() { return canvas().getGraphicsContext2D(); }
    DoubleProperty scalingProperty();
    default void setScaling(double value) { scalingProperty().set(value); }
    default double scaling() { return scalingProperty().get(); }
    default double scaled(double value) { return scaling() * value; }
    default Font scaledArcadeFont(double size) {
        return assets().font("font.arcade", scaled(size));
    }

    /**
     * @param position Unscaled position where message (READY etc.) is displayed.
     *                 Message text is centered horizontally to the x-coordinate
     *                 and text baseline is y-coordinate of given position
     */
    public void setMessagePosition(Vector2f position);

    /**
     * @return Unscaled position where message (READY etc.) is displayed.
     *         Message text is centered horizontally to the x-coordinate
     *         and text baseline is y-coordinate of given position
     */
    Vector2f getMessagePosition();

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
     * @param sprite        the sprite sheet section to draw
     * @param x             scaled x-coordinate
     * @param y             scaled y-coordinate
     */
    default void drawSpriteUnscaled(RectArea sprite, double x, double y) {
        drawSubImage(spriteSheet().sourceImage(), sprite, x, y);
    }

    /**
     * Draws a sprite using the current scene scaling.
     *
     * @param sprite    sprite sheet region ("sprite")
     * @param x         UNSCALED x position
     * @param y         UNSCALED y position
     */
    default void drawSpriteScaled(RectArea sprite, double x, double y) {
        drawSubImageScaled(spriteSheet().sourceImage(), sprite, x, y);
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
     * Draws a sprite centered over a tile (e.g. the collision box of creature).
     * <p>
     * The position specifies the left-upper corner of the tile. Note that the sprites for
     * Pac-Man and the ghosts are larger than one tile but their collision box is exactly one tile large.
     * </p>
     *
     * @param sprite sprite (region in sprite sheet) (may be null)
     * @param tileX  x-coordinate of left-upper corner of bounding box
     * @param tileY  y-coordinate of left-upper corner of bounding box
     */
    default void drawSpriteCenteredOverTile(RectArea sprite, double tileX, double tileY) {
        drawSpriteCenteredOverPosition(sprite, tileX + HTS, tileY + HTS);
    }

    /**
     * Draws a sprite centered over a position.
     *
     * @param sprite sprite (region in sprite sheet) (may be null)
     * @param x  x-coordinate of the position
     * @param y  y-coordinate of the position
     */
    default void drawSpriteCenteredOverPosition(RectArea sprite, double x, double y) {
        drawSpriteScaled(sprite, x - 0.5 * sprite.width(), y - 0.5 * sprite.height());
    }

    /**
     * Draws the sprite over the collision box (one tile large) of the given entity (if visible).
     *
     * @param entity an entity e.g. Pac-Man or a ghost
     * @param sprite sprite sheet region (can be null)
     */
    default void drawEntitySprite(Entity entity, RectArea sprite) {
        checkNotNull(entity);
        if (entity.isVisible() && sprite != null) {
            drawSpriteCenteredOverTile(sprite, entity.posX(), entity.posY());
        }
    }

    /**
     * Draws animated entity (Pac-Man, ghost, moving bonus) if entity is visible.
     *
     * @param character the animated entity
     */
    default void drawAnimatedEntity(AnimatedEntity character) {
        if (character == null || !character.isVisible()) {
            return;
        }
        character.optAnimations().ifPresent(animations -> {
            if (animations instanceof SpriteAnimationCollection spriteAnimations) {
                SpriteAnimation currentAnimation = spriteAnimations.currentAnimation();
                if (currentAnimation != null) {
                    drawEntitySprite(character.entity(), spriteAnimations.currentSprite(character));
                } else {
                    Logger.error("No current animation for character {}", character);
                }
            }
        });
    }

    void drawBonus(Bonus bonus);

    void drawWorld(GameWorld world, double x, double y);

    void setMazeHighlighted(boolean on);

    void setBlinking(boolean on);

    default void clearCanvas() {
        ctx().setFill(backgroundColor());
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
        world.energizerTiles().filter(condition).forEach(tile -> overPaint(tile, 10));
    }

    /**
     * Draws a square of the given size in background color over the tile. Used to hide eaten food and energizers.
     * Assumes to be called in scaled graphics context!
     */
    private void overPaint(Vector2i tile, double squareSize) {
        double centerX = tile.x() * TS + HTS, centerY = tile.y() * TS + HTS;
        ctx().setFill(backgroundColor());
        ctx().fillRect(centerX - 0.5 * squareSize, centerY - 0.5 * squareSize, squareSize, squareSize);
    }

    /**
     * @param animatedCreature animated entity (must contain an entity of type {@link Creature}
     */
    default void drawAnimatedCreatureInfo(AnimatedEntity animatedCreature) {
        animatedCreature.optAnimations().map(SpriteAnimationCollection.class::cast).ifPresent(animations -> {
            var guy = (Creature) animatedCreature.entity();
            String animID = animations.currentAnimationID();
            if (animID != null) {
                String text = animID + " " + animations.currentAnimation().frameIndex();
                ctx().setFill(Color.WHITE);
                ctx().setFont(Font.font("Monospaced", scaled(6)));
                ctx().fillText(text, scaled(guy.posX() - 4), scaled(guy.posY() - 4));
            }
            if (guy.wishDir() != null) {
                float scaling = (float) scaling();
                Vector2f arrowHead = guy.center().plus(guy.wishDir().vector().scaled(12f)).scaled(scaling);
                Vector2f guyCenter = guy.center().scaled(scaling);
                float radius = scaling * 2, diameter = 2 * radius;
                ctx().setStroke(Color.WHITE);
                ctx().setLineWidth(0.5);
                ctx().strokeLine(guyCenter.x(), guyCenter.y(), arrowHead.x(), arrowHead.y());
                ctx().setFill(guy.isNewTileEntered() ? Color.YELLOW : Color.GREEN);
                ctx().fillOval(arrowHead.x() - radius, arrowHead.y() - radius, diameter, diameter);
            }
        });
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
            drawText("+" + moreLivesThanSymbols, Color.YELLOW, font, x + TS * 10, y + TS);
        }
    }

    default void drawLevelCounter(GameContext context, double x, double y) {
//        double x = sceneSize.x() - 4 * TS, y = sceneSize.y() - 2 * TS;
        for (byte symbol : context.game().levelCounter()) {
            drawSpriteScaled(spriteSheet().bonusSymbolSprite(symbol), x, y);
            x -= TS * 2;
        }
    }

    default void drawScores(GameContext context) {
        Color color = Color.valueOf(Arcade.Palette.WHITE);
        Font font = scaledArcadeFont(TS);
        drawScore(context.game().scoreManager().score(),     "SCORE",      t(1),  t(1), font, color);
        drawScore(context.game().scoreManager().highScore(), "HIGH SCORE", t(14), t(1), font, color);
    }

    default void drawScore(Score score, String title, double x, double y, Font font, Color color) {
        var pointsText = String.format("%02d", score.points());
        drawText(title, color, font, x, y);
        drawText(String.format("%7s", pointsText), color, font, x, y + TS + 1);
        if (score.points() != 0) {
            drawText("L" + score.levelNumber(), color, font, x + t(8), y + TS + 1);
        }
    }

    default void drawTileGrid(double sizeX, double sizeY) {
        double thin = 0.2, medium = 0.4, thick = 0.8;
        int numCols = (int) (sizeX / TS), numRows = (int) (sizeY / TS);
        double width = scaled(numCols * TS), height = scaled(numRows * TS);
        ctx().setStroke(Color.LIGHTGRAY);
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
    }
}