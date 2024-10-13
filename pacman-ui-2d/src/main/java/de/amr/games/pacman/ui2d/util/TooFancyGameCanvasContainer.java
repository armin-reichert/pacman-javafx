/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d.util;

import de.amr.games.pacman.lib.Vector2f;
import javafx.beans.binding.Bindings;
import javafx.beans.property.*;
import javafx.geometry.Dimension2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import org.tinylog.Logger;

import static de.amr.games.pacman.lib.Globals.checkNotNull;

/**
 * @author Armin Reichert
 */
public class TooFancyGameCanvasContainer extends BorderPane {

    static final Vector2f DOWNSCALING_IF_BORDER = new Vector2f(0.85f, 0.93f);

    public final BooleanProperty borderVisiblePy = new SimpleBooleanProperty(true) {
        @Override
        protected void invalidated() {
            doLayout(scaling(), true);
        }
    };

    public final ObjectProperty<Color> borderColorPy = new SimpleObjectProperty<>(Color.LIGHTBLUE);

    public final BooleanProperty enabledPy = new SimpleBooleanProperty(true) {
        @Override
        protected void invalidated() {
            doLayout(scaling(), true);
        }
    };

    public final BooleanProperty roundedCornersPy = new SimpleBooleanProperty(true) {
        @Override
        protected void invalidated() {
            doLayout(scaling(), true);
        }
    };

    public final DoubleProperty scalingPy = new SimpleDoubleProperty(1.0) {
        @Override
        protected void invalidated() {
            doLayout(scaling(), true);
        }
    };

    public final DoubleProperty unscaledCanvasWidthPy = new SimpleDoubleProperty(500) {
        @Override
        protected void invalidated() {
            doLayout(scaling(), true);
        }
    };
    public final DoubleProperty unscaledCanvasHeightPy = new SimpleDoubleProperty(400) {
        @Override
        protected void invalidated() {
            doLayout(scaling(), true);
        }
    };

    private final Canvas canvas;
    private double minScaling = 1.0;

    public TooFancyGameCanvasContainer(Canvas canvas) {
        this.canvas = checkNotNull(canvas);

        setCenter(canvas);

        canvas.widthProperty().bind(unscaledCanvasWidthPy.multiply(scalingPy));
        canvas.heightProperty().bind(unscaledCanvasHeightPy.multiply(scalingPy));

        clipProperty().bind(Bindings.createObjectBinding(() -> {
            if (!isEnabled()) {
                return null;
            }
            Dimension2D size = computeSize();
            var clipNode = new Rectangle(size.getWidth(), size.getHeight());
            if (roundedCornersPy.get()) {
                double arcSize = 26 * scaling(); // TODO avoid magic numbers
                clipNode.setArcWidth(arcSize);
                clipNode.setArcHeight(arcSize);
            }
            return clipNode;
        }, enabledPy, roundedCornersPy, scalingPy, unscaledCanvasWidthPy, unscaledCanvasHeightPy));

        borderProperty().bind(Bindings.createObjectBinding(() -> {
            if (!isEnabled() || !isBorderVisible()) {
                return null;
            }
            double bw = Math.max(5, Math.ceil(computeSize().getHeight() / 55)); // TODO avoid magic numbers
            CornerRadii cr = hasRoundedCorners() ? new CornerRadii(Math.ceil(10 * scaling())) : null;
            return new Border(new BorderStroke(borderColor(), BorderStrokeStyle.SOLID, cr, new BorderWidths(bw)));
        }, enabledPy, borderVisiblePy, roundedCornersPy, scalingPy, unscaledCanvasWidthPy, unscaledCanvasHeightPy));
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
        double width = canvas().getWidth();
        double height = canvas().getHeight();
        if (isEnabled()) {
            Dimension2D size = computeSize();
            width = size.getWidth();
            height = size.getHeight();
        }
        setMinSize(width, height);
        setMaxSize(width, height);
        setPrefSize(width, height);
        setScaling(newScaling);

        Logger.debug("Unscaled canvas size: w={0.0} h={0.0}", unscaledCanvasWidth(), unscaledCanvasHeight());
        Logger.debug("Canvas size: w={0.0} h={0.0}", canvas().getWidth(), canvas().getHeight());
    }

    public Canvas canvas() {
        return canvas;
    }

    private Dimension2D computeSize() {
        return new Dimension2D(
            Math.round((unscaledCanvasWidth()  + 25) * scaling()), // TODO avoid magic numbers
            Math.round((unscaledCanvasHeight() + 15) * scaling())  // TODO avoid magic numbers
        );
    }

    public double scaling() {
        return scalingPy.get();
    }

    public void setScaling(double scaling) {
        scalingPy.set(scaling);
    }

    public void setMinScaling(double value) {
        minScaling = value;
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

    public void resizeTo(double width, double height) {
        if (isEnabled()) {
            double downScaledWidth = width, downScaledHeight = height;
            if (isBorderVisible()) {
                downScaledWidth = DOWNSCALING_IF_BORDER.x() * width;
                downScaledHeight = DOWNSCALING_IF_BORDER.y() * height;
            }
            double scaling = downScaledHeight / unscaledCanvasHeight();
            if (scaling * unscaledCanvasWidth() > downScaledWidth) {
                scaling = downScaledWidth / unscaledCanvasWidth();
            }
            doLayout(scaling, false);
        } else {
            doLayout(height / unscaledCanvasHeight(), false);
        }
    }

    public boolean isEnabled() {
        return enabledPy.get();
    }

    public void setEnabled(boolean enabled) {
        enabledPy.set(enabled);
    }

    public Color borderColor() {
        return borderColorPy.get();
    }

    public void setBorderColor(Color color) {
        borderColorPy.set(color);
    }

    public boolean isBorderVisible() {
        return borderVisiblePy.get();
    }

    public void setBorderVisible(boolean visible) {
        borderVisiblePy.set(visible);
    }

    public boolean hasRoundedCorners() {
        return roundedCornersPy.get();
    }

    public void setRoundedCorers(boolean rounded) {
        roundedCornersPy.set(rounded);
    }
}