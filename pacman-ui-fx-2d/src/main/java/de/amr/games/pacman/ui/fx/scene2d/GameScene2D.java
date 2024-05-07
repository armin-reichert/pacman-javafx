/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui.fx.scene2d;

import de.amr.games.pacman.lib.Score;
import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.model.GameVariants;
import de.amr.games.pacman.model.IllegalGameVariantException;
import de.amr.games.pacman.model.actors.*;
import de.amr.games.pacman.ui.fx.GameScene;
import de.amr.games.pacman.ui.fx.GameSceneContext;
import de.amr.games.pacman.ui.fx.rendering2d.ClapperboardAnimation;
import de.amr.games.pacman.ui.fx.rendering2d.MsPacManGameSpriteSheet;
import de.amr.games.pacman.ui.fx.rendering2d.PacManGameSpriteSheet;
import de.amr.games.pacman.ui.fx.util.SpriteAnimations;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import org.tinylog.Logger;

import static de.amr.games.pacman.lib.Globals.*;

/**
 * Base class of all 2D scenes.
 *
 * @author Armin Reichert
 */
public abstract class GameScene2D implements GameScene {

    public final BooleanProperty infoVisiblePy = new SimpleBooleanProperty(this, "infoVisible", false);
    public final BooleanProperty scoreVisiblePy = new SimpleBooleanProperty(this, "scoreVisible", false);
    public final DoubleProperty scalingPy = new SimpleDoubleProperty(this, "scaling", 1.0);

    protected GameSceneContext context;
    protected GraphicsContext g;

    public abstract boolean isCreditVisible();

    @Override
    public GameSceneContext context() {
        return context;
    }

    @Override
    public void setContext(GameSceneContext context) {
        checkNotNull(context);
        this.context = context;
    }

    public void setCanvas(Canvas canvas) {
        checkNotNull(canvas);
        g = canvas.getGraphicsContext2D();
    }

    public double getScaling() {
        return scalingPy.get();
    }

    public void setScaling(double scaling) {
        if (scaling <= 0) {
            throw new IllegalArgumentException("Scaling value must be positive but is " + scaling);
        }
        scalingPy.set(scaling);
    }

    @Override
    public boolean isScoreVisible() {
        return scoreVisiblePy.get();
    }

    @Override
    public void setScoreVisible(boolean scoreVisible) {
        scoreVisiblePy.set(scoreVisible);
    }

    protected double s(double value) {
        return value * scalingPy.get();
    }

    protected Font sceneFont(double size) {
        return context.theme().font("font.arcade", s(size));
    }

    @Override
    public Node root() {
        return canvas();
    }

    public Canvas canvas() {
        return g != null ? g.getCanvas() : null;
    }

    public void draw() {
        if (g == null) {
            Logger.error("Cannot render game scene {}, no canvas has been assigned",
                getClass().getSimpleName());
            return;
        }
        if (!g.getCanvas().isVisible()) {
            return;
        }
        clearCanvas();
        if (context == null) {
            Logger.error("Cannot render game scene {}, no scene context has been assigned",
                getClass().getSimpleName());
            return;
        }
        if (isScoreVisible()) {
            drawScore(context.game().score(), "SCORE", t(1), t(1));
            drawScore(context.game().highScore(), "HIGH SCORE", t(14), t(1));

        }
        if (isCreditVisible()) {
            drawCredit(context.gameController().credit(), t(2), t(36) - 1);
        }
        drawSceneContent();
        if (infoVisiblePy.get()) {
            drawSceneInfo();
        }
    }

    /**
     * Draws the scene content, e.g. the maze and the guys.
     */
    protected abstract void drawSceneContent();

    /**
     * Draws additional scene info, e.g. tile structure or debug info.
     */
    protected void drawSceneInfo() {
        drawTileGrid(GameModel.ARCADE_MAP_TILES_X, GameModel.ARCADE_MAP_TILES_Y);
    }

    public void clearCanvas() {
        Color fillColor = context.theme() != null ? context.theme().color("canvas.background") : Color.BLACK;
        g.setFill(fillColor);
        g.fillRect(0, 0, g.getCanvas().getWidth(), g.getCanvas().getHeight());
    }

    protected void drawScore(Score score, String title, double x, double y) {
        var pointsText = String.format("%02d", score.points());
        var font = sceneFont(TS);
        drawText(title, context.theme().color("palette.pale"), font, x, y);
        drawText(String.format("%7s", pointsText), context.theme().color("palette.pale"),
            font, x, y + TS + 1);
        if (score.points() != 0) {
            drawText("L" + score.levelNumber(), context.theme().color("palette.pale"),
                font, x + t(8), y + TS + 1);
        }
    }

