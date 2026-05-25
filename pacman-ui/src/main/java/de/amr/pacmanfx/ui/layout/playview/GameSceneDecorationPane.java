/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.layout.playview;

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

import static java.util.Objects.requireNonNull;

//TODO: Still too complicated for what it does
public class GameSceneDecorationPane extends StackPane {

    public record FrameConfig(
        int    arcDiameter,
        int    cornerRadius,
        int    minBorderWidth,
        double borderWidthRatio,
        Color  defaultBorderColor) {}

    public record Config(
        float scalingX,
        float scalingY,
        float paddingX,
        float paddingY,
        FrameConfig frameConfig) {}

    public static final Config DEFAULT_CONFIG = new Config(
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

    private final ObjectProperty<Color> borderColor = new SimpleObjectProperty<>();

    private final DoubleProperty scaling = new SimpleDoubleProperty(1.0);

    private final DoubleProperty unscaledWidth = new SimpleDoubleProperty(400);

    private final DoubleProperty unscaledHeight = new SimpleDoubleProperty(600);

    private Canvas canvas = new Canvas();

    private double minScaling = 1.0;

    private final Config config;

    public GameSceneDecorationPane(double unscaledWidth, double unscaledHeight) {
        this(DEFAULT_CONFIG, unscaledWidth, unscaledHeight);
    }

    public GameSceneDecorationPane(Config config, double unscaledWidth, double unscaledHeight) {
        this.config = requireNonNull(config);
        unscaledWidthProperty().set(unscaledWidth);
        unscaledHeightProperty().set(unscaledHeight);

        borderColor.set(config.frameConfig().defaultBorderColor());

        newCanvas();

        final ChangeListener<? super Number> resizeHandler = (_, _, _) -> doLayout(scaling(), true);
        unscaledWidthProperty().addListener(resizeHandler);
        unscaledHeightProperty().addListener(resizeHandler);
        scaling.addListener(resizeHandler);

        clipProperty().bind(Bindings.createObjectBinding(() -> {
                final double arcDiameter = config.frameConfig().arcDiameter() * scaling();
                final Dimension2D scaledSize = computePaneSize();
                final var rect = new Rectangle(scaledSize.getWidth(), scaledSize.getHeight());
                rect.setArcHeight(arcDiameter);
                rect.setArcWidth(arcDiameter);
                return rect;
            }, scalingProperty(), unscaledWidthProperty(), unscaledHeightProperty())
        );

        borderProperty().bind(Bindings.createObjectBinding(() -> {
                final Dimension2D scaledSize = computePaneSize();
                final double proposedBorderWidth = Math.ceil(scaledSize.getHeight() / config.frameConfig().borderWidthRatio());
                final double borderWidth = Math.max(config.frameConfig().minBorderWidth(), proposedBorderWidth);
                final double cornerRadius = Math.ceil(config.frameConfig().cornerRadius() * scaling());
                return createRoundedBorder(borderColor.get(), borderWidth, cornerRadius);
            }, borderColorProperty(), scalingProperty(), unscaledWidthProperty(), unscaledHeightProperty())
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
        final double realWidth  = config.scalingX() * width;
        final double realHeight = config.scalingY() * height;
        double targetScaling = realHeight / unscaledHeight.get();
        if (targetScaling * unscaledWidth.get() > realWidth) {
            targetScaling = realWidth / unscaledWidth.get();
        }
        doLayout(targetScaling, false);
    }

    public Canvas canvas() {
        return canvas;
    }

    public ObjectProperty<Color> borderColorProperty() {
        return borderColor;
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
            scaling() * (unscaledWidth.get() + config.paddingX()),
            scaling() * (unscaledHeight.get() + config.paddingY())
        );
    }
}