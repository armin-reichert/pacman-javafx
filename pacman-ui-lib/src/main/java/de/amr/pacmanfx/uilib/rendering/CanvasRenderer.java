/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.uilib.rendering;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import static java.util.Objects.requireNonNull;

public interface CanvasRenderer {

    Canvas canvas();

    GraphicsContext ctx();

    DoubleProperty scalingProperty();

    default double scaling() {
        return scalingProperty().get();
    }

    default void setScaling(double value) {
        if (value <= 0) {
            throw new IllegalArgumentException("Scaling value must be positive but is %.2f".formatted(value));
        }
        scalingProperty().set(value);
    }

    default double scaled(double value) { return scaling() * value; }

    ObjectProperty<Color> backgroundColorProperty();

    default void setBackgroundColor(Color color) {
        requireNonNull(color);
        backgroundColorProperty().set(color);
    }

    default Color backgroundColor() {
        return backgroundColorProperty().get();
    }
}
