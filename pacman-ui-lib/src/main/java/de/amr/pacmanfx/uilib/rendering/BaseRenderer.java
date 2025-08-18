package de.amr.pacmanfx.uilib.rendering;

import de.amr.pacmanfx.lib.RectShort;
import de.amr.pacmanfx.lib.Vector2f;
import de.amr.pacmanfx.lib.Vector2i;
import de.amr.pacmanfx.uilib.assets.SpriteSheet;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;

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
    private GraphicsContext ctx;
    protected SpriteSheet<?> spriteSheet;

    public void setCanvas(Canvas canvas) {
        ctx = requireNonNull(canvas).getGraphicsContext2D();
    }

    public GraphicsContext ctx() {
        return requireNonNull(ctx, "Renderer has no canvas to draw");
    }

    public SpriteSheet<?> spriteSheet() {
        return spriteSheet;
    }

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
    public void fillSquareAtTileCenter(Vector2i tile, double sideLength) {
        requireNonNull(tile);
        double centerX = tile.x() * TS + HTS, centerY = tile.y() * TS + HTS;
        double halfSideLength = 0.5f * sideLength;
        ctx.fillRect(centerX - halfSideLength, centerY - halfSideLength, sideLength, sideLength);
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
    public void fillText(String text, Color color, Font font, double x, double y) {
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
    public void fillText(String text, Color color, double x, double y) {
        ctx.setFill(color);
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
    public void fillTextCentered(String text, Color color, Font font, double x, double y) {
        ctx.save();
        ctx.setTextAlign(TextAlignment.CENTER);
        fillText(text, color, font, x, y);
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

    // -- Sprite rendering helpers

    /**
     * Draws a sprite (region inside sprite sheet) unscaled at the given position.
     *
     * @param spriteSheet the sprite sheet
     * @param sprite      the sprite to draw
     * @param x           x-coordinate of left-upper corner
     * @param y           y-coordinate of left-upper corner
     * @param scaled      tells is the destination rectangle's position and size is scaled using the current scaling value
     */
    public void drawSprite(SpriteSheet<?> spriteSheet, RectShort sprite, double x, double y, boolean scaled) {
        requireNonNull(spriteSheet);
        requireNonNull(sprite);
        double s = scaled ? scaling() : 1;
        ctx().drawImage(spriteSheet.sourceImage(),
                sprite.x(), sprite.y(), sprite.width(), sprite.height(),
                s * x, s * y, s * sprite.width(), s * sprite.height());
    }

    /**
     * Draws the sprite centered over the given position. The target position is scaled using the current scaling value.
     *
     * @param center position over which sprite gets drawn
     * @param sprite the actor sprite
     */
    public void drawSpriteCentered(Vector2f center, RectShort sprite) {
        drawSpriteCentered(center.x(), center.y(), sprite);
    }

    public void drawSpriteCentered(double centerX, double centerY, RectShort sprite) {
        drawSprite(spriteSheet(), sprite, centerX - 0.5 * sprite.width(), centerY - 0.5 * sprite.height(), true);
    }
}