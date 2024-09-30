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

    private final DecoratedCanvas decoratedCanvas = new DecoratedCanvas();
    private double minScaling = 1.0;

    public CanvasLayoutPane() {
        setCenter(decoratedCanvas);
        decoratedCanvas.scalingPy.addListener((py, ov, nv) -> doLayout(decoratedCanvas.scaling(), true));
        decoratedCanvas.unscaledCanvasWidthPy.addListener((py, ov, nv) -> doLayout(decoratedCanvas.scaling(), true));
        decoratedCanvas.unscaledCanvasHeightPy.addListener((py, ov, nv) -> doLayout(decoratedCanvas.scaling(), true));
    }

    public DecoratedCanvas canvas() {
        return decoratedCanvas;
    }

    public void setMinScaling(double value) {
        minScaling = value;
    }

    public void resizeTo(double width, double height) {
        if (decoratedCanvas.isDecorated()) {
            double shrunkWidth  = 0.85 * width;
            double shrunkHeight = 0.92 * height;
            double scaling = shrunkHeight / decoratedCanvas.unscaledCanvasHeight();
            if (scaling * decoratedCanvas.unscaledCanvasWidth() > shrunkWidth) {
                scaling = shrunkWidth / decoratedCanvas.unscaledCanvasWidth();
            }
            doLayout(scaling, false);
        } else {
            doLayout(height / decoratedCanvas.unscaledCanvasHeight(), false);
        }
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
        if (!forced && Math.abs(decoratedCanvas.scaling() - newScaling) < 1e-2) { // avoid irrelevant scaling
            Logger.debug("No scaling needed, difference too small");
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
        decoratedCanvas.setScaling(newScaling);

        Logger.debug("Unscaled canvas size: w={0.0} h={0.0}", decoratedCanvas.unscaledCanvasWidth(), decoratedCanvas.unscaledCanvasHeight());
        Logger.debug("Canvas size: w={0.0} h={0.0}", decoratedCanvas.canvas().getWidth(), decoratedCanvas.canvas().getHeight());
    }
}