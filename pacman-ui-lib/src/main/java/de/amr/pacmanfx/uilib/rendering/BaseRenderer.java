/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.uilib.rendering;

import de.amr.pacmanfx.lib.Vector2i;
import de.amr.pacmanfx.uilib.assets.ResourceManager;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;

import static de.amr.pacmanfx.Globals.HTS;
import static de.amr.pacmanfx.Globals.TS;
import static java.util.Objects.requireNonNull;

public class BaseRenderer implements CanvasRenderer {

    private static final Font ARCADE_FONT_TS;

    static {
        ResourceManager rm = () -> BaseRenderer.class;
        ARCADE_FONT_TS = rm.loadFont("/de/amr/pacmanfx/uilib/fonts/emulogic.ttf", TS);
    }

    public static void fillCanvas(Canvas canvas, Paint paint) {
        requireNonNull(canvas);
        requireNonNull(paint);
        GraphicsContext ctx = canvas.getGraphicsContext2D();
        ctx.setFill(paint);
        ctx.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
    }

    private final ObjectProperty<Color> backgroundColor = new SimpleObjectProperty<>(Color.BLACK);
    private final DoubleProperty scaling = new SimpleDoubleProperty(1.0);
    private final ObjectProperty<Font> arcadeFontTS = new SimpleObjectProperty<>();
    private final ObjectProperty<Font> arcadeFont6 = new SimpleObjectProperty<>();

    protected final GraphicsContext ctx;

    public BaseRenderer(Canvas canvas) {
        ctx = requireNonNull(canvas).getGraphicsContext2D();
        arcadeFontTS.bind(scaling.map(s -> Font.font(ARCADE_FONT_TS.getFamily(), scaled(TS))));
        arcadeFont6.bind(scaling.map(s -> Font.font(ARCADE_FONT_TS.getFamily(), scaled(6))));
    }

    @Override
    public Canvas canvas() {
        return ctx.getCanvas();
    }

    @Override
    public GraphicsContext ctx() {
        return ctx;
    }

    @Override
    public DoubleProperty scalingProperty() { return scaling; }

    public ObjectProperty<Color> backgroundColorProperty() {
        return backgroundColor;
    }

    /**
     * @return Arcade font at size "one tile" (8px) scaled with the current renderer scaling.
     */
    public Font arcadeFontTS() {
        return arcadeFontTS.get();
    }

    /**
     * @return Arcade font at size 6px scaled with the current renderer scaling.
     */
    public Font arcadeFont6() { return arcadeFont6.get(); }

    /**
     * @param size unscaled font size
     * @return the Arcade font at the given size scaled by the current renderer scaling value
     */
    public Font arcadeFont(double size) {
        return Font.font(ARCADE_FONT_TS.getFamily(), scaled(size));
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
}