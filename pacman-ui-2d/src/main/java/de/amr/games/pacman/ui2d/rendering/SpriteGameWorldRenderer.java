/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d.rendering;

import de.amr.games.pacman.lib.Vector2i;
import de.amr.games.pacman.model.GameWorld;
import de.amr.games.pacman.model.actors.*;
import de.amr.games.pacman.ui2d.util.SpriteAnimations;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.List;

import static de.amr.games.pacman.lib.Globals.*;

/**
 * @author Armin Reichert
 */
public class SpriteGameWorldRenderer {

    public final DoubleProperty scalingPy = new SimpleDoubleProperty(1);
    public final ObjectProperty<Color> backgroundColorPy = new SimpleObjectProperty<>(this, "backgroundColor", Color.BLACK);

    private GameSpriteSheet spriteSheet;

    public double s(double value) {
        return scalingPy.doubleValue() * value;
    }

    public GameSpriteSheet spriteSheet() {
        return spriteSheet;
    }

    public void setSpriteSheet(GameSpriteSheet spriteSheet) {
        this.spriteSheet = checkNotNull(spriteSheet);
    }

    public void drawPacManWorld(GraphicsContext g, GameWorld world, int tileX, int tileY, boolean flashing, boolean brightPhase) {
        if (flashing) {
            drawPacManWorldFlashing(g, tileX, tileY, brightPhase);
        } else {
            g.save();
            g.scale(scalingPy.get(), scalingPy.get());
            drawSprite(g, spriteSheet.getFullMazeSprite(), t(tileX), t(tileY));
            g.restore();
            world.map().food().tiles().filter(world::hasEatenFoodAt).forEach(tile -> overpaintFood(g, world, tile));
            if (!brightPhase) {
                world.energizerTiles().forEach(tile -> overpaintFood(g, world, tile));
            }
        }
    }

    private void drawPacManWorldFlashing(GraphicsContext g, int tileX, int tileY, boolean brightPhase) {
        g.save();
        g.scale(scalingPy.get(), scalingPy.get());
        if (brightPhase) {
            // bright maze is in separate image, not in sprite sheet
            g.drawImage(spriteSheet.getFlashingMazeImage(), t(tileX), t(tileY));
        } else {
            drawSprite(g, spriteSheet.getEmptyMazeSprite(), t(tileX), t(tileY));
        }
        g.restore();
    }

    private void overpaintFood(GraphicsContext g, GameWorld world, Vector2i tile) {
        double cx = t(tile.x()) + HTS;
        double cy = t(tile.y()) + HTS;
        double s = scalingPy.get();
        double r = world.isEnergizerPosition(tile) ? 4.5 : 2;
        g.setFill(backgroundColorPy.get());
        g.fillRect(s * (cx - r), s * (cy - r), s * (2 * r), s * (2 * r));
    }

    public void drawLivesCounter(GraphicsContext g, int numLivesDisplayed, int tileY) {
        if (numLivesDisplayed == 0) {
            return;
        }
        int maxLives = 5;
        var x = TS * 2;
        var y = TS * tileY;
        for (int i = 0; i < Math.min(numLivesDisplayed, maxLives); ++i) {
            drawSpriteScaled(g, spriteSheet.livesCounterSprite(), x + TS * (2 * i), y);
        }
        // text indicating that more lives are available than displayed
        int excessLives = numLivesDisplayed - maxLives;
        if (excessLives > 0) {
            drawText(g, "+" + excessLives, Color.YELLOW, Font.font("Serif", FontWeight.BOLD, s(8)), x + TS * 10, y + TS);
        }
    }

    public void drawMsPacManWorld(GraphicsContext g, GameWorld world, int mapNumber, boolean flashing, boolean blinkingOn) {
        double x = 0, y = t(3);
        if (flashing) {
            g.save();
            g.scale(scalingPy.get(), scalingPy.get());
            if (blinkingOn) {
                Rectangle2D emptyMazeBright = spriteSheet.highlightedMaze(mapNumber);
                drawSubImage(g, spriteSheet.getFlashingMazesImage(), emptyMazeBright, x - 3, y);
            } else {
                drawSprite(g, spriteSheet.emptyMaze(mapNumber), x, y);
            }
            g.restore();
        } else {
            g.save();
            g.scale(scalingPy.get(), scalingPy.get());
            drawSprite(g, spriteSheet.filledMaze(mapNumber), x, y);
            g.restore();
            world.map().food().tiles().filter(world::hasEatenFoodAt).forEach(tile -> overpaintFood(g, world, tile));
            if (!blinkingOn) {
                world.energizerTiles().forEach(tile -> overpaintFood(g, world, tile));
            }
        }
    }

