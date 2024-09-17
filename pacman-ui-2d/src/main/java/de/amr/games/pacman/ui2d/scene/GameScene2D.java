/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d.scene;

import de.amr.games.pacman.lib.Vector2i;
import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.ui2d.GameContext;
import de.amr.games.pacman.ui2d.rendering.GameWorldRenderer;
import javafx.beans.property.*;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
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

    public final DoubleProperty scalingPy = new SimpleDoubleProperty(this, "scaling", 1.0);
    public final ObjectProperty<Color> backgroundColorPy = new SimpleObjectProperty<>(this, "backgroundColor", Color.BLACK);
    public final BooleanProperty debugInfoPy = new SimpleBooleanProperty(this, "debugInfo", false);

    protected GameContext context;
    protected GraphicsContext g;
    protected GameWorldRenderer renderer;

    public boolean isCreditVisible() {
        return context.game().hasCredit();
    }

    public void setGameContext(GameContext context) {
        this.context = checkNotNull(context);
    }

    public void setCanvas(Canvas canvas) {
        checkNotNull(canvas);
        g = canvas.getGraphicsContext2D();
        clearCanvas();
    }

    public void setRenderer(GameWorldRenderer renderer) {
        this.renderer = checkNotNull(renderer);
    }

    protected double scaled(double value) {
        return value * scalingPy.get();
    }

    protected Font sceneFont(double size) {
        return context.assets().font("font.arcade", scaled(size));
    }

    @Override
    public Node root() {
        return g.getCanvas();
    }

    public void draw() {
        if (g == null) {
            Logger.error("Cannot render game scene {}, no canvas has been assigned", this);
            return;
        }
        clearCanvas();
        if (context.isScoreVisible()) {
            Color scoreColor = context.assets().color("palette.pale");
            Font scoreFont = sceneFont(TS);
            renderer.drawScore(g, context.game().score(), "SCORE", t(1), t(1), scoreFont, scoreColor);
            renderer.drawScore(g, context.game().highScore(), "HIGH SCORE", t(14), t(1), scoreFont, scoreColor);
        }
        drawSceneContent();
        if (debugInfoPy.get()) {
            drawDebugInfo();
        }
        if (isCreditVisible()) {
            int numRows = context.game().world() != null
                ? context.game().world().map().terrain().numRows()
                : GameModel.ARCADE_MAP_TILES_Y;
            Color creditColor = context.assets().color("palette.pale");
            Font creditFont = sceneFont(8);
            renderer.drawText(g, String.format("CREDIT %2d", context.game().credit()), creditColor, creditFont, t(2), t(numRows) - 1);
        }
    }

    protected void drawSceneContent() {
        Font font = Font.font("Monospaced", 20);
        renderer.drawText(g, "Implement method drawSceneContent()!", Color.WHITE, font, 10, 100);
    }

    public void clearCanvas() {
        g.setFill(backgroundColorPy.get());
        g.fillRect(0, 0, g.getCanvas().getWidth(), g.getCanvas().getHeight());
    }

    /**
     * Draws additional scene info, e.g. tile structure or debug info.
     */
    protected void drawDebugInfo() {
        Vector2i worldSize = context.worldSize();
        renderer.drawTileGrid(g, worldSize.x(), worldSize.y());
    }

    protected void drawLevelCounter(GraphicsContext g) {
        Vector2i worldSize = context.worldSize();
        renderer.drawLevelCounter(g, context.game().levelCounter(), t(worldSize.x()) - 4, t(worldSize.y()) - 2);
    }

    protected void drawMidwayCopyright(double x, double y) {
        renderer.drawText(g, "© 1980 MIDWAY MFG.CO.", context.assets().color("palette.pink"), sceneFont(8), x, y);
    }
}