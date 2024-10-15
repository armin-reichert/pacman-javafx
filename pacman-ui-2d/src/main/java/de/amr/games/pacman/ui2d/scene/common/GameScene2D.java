/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d.scene.common;

import de.amr.games.pacman.lib.Vector2i;
import de.amr.games.pacman.ui2d.GameContext;
import de.amr.games.pacman.ui2d.rendering.GameRenderer;
import javafx.beans.property.*;
import javafx.scene.paint.Color;

import static de.amr.games.pacman.lib.Globals.TS;
import static de.amr.games.pacman.lib.Globals.checkNotNull;
import static de.amr.games.pacman.ui2d.GameAssets2D.ARCADE_PALE;

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

    public void setGameContext(GameContext context) {
        this.context = checkNotNull(context);
    }

    protected double scaling() {
        return scalingPy.get();
    }

    protected double scaled(double value) {
        return value * scaling();
    }

    protected abstract void drawSceneContent(GameRenderer renderer);

    @Override
    public void draw(GameRenderer renderer) {
        renderer.scalingProperty().set(scaling());
        renderer.setBackgroundColor(backgroundColorPy.get());
        renderer.clearCanvas();
        if (context.isScoreVisible()) {
            renderer.drawScores(context);
        }
        drawSceneContent(renderer);
        if (debugInfoPy.get()) {
            drawDebugInfo(renderer);
        }
    }

    protected void drawCredit(GameRenderer renderer, Vector2i worldSize) {
        double x = 2 * TS, y = worldSize.y() * TS - 2;
        renderer.drawText("CREDIT %2d".formatted(context.game().credit()), ARCADE_PALE, renderer.scaledArcadeFont(TS), x, y);
    }

    protected void drawLevelCounter(GameRenderer renderer, Vector2i worldSize) {
        renderer.drawLevelCounter(context.game().levelNumber(), context.game().levelCounter(), worldSize);
    }

    /**
     * Draws additional scene info, e.g. tile structure or debug info.
     */
    protected void drawDebugInfo(GameRenderer renderer) {
        renderer.drawTileGrid(context.worldSizeTilesOrDefault());
    }
}