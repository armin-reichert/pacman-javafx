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
import org.tinylog.Logger;

/**
 * @author Armin Reichert
 */
public class DecoratedCanvas extends BorderPane {

    public static Dimension2D computeSize(double unscaledCanvasWidth, double unscaledCanvasHeight, double scaling) {
        return new Dimension2D(
         Math.round(unscaledCanvasWidth * scaling + 25 * scaling), // TODO magic number
            Math.round(unscaledCanvasHeight * scaling + 15 * scaling) // TODO magic number
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

        clipProperty().bind(Bindings.createObjectBinding(
            () -> {
                if (decoratedPy.get()) {
                    var clipRect = new Rectangle(getSize().getWidth(), getSize().getHeight());
                    clipRect.setArcWidth(26 * getScaling());
                    clipRect.setArcHeight(26 * getScaling());
                    return clipRect;
                }
                return null;
            }, decoratedPy, scalingPy, unscaledCanvasWidthPy, unscaledCanvasHeightPy
        ));

        borderProperty().bind(Bindings.createObjectBinding(
            () -> {
                if (decoratedPy.get()) {
                    double borderWidth = Math.max(5, Math.ceil(getSize().getHeight() / 55)); // TODO magic number
                    double cornerRadius = Math.ceil(10 * getScaling());
                    return new Border(
                        new BorderStroke(borderColorPy.get(),
                            BorderStrokeStyle.SOLID,
                            new CornerRadii(cornerRadius),
                            new BorderWidths(borderWidth)));
                }
                return null;
            },
            decoratedPy, scalingPy, unscaledCanvasWidthPy, unscaledCanvasHeightPy
        ));
    }

    public Canvas canvas() {
        return canvas;
    }

    public Dimension2D getSize() {
        return computeSize(getUnscaledCanvasWidth(), getUnscaledCanvasHeight(), getScaling());
    }

    public double getScaling() {
        return scalingPy.get();
    }

    public void setScaling(double scaling) {
        scalingPy.set(scaling);
    }

    public double getUnscaledCanvasWidth() {
        return unscaledCanvasWidthPy.get();
    }

    public void setUnscaledCanvasWidth(double w) {
        unscaledCanvasWidthPy.set(w);
    }

    public double getUnscaledCanvasHeight() {
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

    public Color getBorderColor() {
        return borderColorPy.get();
    }

    public void setBorderColor(Color color) {
        borderColorPy.set(color);
    }

    public void logCanvasSize() {
        Logger.debug("Unscaled canvas size: w={0.0} h={0.0}", getUnscaledCanvasWidth(), getUnscaledCanvasHeight());
        Logger.debug("Canvas size: w={0.0} h={0.0}", canvas.getWidth(), canvas.getHeight());
    }
}