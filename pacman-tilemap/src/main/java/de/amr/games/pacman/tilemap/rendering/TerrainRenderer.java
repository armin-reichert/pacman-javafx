/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.tilemap.rendering;

import de.amr.games.pacman.lib.Vector2i;
import de.amr.games.pacman.lib.tilemap.Obstacle;
import de.amr.games.pacman.lib.tilemap.ObstacleSegment;
import de.amr.games.pacman.lib.tilemap.TileEncoding;
import de.amr.games.pacman.lib.tilemap.TileMap;
import javafx.beans.property.FloatProperty;
import javafx.beans.property.SimpleFloatProperty;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import java.util.Set;

import static de.amr.games.pacman.lib.Globals.*;

/**
 * Vector renderer for terrain tile maps.
 */
public class TerrainRenderer implements TileMapRenderer {

    public static final TerrainColorScheme DEFAULT_COLOR_SCHEME = new TerrainColorScheme(Color.BLACK, Color.RED,  Color.GOLD, Color.PINK);

    public final FloatProperty scalingPy = new SimpleFloatProperty(this, "scaling", 1.0f);

    protected double doubleStrokeOuterWidth;
    protected double doubleStrokeInnerWidth;
    protected double singleStrokeWidth;
    protected TerrainColorScheme colors;

    public TerrainRenderer() {
        doubleStrokeOuterWidth = 4;
        doubleStrokeInnerWidth = 2;
        singleStrokeWidth = 1;
        colors = DEFAULT_COLOR_SCHEME;
    }

    public void setColors(TerrainColorScheme colors) {
        this.colors = assertNotNull(colors);
    }

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

    public void drawTerrain(GraphicsContext g, TileMap terrainMap, Set<Obstacle> obstacles) {
        g.save();
        g.scale(scaling(), scaling());
        for (Obstacle obstacle : obstacles) {
            if (obstacle.hasDoubleWalls()) {
                drawObstacle(g, obstacle, doubleStrokeOuterWidth, false, colors.wallFillColor(), colors.wallStrokeColor());
                drawObstacle(g, obstacle, doubleStrokeInnerWidth, false, colors.wallFillColor(), colors.wallFillColor());
            } else {
                boolean hasParent = obstacle.getParent() != null;
                drawObstacle(g, obstacle, singleStrokeWidth, true, hasParent ? colors.backgroundColor() : colors.wallFillColor(), colors.wallStrokeColor());
            }
        }
        terrainMap.tiles(TileEncoding.DOOR).forEach(door -> drawDoor(g, door, terrainMap.get(door.y(), door.x() - 1) != TileEncoding.DOOR));
        g.restore();
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

    // assume we always have a pair of horizontally neighbored doors
    private void drawDoor(GraphicsContext g, Vector2i tile, boolean leftDoor) {
        double x = tile.x() * TS, y = tile.y() * TS + 3;
        if (leftDoor) {
            g.setFill(colors.backgroundColor());
            g.fillRect(x, y - 1, 2 * TS, 4);
            g.setFill(colors.doorColor());
            g.fillRect(x, y, 2 * TS, 2);
            g.setFill(colors.wallStrokeColor());
            g.fillRect(x - 1, y - 1, 1, 3);
            g.fillRect(x + 2 * TS, y - 1, 1, 3);
        }
    }
}