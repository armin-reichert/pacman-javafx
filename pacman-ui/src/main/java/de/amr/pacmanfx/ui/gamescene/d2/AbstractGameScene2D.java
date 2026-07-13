/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.ui.gamescene.d2;

import de.amr.basics.math.Vector2i;
import de.amr.pacmanfx.core.Validations;
import de.amr.pacmanfx.core.model.level.GameLevel;
import de.amr.pacmanfx.core.model.world.WorldMap;
import de.amr.pacmanfx.ui.action.core.GameActionContext;
import de.amr.pacmanfx.ui.gamescene.common.AbstractGameScene;
import de.amr.pacmanfx.uilib.rendering.Renderer;
import javafx.beans.property.*;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.ContextMenu;
import javafx.scene.input.ScrollEvent;
import javafx.scene.paint.Color;

import java.util.Optional;

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
public abstract class AbstractGameScene2D extends AbstractGameScene {

    private final IntegerProperty unscaledWidth = new SimpleIntegerProperty(WorldMap.ARCADE_MAP_SIZE_IN_PIXELS.x());

    private final IntegerProperty unscaledHeight = new SimpleIntegerProperty(WorldMap.ARCADE_MAP_SIZE_IN_PIXELS.y());

    private final DoubleProperty scaling = new SimpleDoubleProperty(1.0);

    private final ObjectProperty<Color> backgroundColor = new SimpleObjectProperty<>(Color.BLACK);

    protected Canvas canvas;

    public AbstractGameScene2D(GameActionContext actionContext) {
        super(actionContext);
    }

    @Override
    public void onBeforeEmbedded() {
        //TODO remove this hook method
    }

    @Override
    public void onScroll(ScrollEvent scrollEvent) {
        // Used only by very few subclasses
    }

    @Override
    public Optional<ContextMenu> optContextMenu() {
        return Optional.empty();
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

    /** @return the unscaled scene width property */
    public IntegerProperty unscaledWidthProperty() {
        return unscaledWidth;
    }

    /** @return the unscaled scene width in pixels */
    public int unscaledWidth() {
        return unscaledWidthProperty().get();
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
    public double width() {
        return scaling() * unscaledWidth();
    }

    /** @return the scaled scene height in pixels */
    public double height() {
        return scaling() * unscaledHeight();
    }

    /** @return the aspect ratio (width / height) */
    public double aspectRatio() {
        return width() / height();
    }

    /**
     * If a 3D-variant of this game scene is active when the game level gets created, this method has not yet been called,
     * but it gets called when the 3D->2D scene switch happens.
     */
    public void acceptGameLevel(GameLevel level) {
        final Vector2i terrainSize = level.worldMap().terrainLayer().sizeInPixel();
        unscaledWidthProperty().set(terrainSize.x());
        unscaledHeightProperty().set(terrainSize.y());
    }
}
