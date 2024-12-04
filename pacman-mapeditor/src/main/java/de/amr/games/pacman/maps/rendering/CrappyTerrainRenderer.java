/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.maps.rendering;

import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.Vector2f;
import de.amr.games.pacman.lib.Vector2i;
import de.amr.games.pacman.lib.tilemap.*;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import static de.amr.games.pacman.lib.Globals.HTS;
import static de.amr.games.pacman.lib.Globals.TS;

/**
 * Vector renderer for terrain tile maps.
 * TODO: needs total rewrite
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
        doubleStrokeOuterWidth = 5;
        doubleStrokeInnerWidth = 3;
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
    public void drawTerrain(GraphicsContext g, TileMap map, TerrainData terrainData) {
        double baseLineWidth = adaptLineWidthToCanvasSize(g.getCanvas().getHeight());
        g.save();
        g.scale(scaling(), scaling());

        terrainData.doubleStrokePaths().forEach(path -> {
            drawPath(g, map, path, false,  doubleStrokeOuterWidth * baseLineWidth, wallStrokeColor, null);
            drawPath(g, map, path, false,  doubleStrokeInnerWidth * baseLineWidth, wallFillColor, null);
        });
        terrainData.fillerPaths().forEach(
            path -> drawPath(g, map, path, true, singleStrokeWidth * baseLineWidth, wallFillColor, wallFillColor)
        );
        map.tiles(Tiles.DOOR).forEach(door -> drawDoor(g, map, door, singleStrokeWidth * baseLineWidth, doorColor));
        uglyConcavityHack(g, terrainData, baseLineWidth);

        // new rendering
        terrainData.obstacles().forEach(obstacle -> drawObstacle(g, obstacle, true, singleStrokeWidth * baseLineWidth));

        g.restore();
    }

    private void drawObstacle(GraphicsContext g, Obstacle obstacle, boolean fill, double lineWidth) {
        int r = HTS;
        g.beginPath();
        g.moveTo(obstacle.startPoint().x(), obstacle.startPoint().y());
        Vector2f p = obstacle.startPoint();
        for (int i = 0; i < obstacle.segments().size(); ++i) {
            Vector2f seg = obstacle.segment(i);
            byte content = obstacle.content(i);
            boolean counterClockwise = obstacle.orientation(i) == Direction.LEFT;
            p = p.plus(seg);
            if (seg.x() == 0 || seg.y() == 0) {
                g.lineTo(p.x(), p.y());
            } else {
                if (content == Tiles.CORNER_NW) { // NW corner
                    if (counterClockwise) g.arc(p.x()+r, p.y(), r, r, 90, 90);
                    else                  g.arc(p.x(), p.y()+r, r, r, 180, -90);
                }
                else if (content == Tiles.CORNER_SW) { // SW corner
                    if (counterClockwise) g.arc(p.x(),p.y()-r, r, r, 180, 90);
                    else                  g.arc(p.x()+r, p.y(), r, r, 270, -90);
                }
                else if (content == Tiles.CORNER_SE) { // SE corner
                    if (counterClockwise) g.arc(p.x()-r, p.y(), r, r, 270, 90);
                    else                  g.arc(p.x(), p.y()-r, r, r, 0, -90);
                }
                else if (content == Tiles.CORNER_NE) { // NE corner
                    if (counterClockwise) g.arc(p.x(), p.y()+r, r, r, 0, 90);
                    else                  g.arc(p.x()-r, p.y(), r, r, 90, -90);
                }
                g.lineTo(p.x(), p.y());
            }
        }
        if (obstacle.isClosed()) {
            g.closePath();
        }
        if (fill) {
            g.setFill(wallFillColor);
            g.fill();
        }
        g.setLineWidth(lineWidth);
        g.setStroke(wallStrokeColor);
        g.stroke();
    }






    // Fix concavities drawing (ugly hack)
    private void uglyConcavityHack(GraphicsContext g, TerrainData terrainData, double baseLineWidth) {
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
            g.setLineWidth(baseLineWidth * singleStrokeWidth);
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
            g.setLineWidth(baseLineWidth);
            g.strokeLine(left, y, left + w, y);
        });
    }

    @Override
    public void drawTile(GraphicsContext g, Vector2i tile, byte content) {
        // this renderer doesn't draw tiles individually but computes contour paths and draws these
    }

    private double adaptLineWidthToCanvasSize(double canvasHeight) {
        // increase line width for small display
        if (canvasHeight <  36 * TS * 1.5) {
            return 1.25;
        }
        if (canvasHeight < 36 * TS * 2.5) {
            return 1;
        }
        return 0.75;
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

    private void drawPath(GraphicsContext g, TileMap map, TileMapPath path,
        boolean fill, double lineWidth, Color strokeColor, Color fillColor)
    {
        g.beginPath();
        buildPath(g, map, path);
        if (fill) {
            g.setFill(fillColor);
            g.fill();
        }
        g.setLineWidth(lineWidth);
        g.setStroke(strokeColor);
        g.stroke();
    }

    //TODO: Needs to be cleaned up or even reimplemented. Maybe we should represent a path by points instead of directions?
    private void buildPath(GraphicsContext g, TileMap map, TileMapPath path) {
        Vector2i tile = path.startTile();
        if (tile.x() == 0) {
            int cx = HTS, cy = tile.y() * TS + HTS;
            if (map.get(tile) == Tiles.DWALL_V) {
                g.moveTo(cx, cy);
            } else {
                // start path at left border, not at tile center
                g.moveTo(0, cy);
                if (map.get(tile) == Tiles.DWALL_H) {
                    g.lineTo(cx, cy);
                }
            }
        }
        //TODO this is unclear
        extendPath(g, map.get(tile), center(tile), true, true, tile.x() != 0, true);
        for (Vector2i vector : path) {
            tile = tile.plus(vector);
            extendPath(g, map.get(tile), center(tile),
               vector.x() < 0 && vector.y() == 0,
               vector.x() > 0 && vector.y() == 0,
               vector.x() == 0 && vector.y() < 0,
               vector.x() == 0 && vector.y() > 0);
        }
        if (tile.x() == 0 && map.get(tile) == Tiles.DWALL_H) {
            // end path at left border
            g.lineTo(0, tile.y() * TS + HTS);
        }
    }

    //TODO clean up and simplify this mess
    private void extendPath(GraphicsContext g, byte tileValue, Vector2f center, boolean left, boolean right, boolean up, boolean down) {
        float r = HTS;
        float cx = center.x(), cy = center.y();
        switch (tileValue) {
            case Tiles.WALL_H,
                 Tiles.DWALL_H    -> g.lineTo(cx + r, cy);
            case Tiles.WALL_V,
                 Tiles.DWALL_V    -> g.lineTo(cx, cy + r);
            //TODO: Should we use arcTo() instead?
            case Tiles.CORNER_NW,
                 Tiles.DCORNER_NW -> g.arc(cx + r, cy + r, r, r, left?   90:180, left?  90:-90);
            case Tiles.CORNER_SW,
                 Tiles.DCORNER_SW -> g.arc(cx + r, cy - r, r, r, down?  180:270, down?  90:-90);
            case Tiles.CORNER_NE,
                 Tiles.DCORNER_NE -> g.arc(cx - r, cy + r, r, r, up?      0: 90, up?    90:-90);
            case Tiles.CORNER_SE,
                 Tiles.DCORNER_SE -> g.arc(cx - r, cy - r, r, r, right? 270:  0, right? 90:-90);
            case Tiles.DCORNER_ANGULAR_NW -> {
                g.lineTo(cx, cy);
                if (left) {
                    g.lineTo(cx, cy + r);
                } else if (up) {
                    g.lineTo(cx + r, cy);
                }
            }
            case Tiles.DCORNER_ANGULAR_SW -> {
                g.lineTo(cx, cy);
                if (left) {
                    g.lineTo(cx, cy);
                } else if (down) {
                    g.lineTo(cx + r, cy);
                }
            }
            case Tiles.DCORNER_ANGULAR_NE -> {
                g.lineTo(cx, cy);
                if (right) {
                    g.lineTo(cx, cy + r);
                } else if (up) {
                    g.lineTo(cx - r, cy);
                }
            }
            case Tiles.DCORNER_ANGULAR_SE -> {
                g.lineTo(cx, cy);
                if (right) {
                    g.lineTo(cx, cy - r);
                } else if (down) {
                    g.lineTo(cx - r, cy);
                }
            }
            default -> {}
        }
    }
}