    public void drawClapperBoard(
        GraphicsContext g,
        Font font, Color textColor,
        ClapperboardAnimation animation, double x, double y)
    {
        var sprite = animation.currentSprite(spriteSheet.clapperboardSprites());
        if (sprite != null) {
            drawSpriteCenteredOverBox(g, sprite, x, y);
            g.setFont(font);
            g.setFill(textColor.darker());
            var numberX = s(x + sprite.getWidth() - 25);
            var numberY = s(y + 18);
            g.setFill(textColor);
            g.fillText(animation.number(), numberX, numberY);
            var textX = s(x + sprite.getWidth());
            g.fillText(animation.text(), textX, numberY);
        }
    }

    public void drawLevelCounter(GraphicsContext g, List<Byte> symbols, double x, double y) {
        for (byte symbol : symbols) {
            drawSpriteScaled(g, spriteSheet.bonusSymbolSprite(symbol), x, y);
            x -= TS * 2;
        }
    }

    public void drawPac(GraphicsContext g, Pac pac) {
        if (pac.isVisible() && pac.animations().isPresent() && pac.animations().get() instanceof SpriteAnimations sa) {
            drawEntitySprite(g, pac, sa.currentSprite());
        }
    }

    public void drawPacInfo(GraphicsContext g, Pac pac) {
        if (pac.animations().isPresent() && pac.animations().get() instanceof SpriteAnimations sa) {
            if (sa.currentAnimationName() != null) {
                var text = sa.currentAnimationName() + " " + sa.currentAnimation().frameIndex();
                g.setFill(Color.WHITE);
                g.setFont(Font.font("Monospaced", s(6)));
                g.fillText(text, s(pac.posX() - 4), s(pac.posY() - 4));
            }
            drawWishDir(g, pac);
        }
    }

    public void drawGhost(GraphicsContext g, Ghost ghost) {
        if (!ghost.isVisible()) {
            return;
        }
        ghost.animations().ifPresent(ga -> {
            if (ga instanceof SpriteAnimations animations) {
                drawEntitySprite(g,  ghost, animations.currentSprite());
            }
        });
    }

    public void drawGhostInfo(GraphicsContext g, Ghost ghost) {
        if (ghost.animations().isPresent() && ghost.animations().get() instanceof SpriteAnimations sa) {
            if (sa.currentAnimationName() != null) {
                var text = sa.currentAnimationName() + " " + sa.currentAnimation().frameIndex();
                g.setFill(Color.WHITE);
                g.setFont(Font.font("Monospaced", s(6)));
                g.fillText(text, s(ghost.posX() - 4), s(ghost.posY() - 4));
            }
        }
        drawWishDir(g, ghost);
    }

    private void drawWishDir(GraphicsContext g, Creature guy) {
        if (guy.wishDir() != null) {
            float r = 2;
            var pacCenter = guy.center();
            var indicatorCenter = guy.center().plus(guy.wishDir().vector().toVector2f().scaled(1.5f * TS));
            var indicatorTopLeft = indicatorCenter.minus(r, r);
            g.setStroke(Color.WHITE);
            g.strokeLine(s(pacCenter.x()), s(pacCenter.y()), s(indicatorCenter.x()), s(indicatorCenter.y()));
            g.setFill(guy.isNewTileEntered() ? Color.YELLOW : Color.GREEN);
            g.fillOval(s(indicatorTopLeft.x()), s(indicatorTopLeft.y()), s(2 * r), s(2 * r));
        }
    }

    /**
     * Draws a moving bonus entity at its current position (including jump offset).
     * TODO reconsider this way of implementing the jumping bonus
     *
     * @param g     graphics context
     * @param movingBonus moving bonus entity
     */
    public void drawMovingBonus(GraphicsContext g, MovingBonus movingBonus) {
        g.save();
        g.translate(0, movingBonus.elongationY());
        if (movingBonus.state() == Bonus.STATE_EDIBLE) {
            drawEntitySprite(g,  movingBonus.entity(), spriteSheet.bonusSymbolSprite(movingBonus.symbol()));
        } else if (movingBonus.state() == Bonus.STATE_EATEN) {
            drawEntitySprite(g, movingBonus.entity(), spriteSheet.bonusValueSprite(movingBonus.symbol()));
        }
        g.restore();
    }

