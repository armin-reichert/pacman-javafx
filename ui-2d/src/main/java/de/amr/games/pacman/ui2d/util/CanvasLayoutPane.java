package de.amr.games.pacman.ui2d.util;

import de.amr.games.pacman.lib.Vector2f;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.property.*;
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
    public final DoubleProperty unscaledCanvasWidthPy = new SimpleDoubleProperty(this, "unscaledCanvasWidth", 500);
    public final DoubleProperty unscaledCanvasHeightPy = new SimpleDoubleProperty(this, "unscaledCanvasHeight", 400);
    public final BooleanProperty canvasBorderEnabledPy = new SimpleBooleanProperty(this, "canvasBorderEnabled", true);

    protected final BorderPane canvasLayer = new BorderPane();
    protected final BorderPane canvasContainer = new BorderPane();
    protected final Canvas canvas = new Canvas();

    protected double minScaling = 1.0;
    protected Color canvasBorderColor = Color.WHITE;

    public CanvasLayoutPane() {
        canvasContainer.setCenter(canvas);
        canvasContainer.widthProperty().addListener((py, ov, nv) -> rescale(getScaling(), false));
        canvasContainer.heightProperty().addListener((py, ov, nv) -> rescale(getScaling(), false));

        canvasContainer.clipProperty().bind(Bindings.createObjectBinding(
            () -> {
                if (canvasBorderEnabledPy.get()) {
                    double s = getScaling();
                    Vector2f size = canvasContainerSizeWithBorder();
                    double arcSize = 26 * s;
                    var clipRect = new Rectangle(size.x(), size.y());
                    clipRect.setArcWidth(arcSize);
                    clipRect.setArcHeight(arcSize);
                    return clipRect;
                } else {
                    return null;
                }
            }, canvasBorderEnabledPy, scalingPy, unscaledCanvasWidthPy, unscaledCanvasHeightPy
        ));

        canvasContainer.borderProperty().bind(Bindings.createObjectBinding(
            () -> {
                if (canvasBorderEnabledPy.get()) {
                    Vector2f size = canvasContainerSizeWithBorder();
                    double borderWidth = Math.max(5, Math.ceil(size.y() / 55));
                    double cornerRadius = Math.ceil(10 * getScaling());
                    return new Border(
                        new BorderStroke(canvasBorderColor,
                            BorderStrokeStyle.SOLID,
                            new CornerRadii(cornerRadius),
                            new BorderWidths(borderWidth)));
                } else {
                    return null;
                }
            },
            canvasBorderEnabledPy, scalingPy, unscaledCanvasWidthPy, unscaledCanvasHeightPy
        ));

        canvasLayer.setCenter(canvasContainer);
        getChildren().add(canvasLayer);
    }

    private Vector2f canvasContainerSizeWithBorder() {
        return new Vector2f(
            (float) Math.round((getUnscaledCanvasWidth() + 25) * getScaling()),
            (float) Math.round((getUnscaledCanvasHeight() + 15) * getScaling()));
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

    public void setMinScaling(double minScaling) {
        this.minScaling = minScaling;
    }

    public boolean getCanvasBorderEnabled() {
        return canvasBorderEnabledPy.get();
    }

    public void setCanvasBorderEnabled(boolean enabled) {
        this.canvasBorderEnabledPy.set(enabled);
    }

    public void setCanvasBorderColor(Color canvasBorderColor) {
        this.canvasBorderColor = canvasBorderColor;
    }

    public void setSize(double width, double height) {
        double shrink_width  = getCanvasBorderEnabled() ? 0.85 : 1.0;
        double shrink_height = getCanvasBorderEnabled() ? 0.92 : 1.0;
        double s = shrink_height * height / getUnscaledCanvasHeight();
        if (s * getUnscaledCanvasWidth() > shrink_width * width) {
            s = shrink_width * width / getUnscaledCanvasWidth();
        }
        rescale(s, false);
    }

    //TODO use data binding
    protected void rescale(double newScaling, boolean always) {
        if (newScaling < minScaling) {
            Logger.warn("Cannot scale to {}, minimum scaling is {}", newScaling, minScaling);
            return;
        }
        if (Math.abs(getScaling() - newScaling) < 1e-2 && !always) { // avoid useless scaling
            return;
        }
        setScaling(newScaling);
        canvas.setWidth(getUnscaledCanvasWidth() * getScaling());
        canvas.setHeight(getUnscaledCanvasHeight() * getScaling());
        if (getCanvasBorderEnabled()) {
            var size = canvasContainerSizeWithBorder();
            resizeRegion(canvasContainer, size.x(), size.y());
        } else {
            resizeRegion(canvasContainer, canvas.getWidth(), canvas.getHeight());
        }
    }
}