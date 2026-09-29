package de.amr.pacmanfx.ui.rendering;

import javafx.beans.property.*;
import javafx.scene.canvas.Canvas;
import javafx.scene.paint.Color;

public class RenderTarget {

    private Canvas canvas;

    private final ObjectProperty<Color> backgroundColor = new SimpleObjectProperty<>(Color.BLACK);

    private final IntegerProperty unscaledWidth = new SimpleIntegerProperty();

    private final IntegerProperty unscaledHeight = new SimpleIntegerProperty();

    private final DoubleProperty scaling = new SimpleDoubleProperty(1.0);

    public RenderTarget() {
    }

    public Canvas canvas() {
        return canvas;
    }

    public void setCanvas(Canvas canvas) {
        this.canvas = canvas;
    }

    public Color getBackgroundColor() {
        return backgroundColor.get();
    }

    public ObjectProperty<Color> backgroundColorProperty() {
        return backgroundColor;
    }

    public void setBackgroundColor(Color backgroundColor) {
        this.backgroundColor.set(backgroundColor);
    }

    public int getUnscaledWidth() {
        return unscaledWidth.get();
    }

    public IntegerProperty unscaledWidthProperty() {
        return unscaledWidth;
    }

    public void setUnscaledWidth(int unscaledWidth) {
        this.unscaledWidth.set(unscaledWidth);
    }

    public int getUnscaledHeight() {
        return unscaledHeight.get();
    }

    public IntegerProperty unscaledHeightProperty() {
        return unscaledHeight;
    }

    public void setUnscaledHeight(int unscaledHeight) {
        this.unscaledHeight.set(unscaledHeight);
    }

    public double getScaling() {
        return scaling.get();
    }

    public DoubleProperty scalingProperty() {
        return scaling;
    }

    public void setScaling(double scaling) {
        this.scaling.set(scaling);
    }
}
