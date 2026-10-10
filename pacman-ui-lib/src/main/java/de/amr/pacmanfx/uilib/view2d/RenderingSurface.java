package de.amr.pacmanfx.uilib.view2d;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class RenderingSurface {

    private final DoubleProperty scaling = new SimpleDoubleProperty(1);

    private final Canvas canvas = new Canvas(300, 400);

    public RenderingSurface() {}

    public GraphicsContext ctx() {
        return canvas.getGraphicsContext2D();
    }

    public Canvas canvas() {
        return canvas;
    }

    public double scaling() {
        return scaling.get();
    }

    public DoubleProperty scalingProperty() {
        return scaling;
    }

    public void setScaling(double scaling) {
        this.scaling.set(scaling);
    }

    public void fill(Color color) {
        ctx().setFill(color);
        ctx().fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
    }
}
