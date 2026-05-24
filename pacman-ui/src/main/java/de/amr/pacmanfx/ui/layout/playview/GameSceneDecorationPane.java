/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.ui.layout.playview;

import de.amr.pacmanfx.Globals;
import javafx.beans.binding.Bindings;
import javafx.beans.property.*;
import javafx.beans.value.ChangeListener;
import javafx.geometry.Dimension2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;
import org.tinylog.Logger;

import static java.util.Objects.requireNonNull;

public class GameSceneDecorationPane extends StackPane {

    private static final float SCALING_X = 0.85f;
    private static final float SCALING_Y = 0.93f;

    // Padding between canvas and rounded frame
    private static final int PADDING_X = 20;
    private static final int PADDING_Y = 20;

    private static final int    FRAME_ARC_DIAMETER = 26;
    private static final int    FRAME_CORNER_RADIUS = 10;
    private static final int    FRAME_MIN_BORDER_WIDTH = 5;
    private static final double FRAME_BORDER_WIDTH_RATIO = 55.0;
    private static final Color  FRAME_DEFAULT_BORDER_COLOR = Color.WHITE;

    private static Border createRoundedBorder(Paint strokeColor, double borderWidth, double cornerRadius) {
        final var stroke = new BorderStroke(strokeColor, BorderStrokeStyle.SOLID,
            new CornerRadii(cornerRadius),
            new BorderWidths(borderWidth));
        return new Border(stroke);
    }

    private final ObjectProperty<Color> borderColor = new SimpleObjectProperty<>(FRAME_DEFAULT_BORDER_COLOR);

    private final DoubleProperty scaling = new SimpleDoubleProperty(1.0);

    private final DoubleProperty unscaledWidth = new SimpleDoubleProperty(Globals.ARCADE_MAP_SIZE_IN_PIXELS.x());

    private final DoubleProperty unscaledHeight = new SimpleDoubleProperty(Globals.ARCADE_MAP_SIZE_IN_PIXELS.y());

    private Canvas canvas;
    private double minScaling = 1.0;

    public GameSceneDecorationPane() {

        final ChangeListener<? super Number> resizeHandler = (_, _, _) -> updateLayout();
        unscaledWidth.addListener(resizeHandler);
        unscaledHeight.addListener(resizeHandler);
        scaling.addListener(resizeHandler);

        clipProperty().bind(Bindings.createObjectBinding(() -> {
                final double arcDiameter = FRAME_ARC_DIAMETER * scaling();
                final Dimension2D scaledSize = computeScaledSize();
                final var rect = new Rectangle(scaledSize.getWidth(), scaledSize.getHeight());
                rect.setArcHeight(arcDiameter);
                rect.setArcWidth(arcDiameter);
                return rect;
            }, scaling, unscaledWidth, unscaledHeight)
        );

        borderProperty().bind(Bindings.createObjectBinding(() -> {
                final Dimension2D scaledSize = computeScaledSize();
                final double proposedBorderWidth = Math.ceil(scaledSize.getHeight() / FRAME_BORDER_WIDTH_RATIO);
                final double borderWidth = Math.max(FRAME_MIN_BORDER_WIDTH, proposedBorderWidth);
                final double cornerRadius = Math.ceil(FRAME_CORNER_RADIUS * scaling());
                return createRoundedBorder(borderColor.get(), borderWidth, cornerRadius);
            }, borderColor, scaling, unscaledWidth, unscaledHeight)
        );
    }

    public void setCanvas(Canvas canvas) {
        this.canvas = requireNonNull(canvas);
        getChildren().setAll(canvas);

        canvas.widthProperty().bind(Bindings.createDoubleBinding(
            () -> scaling() * unscaledWidth.get(), scaling, unscaledWidth, unscaledHeight)
        );

        canvas.heightProperty().bind(Bindings.createDoubleBinding(
            () -> scaling() * unscaledHeight.get(), scaling, unscaledWidth, unscaledHeight)
        );
    }

    public void updateLayout() {
        doLayout(scaling(), true);
    }

    public void resizeTo(double width, double height) {
        final double realWidth  = SCALING_X * width;
        final double realHeight = SCALING_Y * height;
        double newScaling = realHeight / unscaledHeight.get();
        if (newScaling * unscaledWidth.get() > realWidth) {
            newScaling = realWidth / unscaledWidth.get();
        }
        doLayout(newScaling, false);
        Logger.debug("Canvas container resized to width={} height={}", getWidth(), getHeight());
    }

    public Canvas canvas() {
        return canvas;
    }

    public void setUnscaledSize(int width, int height) {
        unscaledWidth.set(width);
        unscaledHeight.set(height);
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

    private void doLayout(double newScaling, boolean forced) {
        if (newScaling < minScaling) {
            Logger.warn("Cannot scale to {}, minimum scaling is {}", newScaling, minScaling);
            return;
        }
        if (!forced && Math.abs(scaling.get() - newScaling) < 1e-2) { // ignore tiny scaling changes
            Logger.debug("No scaling needed, difference too small");
            return;
        }
        scaling.set(newScaling);

        final Dimension2D size = computeScaledSize();
        setMinSize(size.getWidth(),  size.getHeight());
        setMaxSize(size.getWidth(),  size.getHeight());
        setPrefSize(size.getWidth(), size.getHeight());
    }

    private Dimension2D computeScaledSize() {
        final double s = scaling();
        return new Dimension2D(s * (unscaledWidth.get() + PADDING_X), s * (unscaledHeight.get() + PADDING_Y));
    }

}