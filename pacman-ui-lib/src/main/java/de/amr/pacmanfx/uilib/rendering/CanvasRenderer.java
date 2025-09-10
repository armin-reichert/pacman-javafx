/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.uilib.rendering;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;

public interface CanvasRenderer {

    GraphicsContext ctx();

    DoubleProperty scalingProperty();

    double scaling();

    void setScaling(double value);

    default double scaled(double value) { return scaling() * value; }

    ObjectProperty<Color> backgroundColorProperty();

    void setBackgroundColor(Color color);

    Color backgroundColor();

    void fillCanvas(Paint paint);

    default void clearCanvas() {
        fillCanvas(backgroundColor());
    }
}
