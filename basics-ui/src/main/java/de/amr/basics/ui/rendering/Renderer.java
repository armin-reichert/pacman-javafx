/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.basics.ui.rendering;

import de.amr.basics.InfoMap;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public interface Renderer {

    Renderer NULL_RENDERER = new Renderer() {

        @Override
        public void render(Renderable r, long tick) {}

        @Override
        public InfoMap info() {return null;}

        @Override
        public GraphicsContext ctx() {return null;}

        @Override
        public void clearCanvas() {}

        @Override
        public void fillCanvas(Color color) {}

        @Override
        public DoubleProperty scalingProperty() {return null;}

        @Override
        public double scaling() {return 0;}

        @Override
        public void setScaling(double value) {}

        @Override
        public ObjectProperty<Color> backgroundColorProperty() {return null;}
    };

    int HTS = 4; // half tile size

    int TS = 8;

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
}