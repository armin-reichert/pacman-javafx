/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d.scene;

import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.model.actors.Creature;
import de.amr.games.pacman.model.actors.Ghost;
import de.amr.games.pacman.model.actors.Pac;
import de.amr.games.pacman.ui2d.GameContext;
import de.amr.games.pacman.ui2d.rendering.GameWorldRenderer;
import de.amr.games.pacman.ui2d.rendering.ms_pacman.MsPacManGameWorldRenderer;
import de.amr.games.pacman.ui2d.rendering.pacman.PacManGameWorldRenderer;
import de.amr.games.pacman.ui2d.rendering.pacman_xxl.PacManXXLGameWorldRenderer;
import de.amr.games.pacman.ui2d.rendering.tengen.TengenMsPacManGameWorldRenderer;
import de.amr.games.pacman.ui2d.util.SpriteAnimations;
import javafx.beans.property.*;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import org.tinylog.Logger;

import java.util.List;

import static de.amr.games.pacman.lib.Globals.*;
import static de.amr.games.pacman.ui2d.PacManGames2dApp.PY_CANVAS_COLOR;

/**
 * Base class of all 2D scenes.
 *
 * @author Armin Reichert
 */
public class GameScene2D implements GameScene {

    public final BooleanProperty infoVisiblePy = new SimpleBooleanProperty(this, "infoVisible", false);
    public final DoubleProperty scalingPy = new SimpleDoubleProperty(this, "scaling", 1.0);
    public final ObjectProperty<Color> backgroundColorPy = new SimpleObjectProperty<>(this, "backgroundColor", Color.BLACK);

    protected GameWorldRenderer renderer;

    protected GameContext context;
    protected GraphicsContext g;

    public boolean isCreditVisible() {
        return context.game().hasCredit();
    }

    protected void drawSceneContent() {
        Font font = Font.font("Monospaced", 20);
        renderer.drawText(g, "Implement method drawSceneContent()!", Color.WHITE, font, 10, 100);
    }

    public void setContext(GameContext context) {
        checkNotNull(context);
        this.context = context;
    }

    public void setCanvas(Canvas canvas) {
        checkNotNull(canvas);
        g = canvas.getGraphicsContext2D();
        clearCanvas();
    }

    protected double s(double value) {
        return value * scalingPy.get();
    }

    protected Font sceneFont(double size) {
        return context.assets().font("font.arcade", s(size));
    }

    @Override
    public Node root() {
        return g.getCanvas();
    }

    @Override
    public void init() {
        backgroundColorPy.bind(PY_CANVAS_COLOR);
        renderer = switch (context.game().variant()) {
            case MS_PACMAN -> new MsPacManGameWorldRenderer(context.assets());
            case MS_PACMAN_TENGEN -> new TengenMsPacManGameWorldRenderer(context.assets());
            case PACMAN -> new PacManGameWorldRenderer(context.assets());
            case PACMAN_XXL -> new PacManXXLGameWorldRenderer(context.assets());
        };
        renderer.scalingProperty().bind(scalingPy);
        renderer.backgroundColorProperty().bind(PY_CANVAS_COLOR);
    }

    @Override
    public void update() {
    }

    public void draw() {
        if (g == null) {
            Logger.error("Cannot render game scene {}, no canvas has been assigned", this);
            return;
        }
        clearCanvas();
        if (context.isScoreVisible()) {
            Color color = context.assets().color("palette.pale");
            Font font = sceneFont(TS);
            renderer.drawScore(g, context.game().score(), "SCORE", t(1), t(1), font, color);
            renderer.drawScore(g, context.game().highScore(), "HIGH SCORE", t(14), t(1), font, color);
        }
        if (isCreditVisible()) {
            int numRows = context.game().world() != null
                ? context.game().world().map().terrain().numRows()
                : GameModel.ARCADE_MAP_TILES_Y;
            renderer.drawText(g, String.format("CREDIT %2d", context.game().credit()),
                context.assets().color("palette.pale"), sceneFont(8), t(2), t(numRows) - 1);
        }
        drawSceneContent();
        if (infoVisiblePy.get()) {
            drawSceneInfo();
        }
    }

    public void clearCanvas() {
        g.setFill(backgroundColorPy.get());
        g.fillRect(0, 0, g.getCanvas().getWidth(), g.getCanvas().getHeight());
    }

    /**
     * Draws additional scene info, e.g. tile structure or debug info.
     */
    protected void drawSceneInfo() {
        renderer.drawTileGrid(g, numWorldTilesX(), numWorldTilesY());
    }

    protected int numWorldTilesX() {
        return context.game().world() != null ? context.game().world().map().terrain().numCols() : GameModel.ARCADE_MAP_TILES_X;
    }

    protected int numWorldTilesY() {
        return context.game().world() != null ? context.game().world().map().terrain().numRows() : GameModel.ARCADE_MAP_TILES_Y;
    }

    protected void drawLevelCounter(GraphicsContext g) {
        renderer.drawLevelCounter(g, context.game().levelCounter(), t(numWorldTilesX() - 4), t(numWorldTilesY() - 2));
    }

    protected void drawPac(GraphicsContext g, Pac pac) {
        if (pac.isVisible() && pac.animations().isPresent() && pac.animations().get() instanceof SpriteAnimations sa) {
            renderer.spriteRenderer().drawEntitySprite(g, pac, sa.currentSprite());
        }
    }

    protected void drawPacInfo(GraphicsContext g, Pac pac) {
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

    protected void drawWishDir(GraphicsContext g, Creature guy) {
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

    protected void drawGhost(GraphicsContext g, Ghost ghost) {
        if (!ghost.isVisible()) {
            return;
        }
        ghost.animations().ifPresent(ga -> {
            if (ga instanceof SpriteAnimations animations) {
                renderer.spriteRenderer().drawEntitySprite(g,  ghost, animations.currentSprite());
            }
        });
    }

    protected void drawGhostInfo(GraphicsContext g, Ghost ghost) {
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

    protected void drawMidwayCopyright(double x, double y) {
        renderer.drawText(g, "© 1980 MIDWAY MFG.CO.", context.assets().color("palette.pink"), sceneFont(8), x, y);
    }

    protected void drawMsPacManCopyright(double x, double y) {
        Image logo = context.assets().get("ms_pacman.logo.midway");
        renderer.spriteRenderer().drawImageScaled(g, logo, x, y + 2, t(4) - 2, t(4));
        g.setFill(context.assets().color("palette.red"));
        g.setFont(sceneFont(TS));
        g.fillText("©", s(x + TS * 5), s(y + TS * 2 + 2));
        g.fillText("MIDWAY MFG CO", s(x + TS * 7), s(y + TS * 2));
        g.fillText("1980/1981", s(x + TS * 8), s(y + TS * 4));
    }
}