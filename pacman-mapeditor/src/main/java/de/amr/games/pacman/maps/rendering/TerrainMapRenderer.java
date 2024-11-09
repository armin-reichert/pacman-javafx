/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.maps.rendering;

import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.Vector2f;
import de.amr.games.pacman.lib.Vector2i;
import de.amr.games.pacman.lib.tilemap.TerrainData;
import de.amr.games.pacman.lib.tilemap.TileMap;
import de.amr.games.pacman.lib.tilemap.TileMapPath;
import de.amr.games.pacman.lib.tilemap.Tiles;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import static de.amr.games.pacman.maps.editor.TileMapUtil.HALF_TILE_SIZE;
import static de.amr.games.pacman.maps.editor.TileMapUtil.TILE_SIZE;

/**
 * @author Armin Reichert
 */
public class TerrainMapRenderer implements TileMapRenderer {

    static final double DOUBLE_STROKE_OUTER_WIDTH = 5;
    static final double DOUBLE_STROKE_INNER_WIDTH = 3;
    static final double SINGLE_STROKE_WIDTH = 1.0;

    public final DoubleProperty scalingPy = new SimpleDoubleProperty(this, "scaling", 1.0);

    private Color mapBackgroundColor = Color.BLACK;
    private Color wallFillColor = Color.BLACK;
    private Color wallStrokeColor = Color.GREEN;
    private Color doorColor = Color.PINK;

    @Override
    public void drawMap(GraphicsContext g, TileMap map) {
        map.terrainData().ifPresent(terrainData -> {
            double baseLineWidth = adaptLineWidthToCanvasSize(g.getCanvas().getHeight());
            g.save();
            g.scale(scaling(), scaling());
            terrainData.doubleStrokePaths().forEach(path -> {
                drawPath(g, map, path, false,  DOUBLE_STROKE_OUTER_WIDTH * baseLineWidth, wallStrokeColor, null);
                drawPath(g, map, path, false,  DOUBLE_STROKE_INNER_WIDTH * baseLineWidth, wallFillColor, null);
            });
            terrainData.fillerPaths().forEach(
                path -> drawPath(g, map, path, true, SINGLE_STROKE_WIDTH * baseLineWidth, wallFillColor, wallFillColor)
            );
            terrainData.singleStrokePaths().forEach(
                path -> drawPath(g, map, path, true, SINGLE_STROKE_WIDTH * baseLineWidth, wallStrokeColor, wallFillColor)
            );
            map.tiles(Tiles.DOOR).forEach(door -> drawDoor(g, map, door, SINGLE_STROKE_WIDTH * baseLineWidth, doorColor));
            uglyConcavityHack(g, terrainData, baseLineWidth);
            g.restore();
        });
    }

    // Fix concavities drawing (ugly hack)
    private void uglyConcavityHack(GraphicsContext g, TerrainData terrainData, double baseLineWidth) {
        Color fillColor = wallFillColor;
        terrainData.topConcavityEntries().forEach(entry -> {
            double left = entry.x() * TILE_SIZE;
            double y = entry.y() * TILE_SIZE + 0.5 * (TILE_SIZE - DOUBLE_STROKE_OUTER_WIDTH) + SINGLE_STROKE_WIDTH;
            //TODO this is a hack:
            if (scaling() < 2.8) {
                y -= 0.5;
            } else if (scaling() < 1.5) {
                y -= 1.25;
            }
            double w = 2 * TILE_SIZE;
            double h = 0.5 * DOUBLE_STROKE_OUTER_WIDTH; // TODO check this
            g.setFill(wallFillColor);
            g.fillRect(left, y, w, h);
            g.setStroke(wallStrokeColor);
            g.setLineWidth(baseLineWidth * SINGLE_STROKE_WIDTH);
            g.strokeLine(left, y, left + w, y);
        });

        terrainData.bottomConcavityEntries().forEach(entry -> {
            double left = entry.x() * TILE_SIZE;
            double y = (entry.y() * TILE_SIZE + HALF_TILE_SIZE + 0.5 * DOUBLE_STROKE_INNER_WIDTH); // TODO check this
            double w = 2 * TILE_SIZE, h = DOUBLE_STROKE_INNER_WIDTH * 0.75;
            g.setFill(fillColor);
            g.fillRect(left, y - h, w, h);
            g.fillRect(left + HALF_TILE_SIZE, entry.y() * TILE_SIZE - HALF_TILE_SIZE, TILE_SIZE, TILE_SIZE);
            g.setStroke(wallStrokeColor);
            g.setLineWidth(baseLineWidth);
            g.strokeLine(left, y, left + w, y);
        });
    }

