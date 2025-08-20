/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.ui._2d;

import de.amr.pacmanfx.GameContext;
import de.amr.pacmanfx.event.GameEvent;
import de.amr.pacmanfx.lib.Vector2f;
import de.amr.pacmanfx.model.actors.Actor;
import de.amr.pacmanfx.ui.DefaultActionBindingsManager;
import de.amr.pacmanfx.ui.api.ActionBindingsManager;
import de.amr.pacmanfx.ui.api.GameScene;
import de.amr.pacmanfx.ui.api.GameUI;
import de.amr.pacmanfx.uilib.animation.AnimationRegistry;
import de.amr.pacmanfx.uilib.rendering.BaseRenderer;
import de.amr.pacmanfx.uilib.rendering.DebugInfoRenderer;
import javafx.beans.property.*;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import org.tinylog.Logger;

import java.util.ArrayList;
import java.util.List;

import static de.amr.pacmanfx.uilib.rendering.GameLevelRenderer.fillCanvas;
import static java.util.Objects.requireNonNull;

/**
 * Base class of all 2D scenes.
 */
public abstract class GameScene2D implements GameScene {

    protected final GameUI ui;

    protected final ObjectProperty<Font> arcadeFont8 = new SimpleObjectProperty<>();
    protected final ObjectProperty<Font> arcadeFont6 = new SimpleObjectProperty<>();
    protected final ObjectProperty<Color> backgroundColor = new SimpleObjectProperty<>(Color.BLACK);
    protected final BooleanProperty debugInfoVisible = new SimpleBooleanProperty(false);
    protected final DoubleProperty scaling = new SimpleDoubleProperty(1.0f);

    protected final ActionBindingsManager actionBindings;
    protected final AnimationRegistry animationRegistry = new AnimationRegistry();

    protected DebugInfoRenderer debugInfoRenderer;

    protected Canvas canvas;
    protected final List<Actor> actorsInZOrder = new ArrayList<>();

    protected GameScene2D(GameUI ui) {
        this.ui = requireNonNull(ui);
        actionBindings = new DefaultActionBindingsManager();
    }

    @Override
    public GameContext gameContext() {
        return ui.gameContext();
    }

    @Override
    public final void init() {
        debugInfoRenderer = new DefaultDebugInfoRenderer(ui, canvas);
        debugInfoRenderer.scalingProperty().bind(scaling);

        arcadeFont8.bind(scaling.map(s -> ui.assets().arcadeFont(s.floatValue() * 8)));
        arcadeFont6.bind(scaling.map(s -> ui.assets().arcadeFont(s.floatValue() * 6)));

        doInit();

        actionBindings.installBindings(ui.keyboard());
        ui.keyboard().logCurrentBindings();
    }

    @Override
    public final void end() {
        ui.soundManager().stopAll();
        doEnd();
    }

    @Override
    public void handleKeyboardInput() {
        actionBindings.matchingAction(ui.keyboard()).ifPresent(gameAction -> gameAction.executeIfEnabled(ui));
    }

    protected abstract void doInit();
    protected abstract void doEnd();

    protected void bindRendererScaling(BaseRenderer... renderers) {
        for (BaseRenderer renderer : renderers) {
            renderer.scalingProperty().bind(scaling);
        }
    }

    @Override
    public ActionBindingsManager actionBindings() { return actionBindings; }

    @Override
    public void onStopAllSounds(GameEvent event) { ui.soundManager().stopAll(); }

    @Override
    public void onUnspecifiedChange(GameEvent event) {
        // TODO: remove (this is only used by game state GameState.TESTING_CUT_SCENES)
        ui.updateGameScene(true);
    }

    public void  setScaling(double scaling) { this.scaling.set((float) scaling); }
    public double scaling() { return scaling.get(); }
    public double scaled(double value) { return (float) value * scaling(); }

    public Font scaledArcadeFont8() { return arcadeFont8.get(); }
    public Font scaledArcadeFont6() { return arcadeFont6.get(); }

    public Color backgroundColor() { return backgroundColor.get(); }
    public void setBackgroundColor(Color color) { backgroundColor.set(color); }

    public Canvas canvas() { return canvas; }
    public void setCanvas(Canvas canvas) { this.canvas = canvas; }

    public GraphicsContext ctx() {
        requireNonNull(canvas);
        return canvas.getGraphicsContext2D();
    }

    public ObjectProperty<Color> backgroundColorProperty() { return backgroundColor; }

    public BooleanProperty debugInfoVisibleProperty() { return debugInfoVisible; }
    public boolean isDebugInfoVisible() { return debugInfoVisible.get(); }

    public DoubleProperty scalingProperty() { return scaling; }

    /**
     * @return (unscaled) scene size in pixels e.g. 224x288
     */
    public abstract Vector2f sizeInPx();

    public void clear() {
        if (canvas != null) {
            fillCanvas(canvas, backgroundColor());
        } else {
            Logger.error("Cannot clear scene, canvas not available");
        }
    }

    /**
     * Default implementation: scales the renderer to the current scene scaling,
     * clears the canvas and draws the scores (if on), scene content and debug information (if on).
     */
    public void draw() {
        clear();
        drawSceneContent();
        if (debugInfoVisible.get() && debugInfoRenderer != null) {
            debugInfoRenderer.drawDebugInfo();
        }
        drawHUD();
    }

    public abstract void drawHUD();

    /**
     * Draws the scene content using the already scaled game renderer.
     */
    public abstract void drawSceneContent();
}