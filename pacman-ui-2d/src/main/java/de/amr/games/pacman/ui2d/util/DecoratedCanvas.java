/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d.util;

import javafx.beans.binding.Bindings;
import javafx.beans.property.*;
import javafx.geometry.Dimension2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

/**
 * @author Armin Reichert
 */
public class DecoratedCanvas extends BorderPane {

    public static Dimension2D computeSize(double unscaledCanvasWidth, double unscaledCanvasHeight, double scaling) {
        return new Dimension2D(
            Math.round(unscaledCanvasWidth  * scaling + 25 * scaling), // TODO avoid magic numbers
            Math.round(unscaledCanvasHeight * scaling + 15 * scaling)  // TODO avoid magic numbers
        );
    }

    public final DoubleProperty scalingPy              = new SimpleDoubleProperty(this, "scaling", 1.0);
    public final BooleanProperty decoratedPy           = new SimpleBooleanProperty(this, "decorated", true);
    public final DoubleProperty unscaledCanvasWidthPy  = new SimpleDoubleProperty(this, "unscaledCanvasWidth", 500);
    public final DoubleProperty unscaledCanvasHeightPy = new SimpleDoubleProperty(this, "unscaledCanvasHeight", 400);
    public final ObjectProperty<Color> borderColorPy   = new SimpleObjectProperty<>(this, "borderColor", Color.LIGHTBLUE);

    private final Canvas canvas = new Canvas();

    public DecoratedCanvas() {
        setCenter(canvas);
        canvas.widthProperty().bind(unscaledCanvasWidthPy.multiply(scalingPy));
        canvas.heightProperty().bind(unscaledCanvasHeightPy.multiply(scalingPy));

        clipProperty().bind(Bindings.createObjectBinding(() -> {
            if (!isDecorated()) {
                return null;
            }
            var clipRect = new Rectangle(getSize().getWidth(), getSize().getHeight());
            // TODO avoid magic numbers
            double diameter = 26 * scaling();
            clipRect.setArcWidth(diameter);
            clipRect.setArcHeight(diameter);
            return clipRect;
        }, decoratedPy, scalingPy, unscaledCanvasWidthPy, unscaledCanvasHeightPy));

        borderProperty().bind(Bindings.createObjectBinding(() -> {
            if (!isDecorated()) {
                return null;
            }
            // TODO avoid magic numbers
            double w = Math.max(5, Math.ceil(getSize().getHeight() / 55));
            double r = Math.ceil(10 * scaling());
            return new Border(
                new BorderStroke(borderColor(), BorderStrokeStyle.SOLID, new CornerRadii(r), new BorderWidths(w)));
        }, decoratedPy, scalingPy, unscaledCanvasWidthPy, unscaledCanvasHeightPy));
    }

    public Canvas canvas() {
        return canvas;
    }

    public Dimension2D getSize() {
        return computeSize(unscaledCanvasWidth(), unscaledCanvasHeight(), scaling());
    }

    public double scaling() {
        return scalingPy.get();
    }

    public void setScaling(double scaling) {
        scalingPy.set(scaling);
    }

    public double unscaledCanvasWidth() {
        return unscaledCanvasWidthPy.get();
    }

    public void setUnscaledCanvasWidth(double w) {
        unscaledCanvasWidthPy.set(w);
    }

    public double unscaledCanvasHeight() {
        return unscaledCanvasHeightPy.get();
    }

    public void setUnscaledCanvasHeight(double h) {
        unscaledCanvasHeightPy.set(h);
    }

    public boolean isDecorated() {
        return decoratedPy.get();
    }

    public void setDecorated(boolean enabled) {
        decoratedPy.set(enabled);
    }

    public Color borderColor() {
        return borderColorPy.get();
    }

    public void setBorderColor(Color color) {
        borderColorPy.set(color);
    }
}