package de.amr.games.pacman.ui2d.util;

import javafx.beans.binding.Bindings;
import javafx.beans.property.*;
import javafx.geometry.Point2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import org.tinylog.Logger;

/**
 * Layered container containing a (decorated) canvas in the center of the lowest layer.
 */
public class CanvasLayoutPane extends StackPane {

    public static void setAllSizes(Region region, double width, double height) {
        region.setMinSize(width, height);
        region.setMaxSize(width, height);
        region.setPrefSize(width, height);
    }

    public final DoubleProperty scalingPy = new SimpleDoubleProperty(this, "scaling", 1.0) {
        @Override
        protected void invalidated() {
            doLayout(getScaling(), true);
        }
    };

    public final DoubleProperty unscaledCanvasWidthPy = new SimpleDoubleProperty(this, "unscaledCanvasWidth", 500);

    public final DoubleProperty unscaledCanvasHeightPy = new SimpleDoubleProperty(this, "unscaledCanvasHeight", 400);

    public final BooleanProperty canvasDecoratedPy = new SimpleBooleanProperty(this, "canvasDecorated", true) {
        @Override
        protected void invalidated() {
            doLayout(getScaling(), true);
        }
    };

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

    public void setSize(double width, double height) {
        double scaling = computeScaling(width, height);
        doLayout(scaling, false);
    }

    public void doLayout(double newScaling, boolean always) {
        if (newScaling < minScaling) {
            Logger.warn("Cannot scale to {}, minimum scaling is {}", newScaling, minScaling);
            return;
        }
        if (Math.abs(getScaling() - newScaling) < 1e-2 && !always) { // avoid useless scaling
            return;
        }
        if (isCanvasDecorated()) {
            var size = canvasContainerSizeWithBorder();
            setAllSizes(canvasContainer, size.getX(), size.getY());
        } else {
            setAllSizes(canvasContainer, canvas.getWidth(), canvas.getHeight());
        }
        setScaling(newScaling);
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
                    var size = canvasContainerSizeWithBorder();
                    double arcSize = 26 * s;
                    var clipRect = new Rectangle(size.getX(), size.getY());
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
                    var size = canvasContainerSizeWithBorder();
                    double borderWidth = Math.max(5, Math.ceil(size.getY() / 55));
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

    private Point2D canvasContainerSizeWithBorder() {
        return new Point2D(
            Math.round((getUnscaledCanvasWidth() + 25) * getScaling()),
            Math.round((getUnscaledCanvasHeight() + 15) * getScaling()));
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