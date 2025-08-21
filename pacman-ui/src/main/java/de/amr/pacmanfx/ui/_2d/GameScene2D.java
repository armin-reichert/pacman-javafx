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
import de.amr.pacmanfx.uilib.rendering.CanvasRenderer;
import de.amr.pacmanfx.uilib.rendering.DebugInfoRenderer;
import javafx.beans.property.*;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import org.tinylog.Logger;

import java.util.ArrayList;
import java.util.List;

import static de.amr.pacmanfx.uilib.rendering.GameLevelRenderer.fillCanvas;
import static java.util.Objects.requireNonNull;

/**
 * Base class of all 2D scenes.
 */
public abstract class GameScene2D implements GameScene {

    protected final ObjectProperty<Color> backgroundColor = new SimpleObjectProperty<>(Color.BLACK);
    protected final BooleanProperty debugInfoVisible = new SimpleBooleanProperty(false);
    protected final DoubleProperty scaling = new SimpleDoubleProperty(1.0f);

    protected final GameUI ui;
    protected final ActionBindingsManager actionBindings;
    protected final AnimationRegistry animationRegistry;
    protected final List<Actor> actorsInZOrder = new ArrayList<>();

    protected Canvas canvas;
    protected DebugInfoRenderer debugInfoRenderer;

    protected GameScene2D(GameUI ui) {
        this.ui = requireNonNull(ui);
        actionBindings = new DefaultActionBindingsManager();
        animationRegistry = new AnimationRegistry();
    }

    @Override
    public GameContext context() {
        return ui.gameContext();
    }

    @Override
    public final void init() {
        requireNonNull(canvas, "No canvas has been assigned to game scene");
        debugInfoRenderer = new DefaultDebugInfoRenderer(ui, canvas);
        debugInfoRenderer.scalingProperty().bind(scaling);
        doInit();
        actionBindings.installBindings(ui.keyboard());
        ui.keyboard().logCurrentBindings();
    }

    @Override
    public final void end() {
        doEnd();
        ui.soundManager().stopAll();
    }

    @Override
    public void handleKeyboardInput() {
        actionBindings.matchingAction(ui.keyboard()).ifPresent(gameAction -> gameAction.executeIfEnabled(ui));
    }

    protected abstract void doInit();
    protected abstract void doEnd();

    protected void bindRendererScaling(CanvasRenderer... renderers) {
        for (CanvasRenderer renderer : renderers) {
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