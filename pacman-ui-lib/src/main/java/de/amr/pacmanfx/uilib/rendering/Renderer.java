/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.uilib.rendering;

import de.amr.basics.InfoMap;
import de.amr.pacmanfx.core.rendering.Renderable;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import java.util.Optional;

public interface Renderer {

    void render(Renderable r, long tick);

    InfoMap info();

    GraphicsContext ctx();

    default Canvas canvas() {
        return ctx().getCanvas();
    }

    void clearCanvas();

    void fillCanvas(Color color);

    DoubleProperty scalingProperty();

    double scaling();

    void setScaling(double value);

    default double scaled(double value) { return scaling() * value; }

    ObjectProperty<Color> backgroundColorProperty();

    default Color backgroundColor() {
        return backgroundColorProperty().get();
    }

    default Optional<BaseRenderer> optDebugInfoRenderer() {
        return Optional.empty();
    }
}