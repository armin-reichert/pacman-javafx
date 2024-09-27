/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d.scene;

import de.amr.games.pacman.lib.Vector2i;
import de.amr.games.pacman.ui2d.GameContext;
import de.amr.games.pacman.ui2d.rendering.GameWorldRenderer;
import javafx.beans.property.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

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

    public boolean isCreditVisible() {
        return context.game().hasCredit();
    }

    public void setGameContext(GameContext context) {
        this.context = checkNotNull(context);
    }

    protected double scaled(double value) {
        return value * scalingPy.get();
    }

    //TODO remove
    protected Font sceneFont(double size) {
        return context.assets().font("font.arcade", scaled(size));
    }

    public void draw(GameWorldRenderer renderer) {
        //TODO use binding
        renderer.scalingProperty().set(scalingPy.get());
        renderer.backgroundColorProperty().set(backgroundColorPy.get());

        renderer.clearCanvas();
        if (context.isScoreVisible()) {
            Font scoreFont = sceneFont(TS);
            renderer.drawScore(context.game().score(),     "SCORE", t(1), t(1), scoreFont, PALETTE_PALE);
            renderer.drawScore(context.game().highScore(), "HIGH SCORE", t(14), t(1), scoreFont, PALETTE_PALE);
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
            double x = 2 * TS, y = context.worldSizeTilesOrDefault().y() * TS - 2;
            renderer.drawText("CREDIT %2d".formatted(context.game().credit()), PALETTE_PALE, sceneFont(8), x, y);
        }
    }

    protected void drawLevelCounter(GameWorldRenderer renderer) {
        Vector2i worldSize = context.worldSizeTilesOrDefault();
        double x = t(worldSize.x() - 4), y = t(worldSize.y() - 2);
        renderer.drawLevelCounter(context.spriteSheet(), context.game().levelCounter(), x, y);
    }

    /**
     * Scenes overwrite this method to draw their specific content.
     */
    protected void drawSceneContent(GameWorldRenderer renderer) {
        Font font = Font.font("Monospaced", 20);
        renderer.drawText("Implement method drawSceneContent()!", Color.WHITE, font, 10, 100);
    }

    /**
     * Draws additional scene info, e.g. tile structure or debug info.
     */
    protected void drawDebugInfo(GameWorldRenderer renderer) {
        Vector2i worldSize = context.worldSizeTilesOrDefault();
        renderer.drawTileGrid(worldSize.x(), worldSize.y());
    }
}