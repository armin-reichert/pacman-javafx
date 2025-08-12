/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.ui._2d;

import de.amr.pacmanfx.lib.Vector2f;
import javafx.beans.binding.Bindings;
import javafx.beans.property.*;
import javafx.geometry.Dimension2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import org.tinylog.Logger;

/**
 * This thing needs to get simplified.
 */
public class CanvasWithFrame extends BorderPane {

    static final Vector2f SCALING_WHEN_BORDER_VISIBLE = new Vector2f(0.85f, 0.93f);

    private final BooleanProperty borderVisible = new SimpleBooleanProperty(true) {
        @Override
        protected void invalidated() { doLayout(scaling(), true); }
    };

    private final ObjectProperty<Color> borderColor = new SimpleObjectProperty<>(Color.LIGHTBLUE);

    private final BooleanProperty roundedBorder = new SimpleBooleanProperty(true) {
        @Override
        protected void invalidated() {
            doLayout(scaling(), true);
        }
    };

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

    public CanvasWithFrame() {
        setCenter(canvas);

        canvas.widthProperty() .bind(scaling.multiply(unscaledCanvasWidth));
        canvas.heightProperty().bind(scaling.multiply(unscaledCanvasHeight));

        clipProperty().bind(Bindings.createObjectBinding(() -> {
            if (!hasRoundedBorder()) {
                return null;
            }
            Dimension2D size = computeSize();
            var clipRect = new Rectangle(size.getWidth(), size.getHeight());
            if (hasRoundedBorder()) {
                double arcSize = 26 * scaling(); // TODO avoid magic numbers
                clipRect.setArcWidth(arcSize);
                clipRect.setArcHeight(arcSize);
            }
            return clipRect;
        }, roundedBorder, scaling, unscaledCanvasWidth, unscaledCanvasHeight));

        borderProperty().bind(Bindings.createObjectBinding(() -> {
            if (!hasRoundedBorder() || !isBorderVisible()) {
                return null;
            }
            double bw = Math.max(5, Math.ceil(computeSize().getHeight() / 55)); // TODO avoid magic numbers
            CornerRadii cr = hasRoundedBorder() ? new CornerRadii(Math.ceil(10 * scaling())) : null;
            return new Border(new BorderStroke(borderColor(), BorderStrokeStyle.SOLID, cr, new BorderWidths(bw)));
        }, roundedBorder, borderVisible, scaling, unscaledCanvasWidth, unscaledCanvasHeight));
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
        double width = canvas.getWidth(), height = canvas.getHeight();
        if (hasRoundedBorder()) {
            Dimension2D size = computeSize();
            width = size.getWidth();
            height = size.getHeight();
        }
        setMinSize(width, height);
        setMaxSize(width, height);
        setPrefSize(width, height);
        setScaling(newScaling);

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
        if (hasRoundedBorder()) {
            double downScaledWidth = width, downScaledHeight = height;
            if (isBorderVisible()) {
                downScaledWidth = SCALING_WHEN_BORDER_VISIBLE.x() * width;
                downScaledHeight = SCALING_WHEN_BORDER_VISIBLE.y() * height;
            }
            double scaling = downScaledHeight / unscaledCanvasHeight();
            if (scaling * unscaledCanvasWidth() > downScaledWidth) {
                scaling = downScaledWidth / unscaledCanvasWidth();
            }
            doLayout(scaling, false);
        } else {
            doLayout(height / unscaledCanvasHeight(), false);
        }
        Logger.debug("Game canvas container resized to width={} height={}", getWidth(), getHeight());
    }

    public Canvas canvas() {
        return canvas;
    }

    public DoubleProperty scalingProperty() { return scaling; }
    public double scaling() {
        return scaling.get();
    }
    public void setScaling(double scaling) {
        this.scaling.set(scaling);
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

    public BooleanProperty roundedBorderProperty() { return roundedBorder; }

    public boolean hasRoundedBorder() {
        return roundedBorder.get();
    }

    public void setRoundedBorder(boolean enabled) {
        roundedBorder.set(enabled);
    }

    public Color borderColor() {
        return borderColor.get();
    }

    public void setBorderColor(Color color) {
        borderColor.set(color);
    }

    public boolean isBorderVisible() {
        return borderVisible.get();
    }

    public void setBorderVisible(boolean visible) {
        borderVisible.set(visible);
    }
}