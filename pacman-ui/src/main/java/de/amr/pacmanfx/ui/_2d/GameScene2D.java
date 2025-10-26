/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.ui._2d;

import de.amr.pacmanfx.GameContext;
import de.amr.pacmanfx.Globals;
import de.amr.pacmanfx.event.GameEvent;
import de.amr.pacmanfx.lib.Vector2i;
import de.amr.pacmanfx.model.actors.Actor;
import de.amr.pacmanfx.ui.DefaultActionBindingsManager;
import de.amr.pacmanfx.ui.api.ActionBindingsManager;
import de.amr.pacmanfx.ui.api.GameScene;
import de.amr.pacmanfx.ui.api.GameUI;
import de.amr.pacmanfx.ui.api.GameUI_Properties;
import de.amr.pacmanfx.uilib.animation.AnimationRegistry;
import de.amr.pacmanfx.uilib.rendering.BaseRenderer;
import de.amr.pacmanfx.uilib.rendering.DebugInfoRenderer;
import de.amr.pacmanfx.uilib.rendering.HUDRenderer;
import de.amr.pacmanfx.uilib.rendering.Renderer;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.canvas.Canvas;
import org.tinylog.Logger;

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
    protected final ActionBindingsManager actionBindingsManager;
    protected final AnimationRegistry animationRegistry;
    protected final List<Actor> actorsInZOrder = new ArrayList<>();

    protected Canvas canvas;
    protected BaseRenderer sceneRenderer;
    protected DebugInfoRenderer debugInfoRenderer;

    protected GameScene2D(GameUI ui) {
        this.ui = requireNonNull(ui);
        actionBindingsManager = new DefaultActionBindingsManager();
        animationRegistry = new AnimationRegistry();
    }

    @Override
    public GameContext context() {
        return ui.gameContext();
    }

    @Override
    public final void init() {
        doInit();
        actionBindingsManager.assignBindingsToKeyboard(ui.keyboard());
    }

    @Override
    public final void end() {
        doEnd();
        ui.soundManager().stopAll();
        Logger.info("{} ends", getClass().getSimpleName());
    }

    @Override
    public void handleKeyboardInput() {
        actionBindingsManager.matchingAction(ui.keyboard()).ifPresent(gameAction -> gameAction.executeIfEnabled(ui));
    }

    protected abstract void doInit();

    protected abstract void doEnd();

    public Renderer sceneRenderer() {
        return sceneRenderer;
    }

    protected abstract HUDRenderer hudRenderer();

    public void setCanvas(Canvas canvas) {
        this.canvas = canvas;
        createRenderers(canvas);
    }

    public Canvas canvas() {
        return canvas;
    }

    protected void createRenderers(Canvas canvas) {
        requireNonNull(canvas);
        sceneRenderer     = configureRenderer(new BaseRenderer(canvas));
        debugInfoRenderer = configureRenderer(new DefaultDebugInfoRenderer(ui, canvas));
    }

    protected final <T extends Renderer> T configureRenderer(T renderer) {
        renderer.backgroundColorProperty().bind(GameUI_Properties.PROPERTY_CANVAS_BACKGROUND_COLOR);
        renderer.scalingProperty().bind(scaling);
        return renderer;
    }

    @Override
    public ActionBindingsManager actionBindings() { return actionBindingsManager; }

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
    public Vector2i sizeInPx() {
        return Globals.ARCADE_MAP_SIZE_IN_PIXELS;
    }

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

    protected void drawHUD() {
        if (hudRenderer() != null) {
            hudRenderer().drawHUD(context().game(), context().game().hud(), sizeInPx());
        }
    }

    /**
     * Draws the scene content using the already scaled game renderer.
     */
    public abstract void drawSceneContent();
}