/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui.fx.rendering2d;

import de.amr.games.pacman.lib.Vector2i;
import de.amr.games.pacman.model.world.TileMap;
import de.amr.games.pacman.model.world.Tiles;
import javafx.geometry.Point2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.shape.ArcType;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import static de.amr.games.pacman.lib.Direction.*;

/**
 * @author Armin Reichert
 */
public class TerrainMapRenderer implements TileMapRenderer {

    static final int TILE_SIZE = 8;

    private float scaling;

    public void setScaling(double scaling) {
        this.scaling = (float) scaling;
    }

    public float s(float times) {
        return scaling * times;
    }

    private Color wallFillColor = Color.BLACK;
    private Color wallStrokeColor = Color.GREEN;

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
        g.save();
        switch (content) {
            case Tiles.WALL_H -> drawWallH(g, tile);
            case Tiles.WALL_V -> drawWallV(g, tile);
            case Tiles.DWALL_H -> drawDWallH(g, tile);
            case Tiles.DWALL_V -> drawDWallV(g, tile);
            case Tiles.CORNER_NW, Tiles.CORNER_NE, Tiles.CORNER_SW, Tiles.CORNER_SE -> drawCorner(g, tile, content);
            case Tiles.DCORNER_NW, Tiles.DCORNER_NE, Tiles.DCORNER_SW, Tiles.DCORNER_SE ->
                drawDCorner(g, tile, content);
            case Tiles.DOOR -> drawDoor(g, tile, Color.PINK);
            case Tiles.TUNNEL -> drawTunnel(g, tile);
            default -> {}
        }
        g.restore();
    }

    public void drawTunnel(GraphicsContext g, Vector2i tile) {
        // overridden by design-time renderer
    }

    public void drawWallH(GraphicsContext g, Vector2i tile) {
        double x = tile.x() * s(8), y = tile.y() * s(8);
        g.setFill(wallStrokeColor);
        // add 1 pixel to avoid gaps
        g.fillRect(x, y + s(3.5f), s(8) + 1, s(1));
    }

    public void drawDWallH(GraphicsContext g, Vector2i tile) {
        double x = tile.x() * s(8), y = tile.y() * s(8);
        g.setFill(wallStrokeColor);
        // add 1 pixel to avoid gaps
        g.fillRect(x, y + s(2.5f), s(8) + 1, s(1));
        g.fillRect(x, y + s(4.5f), s(8) + 1, s(1));
    }

    public void drawWallV(GraphicsContext g, Vector2i tile) {
        double x = tile.x() * s(8), y = tile.y() * s(8);
        g.setFill(wallStrokeColor);
        // add 1 pixel to avoid gaps
        g.fillRect(x + s(3.5f), y, s(1), s(8) + 1);
    }

    public void drawDWallV(GraphicsContext g, Vector2i tile) {
        double x = tile.x() * s(8), y = tile.y() * s(8);
        g.setFill(wallStrokeColor);
        // add 1 pixel to avoid gaps
        g.fillRect(x + s(2.5f), y, s(1), s(8) + 1);
        g.fillRect(x + s(4.5f), y, s(1), s(8) + 1);
    }

    public void drawCorner(GraphicsContext g, Vector2i tile, byte cornerType) {
        double x = tile.x() * s(8), y = tile.y() * s(8);
        g.setStroke(wallStrokeColor);
        g.setLineWidth(s(1));
        switch (cornerType) {
            case Tiles.CORNER_NW -> g.strokeArc(x + s(4), y + s(4), s(8), s(8), 90, 90,  ArcType.OPEN);
            case Tiles.CORNER_NE -> g.strokeArc(x - s(4), y + s(4), s(8), s(8), 0, 90,   ArcType.OPEN);
            case Tiles.CORNER_SE -> g.strokeArc(x - s(4), y - s(4), s(8), s(8), 270, 90, ArcType.OPEN);
            case Tiles.CORNER_SW -> g.strokeArc(x + s(4), y - s(4), s(8), s(8), 180, 90, ArcType.OPEN);
            default -> {}
        }
    }

    public void drawDCorner(GraphicsContext g, Vector2i tile, byte cornerType) {
        double x = tile.x() * s(8), y = tile.y() * s(8);
        double s10 = 10 * scaling;
        g.setStroke(wallStrokeColor);
        g.setLineWidth(s(1));
        switch (cornerType) {
            case Tiles.DCORNER_NW -> {
                g.strokeArc(x + s(5), y + s(5), s(6), s(6), 90, 90, ArcType.OPEN);
                g.strokeArc(x + s(3), y + s(3), s10, s10, 90, 90, ArcType.OPEN);
            }
            case Tiles.DCORNER_NE -> {
                g.strokeArc(x - s(3), y + s(5), s(6), s(6), 0, 90, ArcType.OPEN);
                g.strokeArc(x - s(5), y + s(3), s10, s10, 0, 90, ArcType.OPEN);
            }
            case Tiles.DCORNER_SE -> {
                g.strokeArc(x - s(3), y - s(3), s(6), s(6), 270, 90, ArcType.OPEN);
                g.strokeArc(x - s(5), y - s(5), s10, s10, 270, 90, ArcType.OPEN);
            }
            case Tiles.DCORNER_SW -> {
                g.strokeArc(x + s(5), y - s(3), s(6), s(6), 180, 90, ArcType.OPEN);
                g.strokeArc(x + s(3), y - s(5), s10, s10, 180, 90, ArcType.OPEN);
            }
            default -> {}
        }
    }

    public void drawDoor(GraphicsContext g, Vector2i tile, Color color) {
        double x = tile.x() * s(TILE_SIZE), y = tile.y() * s(TILE_SIZE);
        g.setFill(Color.BLACK);
        g.fillRect(x, y + s(1), s(TILE_SIZE), s(6));
        g.setFill(color);
        g.fillRect(x - 1, y + s(3), s(TILE_SIZE) + 2, s(2));
    }

    private void drawSingleStrokePaths(GraphicsContext g, TileMap map) {
        var explored = new HashSet<Vector2i>();
        map.tiles()
            .filter(tile -> !explored.contains(tile))
            .filter(tile -> map.get(tile) == Tiles.CORNER_NW)
            .map(corner -> map.buildPath(explored, corner, DOWN))
            .forEach(path -> drawPath(g, map, path, true, true, 1*scaling, wallStrokeColor, wallFillColor));
    }

    /*
     * Draws a path with an inside stroke of wall fill color and two outside strokes of wall stroke color.
     */
    private void drawTripleStrokePath(GraphicsContext g, TileMap map, List<Vector2i> path) {
        drawPath(g, map, path, false, false, 3*scaling, wallStrokeColor, null);
        drawPath(g, map, path, false, false, 1*scaling, wallFillColor, null);
    }

    private void drawTripleStrokePaths(GraphicsContext g, TileMap map) {
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
            .map(handle -> map.buildPath(explored, handle, map.newMoveDir(RIGHT, map.get(handle))))
            .forEach(path -> drawTripleStrokePath(g, map, path));

        handlesRight.stream()
            .filter(handle -> !explored.contains(handle))
            .map(handle -> map.buildPath(explored, handle, map.newMoveDir(LEFT, map.get(handle))))
            .forEach(path -> drawTripleStrokePath(g, map, path));

        // ghost house
        map.tiles()
            .filter(tile -> !explored.contains(tile))
            .filter(tile -> map.get(tile) == Tiles.DCORNER_NW)
            .map(corner -> map.buildPath(explored, corner, DOWN))
            .forEach(path -> drawTripleStrokePath(g, map, path));
    }

    private Point2D center(Vector2i tile) {
        return new Point2D(tile.x() * s(TILE_SIZE) + s(4), tile.y() * s(TILE_SIZE) + s(4));
    }

    private void drawPath(GraphicsContext g, TileMap map, List<Vector2i> path,
                          boolean fill, boolean close, double lineWidth, Color outlineColor, Color fillColor) {
        if (close) {
            path.add(path.getFirst()); // close the path
        }
        double r = s(TILE_SIZE / 2f);
        g.beginPath();
        for (int i = 0; i < path.size(); ++i) {
            Vector2i tile = path.get(i), prevTile = (i == 0) ? null : path.get(i - 1);
            Point2D p = center(tile);
            double x = p.getX(), y = p.getY();

            //TODO how to avoid these?
            if (i == 0 && tile.x() == 0 && map.get(tile) == Tiles.DWALL_H) {
                g.moveTo(0, y);
                g.lineTo(r, y);
            }
            if (i == path.size() - 1 && tile.x() == 0 && map.get(tile) == Tiles.DWALL_H) {
                g.lineTo(0, y);
            }

            switch (map.get(tile)) {
                case Tiles.WALL_H, Tiles.DWALL_H -> g.lineTo(x + r, y);
                case Tiles.WALL_V, Tiles.DWALL_V -> g.lineTo(x, y + r);
                case Tiles.CORNER_NW, Tiles.DCORNER_NW -> {
                    if (prevTile == null || prevTile.x() > tile.x()) {
                        g.arc(x + r, y + r, r, r, 90, 90);
                    } else {
                        g.arc(x + r, y + r, r, r, 180, -90);
                    }
                }
                case Tiles.CORNER_SW, Tiles.DCORNER_SW -> {
                    if (prevTile == null || prevTile.y() < tile.y()) {
                        g.arc(x + r, y - r, r, r, 180, 90);
                    } else {
                        g.arc(x + r, y - r, r, r, 270, -90);
                    }
                }
                case Tiles.CORNER_NE, Tiles.DCORNER_NE -> {
                    if (prevTile == null || prevTile.y() > tile.y()) {
                        g.arc(x - r, y + r, r, r, 0, 90);
                    } else {
                        g.arc(x - r, y + r, r, r, 90, -90);
                    }
                }
                case Tiles.CORNER_SE, Tiles.DCORNER_SE -> {
                    if (prevTile == null || prevTile.y() < tile.y()) {
                        g.arc(x - r, y - r, r, r, 0, -90);
                    } else {
                        g.arc(x - r, y - r, r, r, 270, 90);
                    }
                }
                default -> {}
            }
        }
        if (close) {
            g.closePath();
        }
        g.setFill(fillColor);
        g.setStroke(outlineColor);
        g.setLineWidth(lineWidth);
        if (fill) {
            g.fill();
        }
        g.stroke();
    }
}