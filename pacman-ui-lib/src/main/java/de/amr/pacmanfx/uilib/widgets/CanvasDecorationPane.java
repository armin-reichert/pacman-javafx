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
import javafx.geometry.Dimension2D;
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
        final var stroke = new BorderStroke(strokeColor, BorderStrokeStyle.SOLID,
            new CornerRadii(cornerRadius),
            new BorderWidths(borderWidth));
        return new Border(stroke);
    }

    private final ObjectProperty<Color> borderColor = new SimpleObjectProperty<>(Color.LIGHTBLUE);

    private final DoubleProperty scaling = new SimpleDoubleProperty(1.0) {
        @Override
        protected void invalidated() {
            updateLayout();
        }
    };

    private final ObjectProperty<Dimension2D> unscaledCanvasSize = new SimpleObjectProperty<>(new Dimension2D(500, 400));

    private Canvas canvas;
    private double minScaling = 1.0;

    public CanvasDecorationPane() {
        unscaledCanvasSize.addListener((_, _, _) -> updateLayout());

        clipProperty().bind(Bindings.createObjectBinding(() -> {
                final double arcDiameter = ROUNDED_RECT_ARC_DIAMETER * scaling();
                final Dimension2D scaledSize = computeScaledCanvasSize();
                final var rect = new Rectangle(scaledSize.getWidth(), scaledSize.getHeight());
                rect.setArcHeight(arcDiameter);
                rect.setArcWidth(arcDiameter);
                return rect;
            }, scaling, unscaledCanvasSize)
        );

        borderProperty().bind(Bindings.createObjectBinding(() -> {
                final Dimension2D scaledSize = computeScaledCanvasSize();
                final double proposedBorderWidth = Math.ceil(scaledSize.getHeight() / BORDER_WIDTH_RATIO);
                final double borderWidth = Math.max(ROUNDED_RECT_MIN_BORDER_WIDTH, proposedBorderWidth);
                final double cornerRadius = Math.ceil(ROUNDED_RECT_CORNER_RADIUS * scaling());
                return createRoundedBorder(borderColor.get(), borderWidth, cornerRadius);
            }, borderColor, scaling, unscaledCanvasSize)
        );
    }

    private Dimension2D computeScaledCanvasSize() {
        final double s = scaling();
        final Dimension2D size = unscaledCanvasSize.getValue();
        return new Dimension2D(s * (size.getWidth() + PADDING_X), s * (size.getHeight() + PADDING_Y));
    }

    public void setCanvas(Canvas canvas) {
        this.canvas = requireNonNull(canvas);
        getChildren().setAll(canvas);
        canvas.widthProperty() .bind(scaling.multiply(unscaledCanvasSize.get().getWidth()));
        canvas.heightProperty().bind(scaling.multiply(unscaledCanvasSize.get().getHeight()));
    }

    public void updateLayout() {
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

        final Dimension2D scaledSize = computeScaledCanvasSize();
        setMinSize(scaledSize.getWidth(),  scaledSize.getHeight());
        setMaxSize(scaledSize.getWidth(),  scaledSize.getHeight());
        setPrefSize(scaledSize.getWidth(), scaledSize.getHeight());
    }

    public void resizeTo(double width, double height) {
        final double realWidth  = SCALING_X * width;
        final double realHeight = SCALING_Y * height;
        double newScaling = realHeight / unscaledCanvasSize.get().getHeight();
        if (newScaling * unscaledCanvasSize.get().getWidth() > realWidth) {
            newScaling = realWidth / unscaledCanvasSize.get().getWidth();
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
        unscaledCanvasSize.set(new Dimension2D(width, height));
    }

    public void setBorderColor(Color color) {
        borderColor.set(color);
    }
}