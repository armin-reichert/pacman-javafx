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

import static java.util.Objects.requireNonNull;

/**
 * This whole thing need to get clarified.
 */
public class CrudeCanvasContainer extends BorderPane {

    static final Vector2f DOWNSCALING_WHEN_BORDER_VISIBLE = new Vector2f(0.85f, 0.93f);

    private final BooleanProperty borderVisiblePy = new SimpleBooleanProperty(true) {
        @Override
        protected void invalidated() { updateLayout(); }
    };

    private final ObjectProperty<Color> borderColorPy = new SimpleObjectProperty<>(Color.LIGHTBLUE);

    private final BooleanProperty roundedBorderPy = new SimpleBooleanProperty(true) {
        @Override
        protected void invalidated() {
            updateLayout();
        }
    };

    private final DoubleProperty scalingPy = new SimpleDoubleProperty(1.0) {
        @Override
        protected void invalidated() {
            updateLayout();
        }
    };

    private final DoubleProperty unscaledCanvasWidthPy = new SimpleDoubleProperty(500) {
        @Override
        protected void invalidated() {
            updateLayout();
        }
    };

    private final DoubleProperty unscaledCanvasHeightPy = new SimpleDoubleProperty(400) {
        @Override
        protected void invalidated() {
            updateLayout();
        }
    };

    private final Canvas canvas;

    private double minScaling = 1.0;

    public CrudeCanvasContainer(Canvas canvas) {
        this.canvas = requireNonNull(canvas);
        canvas.widthProperty() .bind(scalingPy.multiply(unscaledCanvasWidthPy));
        canvas.heightProperty().bind(scalingPy.multiply(unscaledCanvasHeightPy));
        setCenter(canvas);

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
        }, roundedBorderPy, scalingPy, unscaledCanvasWidthPy, unscaledCanvasHeightPy));

        borderProperty().bind(Bindings.createObjectBinding(() -> {
            if (!hasRoundedBorder() || !isBorderVisible()) {
                return null;
            }
            double bw = Math.max(5, Math.ceil(computeSize().getHeight() / 55)); // TODO avoid magic numbers
            CornerRadii cr = hasRoundedBorder() ? new CornerRadii(Math.ceil(10 * scaling())) : null;
            return new Border(new BorderStroke(borderColor(), BorderStrokeStyle.SOLID, cr, new BorderWidths(bw)));
        }, roundedBorderPy, borderVisiblePy, scalingPy, unscaledCanvasWidthPy, unscaledCanvasHeightPy));
    }

    private void updateLayout() { doLayout(scaling(), true); }

    private void doLayout(double newScaling, boolean forced) {
        if (newScaling < minScaling) {
            Logger.warn("Cannot scale to {}, minimum scaling is {}", newScaling, minScaling);
            return;
        }
        if (!forced && Math.abs(scaling() - newScaling) < 1e-2) { // avoid irrelevant scaling
            Logger.debug("No scaling needed, difference too small");
            return;
        }
        double width = canvas().getWidth();
        double height = canvas().getHeight();
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

    public Canvas canvas() {
        return canvas;
    }

    public void resizeTo(double width, double height) {
        if (hasRoundedBorder()) {
            double downScaledWidth = width, downScaledHeight = height;
            if (isBorderVisible()) {
                downScaledWidth = DOWNSCALING_WHEN_BORDER_VISIBLE.x() * width;
                downScaledHeight = DOWNSCALING_WHEN_BORDER_VISIBLE.y() * height;
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

    public DoubleProperty scalingProperty() { return scalingPy; }
    public double         scaling() {
        return scalingPy.get();
    }
    public void           setScaling(double scaling) {
        scalingPy.set(scaling);
    }

    public void setMinScaling(double value) {
        minScaling = value;
    }

    public double unscaledCanvasWidth() {
        return unscaledCanvasWidthPy.get();
    }
    public double unscaledCanvasHeight() {
        return unscaledCanvasHeightPy.get();
    }

    public void setUnscaledCanvasSize(double width, double height) {
        unscaledCanvasWidthPy.set(width);
        unscaledCanvasHeightPy.set(height);
    }

    public BooleanProperty roundedBorderProperty() { return roundedBorderPy; }
    public boolean         hasRoundedBorder() {
        return roundedBorderPy.get();
    }
    public void            setRoundedBorder(boolean enabled) {
        roundedBorderPy.set(enabled);
    }

    public Color borderColor() {
        return borderColorPy.get();
    }
    public void  setBorderColor(Color color) {
        borderColorPy.set(color);
    }

    public boolean isBorderVisible() {
        return borderVisiblePy.get();
    }
    public void   setBorderVisible(boolean visible) {
        borderVisiblePy.set(visible);
    }
}