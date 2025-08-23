/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.uilib.tilemap;

import de.amr.pacmanfx.lib.Vector2i;
import de.amr.pacmanfx.lib.tilemap.Obstacle;
import de.amr.pacmanfx.lib.tilemap.ObstacleSegment;
import de.amr.pacmanfx.lib.tilemap.WorldMap;
import de.amr.pacmanfx.uilib.rendering.BaseRenderer;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.canvas.Canvas;
import javafx.scene.paint.Color;

import java.util.Set;

import static de.amr.pacmanfx.Globals.HTS;
import static de.amr.pacmanfx.Globals.TS;
import static java.util.Objects.requireNonNull;

/**
 * Vector renderer for terrain tile maps.
 */
public class TerrainMapRenderer extends BaseRenderer implements TileMapRenderer {

    public static final TerrainMapColorScheme DEFAULT_COLOR_SCHEME = new TerrainMapColorScheme(Color.BLACK, Color.RED,  Color.GOLD, Color.PINK);

    protected double doubleStrokeOuterWidth;
    protected double doubleStrokeInnerWidth;
    protected double singleStrokeWidth;
    protected final ObjectProperty<TerrainMapColorScheme> colorScheme = new SimpleObjectProperty<>();

    public TerrainMapRenderer(Canvas canvas) {
        super(canvas);
        doubleStrokeOuterWidth = 4;
        doubleStrokeInnerWidth = 2;
        singleStrokeWidth = 1;
        colorScheme.set(DEFAULT_COLOR_SCHEME);
    }

    public TerrainMapColorScheme colorScheme() {
        return colorScheme.get();
    }

    public void setColorScheme(TerrainMapColorScheme colorScheme) {
        this.colorScheme.set(requireNonNull(colorScheme));
    }

    public ObjectProperty<TerrainMapColorScheme> colorSchemeProperty() {
        return colorScheme;
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

    public void drawTerrain(WorldMap worldMap, Set<Obstacle> obstacles) {
        ctx().save();
        ctx().scale(scaling(), scaling());
        for (Obstacle obstacle : obstacles) {
            if (startsAtBorder(obstacle, worldMap)) {
                drawObstacle(obstacle, doubleStrokeOuterWidth, false, colorScheme().wallFillColor(), colorScheme().wallStrokeColor());
                drawObstacle(obstacle, doubleStrokeInnerWidth, false, colorScheme().wallFillColor(), colorScheme().wallFillColor());
            }
        }
        for (Obstacle obstacle : obstacles) {
            if (!startsAtBorder(obstacle, worldMap)) {
                //boolean hasParent = obstacle.getParent() != null;
                drawObstacle(obstacle, singleStrokeWidth, true, colorScheme().wallFillColor(), colorScheme().wallStrokeColor());
            }
        }
        ctx().restore();
    }

    protected boolean startsAtBorder(Obstacle obstacle, WorldMap worldMap) {
        Vector2i start = obstacle.startPoint();
        return start.x() <= TS || start.x() >= (worldMap.numCols() - 1) * TS
            || start.y() <= 4*TS || start.y() >= (worldMap.numRows() - 1) * TS;
    }

    private void drawObstacle(Obstacle obstacle, double lineWidth, boolean fill, Color fillColor, Color strokeColor) {
        int r = HTS;
        Vector2i p = obstacle.startPoint();
        ctx().beginPath();
        ctx().moveTo(p.x(), p.y());
        for (ObstacleSegment segment : obstacle.segments()) {
            p = p.plus(segment.vector());
            double x = p.x(), y = p.y();
            if (segment.isStraightLine()) {
                ctx().lineTo(x, y);
            } else {
                boolean counterClockwise = segment.ccw();
                if (segment.isRoundedNWCorner()) {
                    if (counterClockwise) ctx().arc(x+r, y, r, r,  90, 90); else ctx().arc(x, y+r, r, r, 180, -90);
                }
                else if (segment.isRoundedSWCorner()) {
                    if (counterClockwise) ctx().arc(x, y-r, r, r, 180, 90); else ctx().arc(x+r, y, r, r, 270, -90);
                }
                else if (segment.isRoundedSECorner()) {
                    if (counterClockwise) ctx().arc(x-r, y, r, r, 270, 90); else ctx().arc(x, y-r, r, r, 0, -90);
                }
                else if (segment.isRoundedNECorner()) {
                    if (counterClockwise) ctx().arc(x, y+r, r, r, 0, 90); else ctx().arc(x-r, y, r, r, 90, -90);
                }
                else if (segment.isAngularNWCorner()) {
                    if (counterClockwise) ctx().lineTo(x,y-r); else ctx().lineTo(x-r, y);
                    ctx().lineTo(x, y);
                }
                else if (segment.isAngularSWCorner()) {
                    if (counterClockwise) ctx().lineTo(x-r, y); else ctx().lineTo(x, y+r);
                    ctx().lineTo(x, y);
                }
                else if (segment.isAngularSECorner()) {
                    if (counterClockwise) ctx().lineTo(x,y+r); else ctx().lineTo(x-r, y);
                    ctx().lineTo(x, y);
                }
                else if (segment.isAngularNECorner()) {
                    if (counterClockwise) ctx().lineTo(x+r, y); else ctx().lineTo(x-r, y-r);
                    ctx().lineTo(x, y);
                }
            }
        }
        if (obstacle.isClosed()) {
            ctx().closePath();
            if (fill) {
                ctx().setFill(fillColor);
                ctx().fill();
            }
        }
        ctx().setLineWidth(lineWidth);
        ctx().setStroke(strokeColor);
        ctx().stroke();
    }

    @Override
    public void drawTile(Vector2i tile, byte content) {
        // this renderer doesn't draw tiles individually but draws complete paths
    }

    public void drawHouse(Vector2i origin, Vector2i size) {
        ctx().save();
        ctx().scale(scaling(), scaling());
        drawHouseWalls(origin, size, colorScheme().wallStrokeColor(), doubleStrokeOuterWidth);
        drawHouseWalls(origin, size, colorScheme().wallFillColor(), doubleStrokeInnerWidth);
        drawDoors(origin.plus((size.x() / 2 - 1), 0));
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
    private void drawDoors(Vector2i tile) {
        double x = tile.x() * TS, y = tile.y() * TS + 3;
        ctx().setFill(colorScheme().backgroundColor());
        ctx().fillRect(x, y - 1, 2 * TS, 4);
        ctx().setFill(colorScheme().doorColor());
        ctx().fillRect(x-2, y, 2 * TS + 4, 2);
    }
}