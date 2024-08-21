/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.mapeditor;

import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.Vector2f;
import de.amr.games.pacman.lib.Vector2i;
import de.amr.games.pacman.lib.tilemap.TileMap;
import de.amr.games.pacman.lib.tilemap.TileMapPath;
import de.amr.games.pacman.lib.tilemap.Tiles;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import static de.amr.games.pacman.lib.Globals.HTS;
import static de.amr.games.pacman.lib.Globals.TS;
import static de.amr.games.pacman.mapeditor.TileMapUtil.TILE_SIZE;

/**
 * @author Armin Reichert
 */
public class TerrainMapRenderer implements TileMapRenderer {

    public DoubleProperty scalingPy = new SimpleDoubleProperty(this, "scaling", 1);

    protected Color wallFillColor = Color.BLACK;
    protected Color wallStrokeColor = Color.GREEN;
    protected Color doorColor = Color.PINK;

    @Override
    public void drawTile(GraphicsContext g, Vector2i tile, byte content) {
        // this renderer doesn't draw tiles individually
    }

    @Override
    public void drawMap(GraphicsContext g, TileMap map) {
        double innerPathWidth = computeLineWidth(g.getCanvas().getHeight());
        double outerPathWidth = 3 * innerPathWidth;
        double outerPathBorderWidth = 0.75 * innerPathWidth;
        g.save();
        g.scale(scaling(), scaling());
        map.doubleStrokePaths().forEach(path -> {
            drawPath(g, map, path, false,  outerPathWidth, wallStrokeColor, null);
            drawPath(g, map, path, false,  outerPathWidth - 2 * outerPathBorderWidth, wallFillColor, null);
        });
        map.singleStrokePaths().forEach(path -> drawPath(g, map, path, true, innerPathWidth, wallStrokeColor, wallFillColor));
        map.tiles(Tiles.DOOR).forEach(door -> drawDoor(g, door, doorColor));
        g.restore();
    }

    public void drawDoor(GraphicsContext g, Vector2i tile, Color color) {
        double x = tile.x() * TILE_SIZE, y = tile.y() * TILE_SIZE;
        double height = TILE_SIZE * 0.25;
        g.setFill(color);
        g.fillRect(x, y + 0.5 * (TILE_SIZE - height), TILE_SIZE, height);
    }

    @Override
    public void setScaling(double scaling) {
        scalingPy.set((float) scaling);
    }

    public double scaling() {
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

    private double computeLineWidth(double canvasHeight) {
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
        return new Vector2f(tile.x() * TILE_SIZE + TILE_SIZE / 2f, tile.y() * TILE_SIZE + TILE_SIZE / 2f);
    }

    //TODO: avoid special cases
    private void drawPath(GraphicsContext g, TileMap map, TileMapPath tileMapPath,
        boolean fill, double lineWidth, Color outlineColor, Color fillColor)
    {
        g.beginPath();
        Vector2i tile = tileMapPath.startTile();
        if (tile.x() == 0) {
            // start path at left border, not at tile center
            g.moveTo(0, tile.y() * TS + HTS);
            if (map.get(tile) == Tiles.DWALL_H) {
                g.lineTo(HTS, tile.y() * TS + HTS);
            }
        }
        //TODO clarify this
        followPath(g, map.get(tile), center(tile), true, true, tile.x() != 0, true);
        for (Direction dir : tileMapPath) {
            Vector2i prev = tile;
            tile = prev.plus(dir.vector());
            followPath(g, map.get(tile), center(tile), dir == Direction.LEFT, dir == Direction.RIGHT, dir == Direction.UP, dir == Direction.DOWN);
        }
        if (tile.x() == 0 && map.get(tile) == Tiles.DWALL_H) {
            // end path at left border
            g.lineTo(0, tile.y() * TS + HTS);
        }
        if (fill) {
            g.setFill(fillColor);
            g.fill();
        }
        g.setStroke(outlineColor);
        g.setLineWidth(lineWidth);
        g.stroke();
    }

    private void followPath(GraphicsContext g, byte tileValue, Vector2f center, boolean left, boolean right, boolean up, boolean down) {
        float radius = 0.5f * TILE_SIZE;
        float cx = center.x(), cy = center.y();
        switch (tileValue) {
            case Tiles.WALL_H,    Tiles.DWALL_H    -> g.lineTo(cx + radius, cy);
            case Tiles.WALL_V,    Tiles.DWALL_V    -> g.lineTo(cx, cy + radius);
            case Tiles.CORNER_NW, Tiles.DCORNER_NW -> g.arc(cx + radius, cy + radius, radius, radius, left?   90:180, left?  90:-90);
            case Tiles.CORNER_SW, Tiles.DCORNER_SW -> g.arc(cx + radius, cy - radius, radius, radius, down?  180:270, down?  90:-90);
            case Tiles.CORNER_NE, Tiles.DCORNER_NE -> g.arc(cx - radius, cy + radius, radius, radius, up?      0: 90, up?    90:-90);
            case Tiles.CORNER_SE, Tiles.DCORNER_SE -> g.arc(cx - radius, cy - radius, radius, radius, right? 270:  0, right? 90:-90);
            default -> {}
        }
    }
}