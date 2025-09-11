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
import de.amr.pacmanfx.ui.api.GameUI_Properties;
import de.amr.pacmanfx.uilib.animation.AnimationRegistry;
import de.amr.pacmanfx.uilib.rendering.BaseSpriteRenderer;
import de.amr.pacmanfx.uilib.rendering.CanvasRenderer;
import de.amr.pacmanfx.uilib.rendering.DebugInfoRenderer;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.canvas.Canvas;

import java.util.ArrayList;
import java.util.List;

import static java.util.Objects.requireNonNull;

/**
 * Base class of all 2D scenes.
 */
public abstract class GameScene2D implements GameScene {

    protected final BooleanProperty debugInfoVisible = new SimpleBooleanProperty(false);
    protected final DoubleProperty scaling = new SimpleDoubleProperty(1.0f);

    protected final GameUI ui;
    protected final ActionBindingsManager actionBindings;
    protected final AnimationRegistry animationRegistry;
    protected final List<Actor> actorsInZOrder = new ArrayList<>();

    protected BaseSpriteRenderer sceneRenderer;
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

    public BaseSpriteRenderer sceneRenderer() {
        return sceneRenderer;
    }

    public void createRenderers(Canvas canvas) {
        requireNonNull(canvas);
        sceneRenderer = new BaseSpriteRenderer(canvas);
        debugInfoRenderer = new DefaultDebugInfoRenderer(ui, canvas);
        bindRendererProperties(sceneRenderer, debugInfoRenderer);
    }

    protected void bindRendererProperties(CanvasRenderer... renderers) {
        for (CanvasRenderer renderer : renderers) {
            renderer.backgroundColorProperty().bind(GameUI_Properties.PROPERTY_CANVAS_BACKGROUND_COLOR);
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

    public DoubleProperty scalingProperty() {
        return scaling;
    }

    public void setScaling(double value) {
        scaling.set(value);
    }

    public double scaling() { return
        scaling.get();
    }

    public double scaled(double value) {
        return value * scaling();
    }

    public BooleanProperty debugInfoVisibleProperty() {
        return debugInfoVisible;
    }

    public boolean debugInfoVisible() {
        return debugInfoVisible.get();
    }

    /**
     * @return (unscaled) scene size in pixels e.g. 224x288
     */
    public abstract Vector2f sizeInPx();

    public List<Actor> actorsInZOrder() {
        return actorsInZOrder;
    }

    /**
     * Default implementation: scales the renderer to the current scene scaling,
     * clears the canvas and draws the scores (if on), scene content and debug information (if on).
     */
    public void draw() {
        sceneRenderer.clearCanvas();
        drawSceneContent();
        if (debugInfoVisible() && debugInfoRenderer != null) {
            debugInfoRenderer.drawDebugInfo();
        }
        drawHUD();
    }

    /**
     * Draws the Heads-Up-Display (HUD).
     */
    public abstract void drawHUD();

    /**
     * Draws the scene content using the already scaled game renderer.
     */
    public abstract void drawSceneContent();
}