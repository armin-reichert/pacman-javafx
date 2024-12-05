/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.maps.rendering;

import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.Vector2f;
import de.amr.games.pacman.lib.Vector2i;
import de.amr.games.pacman.lib.tilemap.Obstacle;
import de.amr.games.pacman.lib.tilemap.TerrainData;
import de.amr.games.pacman.lib.tilemap.TileMap;
import de.amr.games.pacman.lib.tilemap.Tiles;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import java.util.function.Predicate;

import static de.amr.games.pacman.lib.Globals.HTS;
import static de.amr.games.pacman.lib.Globals.TS;

/**
 * Vector renderer for terrain tile maps.
 */
public class CrappyTerrainRenderer implements TileMapRenderer {

    public final DoubleProperty scalingPy = new SimpleDoubleProperty(this, "scaling", 1.0);

    private double doubleStrokeOuterWidth;
    private double doubleStrokeInnerWidth;
    private double singleStrokeWidth;

    private Color mapBackgroundColor;
    private Color wallFillColor;
    private Color wallStrokeColor;
    private Color doorColor;

    public CrappyTerrainRenderer() {
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
    public void drawTerrain(GraphicsContext g, TileMap terrainMap, TerrainData terrainData) {
        g.save();
        g.scale(scaling(), scaling());

 /*
        terrainData.doubleStrokePaths().forEach(path -> {
            drawPath(g, terrainMap, path, false,  doubleStrokeOuterWidth, wallStrokeColor, null);
            drawPath(g, terrainMap, path, false,  doubleStrokeInnerWidth, wallFillColor, null);
        });
        terrainData.fillerPaths().forEach(
            path -> drawPath(g, terrainMap, path, true, singleStrokeWidth, wallFillColor, wallFillColor)
        );
        uglyConcavityHack(g, terrainData);
*/

        // new rendering
        terrainData.obstacles().stream()
            .filter(Obstacle::hasDoubleWalls)
            .forEach(obstacle -> {
                drawObstacle(g, obstacle, doubleStrokeOuterWidth, false, wallStrokeColor);
                drawObstacle(g, obstacle, doubleStrokeInnerWidth, false, wallFillColor);
            });

        terrainData.obstacles().stream()
            .filter(Predicate.not(Obstacle::hasDoubleWalls))
            .forEach(obstacle -> drawObstacle(g, obstacle, singleStrokeWidth, true, wallStrokeColor));

        terrainMap.tiles(Tiles.DOOR).forEach(door -> drawDoor(g, terrainMap, door, singleStrokeWidth, doorColor));

        g.restore();
    }

    // Fix concavities drawing (ugly hack)
    private void uglyConcavityHack(GraphicsContext g, TerrainData terrainData) {
        Color fillColor = wallFillColor;
        terrainData.topConcavityEntries().forEach(entry -> {
            double left = entry.x() * TS;
            double y = entry.y() * TS + 0.5 * (TS - doubleStrokeOuterWidth) + singleStrokeWidth;
            //TODO this is a hack:
            if (scaling() < 2.8) {
                y -= 0.5;
            } else if (scaling() < 1.5) {
                y -= 1.25;
            }
            double w = 2 * TS;
            double h = 0.5 * doubleStrokeOuterWidth; // TODO check this
            g.setFill(wallFillColor);
            g.fillRect(left, y, w, h);
            g.setStroke(wallStrokeColor);
            g.setLineWidth(singleStrokeWidth);
            g.strokeLine(left, y, left + w, y);
        });

        terrainData.bottomConcavityEntries().forEach(entry -> {
            double left = entry.x() * TS;
            double y = (entry.y() * TS + HTS + 0.5 * doubleStrokeInnerWidth); // TODO check this
            double w = 2 * TS, h = doubleStrokeInnerWidth * 0.75;
            g.setFill(fillColor);
            g.fillRect(left, y - h, w, h);
            g.fillRect(left + HTS, entry.y() * TS - HTS, TS, TS);
            g.setStroke(wallStrokeColor);
            g.setLineWidth(singleStrokeWidth);
            g.strokeLine(left, y, left + w, y);
        });
    }

    private void drawObstacle(GraphicsContext g, Obstacle obstacle, double lineWidth, boolean fill, Color strokeColor) {
        int r = HTS;
        Vector2f p = obstacle.startPoint();
        g.beginPath();
        g.moveTo(p.x(), p.y());
        for (int i = 0; i < obstacle.segments().size(); ++i) {
            Obstacle.Segment seg = obstacle.segment(i);
            p = p.plus(seg.vector());
            if (seg.isStraightLine()) {
                g.lineTo(p.x(), p.y());
            } else {
                if (seg.isRoundedNWCorner()) {
                    if (seg.ccw()) g.arc(p.x()+r, p.y(),   r, r,  90, 90);
                    else           g.arc(p.x(),   p.y()+r, r, r, 180, -90);
                }
                else if (seg.isRoundedSWCorner()) {
                    if (seg.ccw()) g.arc(p.x(),   p.y()-r, r, r, 180, 90);
                    else           g.arc(p.x()+r, p.y(),   r, r, 270, -90);
                }
                else if (seg.isRoundedSECorner()) {
                    if (seg.ccw()) g.arc(p.x()-r, p.y(),   r, r, 270, 90);
                    else           g.arc(p.x(),   p.y()-r, r, r, 0, -90);
                }
                else if (seg.isRoundedNECorner()) {
                    if (seg.ccw()) g.arc(p.x(),   p.y()+r, r, r, 0, 90);
                    else           g.arc(p.x()-r, p.y(),   r, r, 90, -90);
                }
                else if (seg.isAngularNWCorner()) {
                    if (seg.ccw()) g.lineTo(p.x(),   p.y()-r);
                    else           g.lineTo(p.x()-r, p.y());
                    g.lineTo(p.x(), p.y());
                }
                else if (seg.isAngularSWCorner()) {
                    if (seg.ccw()) g.lineTo(p.x()-r, p.y());
                    else           g.lineTo(p.x(), p.y()+r);
                    g.lineTo(p.x(), p.y());
                }
                else if (seg.isAngularSECorner()) {
                    if (seg.ccw()) g.lineTo(p.x(), p.y()+r);
                    else           g.lineTo(p.x()-r, p.y());
                    g.lineTo(p.x(), p.y());
                }
                else if (seg.isAngularNECorner()) {
                    if (seg.ccw()) g.lineTo(p.x()+r, p.y());
                    else           g.lineTo(p.x()-r, p.y()-r);
                    g.lineTo(p.x(), p.y());
                }
            }
        }
        if (fill) {
            g.closePath();
            g.setFill(wallFillColor);
            g.fill();
        }
        g.setLineWidth(lineWidth);
        g.setStroke(strokeColor);
        g.stroke();
    }

    @Override
    public void drawTile(GraphicsContext g, Vector2i tile, byte content) {
        // this renderer doesn't draw tiles individually but draws precomputed paths
    }

    private Vector2f center(Vector2i tile) {
        return tile.scaled(TS).plus((float)HTS, (float)HTS);
    }

    // assume we always have a pair of horizontally neighbored doors
    private void drawDoor(GraphicsContext g, TileMap map, Vector2i tile, double lineWidth, Color doorColor) {
        boolean leftDoor = map.get(tile.plus(Direction.RIGHT.vector())) == Tiles.DOOR;
        double height = doubleStrokeInnerWidth * 0.75; // TODO check this
        double x = tile.x() * TS, y = tile.y() * TS;
        double oy = y + 0.5 * (TS - height);
        if (leftDoor) {
            g.setFill(mapBackgroundColor);
            g.fillRect(x, y, 2 * TS, TS);
            g.setFill(wallStrokeColor);
            g.fillRect(x - lineWidth, oy, lineWidth, height);
            g.fillRect(x + 2 * TS, oy, lineWidth, height);
            g.setFill(doorColor);
            g.fillRect(x, y + 0.5 * (TS - height), 2 * TS, height);
        }
    }
}