/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui.fx.rendering2d;

import de.amr.games.pacman.lib.Vector2i;
import de.amr.games.pacman.model.actors.Entity;
import de.amr.games.pacman.model.world.World;
import de.amr.games.pacman.ui.fx.util.SpriteSheet;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

import static de.amr.games.pacman.lib.Globals.HTS;
import static de.amr.games.pacman.lib.Globals.t;

/**
 * @author Armin Reichert
 */
public class ClassicWorldRenderer {

    private final DoubleProperty scalingPy = new SimpleDoubleProperty(1);
    private PacManGameSpriteSheet spriteSheetPacMan;
    private MsPacManGameSpriteSheet spriteSheetMsPacMan;

    public ClassicWorldRenderer(DoubleProperty scalingPy) {
        this.scalingPy.bind(scalingPy);
    }

    public double s(double value) {
        return scalingPy.doubleValue() * value;
    }

    public void setPacManSpriteSheet(PacManGameSpriteSheet spriteSheet) {
        spriteSheetPacMan = spriteSheet;
    }

    public void setMsPacManSpriteSheet(MsPacManGameSpriteSheet spriteSheet) {
        spriteSheetMsPacMan = spriteSheet;
    }

    public void drawPacManWorld(GraphicsContext g, World world, boolean flashing, boolean blinkingOn) {
        if (flashing) {
            g.save();
            g.scale(scalingPy.get(), scalingPy.get());
            if (blinkingOn) {
                g.drawImage(spriteSheetPacMan.getFlashingMazeImage(), 0, t(3));
            } else {
                drawSprite(g, spriteSheetPacMan.source(), spriteSheetPacMan.getEmptyMazeSprite(), 0, t(3));
            }
            g.restore();
        } else {
            g.save();
            g.scale(scalingPy.get(), scalingPy.get());
            drawSprite(g, spriteSheetPacMan.source(), spriteSheetPacMan.getFullMazeSprite(), 0, t(3));
            g.restore();
            world.tiles().filter(world::hasEatenFoodAt).forEach(tile -> hideFoodTileContent(g, world, tile));
            if (!blinkingOn) {
                world.energizerTiles().forEach(tile -> hideFoodTileContent(g, world, tile));
            }
        }
    }

    public void drawMsPacManWorld(GraphicsContext g, World world, int mapNumber, boolean flashing, boolean blinkingOn) {
        double x = 0, y = t(3);
        if (flashing) {
            g.save();
            g.scale(scalingPy.get(), scalingPy.get());
            if (blinkingOn) {
                var emptyMazeBright = spriteSheetMsPacMan.highlightedMaze(mapNumber);
                drawSprite(g, spriteSheetMsPacMan.getFlashingMazesImage(), emptyMazeBright, x - 3, y);
            } else {
                drawSprite(g, spriteSheetMsPacMan.source(), spriteSheetMsPacMan.emptyMaze(mapNumber), x, y);
            }
            g.restore();
        } else {
            g.save();
            g.scale(scalingPy.get(), scalingPy.get());
            drawSprite(g, spriteSheetMsPacMan.source(), spriteSheetMsPacMan.filledMaze(mapNumber), x, y);
            g.restore();
            world.tiles().filter(world::hasEatenFoodAt).forEach(tile -> hideFoodTileContent(g, world, tile));
            if (!blinkingOn) {
                world.energizerTiles().forEach(tile -> hideFoodTileContent(g, world, tile));
            }
        }
    }

    public void drawSprite(GraphicsContext g, Image sourceImage, Rectangle2D sprite, double x, double y) {
        if (sprite != null) {
            g.drawImage(sourceImage,
                sprite.getMinX(), sprite.getMinY(), sprite.getWidth(), sprite.getHeight(),
                x, y, sprite.getWidth(), sprite.getHeight());
        }
    }

    /**
     * Draws a sprite using the current scene scaling.
     *
     * @param source sprite sheet source
     * @param sprite sprite sheet region ("sprite")
     * @param x      UNSCALED x position
     * @param y      UNSCALED y position
     */
    public void drawSpriteScaled(GraphicsContext g, Image source, Rectangle2D sprite, double x, double y) {
        if (sprite != null) {
            g.drawImage(source,
                sprite.getMinX(), sprite.getMinY(), sprite.getWidth(), sprite.getHeight(),
                s(x), s(y), s(sprite.getWidth()), s(sprite.getHeight()));
        }
    }

    /**
     * Draws a sprite centered over a one "square tile" large box (bounding box of creature). The position specifies the
     * left-upper corner of the bounding box. Note that the sprites for Pac-Man and the ghosts are 16 pixels wide but the
     * bounding box is only 8 pixels (one square tile) wide.
     *
     * @param sprite sprite sheet region (can be null)
     * @param x      x coordinate of left-upper corner of bounding box
     * @param y      y coordinate of left-upper corner of bounding box
     */
    public void drawSpriteCenteredOverBox(GraphicsContext g, SpriteSheet ss, Rectangle2D sprite, double x, double y) {
        drawSpriteScaled(g, ss.source(), sprite, x + HTS - 0.5 * sprite.getWidth(), y + HTS - 0.5 * sprite.getHeight());
    }

    /**
     * Draws the sprite over the bounding box of the given entity (if visible).
     *
     * @param entity an entity like Pac-Man or a ghost
     * @param sprite the sprite
     */
    public void drawEntitySprite(GraphicsContext g, SpriteSheet ss, Entity entity, Rectangle2D sprite) {
        if (entity.isVisible()) {
            drawSpriteCenteredOverBox(g, ss, sprite, entity.posX(), entity.posY());
        }
    }

    /**
     * Draws the given image scaled into this scene.
     * @param image image
     * @param x unscaled x
     * @param y unscaled y
     * @param width unscaled width
     * @param height unscaled height
     */
    public void drawImage(GraphicsContext g, Image image, double x, double y, double width, double height) {
        g.drawImage(image, s(x), s(y), s(width), s(height));
    }

    /**
     * Draws the given image scaled into this scene.
     * @param image image
     * @param x unscaled x
     * @param y unscaled y
     */
    public void drawImage(GraphicsContext g, Image image, double x, double y) {
        drawImage(g, image, x, y, image.getWidth(), image.getHeight());
    }

    public void hideFoodTileContent(GraphicsContext g, World world, Vector2i tile) {
        double r = world.isEnergizerTile(tile) ? 4.5 : 2;
        double cx = t(tile.x()) + HTS;
        double cy = t(tile.y()) + HTS;
        double s = scalingPy.get();
        g.setFill(Color.BLACK);
        g.fillRect(s * (cx - r), s * (cy - r), s * (2 * r), s * (2 * r));
    }
}
