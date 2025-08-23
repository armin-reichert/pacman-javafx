/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.uilib.tilemap;

import de.amr.pacmanfx.lib.Vector2i;
import de.amr.pacmanfx.lib.tilemap.Obstacle;
import de.amr.pacmanfx.lib.tilemap.ObstacleSegment;
import de.amr.pacmanfx.lib.tilemap.WorldMap;
import javafx.beans.property.FloatProperty;
import javafx.beans.property.SimpleFloatProperty;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import java.util.Set;

import static de.amr.pacmanfx.Globals.HTS;
import static de.amr.pacmanfx.Globals.TS;
import static java.util.Objects.requireNonNull;

/**
 * Vector renderer for terrain tile maps.
 */
public class TerrainMapRenderer implements TileMapRenderer {

    public static final TerrainMapColorScheme DEFAULT_COLOR_SCHEME = new TerrainMapColorScheme(Color.BLACK, Color.RED,  Color.GOLD, Color.PINK);

    private final FloatProperty scalingPy = new SimpleFloatProperty(this, "scaling", 1.0f);

    protected double doubleStrokeOuterWidth;
    protected double doubleStrokeInnerWidth;
    protected double singleStrokeWidth;
    protected TerrainMapColorScheme colorScheme;

    public TerrainMapRenderer() {
        doubleStrokeOuterWidth = 4;
        doubleStrokeInnerWidth = 2;
        singleStrokeWidth = 1;
        colorScheme = DEFAULT_COLOR_SCHEME;
    }

    public TerrainMapColorScheme colorScheme() {
        return colorScheme;
    }

    public void setColorScheme(TerrainMapColorScheme colors) {
        this.colorScheme = requireNonNull(colors);
    }

    public FloatProperty scalingProperty() { return scalingPy; }

    @Override
    public void setScaling(double scaling) {
        scalingPy.set((float) scaling);
    }

    public double scaling() {
        return scalingPy.get();
    }

    public void setDoubleStrokeInnerWidth(double width) {
        doubleStrokeInnerWidth = width;
    }

    public void setDoubleStrokeOuterWidth(double width) {
        doubleStrokeOuterWidth = width;
    }

    public void setSingleStrokeWidth(double width) {
        singleStrokeWidth = width;
    }

    public void drawTerrain(GraphicsContext g, WorldMap worldMap, Set<Obstacle> obstacles) {
        g.save();
        g.scale(scaling(), scaling());
        for (Obstacle obstacle : obstacles) {
            if (startsAtBorder(obstacle, worldMap)) {
                drawObstacle(g, obstacle, doubleStrokeOuterWidth, false, colorScheme.wallFillColor(), colorScheme.wallStrokeColor());
                drawObstacle(g, obstacle, doubleStrokeInnerWidth, false, colorScheme.wallFillColor(), colorScheme.wallFillColor());
            }
        }
        for (Obstacle obstacle : obstacles) {
            if (!startsAtBorder(obstacle, worldMap)) {
                //boolean hasParent = obstacle.getParent() != null;
                drawObstacle(g, obstacle, singleStrokeWidth, true, colorScheme.wallFillColor(), colorScheme.wallStrokeColor());
            }
        }
        g.restore();
    }

    protected boolean startsAtBorder(Obstacle obstacle, WorldMap worldMap) {
        Vector2i start = obstacle.startPoint();
        return start.x() <= TS || start.x() >= (worldMap.numCols() - 1) * TS
            || start.y() <= 4*TS || start.y() >= (worldMap.numRows() - 1) * TS;
    }

