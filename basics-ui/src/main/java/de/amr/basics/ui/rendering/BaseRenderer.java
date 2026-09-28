/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.basics.ui.rendering;

import de.amr.basics.math.RectShort;
import de.amr.basics.math.Vector2f;
import de.amr.basics.math.Vector2i;
import de.amr.basics.ui.assets.SpriteSheet;
import de.amr.basics.ui.entities.props.CanvasClear;
import de.amr.basics.ui.entities.props.CanvasFill;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;

import static de.amr.basics.TileDimension.HTS;
import static de.amr.basics.TileDimension.TS;
import static java.util.Objects.requireNonNull;

/**
 * Base renderer class providing support for scaling, background color and common font drawing.
 */
public class BaseRenderer implements Renderer {

    private static final Text DUMMY_TEXT = new Text();

    /**
     * Computes the layout width of the given string when rendered with the specified font.
     *
     * @param s    the text to measure
     * @param font the font used for measurement
     * @return the width in pixels
     */
    public static double textWidth(String s, Font font) {
        DUMMY_TEXT.setText(s);
        DUMMY_TEXT.setFont(font);
        return DUMMY_TEXT.getLayoutBounds().getWidth();
    }

    private final ObjectProperty<Color> backgroundColor = new SimpleObjectProperty<>(Color.BLACK);

    private final DoubleProperty scaling = new SimpleDoubleProperty(1.0);

    protected GraphicsContext ctx;

    protected SpriteSheet<?> spriteSheet;

    public BaseRenderer() {}

    public BaseRenderer(Canvas canvas) {
        setCanvas(canvas);
    }

    public void setCanvas(Canvas canvas) {
        requireNonNull(canvas);
        ctx = canvas.getGraphicsContext2D();
    }

    public SpriteSheet<?> spriteSheet() {
        return spriteSheet;
    }

    public void setSpriteSheet(SpriteSheet<?> spriteSheet) {
        this.spriteSheet = spriteSheet;
    }

    // Renderer interface

    @Override
    public void render(Renderable r, long tick) {
        ctx.save();
        switch (r) {
            case CanvasClear _ -> clearCanvas();
            case CanvasFill canvasFill -> fillCanvas(canvasFill.color());
            default -> throw new IllegalStateException("Cannot render: " + r);
        }
        ctx.restore();
    }

    @Override
    public void clearCanvas() {
        fillCanvas(backgroundColor());
    }

    @Override
    public void fillCanvas(Color color) {
        requireNonNull(color);
        ctx.save();
        ctx.setFill(color);
        ctx.fillRect(0, 0, canvas().getWidth(), canvas().getHeight());
        ctx.restore();
    }

    @Override
    public GraphicsContext ctx() {
        return ctx;
    }

    @Override
    public DoubleProperty scalingProperty() { return scaling; }

    @Override
    public double scaling() {
        return scaling.get();
    }

    @Override
    public ObjectProperty<Color> backgroundColorProperty() {
        return backgroundColor;
    }

    @Override
    public Color backgroundColor() {
        return backgroundColorProperty().get();
    }

    // Sprites

    /**
     * Draws a sprite (region inside sprite sheet) at the given position.
     *
     * @param sprite      the sprite to draw
     * @param x           x-coordinate of left-upper corner
     * @param y           y-coordinate of left-upper corner
     * @param scaled      tells is the destination rectangle's position and size will be scaled using the current scaling value
     */
    public void drawSprite(RectShort sprite, double x, double y, boolean scaled) {
        requireNonNull(sprite);
        if (spriteSheet != null) {
            final double s = scaled ? scaling() : 1;
            ctx().drawImage(spriteSheet.sourceImage(),
                sprite.x(), sprite.y(), sprite.width(), sprite.height(),
                s * x, s * y, s * sprite.width(), s * sprite.height());
        }
    }

