/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.ui._2d;

import de.amr.pacmanfx.lib.Vector2f;
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
 * TODO: This thing needs to get simplified. Too many magic numbers.
 */
public class CanvasDecorationPane extends StackPane {

    private static final Vector2f DOWN_SCALING = new Vector2f(0.85f, 0.93f);

    private static Rectangle createRoundedRect(double width, double height, double arcDiameter) {
        var r = new Rectangle(width, height);
        r.setArcHeight(arcDiameter);
        r.setArcWidth(arcDiameter);
        return r;
    }

    private static Border createRoundedBorder(Paint strokeColor, double width, double cornerRadius) {
        var stroke = new BorderStroke(strokeColor, BorderStrokeStyle.SOLID, new CornerRadii(cornerRadius), new BorderWidths(width));
        return new Border(stroke);
    }

    private final ObjectProperty<Color> borderColor = new SimpleObjectProperty<>(Color.LIGHTBLUE);

    private final DoubleProperty scaling = new SimpleDoubleProperty(1.0) {
        @Override
        protected void invalidated() {
            doLayout(scaling.get(), true);
        }
    };

    private final DoubleProperty unscaledCanvasWidth = new SimpleDoubleProperty(500) {
        @Override
        protected void invalidated() {
            doLayout(scaling.get(), true);
        }
    };

    private final DoubleProperty unscaledCanvasHeight = new SimpleDoubleProperty(400) {
        @Override
        protected void invalidated() {
            doLayout(scaling.get(), true);
        }
    };

    private Canvas canvas;
    private double minScaling = 1.0;

    public CanvasDecorationPane() {
        clipProperty().bind(Bindings.createObjectBinding(() -> {
            return createRoundedRect(computeScaledWidth(), computeScaledHeight(), 26 * scaling.get());
        }, scaling, unscaledCanvasWidth, unscaledCanvasHeight));

        borderProperty().bind(Bindings.createObjectBinding(() -> {
            double borderWidth = Math.max(5, Math.ceil(computeScaledHeight() / 55.0));
            return createRoundedBorder(borderColor(), borderWidth, Math.ceil(10 * scaling.get()));
        }, borderColor, scaling, unscaledCanvasWidth, unscaledCanvasHeight));
    }

    public void setCanvas(Canvas canvas) {
        this.canvas = requireNonNull(canvas);
        getChildren().setAll(canvas);
        canvas.widthProperty() .bind(scaling.multiply(unscaledCanvasWidth));
        canvas.heightProperty().bind(scaling.multiply(unscaledCanvasHeight));
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

        final double width = computeScaledWidth();
        final double height = computeScaledHeight();
        setMinSize(width, height);
        setMaxSize(width, height);
        setPrefSize(width, height);
    }

    private long computeScaledWidth() {
        return Math.round((unscaledCanvasWidth.get() + 25) * scaling.get());
    }

    private long computeScaledHeight() {
        return Math.round((unscaledCanvasHeight.get() + 15) * scaling.get());
    }

    public void resizeTo(double width, double height) {
        final double downScaledWidth = DOWN_SCALING.x() * width;
        final double downScaledHeight = DOWN_SCALING.y() * height;
        double scaling = downScaledHeight / unscaledCanvasHeight.get();
        if (scaling * unscaledCanvasWidth.get() > downScaledWidth) {
            scaling = downScaledWidth / unscaledCanvasWidth.get();
        }
        doLayout(scaling, false);
        Logger.debug("Game canvas container resized to width={} height={}", getWidth(), getHeight());
    }

    public Canvas canvas() {
        return canvas;
    }

    public DoubleProperty scalingProperty() { return scaling; }

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