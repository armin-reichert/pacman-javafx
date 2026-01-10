/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.uilib.widgets;

import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.canvas.Canvas;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;
import org.tinylog.Logger;

import static java.util.Objects.requireNonNull;

/**
 * A canvas container that
 * <ul>
 *     <li>centers the contained canvas</li>
 *     <li>adds an oval border decoration </li>
 * </ul>
 * <p>
 */
public class CanvasDecorationPane extends StackPane {

    private static final float SCALING_X = 0.85f;
    private static final float SCALING_Y = 0.93f;

    private static final int ROUNDED_RECT_ARC_DIAMETER = 26;
    private static final int ROUNDED_RECT_CORNER_RADIUS = 10;
    private static final int ROUNDED_RECT_MIN_BORDER_WIDTH = 5;
    private static final double BORDER_WIDTH_RATIO = 55.0;
    private static final int PADDING_X = 25;
    private static final int PADDING_Y = 15;

    private static Border createRoundedBorder(Paint strokeColor, double borderWidth, double cornerRadius) {
        return new Border(
            new BorderStroke(
                strokeColor,
                BorderStrokeStyle.SOLID,
                new CornerRadii(cornerRadius),
                new BorderWidths(borderWidth)));
    }

    private final ObjectProperty<Color> borderColor = new SimpleObjectProperty<>(Color.LIGHTBLUE);

    private final DoubleProperty scaling = new SimpleDoubleProperty(1.0) {
        @Override
        protected void invalidated() {
            updateLayout();
        }
    };

    private final DoubleProperty unscaledCanvasWidth = new SimpleDoubleProperty(500) {
        @Override
        protected void invalidated() {
            updateLayout();
        }
    };

    private final DoubleProperty unscaledCanvasHeight = new SimpleDoubleProperty(400) {
        @Override
        protected void invalidated() {
            updateLayout();
        }
    };

    private Canvas canvas;
    private double minScaling = 1.0;

    public CanvasDecorationPane() {
        clipProperty().bind(Bindings.createObjectBinding(() -> {
                final double arcDiameter = ROUNDED_RECT_ARC_DIAMETER * scaling();
                final var rect = new Rectangle(scaledWidth(), scaledHeight());
                rect.setArcHeight(arcDiameter);
                rect.setArcWidth(arcDiameter);
                return rect;
            }, scaling, unscaledCanvasWidth, unscaledCanvasHeight)
        );

        borderProperty().bind(Bindings.createObjectBinding(() -> {
                final double proposedBorderWidth = Math.ceil(scaledHeight() / BORDER_WIDTH_RATIO);
                final double borderWidth = Math.max(ROUNDED_RECT_MIN_BORDER_WIDTH, proposedBorderWidth);
                final double cornerRadius = Math.ceil(ROUNDED_RECT_CORNER_RADIUS * scaling());
                return createRoundedBorder(borderColor(), borderWidth, cornerRadius);
            }, borderColor, scaling, unscaledCanvasWidth, unscaledCanvasHeight)
        );
    }

    public void setCanvas(Canvas canvas) {
        this.canvas = requireNonNull(canvas);
        getChildren().setAll(canvas);
        canvas.widthProperty() .bind(scaling.multiply(unscaledCanvasWidth));
        canvas.heightProperty().bind(scaling.multiply(unscaledCanvasHeight));
    }

    private void updateLayout() {
        doLayout(scaling(), true);
    }

    private void doLayout(double newScaling, boolean forced) {
        if (newScaling < minScaling) {
            Logger.warn("Cannot scale to {}, minimum scaling is {}", newScaling, minScaling);
            return;
        }
        if (!forced && Math.abs(scaling.get() - newScaling) < 1e-2) { // ignore tiny scaling changes
            Logger.debug("No scaling needed, difference too small");
            return;
        }
        scaling.set(newScaling);

        final double width = scaledWidth();
        final double height = scaledHeight();
        setMinSize(width, height);
        setMaxSize(width, height);
        setPrefSize(width, height);
    }

    public int unscaledCanvasWidth() {
        return (int) unscaledCanvasWidth.get();
    }

    public int scaledWidth() {
        return (int) ((unscaledCanvasWidth() + PADDING_X) * scaling());
    }

    public int unscaledCanvasHeight() {
        return (int) unscaledCanvasHeight.get();
    }

    public int scaledHeight() {
        return (int) ((unscaledCanvasHeight() + PADDING_Y) * scaling());
    }

    public void resizeTo(double width, double height) {
        final double realWidth  = SCALING_X * width;
        final double realHeight = SCALING_Y * height;
        double newScaling = realHeight / unscaledCanvasHeight();
        if (newScaling * unscaledCanvasWidth() > realWidth) {
            newScaling = realWidth / unscaledCanvasWidth();
        }
        doLayout(newScaling, false);
        Logger.debug("Canvas container resized to width={} height={}", getWidth(), getHeight());
    }

    public Canvas canvas() {
        return canvas;
    }

    public DoubleProperty scalingProperty() { return scaling; }

    public double scaling() {
        return scalingProperty().get();
    }

    public void setMinScaling(double value) {
        minScaling = value;
    }

    public void setUnscaledCanvasSize(double width, double height) {
        unscaledCanvasWidth.set(width);
        unscaledCanvasHeight.set(height);
    }

    public Color borderColor() {
        return borderColor.get();
    }

    public void setBorderColor(Color color) {
        borderColor.set(color);
    }
}