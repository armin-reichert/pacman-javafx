/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.ui.d2;

import de.amr.pacmanfx.Globals;
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
 * A {@code GameScene2D} manages the rendering surface ({@link Canvas}),
 * unscaled scene dimensions, scaling, and background color. Subclasses
 * implement scene-specific activation, deactivation, and rendering logic.
 * <p>
 * The scene is reusable: {@link #onActivate()} and {@link #onDeactivate()}
 * must establish and release all bindings, listeners, and resources created
 * by the subclass.
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
     * Called when the scene is permanently discarded and will not be reused.
     * Subclasses overriding this method must call {@code super.dispose()}.
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
     * @param renderer the renderer to configure
     * @return the same renderer instance for fluent usage
     */
    public <T extends Renderer> T configureRenderer(T renderer) {
        renderer.backgroundColorProperty().bind(backgroundColorProperty());
        renderer.scalingProperty().bind(scalingProperty());
        return renderer;
    }

    /**
     * Hook called when entering this 2D scene from a 3D scene.
     * Subclasses may override to adjust state or transitions.
     */
    public void onEnteredFrom3DScene() {}

    /**
     * Assigns the canvas used for rendering this scene.
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

    /** @return the background color property */
    public ObjectProperty<Color> backgroundColorProperty() {
        return backgroundColor;
    }

    /** @return the current background color */
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

    /** @return the unscaled scene width property */
    public IntegerProperty unscaledWidthProperty() {
        return unscaledWidth;
    }

    /** @return the unscaled scene width in pixels */
    public int getUnscaledWidth() {
        return unscaledWidth.get();
    }

    /** @return the unscaled scene height property */
    public IntegerProperty unscaledHeightProperty() {
        return unscaledHeight;
    }

    /** @return the unscaled scene height in pixels */
    public int getUnscaledHeight() {
        return unscaledHeight.get();
    }

    /** @return the scaling factor property */
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

    /** @return the current scaling factor */
    public double scaling() {
        return scaling.get();
    }

    /** @return the scaled scene width in pixels */
    public double getWidth() {
        return scaling() * getUnscaledWidth();
    }

    /** @return the scaled scene height in pixels */
    public double getHeight() {
        return scaling() * getUnscaledHeight();
    }

    /** @return the aspect ratio (width / height) */
    public double getAspectRatio() {
        return getWidth() / getHeight();
    }
}