    protected void drawLevelCounter() {
        double x = t(GameModel.ARCADE_MAP_TILES_X - 4);
        double y = t(GameModel.ARCADE_MAP_TILES_Y - 2);
        for (byte symbol : context.game().levelCounter()) {
            var sprite = switch (context.game()) {
                case GameVariants.MS_PACMAN -> context.<MsPacManGameSpriteSheet>spriteSheet().bonusSymbolSprite(symbol);
                case GameVariants.PACMAN -> context.<PacManGameSpriteSheet>spriteSheet().bonusSymbolSprite(symbol);
                default -> throw new IllegalGameVariantException(context.game());
            };
            drawSprite(sprite, x, y);
            x -= TS * 2;
        }
    }

    protected void drawLivesCounter(int numLivesDisplayed) {
        if (numLivesDisplayed == 0) {
            return;
        }
        var sprite = switch (context.game()) {
            case GameVariants.MS_PACMAN -> context.<MsPacManGameSpriteSheet>spriteSheet().livesCounterSprite();
            case GameVariants.PACMAN -> context.<PacManGameSpriteSheet>spriteSheet().livesCounterSprite();
            default -> throw new IllegalGameVariantException(context.game());
        };
        var x = TS * 2;
        var y = TS * (GameModel.ARCADE_MAP_TILES_Y - 2);
        int maxLives = 5;
        for (int i = 0; i < Math.min(numLivesDisplayed, maxLives); ++i) {
            drawSprite(sprite, x + TS * (2 * i), y);
        }
        // text indicating that more lives are available than displayed
        int excessLives = numLivesDisplayed - maxLives;
        if (excessLives > 0) {
            drawText("+" + excessLives, context.theme().color("palette.yellow"),
                Font.font("Serif", FontWeight.BOLD, s(8)), x + TS * 10, y + TS);
        }
    }

    protected void drawBonus(Bonus bonus) {
        switch (context.game()) {
            case GameVariants.MS_PACMAN -> {
                var ss = context.<MsPacManGameSpriteSheet>spriteSheet();
                if (bonus instanceof MovingBonus movingBonus) {
                    //TODO reconsider this way of implementing the jumping bonus
                    g.save();
                    g.translate(0, movingBonus.elongationY());
                    if (bonus.state() == Bonus.STATE_EDIBLE) {
                        drawEntitySprite(bonus.entity(), ss.bonusSymbolSprite(bonus.symbol()));
                    } else if (bonus.state() == Bonus.STATE_EATEN) {
                        drawEntitySprite(bonus.entity(), ss.bonusValueSprite(bonus.symbol()));
                    }
                    g.restore();
                }
            }
            case GameVariants.PACMAN -> {
                var ss = context.<PacManGameSpriteSheet>spriteSheet();
                if (bonus.state() == Bonus.STATE_EDIBLE) {
                    drawEntitySprite(bonus.entity(), ss.bonusSymbolSprite(bonus.symbol()));
                } else if (bonus.state() == Bonus.STATE_EATEN) {
                    drawEntitySprite(bonus.entity(), ss.bonusValueSprite(bonus.symbol()));
                }
            }
            default -> throw new IllegalGameVariantException(context.game());

        }
    }

    protected void drawPac(Pac pac) {
        if (!pac.isVisible()) {
            return;
        }
        pac.animations().ifPresent(pa -> {
            if (pa instanceof SpriteAnimations animations) {
                drawEntitySprite(pac, animations.currentSprite());
                if (infoVisiblePy.get()) {
                    g.setFill(Color.WHITE);
                    g.setFont(Font.font("Monospaced", s(6)));
                    var text = animations.currentAnimationName() + " " + animations.currentAnimation().frameIndex();
                    g.fillText(text, s(pac.posX() - 4), s(pac.posY() - 4));
                    drawWishDir(pac);
                }
            }
        });
    }

