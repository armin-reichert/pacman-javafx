/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.tilemap.rendering;

import de.amr.games.pacman.lib.Vector2f;
import de.amr.games.pacman.lib.Vector2i;
import de.amr.games.pacman.lib.tilemap.Obstacle;
import de.amr.games.pacman.lib.tilemap.ObstacleSegment;
import de.amr.games.pacman.lib.tilemap.TileEncoding;
import de.amr.games.pacman.lib.tilemap.TileMap;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import java.util.List;

import static de.amr.games.pacman.lib.Globals.HTS;
import static de.amr.games.pacman.lib.Globals.TS;
import static java.util.function.Predicate.not;

/**
 * Vector renderer for terrain tile maps.
 */
public class TerrainRenderer implements TileMapRenderer {

    public final DoubleProperty scalingPy = new SimpleDoubleProperty(this, "scaling", 1.0);

    private double doubleStrokeOuterWidth;
    private double doubleStrokeInnerWidth;
    private double singleStrokeWidth;

    private Color mapBackgroundColor;
    private Color wallFillColor;
    private Color wallStrokeColor;
    private Color doorColor;

    public TerrainRenderer() {
        mapBackgroundColor = Color.BLACK;
        wallFillColor = Color.BLACK;
        wallStrokeColor = Color.GREEN;
        doorColor = Color.PINK;
        doubleStrokeOuterWidth = 4;
        doubleStrokeInnerWidth = 2;
        singleStrokeWidth = 1;
    }

    @Override
    public void setScaling(double scaling) {
        scalingPy.set((float) scaling);
    }

    public double scaling() {
        return scalingPy.get();
    }

    public void setMapBackgroundColor(Color mapBackgroundColor) {
        this.mapBackgroundColor = mapBackgroundColor;
    }

    public void setWallStrokeColor(Color color) {
        wallStrokeColor = color;
    }

    public void setWallFillColor(Color wallFillColor) {
        this.wallFillColor = wallFillColor;
    }

    public void setDoorColor(Color doorColor) {
        this.doorColor = doorColor;
    }

    public void setDoubleStrokeInnerWidth(double doubleStrokeInnerWidth) {
        this.doubleStrokeInnerWidth = doubleStrokeInnerWidth;
    }

    public void setDoubleStrokeOuterWidth(double doubleStrokeOuterWidth) {
        this.doubleStrokeOuterWidth = doubleStrokeOuterWidth;
    }

    public void setSingleStrokeWidth(double singleStrokeWidth) {
        this.singleStrokeWidth = singleStrokeWidth;
    }

    @Override
    public void drawTerrain(GraphicsContext g, TileMap terrainMap, List<Obstacle> obstacles) {
        g.save();
        g.scale(scaling(), scaling());
        for (Obstacle obstacle : obstacles) {
            if (obstacle.hasDoubleWalls()) {
                drawObstacle(g, obstacle, doubleStrokeOuterWidth, false, wallStrokeColor);
                drawObstacle(g, obstacle, doubleStrokeInnerWidth, false, wallFillColor);
            } else {
                drawObstacle(g, obstacle, singleStrokeWidth, true, wallStrokeColor);
            }
        }
        terrainMap.tiles(TileEncoding.DOOR).forEach(door -> drawDoor(g, door, terrainMap.get(door.y(), door.x() - 1) != TileEncoding.DOOR));
        g.restore();
    }

    private void drawObstacle(GraphicsContext g, Obstacle obstacle, double lineWidth, boolean fill, Color strokeColor) {
        int r = HTS;
        Vector2f p = obstacle.startPoint();
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
                g.setFill(wallFillColor);
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
            g.setFill(mapBackgroundColor);
            g.fillRect(x, y - 1, 2 * TS, 4);
            g.setFill(doorColor);
            g.fillRect(x, y, 2 * TS, 2);
            g.setFill(wallStrokeColor);
            g.fillRect(x - 1, y - 1, 1, 3);
            g.fillRect(x + 2 * TS, y - 1, 1, 3);
        }
    }
}