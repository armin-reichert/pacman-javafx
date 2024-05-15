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
    static double STROKE_WIDTH = 2.0;

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

    public Color getWallFillColor() {
        return wallFillColor;
    }

    public Color getWallStrokeColor() {
        return wallStrokeColor;
    }

    public void drawMap(GraphicsContext g, TileMap map) {
//        map.tiles()
//            .filter(tile -> isDouble(map.get(tile)) || map.get(tile) == Tiles.DOOR)
//            .forEach(tile -> drawTile(g, tile, map.get(tile)));

        drawOutlinePaths(g, map);
        drawPathsInsideMap(g, map);
    }

    public void drawTile(GraphicsContext g, Vector2i tile, byte content) {
        g.save();
        switch (content) {
            case Tiles.DWALL_H -> drawDWallH(g, tile);
            case Tiles.DWALL_V -> drawDWallV(g, tile);
            case Tiles.DCORNER_NW, Tiles.DCORNER_NE, Tiles.DCORNER_SW, Tiles.DCORNER_SE -> drawDCorner(g, tile, content);
            case Tiles.DOOR -> drawDoor(g, tile, Color.PINK);
            case Tiles.TUNNEL -> drawTunnel(g, tile);
            default -> {}
        }
        g.restore();
    }

    public void drawTunnel(GraphicsContext g, Vector2i tile) {
        // overridden by design-time renderer
    }

    public void drawDWallH(GraphicsContext g, Vector2i tile) {
        double x = tile.x() * s(TILE_SIZE), y = tile.y() * s(TILE_SIZE);
        g.setFill(wallStrokeColor);
        // add 1 pixel to avoid gaps
        g.fillRect(x, y + s(2.5f), s(TILE_SIZE) + 1, s(1));
        g.fillRect(x, y + s(4.5f), s(TILE_SIZE) + 1, s(1));
    }

    public void drawDWallV(GraphicsContext g, Vector2i tile) {
        double x = tile.x() * s(TILE_SIZE), y = tile.y() * s(TILE_SIZE);
        g.setFill(wallStrokeColor);
        // add 1 pixel to avoid gaps
        g.fillRect(x + s(2.5f), y, s(1), s(TILE_SIZE) + 1);
        g.fillRect(x + s(4.5f), y, s(1), s(TILE_SIZE) + 1);
    }

    public void drawDCorner(GraphicsContext g, Vector2i tile, byte cornerType) {
        double x = tile.x() * s(TILE_SIZE), y = tile.y() * s(TILE_SIZE);
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
        g.setFill(color);
        g.fillRect(x, y + s(3.5f), s(TILE_SIZE), s(1));
    }

    private boolean isDouble(byte tileContent) {
        return tileContent == Tiles.DWALL_H || tileContent == Tiles.DWALL_V
            || tileContent == Tiles.DCORNER_NE || tileContent == Tiles.DCORNER_SE || tileContent == Tiles.DCORNER_NW || tileContent == Tiles.DCORNER_SW;
    }

    private void drawPathsInsideMap(GraphicsContext g, TileMap map) {
        var explored = new HashSet<Vector2i>();
        map.tiles()
            .filter(tile -> !explored.contains(tile))
            .filter(tile -> map.get(tile) == Tiles.CORNER_NW)
            .map(corner -> map.buildPath(explored, corner, DOWN))
            .forEach(path -> drawPath(g, map, path, true, true));
    }

    private void drawOutlinePaths(GraphicsContext g, TileMap map) {
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
            .forEach(path -> drawPath(g, map, path, false, false));

        handlesRight.stream()
            .filter(handle -> !explored.contains(handle))
            .map(handle -> map.buildPath(explored, handle, map.newMoveDir(LEFT, map.get(handle))))
            .forEach(path -> drawPath(g, map, path, false, false));
    }

    private Point2D center(Vector2i tile) {
        return new Point2D(tile.x() * s(TILE_SIZE) + s(4), tile.y() * s(TILE_SIZE) + s(4));
    }

    private void drawPath(GraphicsContext g, TileMap map, List<Vector2i> path, boolean fill, boolean close) {
        if (close) {
            path.add(path.getFirst()); // close the path
        }
        double r = s(TILE_SIZE / 2f);
        g.setFill(wallFillColor);
        g.setStroke(wallStrokeColor);
        g.setLineWidth(STROKE_WIDTH);
        g.beginPath();
        for (int i = 0; i < path.size(); ++i) {
            Vector2i tile = path.get(i);
            Point2D p = center(tile);
            double x = p.getX(), y = p.getY();
            Vector2i prevTile = i == 0 ? null : path.get(i - 1);

            switch (map.get(tile)) {
                case Tiles.WALL_H -> g.lineTo(x + r, y);
                case Tiles.WALL_V -> g.lineTo(x, y + r);
                case Tiles.CORNER_NW -> {
                    if (prevTile == null || prevTile.x() > tile.x()) {
                        g.arc(x + r, y + r, r, r, 90, 90);
                    } else {
                        g.arc(x + r, y + r, r, r, 180, -90);
                    }
                }
                case Tiles.CORNER_SW -> {
                    if (prevTile == null || prevTile.y() < tile.y()) {
                        g.arc(x + r, y - r, r, r, 180, 90);
                    } else {
                        g.arc(x + r, y - r, r, r, 270, -90);
                    }
                }
                case Tiles.CORNER_NE -> {
                    if (prevTile == null || prevTile.y() > tile.y()) {
                        g.arc(x - r, y + r, r, r, 0, 90);
                    } else {
                        g.arc(x - r, y + r, r, r, 90, -90);
                    }
                }
                case Tiles.CORNER_SE -> {
                    if (prevTile == null || prevTile.y() < tile.y()) {
                        g.arc(x - r, y - r, r, r, 0, -90);
                    } else {
                        g.arc(x - r, y - r, r, r, 270, 90);
                    }
                }

                case Tiles.DWALL_H -> g.lineTo(x + r, y);
                case Tiles.DWALL_V -> g.lineTo(x, y + r);
                case Tiles.DCORNER_NW -> {
                    if (prevTile == null || prevTile.x() > tile.x()) {
                        g.arc(x + r, y + r, r, r, 90, 90);
                    } else {
                        g.arc(x + r, y + r, r, r, 180, -90);
                    }
                }
                case Tiles.DCORNER_SW -> {
                    if (prevTile == null || prevTile.y() < tile.y()) {
                        g.arc(x + r, y - r, r, r, 180, 90);
                    } else {
                        g.arc(x + r, y - r, r, r, 270, -90);
                    }
                }
                case Tiles.DCORNER_NE -> {
                    if (prevTile == null || prevTile.y() > tile.y()) {
                        g.arc(x - r, y + r, r, r, 0, 90);
                    } else {
                        g.arc(x - r, y + r, r, r, 90, -90);
                    }
                }
                case Tiles.DCORNER_SE -> {
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
        if (fill) {
            g.fill();
        }
        g.stroke();
    }


}