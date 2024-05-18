/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui.fx.rendering2d;

import de.amr.games.pacman.lib.Vector2i;
import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.model.GameVariant;
import de.amr.games.pacman.model.actors.*;
import de.amr.games.pacman.model.world.World;
import de.amr.games.pacman.ui.fx.util.SpriteAnimations;
import de.amr.games.pacman.ui.fx.util.SpriteSheet;
import de.amr.games.pacman.ui.fx.util.Theme;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
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
public class ClassicWorldRenderer {

    private final DoubleProperty scalingPy = new SimpleDoubleProperty(1);
    private PacManGameSpriteSheet ssPacMan;
    private MsPacManGameSpriteSheet ssMsPacMan;

    public ClassicWorldRenderer(DoubleProperty scalingPy) {
        this.scalingPy.bind(scalingPy);
    }

    public MsPacManGameSpriteSheet getMsPacManSpriteSheet() {
        return ssMsPacMan;
    }

    public PacManGameSpriteSheet getPacManSpriteSheet() {
        return ssPacMan;
    }

    public double s(double value) {
        return scalingPy.doubleValue() * value;
    }

    public void setPacManSpriteSheet(PacManGameSpriteSheet spriteSheet) {
        ssPacMan = spriteSheet;
    }

    public void setMsPacManSpriteSheet(MsPacManGameSpriteSheet spriteSheet) {
        ssMsPacMan = spriteSheet;
    }

