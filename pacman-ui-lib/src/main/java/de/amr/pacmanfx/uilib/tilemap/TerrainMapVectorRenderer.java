/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.uilib.tilemap;

import de.amr.pacmanfx.lib.Vector2i;
import de.amr.pacmanfx.lib.tilemap.Obstacle;
import de.amr.pacmanfx.lib.tilemap.ObstacleSegment;
import de.amr.pacmanfx.lib.tilemap.WorldMap;
import de.amr.pacmanfx.model.GameLevel;
import de.amr.pacmanfx.uilib.rendering.BaseCanvasRenderer;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.canvas.Canvas;
import javafx.scene.paint.Color;

import java.util.Set;

import static de.amr.pacmanfx.Globals.HTS;
import static de.amr.pacmanfx.Globals.TS;
import static java.util.Objects.requireNonNull;

/**
 * Renderer for terrain layer of world map that renders precomputed paths for (closed) inner obstacles and border walls.
 * Border walls are drawn as double lines where area between these lines is filled with wall fill color,
 * inner obstacles are drawn with single line border and inner area filled with wall fill color.
 */
public class TerrainMapVectorRenderer extends BaseCanvasRenderer implements TerrainMapRenderer {

    /** Width of double lines used for border walls. */
    public static final double DEFAULT_BORDER_WALL_FULL_WIDTH = 4;

    /** Width of space between border wall double lines. */
    public static final double DEFAULT_BORDER_WALL_INNER_WIDTH = 2;

    /** Width of inner obstacle edges. */
    public static final double DEFAULT_INNER_WALL_WIDTH = 1;

    private final ObjectProperty<TerrainMapColorScheme> colorScheme = new SimpleObjectProperty<>(DEFAULT_COLOR_SCHEME);

    public ObjectProperty<TerrainMapColorScheme> colorSchemeProperty() {
        return colorScheme;
    }

    public TerrainMapColorScheme colorScheme() {
        return colorScheme.get();
    }

    public void setColorScheme(TerrainMapColorScheme scheme) {
        colorScheme.set(requireNonNull(scheme));
    }

    private final DoubleProperty borderWallFullWidth = new SimpleDoubleProperty(DEFAULT_BORDER_WALL_FULL_WIDTH);

    public DoubleProperty borderWallFullWidthProperty() {
        return borderWallFullWidth;
    }

    public void setBorderWallFullWidth(double width) {
        borderWallFullWidth.set(width);
    }

    public double borderWallFullWidth() {
        return borderWallFullWidth.get();
    }

    private final DoubleProperty borderWallInnerWidth = new SimpleDoubleProperty(DEFAULT_BORDER_WALL_INNER_WIDTH);

    public DoubleProperty borderWallInnerWidthProperty() {
        return borderWallInnerWidth;
    }

    public void setBorderWallInnerWidth(double width) {
        borderWallInnerWidth.set(width);
    }

    public double borderWallInnerWidth() {
        return borderWallInnerWidth.get();
    }

    private final DoubleProperty innerWallWidth = new SimpleDoubleProperty(DEFAULT_INNER_WALL_WIDTH);

    public DoubleProperty innerWallWidthProperty() {
        return innerWallWidth;
    }

    public void setInnerWallWidth(double width) {
        innerWallWidth.set(width);
    }

    public double innerWallWidth() {
        return innerWallWidth.get();
    }

    public TerrainMapVectorRenderer(Canvas canvas) {
        super(canvas);
    }

    public void draw(WorldMap worldMap, Set<Obstacle> obstacles) {
        ctx().save();
        ctx().scale(scaling(), scaling());
        for (Obstacle obstacle : obstacles) {
            if (isBorderObstacle(obstacle, worldMap)) {
                drawObstacle(obstacle, borderWallFullWidth(), false, colorScheme().wallFillColor(), colorScheme().wallStrokeColor());
                drawObstacle(obstacle, borderWallInnerWidth(), false, colorScheme().wallFillColor(), colorScheme().wallFillColor());
            } else {
                drawObstacle(obstacle, innerWallWidth(), true, colorScheme().wallFillColor(), colorScheme().wallStrokeColor());
            }
        }
        ctx().restore();
    }

    //TODO mark obstacles on creation as "border" vs. "inner"
    private boolean isBorderObstacle(Obstacle obstacle, WorldMap worldMap) {
        Vector2i start = obstacle.startPoint();
        return start.x() <= TS || start.x() >= (worldMap.numCols() - 1) * TS
            || start.y() <= GameLevel.EMPTY_ROWS_OVER_MAZE * TS || start.y() >= (worldMap.numRows() - 1) * TS;
    }

    private void drawObstacle(Obstacle obstacle, double lineWidth, boolean fillInside, Color fillColor, Color strokeColor) {
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
            if (fillInside) {
                ctx().setFill(fillColor);
                ctx().fill();
            }
        }
        ctx().setLineWidth(lineWidth);
        ctx().setStroke(strokeColor);
        ctx().stroke();
    }
}