    private void drawWishDir(Creature guy) {
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

    protected void drawGhost(Ghost ghost) {
        if (!ghost.isVisible()) {
            return;
        }
        ghost.animations().ifPresent(ga -> {
            if (ga instanceof SpriteAnimations animations) {
                drawEntitySprite(ghost, animations.currentSprite());
                if (infoVisiblePy.get()) {
                    g.setFill(Color.WHITE);
                    g.setFont(Font.font("Monospaced", s(6)));
                    var text = animations.currentAnimationName() + " " + animations.currentAnimation().frameIndex();
                    g.fillText(text, s(ghost.posX() - 4), s(ghost.posY() - 4));
                    drawWishDir(ghost);
                }
            }
        });
    }

    /**
     * Draws the given image scaled into this scene.
     * @param image image
     * @param x unscaled x
     * @param y unscaled y
     * @param width unscaled width
     * @param height unscaled height
     */
    protected void drawImage(Image image, double x, double y, double width, double height) {
        g.drawImage(image, s(x), s(y), s(width), s(height));
    }

    /**
     * Draws the given image scaled into this scene.
     * @param image image
     * @param x unscaled x
     * @param y unscaled y
     */
    protected void drawImage(Image image, double x, double y) {
        drawImage(image, x, y, image.getWidth(), image.getHeight());
    }

    /**
     * Draws a sprite using the current scene scaling.
     *
     * @param source sprite sheet source
     * @param sprite sprite sheet region ("sprite")
     * @param x      UNSCALED x position
     * @param y      UNSCALED y position
     */
    protected void drawSprite(Image source, Rectangle2D sprite, double x, double y) {
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
    protected void drawSpriteCenteredOverBox(Rectangle2D sprite, double x, double y) {
        drawSprite(sprite, x + HTS - 0.5 * sprite.getWidth(), y + HTS - 0.5 * sprite.getHeight());
    }

    /**
     * Draws a sprite at the given position (upper left corner).
     *
     * @param sprite sprite sheet region ("sprite")
     * @param x      x coordinate of upper left corner
     * @param y      y coordinate of upper left corner
     */
    protected void drawSprite(Rectangle2D sprite, double x, double y) {
        drawSprite(context.spriteSheet().source(), sprite, x, y);
    }

    /**
     * Draws the sprite over the bounding box of the given entity (if visible).
     *
     * @param entity an entity like Pac-Man or a ghost
     * @param sprite the sprite
     */
    protected void drawEntitySprite(Entity entity, Rectangle2D sprite) {
        if (entity.isVisible()) {
            drawSpriteCenteredOverBox(sprite, entity.posX(), entity.posY());
        }
    }

    protected void drawCredit(int credit, double x, double y) {
        drawText(String.format("CREDIT %2d", credit), context.theme().color("palette.pale"), sceneFont(8), x, y);
    }

    protected void drawMidwayCopyright(double x, double y) {
        drawText("© 1980 MIDWAY MFG.CO.", context.theme().color("palette.pink"), sceneFont(8), x, y);
    }

    protected void drawMsPacManCopyright(double x, double y) {
        Image logo = context.theme().get("mspacman.logo.midway");
        drawImage(logo, x, y + 2, TS * 4 - 2, TS * 4);
        g.setFill(context.theme().color("palette.red"));
        g.setFont(sceneFont(8));
        g.fillText("©", s(x + TS * 5), s(y + TS * 2 + 2));
        g.fillText("MIDWAY MFG CO", s(x + TS * 7), s(y + TS * 2));
        g.fillText("1980/1981", s(x + TS * 8), s(y + TS * 4));
    }

    protected void drawMsPacManClapperBoard(ClapperboardAnimation animation, double x, double y) {
        var ss = context.<MsPacManGameSpriteSheet>spriteSheet();
        var sprite = animation.currentSprite(ss.clapperboardSprites());
        if (sprite != null) {
            drawSpriteCenteredOverBox(sprite, x, y);
            g.setFont(sceneFont(8));
            g.setFill(context.theme().color("palette.pale").darker());
            var numberX = s(x + sprite.getWidth() - 25);
            var numberY = s(y + 18);
            g.setFill(context.theme().color("palette.pale"));
            g.fillText(animation.number(), numberX, numberY);
            var textX = s(x + sprite.getWidth());
            g.fillText(animation.text(), textX, numberY);
        }
    }

    protected void drawText(String text, Color color, Font font, double x, double y) {
        g.setFont(font);
        g.setFill(color);
        g.fillText(text, s(x), s(y));
    }

    protected void drawTileGrid(int tilesX, int tilesY) {
        g.setStroke(context.theme().color("palette.pale"));
        g.setLineWidth(0.2);
        for (int row = 0; row <= tilesY; ++row) {
            g.strokeLine(0, s(TS * (row)), s(tilesX * TS), s(TS * (row)));
        }
        for (int col = 0; col <= tilesY; ++col) {
            g.strokeLine(s(TS * (col)), 0, s(TS * (col)), s(tilesY * TS));
        }
    }
}