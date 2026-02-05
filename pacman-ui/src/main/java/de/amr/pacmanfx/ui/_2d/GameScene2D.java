/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.ui._2d;

import de.amr.pacmanfx.Globals;
import de.amr.pacmanfx.event.UnspecifiedChangeEvent;
import de.amr.pacmanfx.lib.math.Vector2i;
import de.amr.pacmanfx.model.Game;
import de.amr.pacmanfx.ui.ActionBindingsManager;
import de.amr.pacmanfx.ui.GameScene;
import de.amr.pacmanfx.ui.GameUI;
import de.amr.pacmanfx.ui.action.SimpleActionBindingsManager;
import de.amr.pacmanfx.uilib.rendering.Renderer;
import javafx.beans.property.*;
import javafx.scene.canvas.Canvas;
import javafx.scene.paint.Color;
import org.tinylog.Logger;

import static java.util.Objects.requireNonNull;

/**
 * Base class for all 2D game scenes.
 * <p>
 * A {@code GameScene2D} encapsulates the visual representation and interaction
 * logic of a 2D scene in the Pac-Man FX UI layer. It manages rendering-related
 * properties such as background color and scaling, provides access to the
 * {@link Canvas}, and coordinates lifecycle events (initialization, shutdown)
 * with the {@link Game} and {@link GameUI}.
 * <p>
 * Subclasses implement {@link #doInit(Game)} and {@link #doEnd(Game)} to define
 * scene-specific behavior.
 */
public abstract class GameScene2D implements GameScene {

    private final ObjectProperty<Color> backgroundColor = new SimpleObjectProperty<>(Color.BLACK);
    private final BooleanProperty debugInfoVisible = new SimpleBooleanProperty(false);
    private final DoubleProperty scaling = new SimpleDoubleProperty(1.0f);

    protected final ActionBindingsManager actionBindings = new SimpleActionBindingsManager();
    protected GameUI ui;
    protected Canvas canvas;

    /**
     * Creates a new 2D game scene. Subclasses typically configure their
     * rendering and input bindings during {@link #doInit(Game)}.
     */
    protected GameScene2D() {}

    /**
     * Releases bindings and resources held by this scene.
     * Called when the scene is permanently discarded.
     */
    @Override
    public void dispose() {
        backgroundColor.unbind();
        debugInfoVisible.unbind();
        scaling.unbind();
        actionBindings.dispose();
    }

    /**
     * Associates this scene with the UI layer.
     *
     * @param ui the UI instance, must not be {@code null}
     */
    @Override
    public void setUI(GameUI ui) {
        this.ui = requireNonNull(ui);
    }

    /**
     * Binds renderer properties (background color, scaling) to this scene's
     * corresponding properties.
     *
     * @param renderer the renderer to adapt
     * @param <T>      renderer type
     * @return the same renderer instance for fluent usage
     */
    public <T extends Renderer> T adaptRenderer(T renderer) {
        renderer.backgroundColorProperty().bind(backgroundProperty());
        renderer.scalingProperty().bind(scalingProperty());
        return renderer;
    }

    /**
     * Called when the scene is initialized.
     * Subclasses implement their setup logic here (loading assets, configuring
     * input, preparing animations, etc.).
     *
     * @param game the active game instance
     */
    protected abstract void doInit(Game game);

    /**
     * Called when the scene ends.
     * Subclasses implement cleanup logic here (stopping animations, releasing
     * temporary resources, etc.).
     *
     * @param game the active game instance
     */
    protected abstract void doEnd(Game game);

    @Override
    public ActionBindingsManager actionBindings() {
        return actionBindings;
    }

    /**
     * Initializes the scene and registers default keyboard bindings.
     */
    @Override
    public final void init(Game game) {
        doInit(game);
        actionBindings.addAll(GameUI.KEYBOARD);
        Logger.info("2D scene {} initialized", getClass().getSimpleName());
    }

    /**
     * Ends the scene and stops all currently playing sounds.
     */
    @Override
    public final void end(Game game) {
        doEnd(game);
        ui.soundManager().stopAll();
        Logger.info("2D scene {} ends", getClass().getSimpleName());
    }

    /**
     * Handles unspecified change events.
     * Currently used only for testing cut scenes.
     */
    @Override
    public void onUnspecifiedChange(UnspecifiedChangeEvent event) {
        ui.views().getPlayView().updateGameScene(gameContext().currentGame(), true);
    }

    @Override
    public GameUI ui() {
        return ui;
    }

    /**
     * Sets the canvas used for rendering this scene.
     *
     * @param canvas the JavaFX canvas, must not be {@code null}
     */
    public void setCanvas(Canvas canvas) {
        this.canvas = requireNonNull(canvas);
    }

    /**
     * @return the canvas used for rendering this scene
     */
    public Canvas canvas() {
        return canvas;
    }

    /**
     * @return the background color property
     */
    public ObjectProperty<Color> backgroundProperty() {
        return backgroundColor;
    }

    /**
     * @return the current background color
     */
    public Color backgroundColor() {
        return backgroundColor.get();
    }

    /**
     * Sets the background color of the scene.
     *
     * @param color the new background color
     */
    public void setBackgroundColor(Color color) {
        backgroundColor.set(color);
    }

    /**
     * @return the scaling factor property
     */
    public DoubleProperty scalingProperty() {
        return scaling;
    }

    /**
     * Sets the scaling factor applied to the scene.
     *
     * @param value the scaling factor (1.0 = original size)
     */
    public void setScaling(double value) {
        scaling.set(value);
    }

    /**
     * @return the current scaling factor
     */
    public double scaling() {
        return scaling.get();
    }

    /**
     * @return the debug-info visibility property
     */
    public BooleanProperty debugInfoVisibleProperty() {
        return debugInfoVisible;
    }

    /**
     * @return whether debug information is currently visible
     */
    public boolean debugInfoVisible() {
        return debugInfoVisible.get();
    }

    /**
     * Returns the unscaled logical size of the scene in pixels.
     * For arcade-style scenes, this is typically {@code 224×288}.
     *
     * @return the unscaled scene size in pixels
     */
    public Vector2i unscaledSize() {
        return Globals.ARCADE_MAP_SIZE_IN_PIXELS;
    }
}
