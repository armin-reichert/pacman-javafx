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

import static de.amr.games.pacman.lib.Direction.*;

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

        //TODO avoid these special cases
        Vector2i first = path.getFirst();
        if (first.x() == 0 && map.get(first) == Tiles.DWALL_H) {
            // start at left maze border, not at tile center
            double y = center(first).getY();
            g.moveTo(0, y);
            g.lineTo(r, y);
        }

        Point2D center = center(first);
        double x = center.getX(), y = center.getY();
        switch (map.get(first)) {
            case Tiles.WALL_H, Tiles.DWALL_H       -> g.lineTo(x + r, y);
            case Tiles.WALL_V, Tiles.DWALL_V       -> g.lineTo(x, y + r);
            case Tiles.CORNER_NW, Tiles.DCORNER_NW -> g.arc(x+r, y+r, r, r,  90, 90);
            case Tiles.CORNER_SW, Tiles.DCORNER_SW -> g.arc(x+r, y-r, r, r, 180, 90);
            case Tiles.CORNER_NE, Tiles.DCORNER_NE -> g.arc(x-r, y+r, r, r, first.x() != 0 ? 0:90, first.x() != 0? 90:-90);
            case Tiles.CORNER_SE, Tiles.DCORNER_SE -> g.arc(x-r, y-r, r, r, 270, 90);
            default -> {}
        }
        for (int i = 1; i < path.size(); ++i) {
            Vector2i tile = path.get(i);
            Vector2i prev = path.get(i-1);
            center = center(tile);
            x = center.getX();
            y = center.getY();
            boolean left = prev.x() > tile.x(), right = prev.x() < tile.x(), up = prev.y() > tile.y(), down = prev.y() < tile.y();
            switch (map.get(tile)) {
                case Tiles.WALL_H, Tiles.DWALL_H       -> g.lineTo(x + r, y);
                case Tiles.WALL_V, Tiles.DWALL_V       -> g.lineTo(x, y + r);
                case Tiles.CORNER_NW, Tiles.DCORNER_NW -> g.arc(x+r, y+r, r, r, left? 90:180,  left?90:-90);
                case Tiles.CORNER_SW, Tiles.DCORNER_SW -> g.arc(x+r, y-r, r, r, down? 180:270, down?90:-90);
                case Tiles.CORNER_NE, Tiles.DCORNER_NE -> g.arc(x-r, y+r, r, r, up? 0:90,    up?90:-90);
                case Tiles.CORNER_SE, Tiles.DCORNER_SE -> g.arc(x-r, y-r, r, r, right? 270:0,   right?90:-90);
                default -> {}
            }
        }

        if (path.getLast().x() == 0 && map.get(path.getLast()) == Tiles.DWALL_H) {
            g.lineTo(0, center(path.getLast()).getY());
        }

        if (fill) {
            g.setFill(fillColor);
            g.fill();
        }
        g.setStroke(outlineColor);
        g.setLineWidth(lineWidth);
        g.stroke();
    }
}