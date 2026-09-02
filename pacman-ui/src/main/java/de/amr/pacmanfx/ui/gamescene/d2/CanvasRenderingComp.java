/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.gamescene.d2;

import de.amr.basics.Disposable;
import de.amr.pacmanfx.core.Validations;
import de.amr.pacmanfx.core.model.world.map.WorldMap;
import de.amr.pacmanfx.ui.gamescene.common.GameSceneComponent;
import de.amr.pacmanfx.uilib.rendering.Renderer;
import javafx.beans.property.*;
import javafx.scene.canvas.Canvas;
import javafx.scene.paint.Color;

import static java.util.Objects.requireNonNull;

public class CanvasRenderingComp implements GameSceneComponent, Disposable {

    private final IntegerProperty unscaledWidth = new SimpleIntegerProperty();

    private final IntegerProperty unscaledHeight = new SimpleIntegerProperty();

    private final DoubleProperty scaling = new SimpleDoubleProperty(1.0);

    private final ObjectProperty<Color> backgroundColor = new SimpleObjectProperty<>(Color.BLACK);

    private final ObjectProperty<Canvas> canvas = new SimpleObjectProperty<>();

    private boolean clearCanvasBeforeRendering;

    public CanvasRenderingComp() {
        this(WorldMap.ARCADE_MAP_SIZE_IN_PIXELS.x(), WorldMap.ARCADE_MAP_SIZE_IN_PIXELS.y());
    }

    public CanvasRenderingComp(int width, int height) {
        setUnscaledWidth(width);
        setUnscaledHeight(height);
        clearCanvasBeforeRendering = true;
    }

    @Override
    public void dispose() {
        unscaledHeight.unbind();
        unscaledHeight.unbind();
        scaling.unbind();
        backgroundColor.unbind();
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
     * Assigns the canvas inside which this scene is drawn.
     *
     * @param canvas the JavaFX canvas, must not be {@code null}
     */
    public void setCanvas(Canvas canvas) {
        canvasProperty().set(requireNonNull(canvas));
    }

    /**
     * @return the canvas used for rendering this scene
     */
    public Canvas canvas() {
        return canvasProperty().get();
    }

    public ObjectProperty<Canvas> canvasProperty() {
        return canvas;
    }

    /** @return the background color property */
    public ObjectProperty<Color> backgroundColorProperty() {
        return backgroundColor;
    }

    /** @return the current background color */
    public Color backgroundColor() {
        return backgroundColorProperty().get();
    }

    /**
     * Sets the background color of the scene.
     *
     * @param color the new background color
     */
    public void setBackgroundColor(Color color) {
        backgroundColorProperty().set(color);
    }

    public void setUnscaledWidth(int value) {
        unscaledWidth.set(value);
    }

    /** @return the unscaled scene width property */
    public IntegerProperty unscaledWidthProperty() {
        return unscaledWidth;
    }

    /** @return the unscaled scene width in pixels */
    public int unscaledWidth() {
        return unscaledWidthProperty().get();
    }

    public void setUnscaledHeight(int value) {
        unscaledHeight.set(value);
    }

    /** @return the unscaled scene height property */
    public IntegerProperty unscaledHeightProperty() {
        return unscaledHeight;
    }

    /** @return the unscaled scene height in pixels */
    public int unscaledHeight() {
        return unscaledHeightProperty().get();
    }

    /** @return the scaling factor property */
    public DoubleProperty scalingProperty() {
        return scaling;
    }

    /** @return the current scaling factor */
    public double scaling() {
        return scaling.get();
    }

    /**
     * Sets the scaling factor applied to the scene.
     *
     * @param value the scaling factor (1.0 = original size)
     */
    public void setScaling(double value) {
        Validations.requireNonNegative(value);
        scalingProperty().set(value);
    }

    /** @return the scaled scene width in pixels */
    public double scaledWidth() {
        return scaling() * unscaledWidth();
    }

    /** @return the scaled scene height in pixels */
    public double scaledHeight() {
        return scaling() * unscaledHeight();
    }

    /** @return the aspect ratio (width / height) */
    public double aspectRatio() {
        return scaledWidth() / scaledHeight();
    }

    public boolean clearCanvasBeforeRendering() {
        return clearCanvasBeforeRendering;
    }

    public void setClearCanvasBeforeRendering(boolean clearCanvasBeforeRendering) {
        this.clearCanvasBeforeRendering = clearCanvasBeforeRendering;
    }
}
