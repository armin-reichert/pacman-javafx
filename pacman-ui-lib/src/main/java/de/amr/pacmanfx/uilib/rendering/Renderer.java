/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.uilib.rendering;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Paint;

public interface Renderer {

    GraphicsContext ctx();

    DoubleProperty scalingProperty();

    double scaling();

    void setScaling(double value);

    default double scaled(double value) { return scaling() * value; }

    ObjectProperty<Paint> backgroundProperty();

    void setBackground(Paint paint);

    Paint background();

    boolean imageSmoothing();

    void setImageSmoothing(boolean b);
}