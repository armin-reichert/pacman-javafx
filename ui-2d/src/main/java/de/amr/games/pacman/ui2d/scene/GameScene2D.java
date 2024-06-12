/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d.scene;

import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.model.Score;
import de.amr.games.pacman.ui2d.rendering.SpriteGameRenderer;
import de.amr.games.pacman.ui2d.rendering.VectorGraphicsWorldRenderer;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import org.tinylog.Logger;

import static de.amr.games.pacman.lib.Globals.*;

/**
 * Base class of all 2D scenes.
 *
 * @author Armin Reichert
 */
public abstract class GameScene2D implements GameScene {

    public final BooleanProperty infoVisiblePy  = new SimpleBooleanProperty(this, "infoVisible", false);
    public final BooleanProperty scoreVisiblePy = new SimpleBooleanProperty(this, "scoreVisible", false);
    public final DoubleProperty scalingPy       = new SimpleDoubleProperty(this, "scaling", 1.0);

    protected final VectorGraphicsWorldRenderer vectorRenderer = new VectorGraphicsWorldRenderer();
    protected final SpriteGameRenderer spriteRenderer = new SpriteGameRenderer();

    protected GameContext context;
    protected GraphicsContext g;

    public GameScene2D() {
        vectorRenderer.scalingPy.bind(scalingPy);
        spriteRenderer.scalingPy.bind(scalingPy);
    }

    public abstract boolean isCreditVisible();

    @Override
    public GameContext context() {
        return context;
    }

    @Override
    public void setContext(GameContext context) {
        checkNotNull(context);
        this.context = context;
    }

    public void setCanvas(Canvas canvas) {
        checkNotNull(canvas);
        g = canvas.getGraphicsContext2D();
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
        return g.getCanvas();
    }

    @Override
    public void init() {
        spriteRenderer.setSpriteSheet(context.getSpriteSheet(context.game().variant()));
    }

    @Override
    public void draw() {
        if (g == null) {
            Logger.error("Cannot render game scene {}, no canvas has been assigned", this);
            return;
        }
        spriteRenderer.setBackgroundColor(canvasBackground());
        clearCanvas();
        if (isScoreVisible()) {
            drawScore(context.game().score(), "SCORE", t(1), t(1));
            drawScore(context.game().highScore(), "HIGH SCORE", t(14), t(1));
        }
        if (isCreditVisible()) {
            spriteRenderer.drawText(g, String.format("CREDIT %2d", context.gameController().credit()),
                context.theme().color("palette.pale"), sceneFont(8), t(2), t(36) - 1);
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
        drawTileGrid();
    }

    public void clearCanvas() {
        g.setFill(canvasBackground());
        g.fillRect(0, 0, g.getCanvas().getWidth(), g.getCanvas().getHeight());
    }

    protected Color canvasBackground() {
        return context.theme() != null ? context.theme().color("canvas.background") : Color.BLACK;
    }

    protected void drawScore(Score score, String title, double x, double y) {
        var pointsText = String.format("%02d", score.points());
        var font = sceneFont(TS);
        var color = context.theme().color("palette.pale");
        spriteRenderer.drawText(g, title, color, font, x, y);
        spriteRenderer.drawText(g, String.format("%7s", pointsText), color, font, x, y + TS + 1);
        if (score.points() != 0) {
            spriteRenderer.drawText(g, "L" + score.levelNumber(), color, font, x + t(8), y + TS + 1);
        }
    }

    protected void drawLevelCounter(GraphicsContext g) {
        double x = t(24);
        double y = t(34);
        if (context.game().world() != null) {
            x = t(context.game().world().numCols() - 4);
            y = t(context.game().world().numRows() - 2);
        }
        spriteRenderer.drawLevelCounter(g,context.game().levelCounter(), x, y);
    }

    protected void drawMidwayCopyright(double x, double y) {
        spriteRenderer.drawText(g, "© 1980 MIDWAY MFG.CO.", context.theme().color("palette.pink"), sceneFont(8), x, y);
    }

    protected void drawMsPacManCopyright(double x, double y) {
        Image logo = context.theme().get("ms_pacman.logo.midway");
        spriteRenderer.drawImageScaled(g, logo, x, y + 2, TS * 4 - 2, TS * 4);
        g.setFill(context.theme().color("palette.red"));
        g.setFont(sceneFont(8));
        g.fillText("©", s(x + TS * 5), s(y + TS * 2 + 2));
        g.fillText("MIDWAY MFG CO", s(x + TS * 7), s(y + TS * 2));
        g.fillText("1980/1981", s(x + TS * 8), s(y + TS * 4));
    }

    protected void drawTileGrid() {
        int numRows = context.game().world() != null ? context.game().world().numRows() : GameModel.ARCADE_MAP_TILES_Y;
        int numCols = context.game().world() != null ? context.game().world().numCols() : GameModel.ARCADE_MAP_TILES_X;
        g.setStroke(Color.LIGHTGRAY);
        g.setLineWidth(0.2);
        for (int row = 0; row <= numRows; ++row) {
            g.strokeLine(0, s(TS * (row)), s(28 * TS), s(TS * (row)));
        }
        for (int col = 0; col <= numCols; ++col) {
            g.strokeLine(s(TS * (col)), 0, s(TS * (col)), s(36 * TS));
        }
    }
}