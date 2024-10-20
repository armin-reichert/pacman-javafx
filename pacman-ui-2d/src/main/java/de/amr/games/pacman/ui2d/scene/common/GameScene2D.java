/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d.scene.common;

import de.amr.games.pacman.ui2d.GameContext;
import de.amr.games.pacman.ui2d.rendering.GameRenderer;
import javafx.beans.property.*;
import javafx.scene.paint.Color;

import static de.amr.games.pacman.lib.Globals.checkNotNull;

/**
 * Base class of all 2D scenes.
 *
 * @author Armin Reichert
 */
public abstract class GameScene2D implements GameScene {

    private final DoubleProperty scalingPy = new SimpleDoubleProperty(this, "scaling", 1.0);
    private final ObjectProperty<Color> backgroundColorPy = new SimpleObjectProperty<>(this, "backgroundColor", Color.BLACK);
    public final BooleanProperty debugInfoPy = new SimpleBooleanProperty(this, "debugInfo", false);

    protected GameContext context;

    @Override
    public void setGameContext(GameContext context) {
        this.context = checkNotNull(context);
    }

    public DoubleProperty scalingProperty() { return scalingPy; }
    public void setScaling(double scaling) { scalingPy.set(scaling); }
    public double scaling() {
        return scalingPy.get();
    }
    public double scaled(double value) {
        return value * scaling();
    }

    public ObjectProperty<Color> backgroundColorProperty() { return backgroundColorPy; }
    public void setBackgroundColor(Color color) { backgroundColorPy.set(color); }
    public Color backgroundColor() { return backgroundColorPy.get(); }

    protected abstract void drawSceneContent(GameRenderer renderer);
    protected void drawDebugInfo(GameRenderer renderer) {}

    @Override
    public void draw(GameRenderer renderer) {
        renderer.scalingProperty().set(scaling());
        renderer.setBackgroundColor(backgroundColor());
        renderer.clearCanvas();
        if (context.isScoreVisible()) {
            renderer.drawScores(context);
        }
        drawSceneContent(renderer);
        if (debugInfoPy.get()) {
            renderer.ctx().setLineWidth(2);
            renderer.ctx().setStroke(Color.WHITE);
            renderer.ctx().strokeRect(0, 0, renderer.canvas().getWidth(), renderer.canvas().getHeight());
            drawDebugInfo(renderer);
        }
    }
}