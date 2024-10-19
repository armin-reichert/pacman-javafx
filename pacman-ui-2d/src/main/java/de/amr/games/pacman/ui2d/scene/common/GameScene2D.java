/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d.scene.common;

import de.amr.games.pacman.lib.Vector2f;
import de.amr.games.pacman.ui2d.GameContext;
import de.amr.games.pacman.ui2d.rendering.GameRenderer;
import javafx.beans.property.*;
import javafx.scene.paint.Color;

import static de.amr.games.pacman.lib.Globals.TS;
import static de.amr.games.pacman.lib.Globals.checkNotNull;
import static de.amr.games.pacman.ui2d.scene.common.ScalingBehaviour.AUTO;

/**
 * Base class of all 2D scenes.
 *
 * @author Armin Reichert
 */
public abstract class GameScene2D implements GameScene {

    private final DoubleProperty scalingPy = new SimpleDoubleProperty(this, "scaling", 1.0);
    public final ObjectProperty<Color> backgroundColorPy = new SimpleObjectProperty<>(this, "backgroundColor", Color.BLACK);
    public final BooleanProperty debugInfoPy = new SimpleBooleanProperty(this, "debugInfo", false);

    protected GameContext context;

    public ScalingBehaviour scalingBehaviour() {
        return AUTO; // default
    }

    public DoubleProperty scalingProperty() {
        return scalingPy;
    }

    public void setScaling(double scaling) {

        scalingPy.set(scaling);
    }

    public double scaling() {
        return scalingPy.get();
    }

    public double scaled(double value) {
        return value * scaling();
    }

    protected abstract void drawSceneContent(GameRenderer renderer);

    protected void drawDebugInfo(GameRenderer renderer) {}

    @Override
    public void setGameContext(GameContext context) {
        this.context = checkNotNull(context);
    }

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
}