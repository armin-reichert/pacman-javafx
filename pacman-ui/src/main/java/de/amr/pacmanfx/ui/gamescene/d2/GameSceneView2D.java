/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.gamescene.d2;

import de.amr.basics.Disposable;
import de.amr.basics.math.RectShort;
import de.amr.pacmanfx.uilib.view2d.RenderingSurface;
import javafx.beans.property.FloatProperty;
import javafx.beans.property.SimpleFloatProperty;
import javafx.scene.ParallelCamera;
import javafx.scene.PerspectiveCamera;

public class GameSceneView2D implements Disposable {

    private RenderingSurface renderingSurface;

    private final FloatProperty unscaledWidth = new SimpleFloatProperty();

    private final FloatProperty unscaledHeight = new SimpleFloatProperty();

    private boolean autoClearCanvas = true;

    private RectShort clipRect;

    private ParallelCamera camera;

    public GameSceneView2D() {
        setUnscaledWidth(300);
        setUnscaledHeight(400);
    }

    @Override
    public void dispose() {
        unscaledWidth.unbind();
        unscaledHeight.unbind();
    }

    public RenderingSurface renderingSurface() {
        return renderingSurface;
    }

    public void setRenderingSurface(RenderingSurface renderingSurface) {
        this.renderingSurface = renderingSurface;
    }

    public boolean autoClearCanvas() {
        return autoClearCanvas;
    }

    public void setAutoClearCanvas(boolean autoClearCanvas) {
        this.autoClearCanvas = autoClearCanvas;
    }

    public void setUnscaledWidth(int value) {
        unscaledWidth.set(value);
    }

    /** @return the unscaled scene width property */
    public FloatProperty unscaledWidthProperty() {
        return unscaledWidth;
    }

    /** @return the unscaled scene width in pixels */
    public float unscaledWidth() {
        return unscaledWidthProperty().get();
    }

    public void setUnscaledHeight(float value) {
        unscaledHeight.set(value);
    }

    /** @return the unscaled scene height property */
    public FloatProperty unscaledHeightProperty() {
        return unscaledHeight;
    }

    /** @return the unscaled scene height in pixels */
    public float unscaledHeight() {
        return unscaledHeightProperty().get();
    }

    public RectShort clipRect() {
        return clipRect;
    }

    public void setClipRect(RectShort clipRect) {
        this.clipRect = clipRect;
    }

    public ParallelCamera camera() {
        return camera;
    }

    public void setCamera(ParallelCamera camera) {
        this.camera = camera;
    }
}
