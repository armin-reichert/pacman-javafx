/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.basics.ui.rendering;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import static java.util.Objects.requireNonNull;

public interface RenderTarget {

    GraphicsContext ctx();

    double scaling();

    default double scaled(double value) {
        return scaling() * value;
    }

    Color backgroundColor();

    default void fillCanvas(Color color) {
        requireNonNull(color);
        ctx().save();
        ctx().setFill(color);
        ctx().fillRect(0, 0, ctx().getCanvas().getWidth(), ctx().getCanvas().getHeight());
        ctx().restore();
    }

    default void clearCanvas() {
        fillCanvas(backgroundColor());
    }
}
