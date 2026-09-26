package de.amr.basics.ui.rendering;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import static java.util.Objects.requireNonNull;

public record CanvasRenderTarget(Canvas canvas, double scaling, Color backgroundColor)
    implements RenderTarget
{
    public CanvasRenderTarget {
        requireNonNull(canvas);
        requireNonNull(backgroundColor);
    }

    @Override
    public GraphicsContext ctx() {
        return canvas.getGraphicsContext2D();
    }
}