    /**
     * Draws the sprite centered over the given position. The target position will be scaled using the current scaling value.
     *
     * @param sprite the actor sprite
     * @param unscaledX unscaled x-position over which sprite gets drawn
     * @param unscaledY unscaled y-position over which sprite gets drawn
     */
    public void drawSpriteCentered(RectShort sprite, double unscaledX, double unscaledY) {
        drawSprite(sprite, unscaledX - 0.5 * sprite.width(), unscaledY - 0.5 * sprite.height(), true);
    }

    /**
     * Draws the sprite centered over the given position. The target position will be scaled using the current scaling value.
     *
     * @param sprite the actor sprite
     * @param centerUnscaled position over which sprite gets drawn
     */
    public void drawSpriteCentered(RectShort sprite, Vector2f centerUnscaled) {
        drawSpriteCentered(sprite, centerUnscaled.x(), centerUnscaled.y());
    }

    public void setScaling(double value) {
        if (value <= 0) {
            throw new IllegalArgumentException("Scaling value must be positive but is %.2f".formatted(value));
        }
        scalingProperty().set(value);
    }

    /**
     * Fills a square at the center of the given tile with the current fill color. Used to hide pellets, energizers
     * or sprites that are part of a map image.
     *
     * @param tile a tile
     * @param sideLength side length of the square
     */
    public void fillSquareAtTileCenter(Vector2i tile, double sideLength) {
        requireNonNull(tile);
        final double centerX = tile.x() * TS + HTS;
        final double centerY = tile.y() * TS + HTS;
        final double halfSideLength = 0.5f * sideLength;
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
        ctx.save();
        ctx.setFont(font);
        ctx.setFill(color);
        ctx.fillText(text, scaled(x), scaled(y));
        ctx.restore();
    }

    /**
     * Draws text left-aligned at the given position (scaled by the current scaling value).
     *
     * @param text  text
     * @param color text color
     * @param unscaledCenterX     unscaled x-position (center)
     * @param unscaledBaselineY   unscaled y-position (baseline)
     */
    public void fillText(String text, Color color, double unscaledCenterX, double unscaledBaselineY) {
        ctx.save();
        ctx.setFill(color);
        ctx.fillText(text, scaled(unscaledCenterX), scaled(unscaledBaselineY));
        ctx.restore();
    }

    /**
     * Draws text center-aligned at the given x position (scaled by the current scaling value).
     *
     * @param text  text
     * @param color text color
     * @param font  text font
     * @param unscaledCenterX  unscaled center x-position
     * @param unscaledBaselineY unscaled y-position (baseline)
     */
    public void fillTextCentered(String text, Color color, Font font, double unscaledCenterX, double unscaledBaselineY) {
        ctx.save();
        ctx.setTextAlign(TextAlignment.CENTER);
        fillText(text, color, font, unscaledCenterX, unscaledBaselineY);
        ctx.restore();
    }

    public void drawDebugGrid(double sizeX, double sizeY, Color gridColor) {
        final double scaledTileSize = scaled(TS);
        final double thin = 0.2, medium = 0.4, thick = 0.8;
        final int numCols = (int) (sizeX / TS), numRows = (int) (sizeY / TS);
        final double width = numCols * scaledTileSize, height = numRows * scaledTileSize;
        ctx.save();
        ctx.setStroke(gridColor);
        ctx.strokeRect(0, 0, ctx.getCanvas().getWidth(), ctx.getCanvas().getHeight());
        for (int row = 0; row <= numRows; ++row) {
            final double y = row * scaledTileSize;
            ctx.setLineWidth(row % 10 == 0 ? thick : row % 5 == 0 ? medium : thin);
            ctx.strokeLine(0, y, width, y);
        }
        for (int col = 0; col <= numCols; ++col) {
            final double x = col * scaledTileSize;
            ctx.setLineWidth(col % 10 == 0 ? thick : col % 5 == 0? medium : thin);
            ctx.strokeLine(x, 0, x, height);
        }
        ctx.restore();
    }
}