/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui.fx.scene2d;

import de.amr.games.pacman.lib.Score;
import de.amr.games.pacman.ui.fx.GameScene;
import de.amr.games.pacman.ui.fx.GameSceneContext;
import de.amr.games.pacman.ui.fx.PacManGames2dUI;
import de.amr.games.pacman.ui.fx.rendering2d.ClassicWorldRenderer;
import de.amr.games.pacman.ui.fx.rendering2d.ModernWorldRenderer;
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

    protected GameSceneContext context;
    protected GraphicsContext g;
    protected final ModernWorldRenderer modernRenderer = new ModernWorldRenderer(scalingPy);
    protected final ClassicWorldRenderer classicRenderer = new ClassicWorldRenderer(scalingPy);

    protected GameScene2D() {
        classicRenderer.setMsPacManSpriteSheet(PacManGames2dUI.SS_MS_PACMAN);
        classicRenderer.setPacManSpriteSheet(PacManGames2dUI.SS_PACMAN);
    }

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
            Logger.error("Cannot render game scene {}, no canvas has been assigned", getClass().getSimpleName());
            return;
        }
        clearCanvas();
        if (context == null) {
            Logger.error("Cannot render game scene {}, no scene context has been assigned", getClass().getSimpleName());
            return;
        }
        if (isScoreVisible()) {
            drawScore(context.game().score(), "SCORE", t(1), t(1));
            drawScore(context.game().highScore(), "HIGH SCORE", t(14), t(1));
        }
        if (isCreditVisible()) {
            classicRenderer.drawText(g, String.format("CREDIT %2d", context.gameController().credit()),
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
        Color fillColor = context.theme() != null ? context.theme().color("canvas.background") : Color.BLACK;
        g.setFill(fillColor);
        g.fillRect(0, 0, g.getCanvas().getWidth(), g.getCanvas().getHeight());
    }

    protected void drawScore(Score score, String title, double x, double y) {
        var pointsText = String.format("%02d", score.points());
        var font = sceneFont(TS);
        var color = context.theme().color("palette.pale");
        classicRenderer.drawText(g, title, color, font, x, y);
        classicRenderer.drawText(g, String.format("%7s", pointsText), color, font, x, y + TS + 1);
        if (score.points() != 0) {
            classicRenderer.drawText(g, "L" + score.levelNumber(), color, font, x + t(8), y + TS + 1);
        }
    }

    protected void drawMidwayCopyright(double x, double y) {
        classicRenderer.drawText(g, "© 1980 MIDWAY MFG.CO.", context.theme().color("palette.pink"), sceneFont(8), x, y);
    }

    protected void drawMsPacManCopyright(double x, double y) {
        Image logo = context.theme().get("mspacman.logo.midway");
        classicRenderer.drawImageScaled(g, logo, x, y + 2, TS * 4 - 2, TS * 4);
        g.setFill(context.theme().color("palette.red"));
        g.setFont(sceneFont(8));
        g.fillText("©", s(x + TS * 5), s(y + TS * 2 + 2));
        g.fillText("MIDWAY MFG CO", s(x + TS * 7), s(y + TS * 2));
        g.fillText("1980/1981", s(x + TS * 8), s(y + TS * 4));
    }

    protected void drawTileGrid() {
        g.setStroke(context.theme().color("palette.pale"));
        g.setLineWidth(0.2);
        for (int row = 0; row <= 36; ++row) {
            g.strokeLine(0, s(TS * (row)), s(28 * TS), s(TS * (row)));
        }
        for (int col = 0; col <= 28; ++col) {
            g.strokeLine(s(TS * (col)), 0, s(TS * (col)), s(36 * TS));
        }
    }
}