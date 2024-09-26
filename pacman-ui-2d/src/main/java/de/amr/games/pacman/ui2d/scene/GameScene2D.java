/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d.scene;

import de.amr.games.pacman.lib.Vector2i;
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
import static de.amr.games.pacman.ui2d.GameAssets2D.PALETTE_PALE;

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

    public void draw(GameWorldRenderer renderer) {
        if (g == null) {
            Logger.error("Cannot render game scene {}, no canvas has been assigned", this);
            return;
        }
        // TODO: check this: set this on every draw call because picture-in-picture view has different scaling
        renderer.scalingProperty().set(scalingPy.get());
        renderer.backgroundColorProperty().set(backgroundColorPy.get());
        clearCanvas();
        if (context.isScoreVisible()) {
            Color scoreColor = PALETTE_PALE;
            Font scoreFont = sceneFont(TS);
            renderer.drawScore(g, context.game().score(), "SCORE", t(1), t(1), scoreFont, scoreColor);
            renderer.drawScore(g, context.game().highScore(), "HIGH SCORE", t(14), t(1), scoreFont, scoreColor);
        }
        drawSceneContent(renderer);
        if (debugInfoPy.get()) {
            drawDebugInfo(renderer);
        }
        drawCredit(renderer);
        drawLevelCounter(renderer);
    }

    protected void drawCredit(GameWorldRenderer renderer) {
        if (isCreditVisible()) {
            Vector2i worldSize = context.worldSizeOrDefault();
            renderer.drawText(g,
                "CREDIT %2d".formatted(context.game().credit()),
                PALETTE_PALE,
                sceneFont(8),
                t(2), t(worldSize.y()) - 2);
        }
    }

    protected void drawLevelCounter(GameWorldRenderer renderer) {
        Vector2i worldSize = context.worldSizeOrDefault();
        double x = t(worldSize.x() - 4), y = t(worldSize.y() - 2);
        renderer.drawLevelCounter(g, context.spriteSheet(), context.game().levelCounter(), x, y);
    }

    /**
     * Scenes overwrite this method to draw their specific content.
     */
    protected void drawSceneContent(GameWorldRenderer renderer) {
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
    protected void drawDebugInfo(GameWorldRenderer renderer) {
        Vector2i worldSize = context.worldSizeOrDefault();
        renderer.drawTileGrid(g, worldSize.x(), worldSize.y());
    }
}