package de.amr.games.pacman.ui.fx.rendering2d;

import de.amr.games.pacman.model.world.TileMap;
import de.amr.games.pacman.model.world.Tiles;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.shape.ArcType;

public class TileMapRenderer {

    private static final int TS = 8;
    private double scaling = 1.0;
    private Color wallColor;
    private Color wallInnerColor;

    private double s(double times) {
        return scaling * times;
    }

    public void setScaling(double scaling) {
        this.scaling = scaling;
    }

    public void setWallColor(Color wallColor) {
        this.wallColor = wallColor;
    }

    public void setWallInnerColor(Color wallInnerColor) {
        this.wallInnerColor = wallInnerColor;
    }

    public void drawMap(GraphicsContext g, TileMap map) {
        for (int i = 0; i < map.numRows(); ++i) {
            for (int j = 0; j < map.numCols(); ++j) {
                drawTile(g, i, j, map.content(i, j));
            }
        }
    }

    public void drawTile(GraphicsContext g, int row, int col, byte tile) {
        switch (tile) {
            case Tiles.WALL_H -> drawWallH(g, row, col, wallColor, wallInnerColor);
            case Tiles.WALL_V -> drawWallV(g, row, col, wallColor, wallInnerColor);
            case Tiles.DWALL_H -> drawDWallH(g, row, col, wallColor);
            case Tiles.DWALL_V -> drawDWallV(g, row, col, wallColor);
            case Tiles.CORNER_NW, Tiles.CORNER_NE, Tiles.CORNER_SW, Tiles.CORNER_SE -> drawCorner(g, row, col, tile, wallColor);
            case Tiles.DCORNER_NW, Tiles.DCORNER_NE, Tiles.DCORNER_SW, Tiles.DCORNER_SE -> drawDCorner(g, row, col, tile, wallColor);
            case Tiles.DOOR -> drawDoor(g, row, col, Color.PINK);
            default -> {}
        }
    }

    public void drawWallH(GraphicsContext g, int row, int col, Color color, Color innerColor) {
        double s8 = s(8);
        double x = col * s8;
        double y = row * s8;
        g.setFill(color);
        g.fillRect(x, y + s(3.5), s8, s(1));
    }

    public void drawDWallH(GraphicsContext g, int row, int col, Color color) {
        double s8 = s(8);
        double x = col * s8;
        double y = row * s8;
        g.setFill(color);
        g.fillRect(x, y + s(2.5), s8, s(1));
        g.fillRect(x, y + s(4.5), s8, s(1));
    }

    public void drawWallV(GraphicsContext g, int row, int col, Color color, Color innerColor) {
        double s8 = s(8);
        double x = col * s8;
        double y = row * s8;
        g.setFill(color);
        g.fillRect(x + s(3.5), y, s(1), s8);
    }

    public void drawDWallV(GraphicsContext g, int row, int col, Color color) {
        double s8 = s(8);
        double x = col * s8;
        double y = row * s8;
        g.setFill(color);
        g.fillRect(x + s(2.5), y, s(1), s8);
        g.fillRect(x + s(4.5), y, s(1), s8);
    }

    public void drawCorner(GraphicsContext g, int row, int col, byte cornerTile, Color color) {
        double startAngle = 0;
        double originX = col * s(8),  originY = row * s(8);
        double x = originX, y = originY;
        switch (cornerTile) {
            case Tiles.CORNER_NW -> {
                startAngle = 90;
                x = originX + s(4);
                y = originY + s(4);
            }
            case Tiles.CORNER_NE -> {
                startAngle = 0;
                x = originX - s(4);
                y = originY + s(4);
            }
            case Tiles.CORNER_SE -> {
                startAngle = 270;
                x = originX - s(4);
                y = originY - s(4);
            }
            case Tiles.CORNER_SW -> {
                startAngle = 180;
                x = originX + s(4);
                y = originY - s(4);
            }
            default -> {}
        };
        g.setStroke(color);
        g.setLineWidth(s(1));
        g.strokeArc(x, y, s(8), s(8), startAngle, 90, ArcType.OPEN);
    }

    public void drawDCorner(GraphicsContext g, int row, int col, byte cornerTile, Color color) {
        double s3 = s(3), s5 = s(5), s8 = s(8);
        double startAngle = 0;
        double originX = col * s8,  originY = row * s8;
        double x = originX, y = originY;
        double xx = x, yy = y;
        switch (cornerTile) {
            case Tiles.DCORNER_NW -> {
                startAngle = 90;
                x  = originX + s5;
                y  = originY + s5;
                xx = originX + s3;
                yy = originY + s3;
            }
            case Tiles.DCORNER_NE -> {
                startAngle = 0;
                x  = originX - s3;
                y  = originY + s5;
                xx = originX - s5;
                yy = originY + s3;
            }
            case Tiles.DCORNER_SE -> {
                startAngle = 270;
                x  = originX - s3;
                y  = originY - s3;
                xx = originX - s5;
                yy = originY - s5;
            }
            case Tiles.DCORNER_SW -> {
                startAngle = 180;
                x  = originX + s5;
                y  = originY - s3;
                xx = originX + s3;
                yy = originY - s5;
            }
            default -> {}
        };
        g.setStroke(color);
        g.setLineWidth(s(1));
        g.strokeArc(x, y, s(6), s(6), startAngle, 90, ArcType.OPEN);
        //g.setStroke(Color.RED);
        g.strokeArc(xx, yy, s(10), s(10), startAngle, 90, ArcType.OPEN);
    }

    public void drawDoor(GraphicsContext g, int row, int col, Color color) {
        double x = col * s(8), y = row * s(8);
        g.setFill(color);
        g.fillRect(x, y + s(3.5), s(8), s(1));
    }

    public void drawPellet(GraphicsContext g, int row, int col, Color color) {
        double s2 = s(2), s3 = s(3), s8 = s(8);
        double x = col * s8, y = row * s8;
        g.setFill(color);
        g.fillRect(x + s3, y + s3, s2, s2);
    }

    public void drawEnergizer(GraphicsContext g, int row, int col, Color color) {
        double s1 = scaling, s2 = s(2), s4 = s(4), s6 = s(6), s8 = s(8);
        double x = col * s8, y = row * s8;
        g.setFill(color);
        //g.fillOval(x, y, size, size);
        g.fillRect(x + s2, y, s4, s8);
        g.fillRect(x, y + s2, s8, s4);
        g.fillRect(x + s1, y + s1, s6, s6);
    }
}
