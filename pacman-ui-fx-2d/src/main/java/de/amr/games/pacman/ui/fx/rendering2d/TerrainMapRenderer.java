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

    private final double strokeWidth = 2.5;
    private Color wallFillColor = Color.BLACK;
    private Color wallStrokeColor = Color.GREEN;

    public void setWallStrokeColor(Color color) {
        wallStrokeColor = color;
    }

    public void setWallFillColor(Color wallFillColor) {
        this.wallFillColor = wallFillColor;
    }

    public void drawMap(GraphicsContext g, TileMap map) {
        drawOutlinePaths(g, map);
        drawPathsInsideMap(g, map);
        Color doorColor = TileMapRenderer.getTileMapColor(map, "door_color", Color.PINK);
        map.tiles().filter(tile -> map.get(tile) == Tiles.DOOR).forEach(tile -> drawDoor(g, tile, doorColor));
    }

    public void drawTile(GraphicsContext g, Vector2i tile, byte content) {
        g.save();
        switch (content) {
            case Tiles.DOOR -> drawDoor(g, tile, Color.PINK);
            case Tiles.TUNNEL -> drawTunnel(g, tile);
            default -> {}
        }
        g.restore();
    }

    public void drawTunnel(GraphicsContext g, Vector2i tile) {
        // overridden by design-time renderer
    }

    public void drawDoor(GraphicsContext g, Vector2i tile, Color color) {
        double x = tile.x() * s(TILE_SIZE), y = tile.y() * s(TILE_SIZE);
        g.setFill(Color.BLACK);
        g.fillRect(x, y + s(1), s(TILE_SIZE), s(6));
        g.setFill(color);
        g.fillRect(x - 1, y + s(3.0f), s(TILE_SIZE) + 2, s(2));
    }

    private void drawPathsInsideMap(GraphicsContext g, TileMap map) {
        var explored = new HashSet<Vector2i>();
        map.tiles()
            .filter(tile -> !explored.contains(tile))
            .filter(tile -> map.get(tile) == Tiles.CORNER_NW)
            .map(corner -> map.buildPath(explored, corner, DOWN))
            .forEach(path -> drawPath(g, map, path, true, true, strokeWidth, wallStrokeColor));
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
            .forEach(path -> {
                drawPath(g, map, path, false, false, 6 * strokeWidth, wallStrokeColor);
                drawPath(g, map, path, false, false, 3 * strokeWidth, wallFillColor);
            });

        handlesRight.stream()
            .filter(handle -> !explored.contains(handle))
            .map(handle -> map.buildPath(explored, handle, map.newMoveDir(LEFT, map.get(handle))))
            .forEach(path -> {
                drawPath(g, map, path, false, false, 6 * strokeWidth, wallStrokeColor);
                drawPath(g, map, path, false, false, 3 * strokeWidth, wallFillColor);
            });

        // ghost house
        map.tiles()
            .filter(tile -> !explored.contains(tile))
            .filter(tile -> map.get(tile) == Tiles.DCORNER_NW)
            .map(corner -> map.buildPath(explored, corner, DOWN))
            .forEach(path -> {
                drawPath(g, map, path, false, false, 6 * strokeWidth, wallStrokeColor);
                drawPath(g, map, path, false, false, 3 * strokeWidth, wallFillColor);
            });
    }

    private Point2D center(Vector2i tile) {
        return new Point2D(tile.x() * s(TILE_SIZE) + s(4), tile.y() * s(TILE_SIZE) + s(4));
    }

    private void drawPath(GraphicsContext g, TileMap map, List<Vector2i> path, boolean fill, boolean close, double strokeWidth, Color strokeColor) {
        if (close) {
            path.add(path.getFirst()); // close the path
        }
        double r = s(TILE_SIZE / 2f);
        g.setFill(wallFillColor);
        g.setStroke(strokeColor);
        g.setLineWidth(strokeWidth);
        g.beginPath();
        for (int i = 0; i < path.size(); ++i) {
            Vector2i tile = path.get(i);
            Point2D p = center(tile);
            double x = p.getX(), y = p.getY();
            Vector2i prevTile = i == 0 ? null : path.get(i - 1);

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
        if (fill) {
            g.fill();
        }
        g.stroke();
    }


}