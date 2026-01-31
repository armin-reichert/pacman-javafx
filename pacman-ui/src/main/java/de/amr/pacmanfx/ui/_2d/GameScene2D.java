/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.ui._2d;

import de.amr.pacmanfx.GameContext;
import de.amr.pacmanfx.Globals;
import de.amr.pacmanfx.event.UnspecifiedChangeEvent;
import de.amr.pacmanfx.lib.math.Vector2i;
import de.amr.pacmanfx.model.Game;
import de.amr.pacmanfx.ui.ActionBindingsManager;
import de.amr.pacmanfx.ui.GameScene;
import de.amr.pacmanfx.ui.GameUI;
import de.amr.pacmanfx.ui.action.DefaultActionBindingsManager;
import de.amr.pacmanfx.uilib.rendering.Renderer;
import javafx.beans.property.*;
import javafx.scene.canvas.Canvas;
import javafx.scene.paint.Color;
import org.tinylog.Logger;

import static java.util.Objects.requireNonNull;

/**
 * Base class of all 2D scenes.
 */
public abstract class GameScene2D implements GameScene {

    private final ObjectProperty<Color> backgroundColor = new SimpleObjectProperty<>(Color.BLACK);
    private final BooleanProperty debugInfoVisible = new SimpleBooleanProperty(false);
    private final DoubleProperty scaling = new SimpleDoubleProperty(1.0f);

    protected final ActionBindingsManager actionBindings;

    protected GameContext gameContext;
    protected GameUI ui;
    protected Canvas canvas;

    protected GameScene2D() {
        actionBindings = new DefaultActionBindingsManager();
    }

    @Override
    public void dispose() {
        actionBindings.dispose();
    }

    @Override
    public GameContext gameContext() {
        return gameContext;
    }

    /**
     * Associates the global game context with this scene.
     * @param context the game context
     */
    public void setGameContext(GameContext context) {
        this.gameContext = requireNonNull(context);
    }

    /**
     * Associates this game scene with the UI.
     * @param ui the UI
     */
    public void setUI(GameUI ui) {
        this.ui = requireNonNull(ui);
    }

    public <T extends Renderer> T adaptRenderer(T renderer) {
        renderer.backgroundColorProperty().bind(backgroundProperty());
        renderer.scalingProperty().bind(scalingProperty());
        return renderer;
    }

    /**
     * Hook method called when scene is initialized.
     */
    protected abstract void doInit(Game game);

    /**
     * Hook method called when scene ends.
     */
    protected abstract void doEnd(Game game);

    // GameScene interface

    @Override
    public ActionBindingsManager actionBindings() { return actionBindings; }

    @Override
    public final void init(Game game) {
        doInit(game);
        actionBindings.activateBindings(GameUI.KEYBOARD);
        Logger.info("2D scene {} initialized", getClass().getSimpleName());
    }

    @Override
    public final void end(Game game) {
        doEnd(game);
        ui.soundManager().stopAll();
        Logger.info("2D scene {} ends", getClass().getSimpleName());
    }

    @Override
    public void onUnspecifiedChange(UnspecifiedChangeEvent event) {
        // TODO: remove (this is only used by game state GameState.TESTING_CUT_SCENES)
        ui.playView().updateGameScene(gameContext.currentGame(), true);
    }

    @Override
    public GameUI ui() {
        return ui;
    }

    public void setCanvas(Canvas canvas) {
        this.canvas = requireNonNull(canvas);
    }

    // other methods

    public Canvas canvas() {
        return canvas;
    }

    public ObjectProperty<Color> backgroundProperty() {
        return backgroundColor;
    }

    public Color backgroundColor() {
        return backgroundColor.get();
    }

    public void setBackgroundColor(Color color) {
        backgroundColor.set(color);
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

    public BooleanProperty debugInfoVisibleProperty() {
        return debugInfoVisible;
    }

    public boolean debugInfoVisible() {
        return debugInfoVisible.get();
    }

    /**
     * @return (unscaled) scene size in pixels e.g. 224x288 for Arcade scenes
     */
    public Vector2i unscaledSize() {
        return Globals.ARCADE_MAP_SIZE_IN_PIXELS;
    }
}