    /**
     * Draws a static bonus entity.
     *
     * @param g     graphics context
     * @param bonus bonus containing entity
     */
    public void drawStaticBonus(GraphicsContext g, Bonus bonus)
    {
        if (bonus.state() == Bonus.STATE_EDIBLE) {
            drawEntitySprite(g,  bonus.entity(), spriteSheet.bonusSymbolSprite(bonus.symbol()));
        } else if (bonus.state() == Bonus.STATE_EATEN) {
            drawEntitySprite(g,  bonus.entity(), spriteSheet.bonusValueSprite(bonus.symbol()));
        }
    }

    /**
     * Draws a sprite (section of the sprite sheet source) at the given (scaled) position.
     *
     * @param g             graphics context
     * @param sprite        the sprite sheet section to draw
     * @param x             scaled x-coordinate
     * @param y             scaled y-coordinate
     */
    public void drawSprite(GraphicsContext g, Rectangle2D sprite, double x, double y) {
        drawSubImage(g,  spriteSheet.source(), sprite, x, y);
    }

    /**
     * Draws a section of an image at the given (scaled) position.
     *
     * @param g             graphics context
     * @param sourceImage   the source image e.g. a spritesheet source
     * @param sprite        the sprite sheet section to draw
     * @param x             scaled x-coordinate
     * @param y             scaled y-coordinate
     */
    public void drawSubImage(GraphicsContext g, Image sourceImage, Rectangle2D sprite, double x, double y) {
        if (sprite != null) {
            g.drawImage(sourceImage,
                sprite.getMinX(), sprite.getMinY(), sprite.getWidth(), sprite.getHeight(),
                x, y, sprite.getWidth(), sprite.getHeight());
        }
    }

    /**
     * Draws a sprite using the current scene scaling.
     *
     * @param g         graphics context
     * @param sprite    sprite sheet region ("sprite")
     * @param x         UNSCALED x position
     * @param y         UNSCALED y position
     */
    public void drawSpriteScaled(GraphicsContext g, Rectangle2D sprite, double x, double y) {
        if (sprite != null) {
            g.drawImage(spriteSheet.source(),
                sprite.getMinX(), sprite.getMinY(), sprite.getWidth(), sprite.getHeight(),
                s(x), s(y), s(sprite.getWidth()), s(sprite.getHeight()));
        }
    }

    /**
     * Draws a sprite centered over a one "square tile" large box (bounding box of creature). The position specifies the
     * left-upper corner of the bounding box. Note that the sprites for Pac-Man and the ghosts are 16 pixels wide but the
     * bounding box is only 8 pixels (one square tile) wide.
     *
     * @param g         graphics context
     * @param sprite    sprite sheet region (can be null)
     * @param x         x-coordinate of left-upper corner of bounding box
     * @param y         y-coordinate of left-upper corner of bounding box
     */
    public void drawSpriteCenteredOverBox(GraphicsContext g, Rectangle2D sprite, double x, double y) {
        drawSpriteScaled(g, sprite, x + HTS - 0.5 * sprite.getWidth(), y + HTS - 0.5 * sprite.getHeight());
    }

    /**
     * Draws the sprite over the bounding box of the given entity (if visible).
     *
     * @param g         graphics context
     * @param entity    an entity like Pac-Man or a ghost
     * @param sprite    sprite sheet region (can be null)
     */
    public void drawEntitySprite(GraphicsContext g, Entity entity, Rectangle2D sprite) {
        if (entity.isVisible()) {
            drawSpriteCenteredOverBox(g,  sprite, entity.posX(), entity.posY());
        }
    }

    /**
     * Draws the given image scaled by the current scaling value.
     *
     * @param g         graphics context
     * @param image     image
     * @param x         unscaled x-coordinate
     * @param y         unscaled y-coordinate
     * @param width     unscaled width
     * @param height    unscaled height
     */
    public void drawImageScaled(GraphicsContext g, Image image, double x, double y, double width, double height) {
        g.drawImage(image, s(x), s(y), s(width), s(height));
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
    public void drawText(GraphicsContext g, String text, Color color, Font font, double x, double y) {
        g.setFont(font);
        g.setFill(color);
        g.fillText(text, s(x), s(y));
    }
}