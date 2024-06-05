package de.amr.games.pacman.ui2d.util;

import javafx.beans.binding.Bindings;
import javafx.beans.property.*;
import javafx.geometry.Dimension2D;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import org.tinylog.Logger;

import java.util.Objects;

/**
 * Layered container containing a (decorated) canvas in the center of the lowest layer.
 */
public class CanvasLayoutPane extends StackPane {

    public static void setAllSizes(Region region, double width, double height) {
        region.setMinSize(width, height);
        region.setMaxSize(width, height);
        region.setPrefSize(width, height);
    }

    public static Dimension2D canvasContainerSize(double unscaledCanvasWidth, double unscaledCanvasHeight, double scaling) {
        return new Dimension2D(
            Math.round(unscaledCanvasWidth * scaling + 25 * scaling), // TODO magic number
            Math.round(unscaledCanvasHeight * scaling + 15 * scaling) // TODO magic number
        );
    }

    private void logCanvasSize() {
        Logger.debug("Unscaled canvas size: w={0.0} h={0.0}", getUnscaledCanvasWidth(), getUnscaledCanvasHeight());
        Logger.debug("Canvas size: w={0.0} h={0.0}", canvas.getWidth(), canvas.getHeight());
    }

    public final DoubleProperty scalingPy = new SimpleDoubleProperty(this, "scaling", 1.0) {
        @Override
        protected void invalidated() {
            doLayout(getScaling(), true);
        }
    };

    public final DoubleProperty unscaledCanvasWidthPy = new SimpleDoubleProperty(this, "unscaledCanvasWidth", 500);

    public final DoubleProperty unscaledCanvasHeightPy = new SimpleDoubleProperty(this, "unscaledCanvasHeight", 400);

    public final BooleanProperty canvasDecoratedPy = new SimpleBooleanProperty(this, "canvasDecorated", true);

    public final ObjectProperty<Color> canvasBorderColorPy = new SimpleObjectProperty<>(this, "canvasBorderColor", Color.LIGHTBLUE);

    protected BorderPane canvasLayer;
    protected BorderPane canvasContainer;
    protected Canvas canvas;
    protected double minScaling = 1.0;

    public CanvasLayoutPane() {
        canvas = new Canvas();
        canvas.widthProperty().bind(unscaledCanvasWidthPy.multiply(scalingPy));
        canvas.heightProperty().bind(unscaledCanvasHeightPy.multiply(scalingPy));
        createCanvasContainer();
        canvasLayer = new BorderPane(canvasContainer);
        getChildren().add(canvasLayer);
    }

    public void replaceCanvasLayer(Node node) {
        Objects.requireNonNull(node);
        getChildren().set(0, node);
    }

    public void restoreCanvasLayer() {
        if (getChildren().getFirst() != canvasLayer) {
            getChildren().set(0, canvasLayer);
        }
    }

    public void resizeTo(double width, double height) {
        double scaling = computeScaling(width, height);
        doLayout(scaling, false);
    }

    public void setUnscaledCanvasSize(double width, double height) {
        setUnscaledCanvasWidth(width);
        setUnscaledCanvasHeight(height);
        doLayout(getScaling(), true);
    }

    public void doLayout(double newScaling, boolean forced) {
        if (newScaling < minScaling) {
            Logger.warn("Cannot scale to {}, minimum scaling is {}", newScaling, minScaling);
            return;
        }
        if (!forced && Math.abs(getScaling() - newScaling) < 1e-2) { // avoid irrelevant scaling
            return;
        }
        double width = canvas.getWidth(), height = canvas.getHeight();
        if (isCanvasDecorated()) {
            var size = canvasContainerSize();
            width = size.getWidth();
            height = size.getHeight();
        }
        setAllSizes(canvasContainer, width, height);
        setScaling(newScaling);
        logCanvasSize();
    }

    private double computeScaling(double width, double height) {
        if (isCanvasDecorated()) {
            double shrinkedWidth = 0.85 * width;
            double shrinkedHeight = 0.92 * height;
            double scaling = shrinkedHeight / getUnscaledCanvasHeight();
            if (scaling * getUnscaledCanvasWidth() > shrinkedWidth) {
                scaling = shrinkedWidth / getUnscaledCanvasWidth();
            }
            return scaling;
        } else {
            return height / getUnscaledCanvasHeight();
        }
    }

    private void createCanvasContainer() {
        canvasContainer = new BorderPane(canvas);
        canvasContainer.clipProperty().bind(Bindings.createObjectBinding(
            () -> {
                if (canvasDecoratedPy.get()) {
                    double s = getScaling();
                    var size = canvasContainerSize();
                    double arcSize = 26 * s;
                    var clipRect = new Rectangle(size.getWidth(), size.getHeight());
                    clipRect.setArcWidth(arcSize);
                    clipRect.setArcHeight(arcSize);
                    return clipRect;
                } else {
                    return null;
                }
            }, canvasDecoratedPy, scalingPy, unscaledCanvasWidthPy, unscaledCanvasHeightPy
        ));

        canvasContainer.borderProperty().bind(Bindings.createObjectBinding(
            () -> {
                if (canvasDecoratedPy.get()) {
                    var size = canvasContainerSize();
                    double borderWidth = Math.max(5, Math.ceil(size.getHeight() / 55)); // TODO magic number?
                    double cornerRadius = Math.ceil(10 * getScaling());
                    return new Border(
                        new BorderStroke(canvasBorderColorPy.get(),
                            BorderStrokeStyle.SOLID,
                            new CornerRadii(cornerRadius),
                            new BorderWidths(borderWidth)));
                } else {
                    return null;
                }
            },
            canvasDecoratedPy, scalingPy, unscaledCanvasWidthPy, unscaledCanvasHeightPy
        ));
    }

    private Dimension2D canvasContainerSize() {
        return canvasContainerSize(getUnscaledCanvasWidth(), getUnscaledCanvasHeight(), getScaling());
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

    public double getScaling() {
        return scalingPy.get();
    }

    public void setScaling(double scaling) {
        scalingPy.set(scaling);
    }

    public double getUnscaledCanvasWidth() {
        return unscaledCanvasWidthPy.get();
    }

    public void setUnscaledCanvasWidth(double w) {
        unscaledCanvasWidthPy.set(w);
    }

    public double getUnscaledCanvasHeight() {
        return unscaledCanvasHeightPy.get();
    }

    public void setUnscaledCanvasHeight(double h) {
        unscaledCanvasHeightPy.set(h);
    }

    public void setMinScaling(double value) {
        minScaling = value;
    }

    public boolean isCanvasDecorated() {
        return canvasDecoratedPy.get();
    }

    public void setCanvasDecorated(boolean enabled) {
        canvasDecoratedPy.set(enabled);
    }

    public Color getCanvasBorderColor() {
        return canvasBorderColorPy.get();
    }

    public void setCanvasBorderColor(Color color) {
        canvasBorderColorPy.set(color);
    }
}