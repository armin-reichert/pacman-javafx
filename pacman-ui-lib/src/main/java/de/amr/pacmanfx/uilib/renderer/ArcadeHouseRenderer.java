/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.uilib.renderer;

import de.amr.basics.math.Vector2i;
import de.amr.basics.ui.rendering.BaseRenderer;
import de.amr.basics.ui.rendering.Renderable;
import de.amr.basics.ui.rendering.RenderableGameEntity;
import de.amr.pacmanfx.core.entities.world.house.House;
import de.amr.pacmanfx.uilib.rendering.TerrainMapColoring;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.canvas.Canvas;
import javafx.scene.paint.Color;

/**
 * Renders an arcade house shape. Used by generic level renderer (XXL game variants) and by the world map editor preview.
 */
public class ArcadeHouseRenderer extends BaseRenderer {

    // Define tile size here to avoid dependency.
    static final int TS = 8, HTS = 4;
    
    private final ObjectProperty<TerrainMapColoring> mapColoring = new SimpleObjectProperty<>(TerrainMapRenderer.DEFAULT_MAP_COLORING);

    public void setMapColoring(TerrainMapColoring mapColoring) {
        this.mapColoring.set(mapColoring);
    }

    public TerrainMapColoring mapColoring() {
        return mapColoring.get();
    }

    public ObjectProperty<TerrainMapColoring> mapColoringProperty() {
        return mapColoring;
    }

    private final DoubleProperty borderWallFullWidth = new SimpleDoubleProperty(4);

    public DoubleProperty borderWallFullWidthProperty() {
        return borderWallFullWidth;
    }

    public void setBorderWallFullWidth(double width) {
        borderWallFullWidth.set(width);
    }

    public double borderWallFullWidth() {
        return borderWallFullWidth.get();
    }

    private final DoubleProperty borderWallInnerWidth = new SimpleDoubleProperty(2);

    public DoubleProperty borderWallInnerWidthProperty() {
        return borderWallInnerWidth;
    }

    public void setBorderWallInnerWidth(double width) {
        borderWallInnerWidth.set(width);
    }

    public double borderWallInnerWidth() {
        return borderWallInnerWidth.get();
    }

    public ArcadeHouseRenderer(Canvas canvas) {
        super(canvas);
    }

    @Override
    public void render(Renderable r, long tick) {
        if (r instanceof RenderableGameEntity rge
            && rge.gameEntity() instanceof House house) {
            renderHouse(house);
        }
    }

    public void renderHouse(House house) {
        drawHouse(
            house.floorplan().minTile(),
            house.sizeInTiles(),
            borderWallFullWidth(),
            borderWallInnerWidth()
        );
    }

    /**
     * Draws the house shape at the given origin. This entry point is used by the world map editor to draw the house
     * without having created an entity for it.
     *
     * @param originTile the left-upper corner tile of the house
     * @param sizeInTiles the house size in tiles
     * @param doubleStrokeOuterWidth the stroke width of the house border
     * @param doubleStrokeInnerWidth the inners stroke width of the house border 
     */
    public void drawHouse(Vector2i originTile, Vector2i sizeInTiles, double doubleStrokeOuterWidth, double doubleStrokeInnerWidth) {
        ctx.save();
        ctx.scale(scaling(), scaling());
        drawHouseWalls(originTile, sizeInTiles, mapColoring().wallStrokeColor(), doubleStrokeOuterWidth);
        drawHouseWalls(originTile, sizeInTiles, mapColoring().wallFillColor(), doubleStrokeInnerWidth);
        drawDoors(originTile.plus((sizeInTiles.x() / 2 - 1), 0), mapColoring().floorColor(), mapColoring().doorColor());
        ctx.restore();
    }

    private void drawHouseWalls(Vector2i originTile, Vector2i sizeInTiles, Color color, double lineWidth) {
        Vector2i p = originTile.scaled(TS).plus(HTS, HTS);
        double w = (sizeInTiles.x() - 1) * TS, h = (sizeInTiles.y() - 1) * TS - 2;
        ctx.save();
        ctx.beginPath();
        ctx.moveTo(p.x(), p.y());
        ctx.lineTo(p.x(), p.y() + h);
        ctx.lineTo(p.x() + w, p.y() + h);
        ctx.lineTo(p.x() + w, p.y());
        ctx.lineTo(p.x() + w - 2 * TS, p.y());
        ctx.moveTo(p.x(), p.y());
        ctx.lineTo(p.x() + 2 * TS, p.y());
        ctx.setLineWidth(lineWidth);
        ctx.setStroke(color);
        ctx.stroke();
        ctx.restore();
    }

    // Assume pair of 2 horizontally neighbored doors
    private void drawDoors(Vector2i tile, Color floorColor, Color doorColor) {
        double x = tile.x() * TS, y = tile.y() * TS + 3;
        ctx.setFill(floorColor);
        ctx.fillRect(x, y - 1, 2 * TS, 4);
        ctx.setFill(doorColor);
        ctx.fillRect(x-2, y, 2 * TS + 4, 2);
    }
}
