/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui.fx.rendering2d;

import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.Vector2i;
import de.amr.games.pacman.model.world.TileMap;
import de.amr.games.pacman.model.world.Tiles;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.shape.Arc;
import javafx.scene.shape.ArcType;
import org.tinylog.Logger;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static de.amr.games.pacman.lib.Direction.*;
import static de.amr.games.pacman.lib.Direction.UP;

public class TerrainMapRenderer implements TileMapRenderer {

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
        double x = tile.x() * s(8), y = tile.y() * s(8);
        g.setFill(color);
        g.fillRect(x, y + s(3.5f), s(8), s(1));
    }

    private boolean isDouble(byte tileContent) {
        return tileContent == Tiles.DWALL_H || tileContent == Tiles.DWALL_V
            || tileContent == Tiles.DCORNER_NE || tileContent == Tiles.DCORNER_SE || tileContent == Tiles.DCORNER_NW || tileContent == Tiles.DCORNER_SW;
    }

    public void _drawMap(GraphicsContext g, TileMap map) {
        map.tiles()
            .filter(tile -> isDouble(map.get(tile)) || map.get(tile) == Tiles.DOOR)
            .forEach(tile -> drawTile(g, tile, map.get(tile)));
        drawPathsInsideMap(g, map);
    }

    private void drawPathsInsideMap(GraphicsContext g, TileMap map) {
        var explored = new HashSet<Vector2i>();

        // Obstacles inside maze
        map.tiles()
            .filter(tile -> map.get(tile) == Tiles.CORNER_NW)
            .filter(corner -> corner.x() > 0 && corner.x() < map.numCols() - 1)
            .filter(corner -> corner.y() > 0 && corner.y() < map.numRows() - 1)
            .map(corner -> buildPath(map, explored, corner, RIGHT))
            .forEach(path -> drawPath(g, map, path));
    }

    private void drawPath(GraphicsContext g, TileMap map, List<Vector2i> path) {
        double s4 = s(4), s8 = s(8);
        g.setFill(wallFillColor);
        g.setStroke(wallStrokeColor);
        g.setLineWidth(s(0.75f));
        g.beginPath();
        g.moveTo(path.getFirst().x() * s8 + s4, path.getFirst().y() * s8 + s4);
        for (int i = 1; i < path.size(); ++i) {
            var next = path.get(i);
            var centerX = next.x() * s8 + s4;
            var centerY = next.y() * s8 + s4;
            switch (map.get(next)) {
                default -> g.lineTo(centerX, centerY);
            }
        }
        g.closePath();
        g.fill();
        g.stroke();
    }

    private static List<Vector2i> buildPath(
        TileMap terrainMap, Set<Vector2i> explored, Vector2i startTile, Direction startDirection)
    {
        List<Vector2i> path = new ArrayList<>();
        Vector2i current = startTile;
        Direction moveDir = startDirection;
        while (true) {
            path.add(current);
            explored.add(current);
            var next = current.plus(moveDir.vector());
            if (!terrainMap.insideBounds(next)) {
                break;
            }
            if (explored.contains(next)) {
                path.add(next);
                break;
            }
            moveDir = newMoveDir(moveDir, terrainMap.get(next));
            current = next;
        }
        return path;
    }

    private static Direction newMoveDir(Direction moveDir, byte tileValue) {
        return switch (tileValue) {
            case Tiles.CORNER_NW, Tiles.DCORNER_NW -> moveDir == LEFT  ? DOWN  : RIGHT;
            case Tiles.CORNER_NE, Tiles.DCORNER_NE -> moveDir == RIGHT ? DOWN  : LEFT;
            case Tiles.CORNER_SE, Tiles.DCORNER_SE -> moveDir == DOWN  ? LEFT  : UP;
            case Tiles.CORNER_SW, Tiles.DCORNER_SW -> moveDir == DOWN  ? RIGHT : UP;
            default -> moveDir;
        };
    }

}