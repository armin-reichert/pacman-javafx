/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui.fx.rendering2d;

import de.amr.games.pacman.lib.Vector2i;
import de.amr.games.pacman.model.world.TileMap;
import de.amr.games.pacman.model.world.Tiles;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.shape.ArcType;

/**
 * @author Armin Reichert
 */
public class TileMapRenderer {

    private float scaling = 1.0f;
    private final float[] s = new float[9];
    private Color wallColor;

    private float s(float times) {
        return scaling * times;
    }

    public void setScaling(double scaling) {
        this.scaling = (float) scaling;
        // cache some values
        for (int i = 0; i <= 8; ++i) {
            s[i] = s(i);
        }
    }

    public void setWallColor(Color wallColor) {
        this.wallColor = wallColor;
    }

    public void drawMap(GraphicsContext g, TileMap map) {
        map.tiles().forEach(tile -> drawTile(g, tile, map.content(tile)));
    }

    public void drawTile(GraphicsContext g, Vector2i tile, byte content) {
        switch (content) {
            case Tiles.WALL_H -> drawWallH(g, tile);
            case Tiles.WALL_V -> drawWallV(g, tile);
            case Tiles.DWALL_H -> drawDWallH(g, tile);
            case Tiles.DWALL_V -> drawDWallV(g, tile);
            case Tiles.CORNER_NW, Tiles.CORNER_NE, Tiles.CORNER_SW, Tiles.CORNER_SE -> drawCorner(g, tile, content);
            case Tiles.DCORNER_NW, Tiles.DCORNER_NE, Tiles.DCORNER_SW, Tiles.DCORNER_SE -> drawDCorner(g, tile, content);
            case Tiles.DOOR -> drawDoor(g, tile, Color.PINK);
            default -> {}
        }
    }

    public void drawWallH(GraphicsContext g, Vector2i tile) {
        double x = tile.x() * s[8], y = tile.y() * s[8];
        g.setFill(wallColor);
        // add 1 pixel to avoid gaps
        g.fillRect(x, y + s(3.5f), s[8] + 1, s[1]);
    }

    public void drawDWallH(GraphicsContext g, Vector2i tile) {
        double x = tile.x() * s[8], y = tile.y() * s[8];
        g.setFill(wallColor);
        // add 1 pixel to avoid gaps
        g.fillRect(x, y + s(2.5f), s[8] + 1, s[1]);
        g.fillRect(x, y + s(4.5f), s[8] + 1, s[1]);
    }

    public void drawWallV(GraphicsContext g, Vector2i tile) {
        double x = tile.x() * s[8], y = tile.y() * s[8];
        g.setFill(wallColor);
        // add 1 pixel to avoid gaps
        g.fillRect(x + s(3.5f), y, s[1], s[8] + 1);
    }

    public void drawDWallV(GraphicsContext g, Vector2i tile) {
        double x = tile.x() * s[8], y = tile.y() * s[8];
        g.setFill(wallColor);
        // add 1 pixel to avoid gaps
        g.fillRect(x + s(2.5f), y, s[1], s[8] + 1);
        g.fillRect(x + s(4.5f), y, s[1], s[8] + 1);
    }

    public void drawCorner(GraphicsContext g, Vector2i tile, byte cornerType) {
        double x = tile.x() * s[8], y = tile.y() * s[8];
        g.setStroke(wallColor);
        g.setLineWidth(s[1]);
        switch (cornerType) {
            case Tiles.CORNER_NW -> g.strokeArc(x + s[4], y + s[4], s[8], s[8], 90, 90,  ArcType.OPEN);
            case Tiles.CORNER_NE -> g.strokeArc(x - s[4], y + s[4], s[8], s[8], 0, 90,   ArcType.OPEN);
            case Tiles.CORNER_SE -> g.strokeArc(x - s[4], y - s[4], s[8], s[8], 270, 90, ArcType.OPEN);
            case Tiles.CORNER_SW -> g.strokeArc(x + s[4], y - s[4], s[8], s[8], 180, 90, ArcType.OPEN);
            default -> {}
        }
    }

    public void drawDCorner(GraphicsContext g, Vector2i tile, byte cornerType) {
        double x = tile.x() * s[8], y = tile.y() * s[8];
        double s10 = 10 * scaling;
        g.setStroke(wallColor);
        g.setLineWidth(s[1]);
        switch (cornerType) {
            case Tiles.DCORNER_NW -> {
                g.strokeArc(x + s[5], y + s[5], s[6], s[6], 90, 90, ArcType.OPEN);
                g.strokeArc(x + s[3], y + s[3], s10, s10, 90, 90, ArcType.OPEN);
            }
            case Tiles.DCORNER_NE -> {
                g.strokeArc(x - s[3], y + s[5], s[6], s[6], 0, 90, ArcType.OPEN);
                g.strokeArc(x - s[5], y + s[3], s10, s10, 0, 90, ArcType.OPEN);
            }
            case Tiles.DCORNER_SE -> {
                g.strokeArc(x - s[3], y - s[3], s[6], s[6], 270, 90, ArcType.OPEN);
                g.strokeArc(x - s[5], y - s[5], s10, s10, 270, 90, ArcType.OPEN);
            }
            case Tiles.DCORNER_SW -> {
                g.strokeArc(x + s[5], y - s[3], s[6], s[6], 180, 90, ArcType.OPEN);
                g.strokeArc(x + s[3], y - s[5], s10, s10, 180, 90, ArcType.OPEN);
            }
            default -> {}
        }
    }

    public void drawDoor(GraphicsContext g, Vector2i tile, Color color) {
        double x = tile.x() * s[8], y = tile.y() * s[8];
        g.setFill(color);
        g.fillRect(x, y + s(3.5f), s[8], s[1]);
    }

    public void drawPellet(GraphicsContext g, Vector2i tile, Color color) {
        double x = tile.x() * s[8], y = tile.y() * s[8];
        g.setFill(color);
        g.fillRect(x + s[3], y + s[3], s[2], s[2]);
    }

    public void drawEnergizer(GraphicsContext g, Vector2i tile, Color color) {
        double x = tile.x() * s[8], y = tile.y() * s[8];
        g.setFill(color);
        //g.fillOval(x, y, size, size);
        g.fillRect(x + s[2], y, s[4], s[8]);
        g.fillRect(x, y + s[2], s[8], s[4]);
        g.fillRect(x + s[1], y + s[1], s[6], s[6]);
    }
}