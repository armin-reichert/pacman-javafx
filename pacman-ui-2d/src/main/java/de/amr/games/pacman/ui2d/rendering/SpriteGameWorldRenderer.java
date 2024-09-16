/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d.rendering;

import de.amr.games.pacman.lib.Vector2i;
import de.amr.games.pacman.model.GameWorld;
import de.amr.games.pacman.model.actors.*;
import de.amr.games.pacman.ui2d.rendering.ms_pacman.ClapperboardAnimation;
import de.amr.games.pacman.ui2d.util.SpriteAnimations;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
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

    @SuppressWarnings("unchecked")
    public <T extends GameSpriteSheet> T spriteSheet() {
        return (T) spriteSheet;
    }

    public void setSpriteSheet(GameSpriteSheet spriteSheet) {
        this.spriteSheet = checkNotNull(spriteSheet);
    }

    public void overpaintFood(GraphicsContext g, GameWorld world, Vector2i tile) {
        double cx = t(tile.x()) + HTS;
        double cy = t(tile.y()) + HTS;
        double s = scalingPy.get();
        double r = world.isEnergizerPosition(tile) ? 4.5 : 2;
        g.setFill(backgroundColorPy.get());
        g.fillRect(s * (cx - r), s * (cy - r), s * (2 * r), s * (2 * r));
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
    public void drawSprite(GraphicsContext g, SpriteArea sprite, double x, double y) {
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
    public void drawSubImage(GraphicsContext g, Image sourceImage, SpriteArea sprite, double x, double y) {
        if (sprite != null) {
            g.drawImage(sourceImage,
                sprite.x(), sprite.y(), sprite.width(), sprite.height(),
                x, y, sprite.width(), sprite.height());
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
    public void drawSpriteScaled(GraphicsContext g, SpriteArea sprite, double x, double y) {
        if (sprite != null) {
            g.drawImage(spriteSheet.source(),
                sprite.x(), sprite.y(), sprite.width(), sprite.height(),
                s(x), s(y), s(sprite.width()), s(sprite.height()));
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
    public void drawSpriteCenteredOverBox(GraphicsContext g, SpriteArea sprite, double x, double y) {
        drawSpriteScaled(g, sprite, x + HTS - 0.5 * sprite.width(), y + HTS - 0.5 * sprite.height());
    }

    /**
     * Draws the sprite over the bounding box of the given entity (if visible).
     *
     * @param g         graphics context
     * @param entity    an entity like Pac-Man or a ghost
     * @param sprite    sprite sheet region (can be null)
     */
    public void drawEntitySprite(GraphicsContext g, Entity entity, SpriteArea sprite) {
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