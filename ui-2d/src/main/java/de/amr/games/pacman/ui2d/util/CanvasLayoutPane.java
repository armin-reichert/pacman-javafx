package de.amr.games.pacman.ui2d.util;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.canvas.Canvas;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import org.tinylog.Logger;

/**
 * Layered container containing a canvas in the center of the lowest layer.
 */
public class CanvasLayoutPane extends StackPane {

    public static void resizeRegion(Region region, double width, double height) {
        region.setMinSize(width, height);
        region.setMaxSize(width, height);
        region.setPrefSize(width, height);
    }

    public final DoubleProperty scalingPy = new SimpleDoubleProperty(this, "scaling", 1.0);

    protected final BorderPane canvasLayer = new BorderPane();
    protected final BorderPane canvasContainer = new BorderPane();
    protected final Canvas canvas = new Canvas();

    protected double minScaling = 1.0;
    protected double unscaledCanvasWidth = 300;
    protected double unscaledCanvasHeight = 400;
    protected Color canvasBorderColor = Color.WHITE;
    protected boolean canvasBorderEnabled = false;
    protected boolean discreteScaling = false;

    public CanvasLayoutPane() {
        canvasContainer.setCenter(canvas);
        canvasContainer.widthProperty().addListener((py, ov, nv) -> rescale(getScaling(), false));
        canvasContainer.heightProperty().addListener((py, ov, nv) -> rescale(getScaling(), false));
        canvasLayer.setCenter(canvasContainer);
        getChildren().add(canvasLayer);
    }

    public double getScaling() {
        return scalingPy.get();
    }

    public void setScaling(double scaling) {
        scalingPy.set(scaling);
    }

    public BorderPane getCanvasLayer() {
        return canvasLayer;
    }

    public BorderPane getCanvasContainer() {
        return canvasContainer;
    }

    public Canvas getCanvas() {
        return canvas;
    }

    public double getUnscaledCanvasWidth() {
        return unscaledCanvasWidth;
    }

    public void setUnscaledCanvasWidth(double w) {
        unscaledCanvasWidth = w;
    }

    public double getUnscaledCanvasHeight() {
        return unscaledCanvasHeight;
    }

    public void setUnscaledCanvasHeight(double h) {
        unscaledCanvasHeight = h;
    }

    public void setMinScaling(double minScaling) {
        this.minScaling = minScaling;
    }

    public void setDiscreteScaling(boolean discreteScaling) {
        this.discreteScaling = discreteScaling;
    }

    public void setCanvasBorderEnabled(boolean canvasBorderEnabled) {
        this.canvasBorderEnabled = canvasBorderEnabled;
    }

    public void setCanvasBorderColor(Color canvasBorderColor) {
        this.canvasBorderColor = canvasBorderColor;
    }

    public void setSize(double width, double height) {
        double shrink_width = canvasBorderEnabled ? 0.85 : 1.0;
        double shrink_height = canvasBorderEnabled ? 0.92 : 1.0;

        double s = shrink_height * height / unscaledCanvasHeight;
        if (s * unscaledCanvasWidth > shrink_width * width) {
            s = shrink_width * width / unscaledCanvasWidth;
        }

        if (discreteScaling) {
            s = Math.floor(s * 10) / 10; // round scaling factor to first decimal digit
        }

        rescale(s, false);
    }

    protected void rescale(double newScaling, boolean always) {
        if (newScaling < minScaling) {
            Logger.error("Cannot scale to {}, minimum scaling is {}", newScaling, minScaling);
            return;
        }
        if (getScaling() == newScaling && !always) {
            // avoid useless scaling
            return;
        }
        setScaling(newScaling);

        canvas.setWidth(unscaledCanvasWidth * getScaling());
        canvas.setHeight(unscaledCanvasHeight * getScaling());

        if (canvasBorderEnabled) {
            double w = Math.round((unscaledCanvasWidth + 25) * getScaling());
            double h = Math.round((unscaledCanvasHeight + 15) * getScaling());
            var roundedRect = new Rectangle(w, h);
            roundedRect.setArcWidth(26 * getScaling());
            roundedRect.setArcHeight(26 * getScaling());
            canvasContainer.setClip(roundedRect);

            double borderWidth = Math.max(5, Math.ceil(h / 55));
            double cornerRadius = Math.ceil(10 * getScaling());
            var roundedBorder = new Border(
                new BorderStroke(canvasBorderColor,
                    BorderStrokeStyle.SOLID,
                    new CornerRadii(cornerRadius),
                    new BorderWidths(borderWidth)));
            canvasContainer.setBorder(roundedBorder);
            resizeRegion(canvasContainer, w, h);
            Logger.trace("Canvas container resized: scaling: {}, canvas size: {000} x {000} px, border: {0} px",
                getScaling(), canvas.getWidth(), canvas.getHeight(), borderWidth);
        } else {
            canvasContainer.setBorder(null);
            resizeRegion(canvasContainer, canvas.getWidth(), canvas.getHeight());
            Logger.trace("Canvas container resized: scaling: {}, canvas size: {000} x {000} px, no border",
                getScaling(), canvas.getWidth(), canvas.getHeight());
        }
    }
}