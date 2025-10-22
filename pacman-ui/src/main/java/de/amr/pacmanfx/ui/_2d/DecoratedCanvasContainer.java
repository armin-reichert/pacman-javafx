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
import javafx.geometry.Dimension2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import org.tinylog.Logger;

/**
 * TODO: This thing needs to get simplified.
 */
public class DecoratedCanvasContainer extends BorderPane {

    static final Vector2f SCALING_WHEN_BORDER_VISIBLE = new Vector2f(0.85f, 0.93f);

    private final ObjectProperty<Color> borderColor = new SimpleObjectProperty<>(Color.LIGHTBLUE);

    private final DoubleProperty scaling = new SimpleDoubleProperty(1.0) {
        @Override
        protected void invalidated() {
            doLayout(scaling(), true);
        }
    };

    private final DoubleProperty unscaledCanvasWidth = new SimpleDoubleProperty(500) {
        @Override
        protected void invalidated() {
            doLayout(scaling(), true);
        }
    };

    private final DoubleProperty unscaledCanvasHeight = new SimpleDoubleProperty(400) {
        @Override
        protected void invalidated() {
            doLayout(scaling(), true);
        }
    };

    private final Canvas canvas = new Canvas();
    private double minScaling = 1.0;

    public DecoratedCanvasContainer() {
        setCenter(canvas);

        canvas.widthProperty() .bind(scaling.multiply(unscaledCanvasWidth));
        canvas.heightProperty().bind(scaling.multiply(unscaledCanvasHeight));

        clipProperty().bind(Bindings.createObjectBinding(() -> {
            Dimension2D size = computeSize();
            var clipRect = new Rectangle(size.getWidth(), size.getHeight());
            double arcSize = 26 * scaling(); // TODO avoid magic numbers
            clipRect.setArcWidth(arcSize);
            clipRect.setArcHeight(arcSize);
            return clipRect;
        }, scaling, unscaledCanvasWidth, unscaledCanvasHeight));

        borderProperty().bind(Bindings.createObjectBinding(() -> {
            double borderWidth = Math.max(5, Math.ceil(computeSize().getHeight() / 55)); // TODO avoid magic numbers
            return new Border(
                new BorderStroke(
                    borderColor(),
                    BorderStrokeStyle.SOLID,
                    new CornerRadii(Math.ceil(10 * scaling())),
                    new BorderWidths(borderWidth)
                )
            );
        }, scaling, unscaledCanvasWidth, unscaledCanvasHeight));
    }

    private void doLayout(double newScaling, boolean forced) {
        if (newScaling < minScaling) {
            Logger.warn("Cannot scale to {}, minimum scaling is {}", newScaling, minScaling);
            return;
        }
        if (!forced && Math.abs(scaling() - newScaling) < 1e-2) { // avoid irrelevant scaling
            Logger.debug("No scaling needed, difference too small");
            return;
        }
        setScaling(newScaling);

        final Dimension2D size = computeSize();
        final double width = size.getWidth();
        final double height = size.getHeight();
        setMinSize(width, height);
        setMaxSize(width, height);
        setPrefSize(width, height);

        Logger.debug("Unscaled canvas size: w={0.0} h={0.0}", unscaledCanvasWidth(), unscaledCanvasHeight());
        Logger.debug("Canvas size: w={0.0} h={0.0} aspect={0.00} scaling={0.00}",
            canvas.getWidth(), canvas.getHeight(), canvas.getWidth() / canvas.getHeight(), scaling());
    }

    private Dimension2D computeSize() {
        return new Dimension2D(
            Math.round((unscaledCanvasWidth()  + 25) * scaling()), // TODO avoid magic numbers
            Math.round((unscaledCanvasHeight() + 15) * scaling())  // TODO avoid magic numbers
        );
    }

    public void resizeTo(double width, double height) {
        final double downScaledWidth = SCALING_WHEN_BORDER_VISIBLE.x() * width;
        final double downScaledHeight = SCALING_WHEN_BORDER_VISIBLE.y() * height;
        double scaling = downScaledHeight / unscaledCanvasHeight();
        if (scaling * unscaledCanvasWidth() > downScaledWidth) {
            scaling = downScaledWidth / unscaledCanvasWidth();
        }
        doLayout(scaling, false);
        Logger.debug("Game canvas container resized to width={} height={}", getWidth(), getHeight());
    }

    public Canvas canvas() {
        return canvas;
    }

    public DoubleProperty scalingProperty() { return scaling; }

    public double scaling() {
        return scaling.get();
    }

    public void setScaling(double value) {
        scaling.set(value);
    }

    public void setMinScaling(double value) {
        minScaling = value;
    }

    public double unscaledCanvasWidth() {
        return unscaledCanvasWidth.get();
    }

    public double unscaledCanvasHeight() {
        return unscaledCanvasHeight.get();
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