    @Override
    public void drawTile(GraphicsContext g, Vector2i tile, byte content) {
        // this renderer doesn't draw tiles individually but computes contour paths and draws these
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

    private double adaptLineWidthToCanvasSize(double canvasHeight) {
        // increase line width for small display
        if (canvasHeight <  36 * TILE_SIZE * 1.5) {
            return 1.25;
        }
        if (canvasHeight < 36 * TILE_SIZE * 2.5) {
            return 1;
        }
        return 0.75;
    }

    private Vector2f center(Vector2i tile) {
        return tile.scaled(TILE_SIZE).plus((float)HALF_TILE_SIZE, (float)HALF_TILE_SIZE);
    }

    // assume we always have a pair of horizontally neighbored doors
    private void drawDoor(GraphicsContext g, TileMap map, Vector2i tile, double lineWidth, Color doorColor) {
        boolean leftDoor = map.get(tile.plus(Direction.RIGHT.vector())) == Tiles.DOOR;
        double height = DOUBLE_STROKE_INNER_WIDTH * 0.75; // TODO check this
        double x = tile.x() * TILE_SIZE, y = tile.y() * TILE_SIZE;
        double oy = y + 0.5 * (TILE_SIZE - height);
        if (leftDoor) {
            g.setFill(mapBackgroundColor);
            g.fillRect(x, y, 2 * TILE_SIZE, TILE_SIZE);
            g.setFill(wallStrokeColor);
            g.fillRect(x - lineWidth, oy, lineWidth, height);
            g.fillRect(x + 2 * TILE_SIZE, oy, lineWidth, height);
            g.setFill(doorColor);
            g.fillRect(x, y + 0.5 * (TILE_SIZE - height), 2 * TILE_SIZE, height);
        }
    }

    private void drawSSPath(GraphicsContext g, TileMap map, TileMapPath path,
        boolean fill, double lineWidth, Color strokeColor, Color fillColor)
    {
        g.beginPath();
        drawSSPath(g, map, path);
        if (fill) {
            g.setFill(fillColor);
            g.fill();
        }
        g.setLineWidth(lineWidth);
        g.setStroke(strokeColor);
        g.stroke();
    }

    private void drawSSPath(GraphicsContext g, TileMap map, TileMapPath path) {
        Vector2i tile = path.startTile();
        Vector2f point = tile.scaled(8f).plus(8,6);
        g.moveTo(point.x(), point.y());
        int orientation = 1; // counter clockwise
        for (Direction dir : path) {
            byte content = map.get(tile);
            point = switch (content) {
                case Tiles.CORNER_NW -> point.plus(-2, 2).scaled(orientation);
                case Tiles.CORNER_SW -> point.plus(2, 2).scaled(orientation);
                case Tiles.CORNER_SE -> point.plus(2, -2).scaled(orientation);
                case Tiles.CORNER_NE -> point.plus(-2, -2).scaled(orientation);
                default -> point.plus(dir.vector().scaled(8f));
            };
            g.lineTo(point.x(), point.y());
            tile = tile.plus(dir.vector());
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
            int cx = HALF_TILE_SIZE, cy = tile.y() * TILE_SIZE + HALF_TILE_SIZE;
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
        for (Direction dir : path) {
            tile = tile.plus(dir.vector());
            extendPath(g, map.get(tile), center(tile), dir == Direction.LEFT, dir == Direction.RIGHT, dir == Direction.UP, dir == Direction.DOWN);
        }
        if (tile.x() == 0 && map.get(tile) == Tiles.DWALL_H) {
            // end path at left border
            g.lineTo(0, tile.y() * TILE_SIZE + HALF_TILE_SIZE);
        }
    }

    private void extendPath(GraphicsContext g, byte tileValue, Vector2f center, boolean left, boolean right, boolean up, boolean down) {
        float r = HALF_TILE_SIZE;
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