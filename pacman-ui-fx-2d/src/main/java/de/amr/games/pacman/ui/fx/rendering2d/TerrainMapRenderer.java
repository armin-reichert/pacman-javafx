/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui.fx.rendering2d;

import de.amr.games.pacman.lib.Vector2i;
import de.amr.games.pacman.model.world.TileMap;
import de.amr.games.pacman.model.world.TileMapPath;
import de.amr.games.pacman.model.world.Tiles;
import javafx.geometry.Point2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import static de.amr.games.pacman.lib.Direction.LEFT;
import static de.amr.games.pacman.lib.Direction.RIGHT;

/**
 * @author Armin Reichert
 */
public class TerrainMapRenderer implements TileMapRenderer {

    public static final int TILE_SIZE = 8;

    protected float scaling;
    protected Color wallFillColor = Color.BLACK;
    protected Color wallStrokeColor = Color.GREEN;

    public void setScaling(double scaling) {
        this.scaling = (float) scaling;
    }

    public float s(float times) {
        return scaling * times;
    }

    public void setWallStrokeColor(Color color) {
        wallStrokeColor = color;
    }

    public void setWallFillColor(Color wallFillColor) {
        this.wallFillColor = wallFillColor;
    }

    public void drawMap(GraphicsContext g, TileMap map) {
        drawTripleStrokePaths(g, map);
        drawSingleStrokePaths(g, map);
        Color doorColor = TileMapRenderer.getTileMapColor(map, "door_color", Color.PINK);
        map.tiles().filter(tile -> map.get(tile) == Tiles.DOOR).forEach(tile -> drawDoor(g, tile, doorColor));
    }

    public void drawTile(GraphicsContext g, Vector2i tile, byte content) {
        if (content == Tiles.DOOR) {
            drawDoor(g, tile, Color.PINK);
        }
    }

    public void drawDoor(GraphicsContext g, Vector2i tile, Color color) {
        g.save();
        double x = tile.x() * s(TILE_SIZE), y = tile.y() * s(TILE_SIZE);
        g.setFill(Color.BLACK);
        g.fillRect(x, y + s(1), s(TILE_SIZE), s(6));
        g.setFill(color);
        g.fillRect(x - 1, y + s(3), s(TILE_SIZE) + 2, s(2));
        g.restore();
    }

    public void drawSingleStrokePaths(GraphicsContext g, TileMap map) {
        var explored = new HashSet<Vector2i>();
        map.tiles()
            .filter(tile -> !explored.contains(tile))
            .filter(tile -> map.get(tile) == Tiles.CORNER_NW)
            .map(corner -> TileMapPath.buildPath(map, explored, corner, LEFT))
            .forEach(path -> drawPath(g, map, path, true, 1*scaling, wallStrokeColor, wallFillColor));
    }

    /*
     * Draws a path with an inside stroke of wall fill color and two outside strokes of wall stroke color.
     */
    public void drawTripleStrokePath(GraphicsContext g, TileMap map, TileMapPath path) {
        drawPath(g, map, path, false,  3*scaling, wallStrokeColor, null);
        drawPath(g, map, path, false,  1*scaling, wallFillColor, null);
    }

