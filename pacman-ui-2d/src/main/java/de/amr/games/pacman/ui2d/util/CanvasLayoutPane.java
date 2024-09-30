/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d.util;

import javafx.scene.layout.BorderPane;
import org.tinylog.Logger;

/**
 * @author Armin Reichert
 */
public class CanvasLayoutPane extends BorderPane {

    private final DecoratedCanvas dcanvas = new DecoratedCanvas();
    private double minScaling = 1.0;

    public CanvasLayoutPane() {
        setCenter(dcanvas);
        dcanvas.scalingPy.addListener((py, ov, nv) -> doLayout(dcanvas.scaling(), true));
        dcanvas.unscaledCanvasWidthPy.addListener((py, ov, nv) -> doLayout(dcanvas.scaling(), true));
        dcanvas.unscaledCanvasHeightPy.addListener((py, ov, nv) -> doLayout(dcanvas.scaling(), true));
    }

    public void resizeTo(double width, double height) {
        doLayout(computeScaling(width, height), false);
    }

    public void setUnscaledCanvasSize(double width, double height) {
        dcanvas.setUnscaledCanvasWidth(width);
        dcanvas.setUnscaledCanvasHeight(height);
    }

    private void doLayout(double newScaling, boolean forced) {
        if (newScaling < minScaling) {
            Logger.warn("Cannot scale to {}, minimum scaling is {}", newScaling, minScaling);
            return;
        }
        if (!forced && Math.abs(dcanvas.scaling() - newScaling) < 1e-2) { // avoid irrelevant scaling
            return;
        }
        double width = dcanvas.canvas().getWidth();
        double height = dcanvas.canvas().getHeight();
        if (dcanvas.isDecorated()) {
            var size = dcanvas.getSize();
            width = size.getWidth();
            height = size.getHeight();
        }
        dcanvas.setMinSize(width, height);
        dcanvas.setMaxSize(width, height);
        dcanvas.setPrefSize(width, height);
        dcanvas.setScaling(newScaling);

        Logger.debug("Unscaled canvas size: w={0.0} h={0.0}", dcanvas.unscaledCanvasWidth(), dcanvas.unscaledCanvasHeight());
        Logger.debug("Canvas size: w={0.0} h={0.0}", dcanvas.canvas().getWidth(), dcanvas.canvas().getHeight());
    }

    private double computeScaling(double width, double height) {
        if (dcanvas.isDecorated()) {
            double shrinkedWidth = 0.85 * width;
            double shrinkedHeight = 0.92 * height;
            double scaling = shrinkedHeight / dcanvas.unscaledCanvasHeight();
            if (scaling * dcanvas.unscaledCanvasWidth() > shrinkedWidth) {
                scaling = shrinkedWidth / dcanvas.unscaledCanvasWidth();
            }
            return scaling;
        } else {
            return height / dcanvas.unscaledCanvasHeight();
        }
    }

    public DecoratedCanvas decoratedCanvas() {
        return dcanvas;
    }

    public void setMinScaling(double value) {
        minScaling = value;
    }
}