    public void drawPacManWorld(GraphicsContext g, World world, boolean flashing, boolean blinkingOn) {
        if (flashing) {
            g.save();
            g.scale(scalingPy.get(), scalingPy.get());
            if (blinkingOn) {
                g.drawImage(ssPacMan.getFlashingMazeImage(), 0, t(3));
            } else {
                drawSprite(g, ssPacMan.source(), ssPacMan.getEmptyMazeSprite(), 0, t(3));
            }
            g.restore();
        } else {
            g.save();
            g.scale(scalingPy.get(), scalingPy.get());
            drawSprite(g, ssPacMan.source(), ssPacMan.getFullMazeSprite(), 0, t(3));
            g.restore();
            world.tiles().filter(world::hasEatenFoodAt).forEach(tile -> hideFoodTileContent(g, world, tile));
            if (!blinkingOn) {
                world.energizerTiles().forEach(tile -> hideFoodTileContent(g, world, tile));
            }
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

    public void drawLivesCounter(GraphicsContext g, GameVariant variant, int numLivesDisplayed) {
        if (numLivesDisplayed == 0) {
            return;
        }
        int maxLives = 5;
        var x = TS * 2;
        var y = TS * (GameModel.ARCADE_MAP_TILES_Y - 2);
        for (int i = 0; i < Math.min(numLivesDisplayed, maxLives); ++i) {
            switch (variant) {
                case MS_PACMAN -> drawSpriteScaled(g, ssMsPacMan.source(), ssMsPacMan.livesCounterSprite(), x + TS * (2 * i), y);
                case PACMAN -> drawSpriteScaled(g, ssPacMan.source(), ssPacMan.livesCounterSprite(), x + TS * (2 * i), y);
            }
        }
        // text indicating that more lives are available than displayed
        int excessLives = numLivesDisplayed - maxLives;
        if (excessLives > 0) {
            drawText(g, "+" + excessLives, Color.YELLOW, Font.font("Serif", FontWeight.BOLD, s(8)), x + TS * 10, y + TS);
        }
    }

    public void drawMsPacManWorld(GraphicsContext g, World world, int mapNumber, boolean flashing, boolean blinkingOn) {
        double x = 0, y = t(3);
        if (flashing) {
            g.save();
            g.scale(scalingPy.get(), scalingPy.get());
            if (blinkingOn) {
                var emptyMazeBright = ssMsPacMan.highlightedMaze(mapNumber);
                drawSprite(g, ssMsPacMan.getFlashingMazesImage(), emptyMazeBright, x - 3, y);
            } else {
                drawSprite(g, ssMsPacMan.source(), ssMsPacMan.emptyMaze(mapNumber), x, y);
            }
            g.restore();
        } else {
            g.save();
            g.scale(scalingPy.get(), scalingPy.get());
            drawSprite(g, ssMsPacMan.source(), ssMsPacMan.filledMaze(mapNumber), x, y);
            g.restore();
            world.tiles().filter(world::hasEatenFoodAt).forEach(tile -> hideFoodTileContent(g, world, tile));
            if (!blinkingOn) {
                world.energizerTiles().forEach(tile -> hideFoodTileContent(g, world, tile));
            }
        }
    }

    public void drawClapperBoard(GraphicsContext g, Theme theme, ClapperboardAnimation animation, double x, double y) {
        var sprite = animation.currentSprite(ssMsPacMan.clapperboardSprites());
        if (sprite != null) {
            drawSpriteCenteredOverBox(g, ssMsPacMan, sprite, x, y);
            g.setFont(theme.font("font.arcade", s(8)));
            g.setFill(theme.color("palette.pale").darker());
            var numberX = s(x + sprite.getWidth() - 25);
            var numberY = s(y + 18);
            g.setFill(theme.color("palette.pale"));
            g.fillText(animation.number(), numberX, numberY);
            var textX = s(x + sprite.getWidth());
            g.fillText(animation.text(), textX, numberY);
        }
    }


    public void drawLevelCounter(GraphicsContext g, GameVariant variant, List<Byte> symbols) {
        double x = t(GameModel.ARCADE_MAP_TILES_X - 4); // TODO
        double y = t(GameModel.ARCADE_MAP_TILES_Y - 2); // TODO
        for (byte symbol : symbols) {
            switch (variant) {
                case MS_PACMAN -> drawSpriteScaled(g, ssMsPacMan.source(), ssMsPacMan.bonusSymbolSprite(symbol), x, y);
                case PACMAN    -> drawSpriteScaled(g, ssPacMan.source(), ssPacMan.bonusSymbolSprite(symbol), x, y);
            }
            x -= TS * 2;
        }
    }

    public void drawPac(GraphicsContext g, GameVariant variant, Pac pac) {
        if (pac.isVisible() && pac.animations().isPresent() && pac.animations().get() instanceof SpriteAnimations sa) {
            switch (variant) {
                case MS_PACMAN -> drawEntitySprite(g, ssMsPacMan, pac, sa.currentSprite());
                case    PACMAN -> drawEntitySprite(g, ssPacMan, pac, sa.currentSprite());
            }
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

    public void drawGhost(GraphicsContext g, GameVariant variant, Ghost ghost) {
        if (!ghost.isVisible()) {
            return;
        }
        ghost.animations().ifPresent(ga -> {
            if (ga instanceof SpriteAnimations animations) {
                switch (variant) {
                    case MS_PACMAN -> drawEntitySprite(g, ssMsPacMan, ghost, animations.currentSprite());
                    case    PACMAN -> drawEntitySprite(g, ssPacMan, ghost, animations.currentSprite());
                }
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
            var indicatorCenter = guy.center().plus(guy.wishDir().vector().toFloatVec().scaled(1.5f * TS));
            var indicatorTopLeft = indicatorCenter.minus(r, r);
            g.setStroke(Color.WHITE);
            g.strokeLine(s(pacCenter.x()), s(pacCenter.y()), s(indicatorCenter.x()), s(indicatorCenter.y()));
            g.setFill(guy.isNewTileEntered() ? Color.YELLOW : Color.GREEN);
            g.fillOval(s(indicatorTopLeft.x()), s(indicatorTopLeft.y()), s(2 * r), s(2 * r));
        }
    }

    public void drawBonus(GraphicsContext g, GameVariant variant, Bonus bonus) {
        switch (variant) {
            case MS_PACMAN -> {
                if (bonus instanceof MovingBonus movingBonus) {
                    //TODO reconsider this way of implementing the jumping bonus
                    g.save();
                    g.translate(0, movingBonus.elongationY());
                    if (bonus.state() == Bonus.STATE_EDIBLE) {
                        drawEntitySprite(g, ssMsPacMan, bonus.entity(), ssMsPacMan.bonusSymbolSprite(bonus.symbol()));
                    } else if (bonus.state() == Bonus.STATE_EATEN) {
                        drawEntitySprite(g, ssMsPacMan, bonus.entity(), ssMsPacMan.bonusValueSprite(bonus.symbol()));
                    }
                    g.restore();
                }
            }
            case PACMAN -> {
                if (bonus.state() == Bonus.STATE_EDIBLE) {
                    drawEntitySprite(g, ssPacMan, bonus.entity(), ssPacMan.bonusSymbolSprite(bonus.symbol()));
                } else if (bonus.state() == Bonus.STATE_EATEN) {
                    drawEntitySprite(g, ssPacMan, bonus.entity(), ssPacMan.bonusValueSprite(bonus.symbol()));
                }
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
     *
     * @param image  image
     * @param x      unscaled x
     * @param y      unscaled y
     * @param width  unscaled width
     * @param height unscaled height
     */
    public void drawImageScaled(GraphicsContext g, Image image, double x, double y, double width, double height) {
        g.drawImage(image, s(x), s(y), s(width), s(height));
    }

    public void drawText(GraphicsContext g, String text, Color color, Font font, double x, double y) {
        g.setFont(font);
        g.setFill(color);
        g.fillText(text, s(x), s(y));
    }
}