    public void drawTripleStrokePaths(GraphicsContext g, TileMap map) {
        var explored = new HashSet<Vector2i>();

        // Paths starting at left and right maze border (over and under tunnel ends)
        var handlesLeft = new ArrayList<Vector2i>();
        var handlesRight = new ArrayList<Vector2i>();
        for (int row = 0; row < map.numRows(); ++row) {
            if (map.get(row, 0) == Tiles.TUNNEL) {
                handlesLeft.add(new Vector2i(0, row - 1));
                handlesLeft.add(new Vector2i(0, row + 1));
            }
            if (map.get(row, map.numCols() - 1) == Tiles.TUNNEL) {
                handlesRight.add(new Vector2i(map.numCols() - 1, row - 1));
                handlesRight.add(new Vector2i(map.numCols() - 1, row + 1));
            }
        }

        handlesLeft.stream()
            .filter(handle -> !explored.contains(handle))
            .map(handle -> TileMapPath.buildPath(map, explored, handle, RIGHT))
            .forEach(path -> drawTripleStrokePath(g, map, path));

        handlesRight.stream()
            .filter(handle -> !explored.contains(handle))
            .map(handle -> TileMapPath.buildPath(map, explored, handle, LEFT))
            .forEach(path -> drawTripleStrokePath(g, map, path));

        // ghost house
        map.tiles()
            .filter(tile -> !explored.contains(tile))
            .filter(tile -> map.get(tile) == Tiles.DCORNER_NW)
            .map(corner -> TileMapPath.buildPath(map, explored, corner, LEFT))
            .forEach(path -> drawTripleStrokePath(g, map, path));
    }

    public Point2D center(Vector2i tile) {
        return new Point2D(tile.x() * s(TILE_SIZE) + s(4), tile.y() * s(TILE_SIZE) + s(4));
    }

    public void drawPath(GraphicsContext g, TileMap map, TileMapPath tileMapPath,
                          boolean fill, double lineWidth, Color outlineColor, Color fillColor) {

        //TODO use new path data structure
        List<Vector2i> path = tileMapPath.toTileList();

        double r = s(4);

        g.beginPath();
        for (int i = 0; i < path.size(); ++i) {
            Vector2i tile = path.get(i), prevTile = (i == 0) ? null : path.get(i - 1);
            Point2D p = center(tile);
            double x = p.getX(), y = p.getY();

            //TODO avoid these special cases
            if (i == 0 && tile.x() == 0) {
                // path starts at left maze border
                if (map.get(tile) == Tiles.DWALL_H) {
                    // start at left maze border, not at tile center
                    g.moveTo(0, y);
                    g.lineTo(r, y);
                } else if (map.get(tile) == Tiles.DCORNER_NE) {
                    // invent predecessor such that path continues clockwise/down
                    prevTile = new Vector2i(tile.x(), tile.y() - 1);
                } else if (map.get(tile) == Tiles.DCORNER_SE) {
                    // invent predecessor such that path continues clockwise/down
                    prevTile = new Vector2i(tile.x(), tile.y() + 1);
                }
            } else if (i == path.size() - 1 && tile.x() == 0) {
                if (map.get(tile) == Tiles.DWALL_H) {
                    // end at left maze border, not at tile center
                    g.lineTo(0, y);
                }
            }
            switch (map.get(tile)) {
                case Tiles.WALL_H, Tiles.DWALL_H       -> g.lineTo(x + r, y);
                case Tiles.WALL_V, Tiles.DWALL_V       -> g.lineTo(x, y + r);
                case Tiles.CORNER_NW, Tiles.DCORNER_NW -> arc(g, x+r, y+r, r,  90, 180, prevTile == null || prevTile.x() > tile.x());
                case Tiles.CORNER_SW, Tiles.DCORNER_SW -> arc(g, x+r, y-r, r, 180, 270, prevTile == null || prevTile.y() < tile.y());
                case Tiles.CORNER_NE, Tiles.DCORNER_NE -> arc(g, x-r, y+r, r,   0,  90, prevTile == null || prevTile.y() > tile.y());
                case Tiles.CORNER_SE, Tiles.DCORNER_SE -> arc(g, x-r, y-r, r, 270,   0, prevTile == null || prevTile.x() < tile.x());
                default -> {}
            }
        }
        if (fill) {
            g.setFill(fillColor);
            g.fill();
        }
        g.setStroke(outlineColor);
        g.setLineWidth(lineWidth);
        g.stroke();
    }

    private void arc(GraphicsContext g, double x, double y, double r, int degreesCCW, int degreesCW, boolean ccw) {
        g.arc(x, y, r, r, ccw ? degreesCCW : degreesCW, ccw ? 90 : -90);
    }
}