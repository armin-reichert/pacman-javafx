/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui.fx.rendering2d;

import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.Vector2f;
import de.amr.games.pacman.lib.Vector2i;
import de.amr.games.pacman.model.world.TileMap;
import de.amr.games.pacman.model.world.TileMapPath;
import de.amr.games.pacman.model.world.Tiles;
import javafx.beans.property.FloatProperty;
import javafx.beans.property.SimpleFloatProperty;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.BitSet;

import static de.amr.games.pacman.lib.Direction.LEFT;
import static de.amr.games.pacman.lib.Direction.RIGHT;

/**
 * @author Armin Reichert
 */
public class TerrainMapRenderer implements TileMapRenderer {

    public FloatProperty scalingPy = new SimpleFloatProperty(this, "scaling", 1f);

    protected Color wallFillColor = Color.BLACK;
    protected Color wallStrokeColor = Color.GREEN;
    protected Color doorColor = Color.PINK;

    public void setScaling(double scaling) {
        scalingPy.set((float) scaling);
    }

    public float scaling() {
        return scalingPy.get();
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

    public void drawMap(GraphicsContext g, TileMap map) {
        drawTripleStrokePaths(g, map);
        drawSingleStrokePaths(g, map);
        map.tiles(Tiles.DOOR).forEach(tile -> drawDoor(g, tile, doorColor));
    }

    public void drawTile(GraphicsContext g, Vector2i tile, byte content) {
        if (content == Tiles.DOOR) {
            drawDoor(g, tile, doorColor);
        }
    }

    public void drawDoor(GraphicsContext g, Vector2i tile, Color color) {
        double x = tile.x() * TILE_SIZE, y = tile.y() * TILE_SIZE;
        g.save();
        g.scale(scaling(), scaling());
        g.setFill(Color.BLACK);
        g.fillRect(x, y, TILE_SIZE, 6);
        g.setFill(color);
        g.fillRect(x-1, y+3, TILE_SIZE+2, 2);
        g.restore();
    }

    public void drawSingleStrokePaths(GraphicsContext g, TileMap map) {
        var explored = new BitSet();
        map.tiles(Tiles.CORNER_NW)
            .filter(corner -> !explored.get(map.index(corner)))
            .map(corner -> TileMapPath.build(map, explored, corner, LEFT))
            .forEach(path -> drawPath(g, map, path, true, 1, wallStrokeColor, wallFillColor));
    }

    /*
     * Draws a path with an inside stroke of wall fill color and two outside strokes of wall stroke color.
     */
    public void drawTripleStrokePath(GraphicsContext g, TileMap map, TileMapPath path) {
        drawPath(g, map, path, false,  3, wallStrokeColor, null);
        drawPath(g, map, path, false,  1, wallFillColor, null);
    }

    public void drawTripleStrokePaths(GraphicsContext g, TileMap map) {
        var explored = new BitSet();

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
            .filter(handle -> !explored.get(map.index(handle)))
            .map(handle -> TileMapPath.build(map, explored, handle, RIGHT))
            .forEach(path -> drawTripleStrokePath(g, map, path));

        handlesRight.stream()
            .filter(handle -> !explored.get(map.index(handle)))
            .map(handle -> TileMapPath.build(map, explored, handle, LEFT))
            .forEach(path -> drawTripleStrokePath(g, map, path));

        // ghost house
        map.tiles(Tiles.DCORNER_NW)
            .filter(corner -> !explored.get(map.index(corner)))
            .map(corner -> TileMapPath.build(map, explored, corner, LEFT))
            .forEach(path -> drawTripleStrokePath(g, map, path));
    }

    public Vector2f center(Vector2i tile) {
        return new Vector2f(tile.x() * TILE_SIZE + TILE_SIZE / 2f, tile.y() * TILE_SIZE + TILE_SIZE / 2f);
    }

    public void drawPath(
        GraphicsContext g,
        TileMap map, TileMapPath tileMapPath,
        boolean fill, double lineWidth, Color outlineColor, Color fillColor) {

        g.save();
        g.scale(scaling(), scaling());

        double r = 4;
        g.beginPath();

        //TODO: avoid these special cases
        Vector2i tile = tileMapPath.startTile;
        if (tile.x() == 0 && map.get(tile) == Tiles.DWALL_H) {
            // start at left maze border, not at tile center
            double y = center(tile).y();
            g.moveTo(0, y);
            g.lineTo(r, y);
        }

        Vector2f c = center(tile);
        switch (map.get(tile)) {
            case Tiles.WALL_H, Tiles.DWALL_H       -> g.lineTo(c.x()+r, c.y());
            case Tiles.WALL_V, Tiles.DWALL_V       -> g.lineTo(c.x(), c.y()+r);
            case Tiles.CORNER_NW, Tiles.DCORNER_NW -> g.arc(c.x()+r, c.y()+r, r, r,  90, 90);
            case Tiles.CORNER_SW, Tiles.DCORNER_SW -> g.arc(c.x()+r, c.y()-r, r, r, 180, 90);
            case Tiles.CORNER_NE, Tiles.DCORNER_NE -> g.arc(c.x()-r, c.y()+r, r, r, tile.x() != 0 ? 0:90, tile.x() != 0? 90:-90);
            case Tiles.CORNER_SE, Tiles.DCORNER_SE -> g.arc(c.x()-r, c.y()-r, r, r, 270, 90);
            default -> {}
        }
        for (Direction dir : tileMapPath.directions) {
            Vector2i prev = tile;
            tile = tile.plus(dir.vector());
            c = center(tile);
            boolean left = prev.x() > tile.x();
            boolean right = prev.x() < tile.x();
            boolean up = prev.y() > tile.y();
            boolean down = prev.y() < tile.y();
            switch (map.get(tile)) {
                case Tiles.WALL_H, Tiles.DWALL_H       -> g.lineTo(c.x() + r, c.y());
                case Tiles.WALL_V, Tiles.DWALL_V       -> g.lineTo(c.x(), c.y() + r);
                case Tiles.CORNER_NW, Tiles.DCORNER_NW -> g.arc(c.x() + r, c.y() + r, r, r, left?  90:180,  left?  90:-90);
                case Tiles.CORNER_SW, Tiles.DCORNER_SW -> g.arc(c.x() + r, c.y() - r, r, r, down?  180:270, down?  90:-90);
                case Tiles.CORNER_NE, Tiles.DCORNER_NE -> g.arc(c.x() - r, c.y() + r, r, r, up?    0:90,    up?    90:-90);
                case Tiles.CORNER_SE, Tiles.DCORNER_SE -> g.arc(c.x() - r, c.y() - r, r, r, right? 270:0,   right? 90:-90);
                default -> {}
            }
        }

        if (tile.x() == 0 && map.get(tile) == Tiles.DWALL_H) {
            g.lineTo(0, center(tile).y());
        }

        if (fill) {
            g.setFill(fillColor);
            g.fill();
        }
        g.setStroke(outlineColor);
        g.setLineWidth(lineWidth);
        g.stroke();

        g.restore();
    }
}