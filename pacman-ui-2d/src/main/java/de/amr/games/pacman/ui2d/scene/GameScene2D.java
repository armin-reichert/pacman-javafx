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

    protected double scaling() {
        return scalingPy.get();
    }

    protected double scaled(double value) {
        return value * scaling();
    }

    @Override
    public void draw(GameWorldRenderer renderer) {
        renderer.scalingProperty().set(scalingPy.get());
        renderer.backgroundColorProperty().set(backgroundColorPy.get());
        renderer.clearCanvas();
        if (context.isScoreVisible()) {
            renderer.drawScores(context);
        }
        drawSceneContent(renderer);
        if (debugInfoPy.get()) {
            drawDebugInfo(renderer);
        }
        if (isCreditVisible()) {
            drawCredit(renderer, context.worldSizeTilesOrDefault());
        }
        drawLevelCounter(renderer, context.worldSizeTilesOrDefault());
    }

    protected void drawCredit(GameWorldRenderer renderer, Vector2i worldSize) {
        double x = 2 * TS, y = worldSize.y() * TS - 2;
        renderer.drawText("CREDIT %2d".formatted(context.game().credit()), PALETTE_PALE, renderer.scaledArcadeFont(TS), x, y);
    }

    protected void drawLevelCounter(GameWorldRenderer renderer, Vector2i worldSize) {
        renderer.drawLevelCounter(context.spriteSheet(), context.game().levelNumber(), context.game().levelCounter(), worldSize);
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
        renderer.drawTileGrid(context.worldSizeTilesOrDefault());
    }
}