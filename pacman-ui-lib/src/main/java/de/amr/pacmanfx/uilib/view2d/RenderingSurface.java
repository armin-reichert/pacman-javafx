package de.amr.pacmanfx.uilib.view2d;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class RenderingSurface {

    private final ObjectProperty<Color> backgroundColor = new SimpleObjectProperty<>(Color.BLACK);

    private final DoubleProperty scaling = new SimpleDoubleProperty(1);

    private final DoubleProperty width = new SimpleDoubleProperty(300);

    private final DoubleProperty height = new SimpleDoubleProperty(300);

    private final Canvas canvas = new Canvas();

    public RenderingSurface() {
        canvas.widthProperty().bind(width);
        canvas.heightProperty().bind(height);
    }

    public GraphicsContext ctx() {
        return canvas.getGraphicsContext2D();
    }

    public Canvas canvas() {
        return canvas;
    }

    public double width() {
        return width.get();
    }

    public DoubleProperty widthProperty() {
        return width;
    }

    public void setWidth(double width) {
        this.width.set(width);
    }

    public double height() {
        return height.get();
    }

    public DoubleProperty heightProperty() {
        return height;
    }

    public void setHeight(double height) {
        this.height.set(height);
    }

    public Color backgroundColor() {
        return backgroundColor.get();
    }

    public ObjectProperty<Color> backgroundColorProperty() {
        return backgroundColor;
    }

    public void setBackgroundColor(Color backgroundColor) {
        this.backgroundColor.set(backgroundColor);
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

    public void clear() {
        ctx().setFill(backgroundColor());
        ctx().fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
    }
}
