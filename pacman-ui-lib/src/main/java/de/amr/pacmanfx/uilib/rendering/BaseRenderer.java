package de.amr.pacmanfx.uilib.rendering;

import de.amr.pacmanfx.lib.Vector2i;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;

import java.util.Optional;

import static de.amr.pacmanfx.Globals.HTS;
import static de.amr.pacmanfx.Globals.TS;
import static java.util.Objects.requireNonNull;

public abstract class BaseRenderer {

    public static void fillCanvas(Canvas canvas, Paint paint) {
        requireNonNull(canvas);
        requireNonNull(paint);
        GraphicsContext ctx = canvas.getGraphicsContext2D();
        ctx.setFill(paint);
        ctx.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
    }

    protected final DoubleProperty scaling = new SimpleDoubleProperty(1.0);
    protected GraphicsContext ctx;

    public void setCanvas(Canvas canvas) {
        ctx = requireNonNull(canvas).getGraphicsContext2D();
    }

    public Optional<GraphicsContext> ctx() { return Optional.ofNullable(ctx); }

    public DoubleProperty scalingProperty() { return scaling; }

    public void setScaling(double value) {
        if (value <= 0) {
            throw new IllegalArgumentException("Scaling value must be positive but is %.2f".formatted(value));
        }
        scalingProperty().set((float)value);
    }

    public double scaling() { return scalingProperty().get(); }

    public double scaled(double value) { return scaling() * value; }

    /**
     * Fills a square at the center of the given tile with the current fill color. Used to hide pellets, energizers
     * or sprites that are part of a map image.
     *
     * @param tile a tile
     * @param sideLength side length of the square
     */
    public void fillSquareAtTileCenter(Vector2i tile, int sideLength) {
        requireNonNull(tile);
        double centerX = tile.x() * TS + HTS, centerY = tile.y() * TS + HTS;
        float halfSideLength = 0.5f * sideLength;
        ctx.fillRect(centerX - halfSideLength, centerY - halfSideLength, sideLength, sideLength);
    }

    /**
     * Draws text at the given tile position (scaled by the current scaling value).
     *
     * @param text  text
     * @param color text color
     * @param font  text font
     * @param tileX unscaled tile x-position
     * @param tileY unscaled tile y-position (baseline)
     */
    public void fillTextAtScaledTilePosition(String text, Color color, Font font, int tileX, int tileY) {
        fillTextAtScaledPosition(text, color, font, TS(tileX), TS(tileY));
    }

    /**
     * Draws text left-aligned at the given position (scaled by the current scaling value).
     *
     * @param text  text
     * @param color text color
     * @param font  text font
     * @param x     unscaled x-position
     * @param y     unscaled y-position (baseline)
     */
    public void fillTextAtScaledPosition(String text, Color color, Font font, double x, double y) {
        ctx.setFont(font);
        ctx.setFill(color);
        ctx.fillText(text, scaled(x), scaled(y));
    }

    /**
     * Draws text left-aligned at the given position (scaled by the current scaling value).
     *
     * @param text  text
     * @param color text color
     * @param x     unscaled x-position
     * @param y     unscaled y-position (baseline)
     */
    public void fillTextAtScaledPosition(String text, Color color, double x, double y) {
        ctx.setFill(color);
        ctx.fillText(text, scaled(x), scaled(y));
    }

    /**
     * Draws text left-aligned at the given position (scaled by the current scaling value).
     *
     * @param text  text
     * @param font  text font
     * @param x     unscaled x-position
     * @param y     unscaled y-position (baseline)
     */
    public void fillTextAtScaledPosition(String text, Font font, double x, double y) {
        ctx.setFont(font);
        ctx.fillText(text, scaled(x), scaled(y));
    }

    /**
     * Draws text left-aligned at the given position (scaled by the current scaling value).
     *
     * @param text  text
     * @param x     unscaled x-position
     * @param y     unscaled y-position (baseline)
     */
    public void fillTextAtScaledPosition(String text, double x, double y) {
        ctx.fillText(text, scaled(x), scaled(y));
    }

    /**
     * Draws text center-aligned at the given x position (scaled by the current scaling value).
     *
     * @param text  text
     * @param color text color
     * @param font  text font
     * @param x     unscaled x-position
     * @param y     unscaled y-position (baseline)
     */
    public void fillTextAtScaledCenter(String text, Color color, Font font, double x, double y) {
        ctx.save();
        ctx.setTextAlign(TextAlignment.CENTER);
        fillTextAtScaledPosition(text, color, font, x, y);
        ctx.restore();
    }

    public void drawTileGrid(double sizeX, double sizeY, Color gridColor) {
        double thin = 0.2, medium = 0.4, thick = 0.8;
        int numCols = (int) (sizeX / TS), numRows = (int) (sizeY / TS);
        double width = scaled(numCols * TS), height = scaled(numRows * TS);
        ctx.save();
        ctx.setStroke(gridColor);
        for (int row = 0; row <= numRows; ++row) {
            double y = scaled(row * TS);
            ctx.setLineWidth(row % 10 == 0 ? thick : row % 5 == 0 ? medium : thin);
            ctx.strokeLine(0, y, width, y);
        }
        for (int col = 0; col <= numCols; ++col) {
            double x = scaled(col * TS);
            ctx.setLineWidth(col % 10 == 0 ? thick : col % 5 == 0? medium : thin);
            ctx.strokeLine(x, 0, x, height);
        }
        ctx.restore();
    }
}
