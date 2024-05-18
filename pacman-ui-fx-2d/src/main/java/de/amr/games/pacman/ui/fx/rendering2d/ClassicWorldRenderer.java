/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui.fx.rendering2d;

import de.amr.games.pacman.lib.Vector2i;
import de.amr.games.pacman.model.world.World;
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

    public void hideFoodTileContent(GraphicsContext g, World world, Vector2i tile) {
        double r = world.isEnergizerTile(tile) ? 4.5 : 2;
        double cx = t(tile.x()) + HTS;
        double cy = t(tile.y()) + HTS;
        double s = scalingPy.get();
        g.setFill(Color.BLACK);
        g.fillRect(s * (cx - r), s * (cy - r), s * (2 * r), s * (2 * r));
    }
}