    private void drawObstacle(GraphicsContext g, Obstacle obstacle, double lineWidth, boolean fill, Color fillColor, Color strokeColor) {
        int r = HTS;
        Vector2i p = obstacle.startPoint();
        g.beginPath();
        g.moveTo(p.x(), p.y());
        for (ObstacleSegment segment : obstacle.segments()) {
            p = p.plus(segment.vector());
            double x = p.x(), y = p.y();
            if (segment.isStraightLine()) {
                g.lineTo(x, y);
            } else {
                boolean counterClockwise = segment.ccw();
                if (segment.isRoundedNWCorner()) {
                    if (counterClockwise) g.arc(x+r, y, r, r,  90, 90); else g.arc(x, y+r, r, r, 180, -90);
                }
                else if (segment.isRoundedSWCorner()) {
                    if (counterClockwise) g.arc(x, y-r, r, r, 180, 90); else g.arc(x+r, y, r, r, 270, -90);
                }
                else if (segment.isRoundedSECorner()) {
                    if (counterClockwise) g.arc(x-r, y, r, r, 270, 90); else g.arc(x, y-r, r, r, 0, -90);
                }
                else if (segment.isRoundedNECorner()) {
                    if (counterClockwise) g.arc(x, y+r, r, r, 0, 90); else g.arc(x-r, y, r, r, 90, -90);
                }
                else if (segment.isAngularNWCorner()) {
                    if (counterClockwise) g.lineTo(x,y-r); else g.lineTo(x-r, y);
                    g.lineTo(x, y);
                }
                else if (segment.isAngularSWCorner()) {
                    if (counterClockwise) g.lineTo(x-r, y); else g.lineTo(x, y+r);
                    g.lineTo(x, y);
                }
                else if (segment.isAngularSECorner()) {
                    if (counterClockwise) g.lineTo(x,y+r); else g.lineTo(x-r, y);
                    g.lineTo(x, y);
                }
                else if (segment.isAngularNECorner()) {
                    if (counterClockwise) g.lineTo(x+r, y); else g.lineTo(x-r, y-r);
                    g.lineTo(x, y);
                }
            }
        }
        if (obstacle.isClosed()) {
            g.closePath();
            if (fill) {
                g.setFill(fillColor);
                g.fill();
            }
        }
        g.setLineWidth(lineWidth);
        g.setStroke(strokeColor);
        g.stroke();
    }

    @Override
    public void drawTile(GraphicsContext g, Vector2i tile, byte content) {
        // this renderer doesn't draw tiles individually but draws complete paths
    }

    public void drawHouse(GraphicsContext g, Vector2i origin, Vector2i size) {
        g.save();
        g.scale(scaling(), scaling());
        drawHouseWalls(g, origin, size, colorScheme.wallStrokeColor(), doubleStrokeOuterWidth);
        drawHouseWalls(g, origin, size, colorScheme.wallFillColor(), doubleStrokeInnerWidth);
        drawDoors(g, origin.plus((size.x() / 2 - 1), 0));
        g.restore();
    }

    private void drawHouseWalls(GraphicsContext g, Vector2i origin, Vector2i size, Color color, double lineWidth) {
        Vector2i p = origin.scaled(TS).plus(HTS, HTS);
        double w = (size.x() - 1) * TS, h = (size.y() - 1) * TS - 2;
        g.save();
        g.beginPath();
        g.moveTo(p.x(), p.y());
        g.lineTo(p.x(), p.y() + h);
        g.lineTo(p.x() + w, p.y() + h);
        g.lineTo(p.x() + w, p.y());
        g.lineTo(p.x() + w - 2 * TS, p.y());
        g.moveTo(p.x(), p.y());
        g.lineTo(p.x() + 2 * TS, p.y());
        g.setLineWidth(lineWidth);
        g.setStroke(color);
        g.stroke();
        g.restore();
    }

    // assume we always have a pair of horizontally neighbored doors
    private void drawDoors(GraphicsContext g, Vector2i tile) {
        double x = tile.x() * TS, y = tile.y() * TS + 3;
        g.setFill(colorScheme.backgroundColor());
        g.fillRect(x, y - 1, 2 * TS, 4);
        g.setFill(colorScheme.doorColor());
        g.fillRect(x-2, y, 2 * TS + 4, 2);
        //g.setFill(colors.wallStrokeColor());
        //g.fillRect(x - 1, y - 1, 1, 3);
        //g.fillRect(x + 2 * TS, y - 1, 1, 3);
    }
}