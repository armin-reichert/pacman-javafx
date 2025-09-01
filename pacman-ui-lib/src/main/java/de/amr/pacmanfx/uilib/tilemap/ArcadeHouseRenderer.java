package de.amr.pacmanfx.uilib.tilemap;

import de.amr.pacmanfx.lib.Vector2i;
import de.amr.pacmanfx.uilib.rendering.BaseCanvasRenderer;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.canvas.Canvas;
import javafx.scene.paint.Color;

import static de.amr.pacmanfx.Globals.HTS;
import static de.amr.pacmanfx.Globals.TS;

public class ArcadeHouseRenderer extends BaseCanvasRenderer {

    private static final TerrainMapColorScheme DEFAULT_COLOR_SCHEME = new TerrainMapColorScheme(
        Color.BLACK, Color.GRAY, Color.BLUE, Color.PINK
    );


    private final ObjectProperty<TerrainMapColorScheme> colorScheme = new SimpleObjectProperty<>(DEFAULT_COLOR_SCHEME);

    public void setColorScheme(TerrainMapColorScheme colorScheme) {
        this.colorScheme.set(colorScheme);
    }

    public TerrainMapColorScheme colorScheme() {
        return colorScheme.get();
    }

    public ObjectProperty<TerrainMapColorScheme> colorSchemeProperty() {
        return colorScheme;
    }

    public ArcadeHouseRenderer(Canvas canvas) {
        super(canvas);
    }

    public void drawHouse(
        Vector2i origin, Vector2i size,
        double doubleStrokeOuterWidth, double doubleStrokeInnerWidth) {
        ctx().save();
        ctx().scale(scaling(), scaling());
        drawHouseWalls(origin, size, colorScheme().wallStrokeColor(), doubleStrokeOuterWidth);
        drawHouseWalls(origin, size, colorScheme().wallFillColor(), doubleStrokeInnerWidth);
        drawDoors(origin.plus((size.x() / 2 - 1), 0), colorScheme().floorColor(), colorScheme().doorColor());
        ctx().restore();
    }

    private void drawHouseWalls(Vector2i origin, Vector2i size, Color color, double lineWidth) {
        Vector2i p = origin.scaled(TS).plus(HTS, HTS);
        double w = (size.x() - 1) * TS, h = (size.y() - 1) * TS - 2;
        ctx().save();
        ctx().beginPath();
        ctx().moveTo(p.x(), p.y());
        ctx().lineTo(p.x(), p.y() + h);
        ctx().lineTo(p.x() + w, p.y() + h);
        ctx().lineTo(p.x() + w, p.y());
        ctx().lineTo(p.x() + w - 2 * TS, p.y());
        ctx().moveTo(p.x(), p.y());
        ctx().lineTo(p.x() + 2 * TS, p.y());
        ctx().setLineWidth(lineWidth);
        ctx().setStroke(color);
        ctx().stroke();
        ctx().restore();
    }

    // assume we always have a pair of horizontally neighbored doors
    private void drawDoors(Vector2i tile, Color floorColor, Color doorColor) {
        double x = tile.x() * TS, y = tile.y() * TS + 3;
        ctx().setFill(floorColor);
        ctx().fillRect(x, y - 1, 2 * TS, 4);
        ctx().setFill(doorColor);
        ctx().fillRect(x-2, y, 2 * TS + 4, 2);
    }
}
