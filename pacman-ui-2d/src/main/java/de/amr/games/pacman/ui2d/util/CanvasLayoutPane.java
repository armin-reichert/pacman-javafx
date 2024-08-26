/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d.util;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.layout.BorderPane;
import org.tinylog.Logger;

/**
 * @author Armin Reichert
 */
public class CanvasLayoutPane extends BorderPane {

    public final DoubleProperty scalingPy = new SimpleDoubleProperty(this, "scaling", 1.0) {
        @Override
        protected void invalidated() {
            doLayout(scaling(), true);
        }
    };

    private final DecoratedCanvas decoratedCanvas = new DecoratedCanvas();
    private double minScaling = 1.0;

    public CanvasLayoutPane() {
        decoratedCanvas.scalingPy.bind(scalingPy);
        decoratedCanvas.unscaledCanvasWidthPy.addListener((py, ov, nv) -> doLayout(scaling(), true));
        decoratedCanvas.unscaledCanvasHeightPy.addListener((py, ov, nv) -> doLayout(scaling(), true));
        setCenter(decoratedCanvas);
    }

    public void resizeTo(double width, double height) {
        double scaling = computeScaling(width, height);
        doLayout(scaling, false);
    }

    public void setUnscaledCanvasSize(double width, double height) {
        decoratedCanvas.setUnscaledCanvasWidth(width);
        decoratedCanvas.setUnscaledCanvasHeight(height);
    }

    private void doLayout(double newScaling, boolean forced) {
        if (newScaling < minScaling) {
            Logger.warn("Cannot scale to {}, minimum scaling is {}", newScaling, minScaling);
            return;
        }
        if (!forced && Math.abs(scaling() - newScaling) < 1e-2) { // avoid irrelevant scaling
            return;
        }
        double width = decoratedCanvas.canvas().getWidth();
        double height = decoratedCanvas.canvas().getHeight();
        if (decoratedCanvas.isDecorated()) {
            var size = decoratedCanvas.getSize();
            width = size.getWidth();
            height = size.getHeight();
        }
        decoratedCanvas.setMinSize(width, height);
        decoratedCanvas.setMaxSize(width, height);
        decoratedCanvas.setPrefSize(width, height);

        setScaling(newScaling);

        Logger.debug("Unscaled canvas size: w={0.0} h={0.0}", decoratedCanvas.unscaledCanvasWidth(), decoratedCanvas.unscaledCanvasHeight());
        Logger.debug("Canvas size: w={0.0} h={0.0}", decoratedCanvas.canvas().getWidth(), decoratedCanvas.canvas().getHeight());
    }

    private double computeScaling(double width, double height) {
        if (decoratedCanvas.isDecorated()) {
            double shrinkedWidth = 0.85 * width;
            double shrinkedHeight = 0.92 * height;
            double scaling = shrinkedHeight / decoratedCanvas.unscaledCanvasHeight();
            if (scaling * decoratedCanvas.unscaledCanvasWidth() > shrinkedWidth) {
                scaling = shrinkedWidth / decoratedCanvas.unscaledCanvasWidth();
            }
            return scaling;
        } else {
            return height / decoratedCanvas.unscaledCanvasHeight();
        }
    }

    public DecoratedCanvas decoratedCanvas() {
        return decoratedCanvas;
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
}