/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.uilib.rendering;

import javafx.beans.property.DoubleProperty;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;

public interface CanvasRenderer {

    Canvas canvas();

    GraphicsContext ctx();

    DoubleProperty scalingProperty();

    default double scaling() {
        return scalingProperty().get();
    }

    default void setScaling(double s) {
        scalingProperty().set(s);
    }
}
