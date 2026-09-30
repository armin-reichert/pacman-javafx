/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.gamescene.d2;

import de.amr.basics.Disposable;
import de.amr.basics.math.RectShort;
import de.amr.pacmanfx.uilib.view2d.RenderingSurface;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;

public class GameSceneRendering2DComp implements Disposable {

    private RenderingSurface renderingSurface;

    private final IntegerProperty unscaledWidth = new SimpleIntegerProperty();

    private final IntegerProperty unscaledHeight = new SimpleIntegerProperty();

    private boolean autoClearCanvas = true;

    private RectShort clipRect;

    public GameSceneRendering2DComp() {
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

    public RectShort clipRect() {
        return clipRect;
    }

    public void setClipRect(RectShort clipRect) {
        this.clipRect = clipRect;
    }
}
