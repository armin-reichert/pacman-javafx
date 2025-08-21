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
