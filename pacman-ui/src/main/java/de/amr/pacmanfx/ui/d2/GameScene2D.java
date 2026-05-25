/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.ui.d2;

import de.amr.pacmanfx.Globals;
import de.amr.pacmanfx.model.Game;
import de.amr.pacmanfx.ui.GameScene;
import de.amr.pacmanfx.ui.GameUI;
import de.amr.pacmanfx.uilib.rendering.Renderer;
import javafx.beans.property.*;
import javafx.scene.canvas.Canvas;
import javafx.scene.paint.Color;

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
 * Subclasses implement {@link #onSceneStart()} and {@link #onSceneEnd()} to define
 * scene-specific behavior.
 */
public class GameScene2D extends GameScene {

    private final IntegerProperty unscaledWidth = new SimpleIntegerProperty();
    private final IntegerProperty unscaledHeight = new SimpleIntegerProperty();
    private final DoubleProperty scaling = new SimpleDoubleProperty(1.0f);
    private final ObjectProperty<Color> backgroundColor = new SimpleObjectProperty<>(Color.BLACK);

    protected Canvas canvas;

    public GameScene2D(GameUI ui) {
        super(ui);
        unscaledWidth.set(Globals.ARCADE_MAP_SIZE_IN_PIXELS.x());
        unscaledHeight.set(Globals.ARCADE_MAP_SIZE_IN_PIXELS.y());
    }

    /**
     * Releases bindings and resources held by this scene.
     * Called when the scene is permanently discarded.
     */
    @Override
    public void dispose() {
        backgroundColor.unbind();
        scaling.unbind();
    }

    /**
     * Binds renderer properties (background color, scaling) to this scene's
     * corresponding properties.
     *
     * @param <T>      renderer type
     * @param renderer the renderer to adapt
     * @return the same renderer instance for fluent usage
     */
    public <T extends Renderer> T configureRenderer(T renderer) {
        renderer.backgroundColorProperty().bind(backgroundColorProperty());
        renderer.scalingProperty().bind(scalingProperty());
        return renderer;
    }

    // TODO: rethink
    public void onEnteredFrom3DScene() {}

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
    public ObjectProperty<Color> backgroundColorProperty() {
        return backgroundColor;
    }

    /**
     * @return the current background color
     */
    public Color getBackgroundColor() {
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

    public IntegerProperty unscaledWidthProperty() {
        return unscaledWidth;
    }

    public int getUnscaledWidth() {
        return unscaledWidth.get();
    }

    public IntegerProperty unscaledHeightProperty() {
        return unscaledHeight;
    }

    public int getUnscaledHeight() {
        return unscaledHeight.get();
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

    public double getWidth() {
        return scaling() * getUnscaledWidth();
    }

    public double getHeight() {
        return scaling() * getUnscaledHeight();
    }

    public double getAspectRatio() {
        return getWidth() / getHeight();
    }
}
