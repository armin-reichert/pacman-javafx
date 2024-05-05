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

    private double scaling = 1.0;
    private final double[] s = new double[9];
    private Color wallColor;

    private double s(double times) {
        return scaling * times;
    }

    public void setScaling(double scaling) {
        this.scaling = scaling;
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
        int row = tile.y(), col = tile.x();
        switch (content) {
            case Tiles.WALL_H -> drawWallH(g, row, col);
            case Tiles.WALL_V -> drawWallV(g, row, col);
            case Tiles.DWALL_H -> drawDWallH(g, row, col);
            case Tiles.DWALL_V -> drawDWallV(g, row, col);
            case Tiles.CORNER_NW, Tiles.CORNER_NE, Tiles.CORNER_SW, Tiles.CORNER_SE -> drawCorner(g, row, col, content);
            case Tiles.DCORNER_NW, Tiles.DCORNER_NE, Tiles.DCORNER_SW, Tiles.DCORNER_SE -> drawDCorner(g, row, col, content);
            case Tiles.DOOR -> drawDoor(g, row, col, Color.PINK);
            default -> {}
        }
    }

    public void drawWallH(GraphicsContext g, int row, int col) {
        double x = col * s[8], y = row * s[8];
        g.setFill(wallColor);
        // add 1 pixel to avoid gaps
        g.fillRect(x, y + s(3.5), s[8] + 1, s(1));
    }

    public void drawDWallH(GraphicsContext g, int row, int col) {
        double x = col * s[8], y = row * s[8];
        g.setFill(wallColor);
        // add 1 pixel to avoid gaps
        g.fillRect(x, y + s(2.5), s[8] + 1, s(1));
        g.fillRect(x, y + s(4.5), s[8] + 1, s(1));
    }

    public void drawWallV(GraphicsContext g, int row, int col) {
        double x = col * s[8], y = row * s[8];
        g.setFill(wallColor);
        // add 1 pixel to avoid gaps
        g.fillRect(x + s(3.5), y, s(1), s[8] + 1);
    }

    public void drawDWallV(GraphicsContext g, int row, int col) {
        double x = col * s[8], y = row * s[8];
        g.setFill(wallColor);
        // add 1 pixel to avoid gaps
        g.fillRect(x + s(2.5), y, s(1), s[8] + 1);
        g.fillRect(x + s(4.5), y, s(1), s[8] + 1);
    }

    public void drawCorner(GraphicsContext g, int row, int col, byte cornerType) {
        double startAngle = 0;
        double x = col * s[8],  y = row * s[8];
        double ax = x, ay = y;
        switch (cornerType) {
            case Tiles.CORNER_NW -> {
                startAngle = 90;
                ax = x + s[4];
                ay = y + s[4];
            }
            case Tiles.CORNER_NE -> {
                startAngle = 0;
                ax = x - s[4];
                ay = y + s[4];
            }
            case Tiles.CORNER_SE -> {
                startAngle = 270;
                ax = x - s[4];
                ay = y - s[4];
            }
            case Tiles.CORNER_SW -> {
                startAngle = 180;
                ax = x + s[4];
                ay = y - s[4];
            }
            default -> {}
        }
        g.setStroke(wallColor);
        g.setLineWidth(s(1));
        g.strokeArc(ax, ay, s(8), s(8), startAngle, 90, ArcType.OPEN);
    }

    public void drawDCorner(GraphicsContext g, int row, int col, byte cornerType) {
        double x = col * s[8],  y = row * s[8];
        double ix = x, iy = y; // inner arc
        double ox = x, oy = y; // outer arc
        double startAngle = 0;
        switch (cornerType) {
            case Tiles.DCORNER_NW -> {
                startAngle = 90;
                ix = x + s[5];
                iy = y + s[5];
                ox = x + s[3];
                oy = y + s[3];
            }
            case Tiles.DCORNER_NE -> {
                startAngle = 0;
                ix = x - s[3];
                iy = y + s[5];
                ox = x - s[5];
                oy = y + s[3];
            }
            case Tiles.DCORNER_SE -> {
                startAngle = 270;
                ix = x - s[3];
                iy = y - s[3];
                ox = x - s[5];
                oy = y - s[5];
            }
            case Tiles.DCORNER_SW -> {
                startAngle = 180;
                ix = x + s[5];
                iy = y - s[3];
                ox = x + s[3];
                oy = y - s[5];
            }
            default -> {}
        }
        g.setStroke(wallColor);
        g.setLineWidth(s(1));
        double s10 = 10 * scaling;
        g.strokeArc(ix, iy, s[6], s[6], startAngle, 90, ArcType.OPEN);
        g.strokeArc(ox, oy, s10, s10, startAngle, 90, ArcType.OPEN);
    }

    public void drawDoor(GraphicsContext g, int row, int col, Color color) {
        double x = col * s(8), y = row * s(8);
        g.setFill(color);
        g.fillRect(x, y + s(3.5), s(8), s(1));
    }

    public void drawPellet(GraphicsContext g, int row, int col, Color color) {
        double x = col * s[8], y = row * s[8];
        g.setFill(color);
        g.fillRect(x + s[3], y + s[3], s[2], s[2]);
    }

    public void drawEnergizer(GraphicsContext g, int row, int col, Color color) {
        double x = col * s[8], y = row * s[8];
        g.setFill(color);
        //g.fillOval(x, y, size, size);
        g.fillRect(x + s[2], y, s[4], s[8]);
        g.fillRect(x, y + s[2], s[8], s[4]);
        g.fillRect(x + s[1], y + s[1], s[6], s[6]);
    }
}
