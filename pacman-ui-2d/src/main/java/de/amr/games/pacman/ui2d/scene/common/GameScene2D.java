/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d.scene.common;

import de.amr.games.pacman.ui2d.GameAction;
import de.amr.games.pacman.ui2d.GameContext;
import de.amr.games.pacman.ui2d.rendering.GameRenderer;
import javafx.beans.property.*;
import javafx.scene.canvas.Canvas;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import org.tinylog.Logger;

import java.util.HashMap;
import java.util.Map;

import static de.amr.games.pacman.lib.Globals.checkNotNull;

/**
 * Base class of all 2D scenes.
 *
 * @author Armin Reichert
 */
public abstract class GameScene2D implements GameScene {

    public static final Font DEBUG_FONT = Font.font("Sans", FontWeight.BOLD, 20);

    protected final DoubleProperty scalingPy = new SimpleDoubleProperty(this, "scaling", 1.0);
    protected final ObjectProperty<Color> backgroundColorPy = new SimpleObjectProperty<>(this, "backgroundColor", Color.BLACK);
    protected final BooleanProperty debugInfoVisiblePy = new SimpleBooleanProperty(this, "debugInfoVisible", false);
    protected final Map<KeyCodeCombination, GameAction> actionBindings = new HashMap<>();
    protected GameContext context;
    protected Canvas canvas;
    protected GameRenderer gr;

    @Override
    public final void init() {
        doInit();
        bindGameActions();
        registerGameActionKeyBindings(context().keyboard());
    }

    @Override
    public final void end() {
        doEnd();
        unregisterGameActionKeyBindings(context().keyboard());
    }

    @Override
    public GameContext context() {
        return context;
    }

    @Override
    public void setGameContext(GameContext context) {
        this.context = checkNotNull(context);
    }

    @Override
    public Map<KeyCodeCombination, GameAction> actionBindings() {
        return actionBindings;
    }

    public DoubleProperty scalingProperty() { return scalingPy; }
    public void setScaling(double scaling) { scalingPy.set(scaling); }
    public double scaling() { return scalingPy.get(); }
    public double scaled(double value) {
        return value * scaling();
    }

    public ObjectProperty<Color> backgroundColorProperty() { return backgroundColorPy; }
    public void setBackgroundColor(Color color) { backgroundColorPy.set(color); }
    public Color backgroundColor() { return backgroundColorPy.get(); }

    public BooleanProperty debugInfoVisibleProperty() { return debugInfoVisiblePy; }

    protected void doInit() {}
    protected void doEnd() {}

    public Canvas canvas() { return canvas; }

    public void setCanvas(Canvas canvas) {
        this.canvas = canvas;
        gr = context.currentGameSceneConfig().createRenderer(canvas);
        gr.clearCanvas();
        Logger.info("Game renderer created: {}", gr);
    }

    protected abstract void drawSceneContent();

    protected void drawDebugInfo() {
        gr.drawTileGrid(size().x(), size().y());
    }

    public void draw() {
        gr.setScaling(scaling());
        gr.setBackgroundColor(backgroundColor());
        gr.clearCanvas();
        if (context.isScoreVisible()) {
            gr.drawScores(context);
        }
        context.game().level().ifPresent(level -> gr.update(level.world().map()));
        drawSceneContent();
        if (debugInfoVisiblePy.get()) {
            drawDebugInfo();
        }
    }
}