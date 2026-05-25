/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.layout.playview;

import de.amr.pacmanfx.Globals;
import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.geometry.Dimension2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;
import org.tinylog.Logger;

//TODO: Still too complicated for what it does
public class GameSceneDecorationPane extends StackPane {

    record FrameConfig(
        int    arcDiameter,
        int    cornerRadius,
        int    minBorderWidth,
        double borderWidthRatio,
        Color  defaultBorderColor) {}

    record Config(
        float scalingX,
        float scalingY,
        float paddingX,
        float paddingY,
        FrameConfig frameConfig) {}

    private static final Config CONFIG = new Config(
        0.85f,
        0.93f,
        20,
        20,
        new FrameConfig(
            26,
            10,
            5,
            55.0,
            Color.WHITE)
    );

    private static Border createRoundedBorder(Paint strokeColor, double borderWidth, double cornerRadius) {
        final var stroke = new BorderStroke(strokeColor, BorderStrokeStyle.SOLID,
            new CornerRadii(cornerRadius),
            new BorderWidths(borderWidth));
        return new Border(stroke);
    }

    private final ObjectProperty<Color> borderColor = new SimpleObjectProperty<>(CONFIG.frameConfig().defaultBorderColor());

    private final DoubleProperty scaling = new SimpleDoubleProperty(1.0);

    private final DoubleProperty unscaledWidth = new SimpleDoubleProperty(Globals.ARCADE_MAP_SIZE_IN_PIXELS.x());

    private final DoubleProperty unscaledHeight = new SimpleDoubleProperty(Globals.ARCADE_MAP_SIZE_IN_PIXELS.y());

    private Canvas canvas = new Canvas();

    private double minScaling = 1.0;

    public GameSceneDecorationPane() {

        newCanvas();

        final ChangeListener<? super Number> resizeHandler = (_, _, _) -> doLayout(scaling(), true);
        unscaledWidth.addListener(resizeHandler);
        unscaledHeight.addListener(resizeHandler);
        scaling.addListener(resizeHandler);

        clipProperty().bind(Bindings.createObjectBinding(() -> {
                final double arcDiameter = CONFIG.frameConfig().arcDiameter() * scaling();
                final Dimension2D scaledSize = computePaneSize();
                final var rect = new Rectangle(scaledSize.getWidth(), scaledSize.getHeight());
                rect.setArcHeight(arcDiameter);
                rect.setArcWidth(arcDiameter);
                return rect;
            }, scaling, unscaledWidth, unscaledHeight)
        );

        borderProperty().bind(Bindings.createObjectBinding(() -> {
                final Dimension2D scaledSize = computePaneSize();
                final double proposedBorderWidth = Math.ceil(scaledSize.getHeight() / CONFIG.frameConfig().borderWidthRatio());
                final double borderWidth = Math.max(CONFIG.frameConfig().minBorderWidth(), proposedBorderWidth);
                final double cornerRadius = Math.ceil(CONFIG.frameConfig().cornerRadius() * scaling());
                return createRoundedBorder(borderColor.get(), borderWidth, cornerRadius);
            }, borderColor, scaling, unscaledWidth, unscaledHeight)
        );
    }

    public void newCanvas() {
        canvas = new Canvas();
        getChildren().setAll(canvas);

        canvas.widthProperty().bind(Bindings.createDoubleBinding(
            () -> scaling() * unscaledWidth.get(), scaling, unscaledWidth, unscaledHeight)
        );

        canvas.heightProperty().bind(Bindings.createDoubleBinding(
            () -> scaling() * unscaledHeight.get(), scaling, unscaledWidth, unscaledHeight)
        );
    }

    public void stretchTo(double width, double height) {
        final double realWidth  = CONFIG.scalingX() * width;
        final double realHeight = CONFIG.scalingY() * height;
        double targetScaling = realHeight / unscaledHeight.get();
        if (targetScaling * unscaledWidth.get() > realWidth) {
            targetScaling = realWidth / unscaledWidth.get();
        }
        doLayout(targetScaling, false);
    }

    public Canvas canvas() {
        return canvas;
    }

    public DoubleProperty unscaledWidthProperty() {
        return unscaledWidth;
    }

    public DoubleProperty unscaledHeightProperty() {
        return unscaledHeight;
    }

    public DoubleProperty scalingProperty() { return scaling; }

    public double scaling() {
        return scalingProperty().get();
    }

    public void setMinScaling(double value) {
        minScaling = value;
    }

    public void setBorderColor(Color color) {
        borderColor.set(color);
    }

    // Private

    private void doLayout(double targetScaling, boolean forced) {
        if (targetScaling < minScaling) {
            Logger.warn("Cannot scale to {}, minimum scaling is {}", targetScaling, minScaling);
            return;
        }
        if (!forced && Math.abs(scaling.get() - targetScaling) < 1e-2) { // ignore tiny scaling changes
            Logger.debug("No scaling needed, difference too small");
            return;
        }
        scaling.set(targetScaling);

        final Dimension2D paneSize = computePaneSize();
        setMinSize(paneSize.getWidth(),  paneSize.getHeight());
        setMaxSize(paneSize.getWidth(),  paneSize.getHeight());
        setPrefSize(paneSize.getWidth(), paneSize.getHeight());
    }

    private Dimension2D computePaneSize() {
        return new Dimension2D(
            scaling() * (unscaledWidth.get() + CONFIG.paddingX()),
            scaling() * (unscaledHeight.get() + CONFIG.paddingY())
        );
    }
}