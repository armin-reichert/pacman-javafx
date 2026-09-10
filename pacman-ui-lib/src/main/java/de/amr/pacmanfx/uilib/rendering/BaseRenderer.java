/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.uilib.rendering;

import de.amr.basics.InfoMap;
import de.amr.basics.math.Vector2i;
import de.amr.basics.util.Ufx;
import de.amr.pacmanfx.core.entities.TextDisplay;
import de.amr.pacmanfx.core.model.world.map.WorldMap;
import de.amr.pacmanfx.core.rendering.ColoredRect;
import de.amr.pacmanfx.core.rendering.Renderable;
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

import java.util.Optional;

import static java.util.Objects.requireNonNull;

/**
 * Base renderer class providing support for scaling, background color and common font drawing.
 */
public abstract class BaseRenderer implements Renderer {

    private static final Text dummy = new Text();

    /**
     * Computes the layout width of the given string when rendered with the specified font.
     *
     * @param s    the text to measure
     * @param font the font used for measurement
     * @return the width in pixels
     */
    public static double textWidth(String s, Font font) {
        dummy.setText(s);
        dummy.setFont(font);
        return dummy.getLayoutBounds().getWidth();
    }

    private final ObjectProperty<Color> backgroundColor = new SimpleObjectProperty<>(Color.BLACK);

    private final DoubleProperty scaling = new SimpleDoubleProperty(1.0);

    protected final GraphicsContext ctx;

    protected InfoMap info;

    private BaseRenderer debugInfoRenderer;

    public BaseRenderer(Canvas canvas) {
        ctx = requireNonNull(canvas).getGraphicsContext2D();
        info = new InfoMap();
    }

    public void setDebugInfoRenderer(BaseRenderer debugInfoRenderer) {
        this.debugInfoRenderer = debugInfoRenderer;
    }

    // Renderer interface


    @Override
    public void render(Renderable r, long tick) {
        switch (r) {
            case ColoredRect coloredRect -> fillColoredRect(coloredRect);
            case TextDisplay textDisplay-> drawCenteredText(textDisplay);
            default -> throw new IllegalStateException("Unexpected value: " + r);
        }
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
    public InfoMap info() {
        return info;
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

    @Override
    public Optional<BaseRenderer> optDebugInfoRenderer() {
        return Optional.ofNullable(debugInfoRenderer);
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
        final double centerX = tile.x() * WorldMap.TS + WorldMap.HTS;
        final double centerY = tile.y() * WorldMap.TS + WorldMap.HTS;
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

    public void drawTileGrid(double sizeX, double sizeY, Color gridColor) {
        final double scaledTileSize = scaled(WorldMap.TS);
        final double thin = 0.2, medium = 0.4, thick = 0.8;
        final int numCols = (int) (sizeX / WorldMap.TS), numRows = (int) (sizeY / WorldMap.TS);
        final double width = numCols * scaledTileSize, height = numRows * scaledTileSize;
        ctx.save();
        ctx.setStroke(Color.YELLOW);
        ctx.strokeRect(0, 0, ctx.getCanvas().getWidth(), ctx.getCanvas().getHeight());
        ctx.setStroke(gridColor);
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

    // ----------------

    private void drawCenteredText(TextDisplay textDisplay) {
        final var center = textDisplay.pos();
        final var data = textDisplay.data();
        fillTextCentered(
            data.text(),
            data.fillColor(),
            Ufx.scaleFontBy(data.font(), scaling()),
            center.x(),
            center.y()
        );
    }

    private void fillColoredRect(ColoredRect coloredRect) {
        final var rect = coloredRect.rect();
        ctx.save();
        ctx.setFill(coloredRect.color());
        ctx.fillRect(scaled(rect.x()), scaled(rect.y()), scaled(rect.width()), scaled(rect.height()));
        ctx.restore();
    }
}