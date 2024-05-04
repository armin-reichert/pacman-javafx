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
    private double lineWidth = 1.25;

    private double s(double times) {
        return scaling * times;
    }

    public void setScaling(double scaling) {
        this.scaling = scaling;
    }

    public void setWallColor(Color wallColor) {
        this.wallColor = wallColor;
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
            case Tiles.WALL_H -> drawWallH(g, row, col, wallColor);
            case Tiles.WALL_V -> drawWallV(g, row, col, wallColor);
            case Tiles.DWALL_H -> drawDWallH(g, row, col, wallColor);
            case Tiles.DWALL_V -> drawDWallV(g, row, col, wallColor);
            case Tiles.CORNER_NW, Tiles.CORNER_NE, Tiles.CORNER_SW, Tiles.CORNER_SE -> drawCorner(g, row, col, tile, wallColor);
            case Tiles.DCORNER_NW, Tiles.DCORNER_NE, Tiles.DCORNER_SW, Tiles.DCORNER_SE -> drawDCorner(g, row, col, tile, wallColor);
            case Tiles.DOOR -> drawDoor(g, row, col, Color.PINK);
            default -> {}
        }
    }

    public void drawWallH(GraphicsContext g, int row, int col, Color color) {
        double size = scaling * TS;
        g.setStroke(color);
        g.setLineWidth(lineWidth);
        double x = col * size;
        double y = row * size;
        g.strokeLine(x, y + s(4), x + size, y + s(4));
    }

    public void drawDWallH(GraphicsContext g, int row, int col, Color color) {
        double size = scaling * TS;
        g.setStroke(color);
        g.setLineWidth(lineWidth);
        double x = col * size;
        double y = row * size;
        g.strokeLine(x, y + s(3), x + size, y + s(3));
        g.strokeLine(x, y + s(5), x + size, y + s(5));
    }

    public void drawWallV(GraphicsContext g, int row, int col, Color color) {
        double size = scaling * TS;
        g.setStroke(color);
        g.setLineWidth(lineWidth);
        double x = col * size;
        double y = row * size;
        g.strokeLine(x + s(4), y, x + s(4), y + size);
    }

    public void drawDWallV(GraphicsContext g, int row, int col, Color color) {
        double size = scaling * TS;
        g.setStroke(color);
        g.setLineWidth(lineWidth);
        double x = col * size;
        double y = row * size;
        g.strokeLine(x + s(3), y, x + s(3), y + size);
        g.strokeLine(x + s(5), y, x + s(5), y + size);
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
        g.setLineWidth(lineWidth);
        g.strokeArc(x, y, s(8), s(8), startAngle, 90, ArcType.OPEN);
    }

    public void drawDCorner(GraphicsContext g, int row, int col, byte cornerTile, Color color) {
        double size = scaling * TS;
        double startAngle = 0;
        double originX = col * size,  originY = row * size;
        double x = originX, y = originY;
        double xx = x, yy = y;
        switch (cornerTile) {
            case Tiles.DCORNER_NW -> {
                startAngle = 90;
                x  = originX + s(5);
                y  = originY + s(5);
                xx = originX + s(3);
                yy = originY + s(3);
            }
            case Tiles.DCORNER_NE -> {
                startAngle = 0;
                x  = originX - s(3);
                y  = originY + s(5);
                xx = originX - s(5);
                yy = originY + s(3);
            }
            case Tiles.DCORNER_SE -> {
                startAngle = 270;
                x  = originX - s(3);
                y  = originY - s(3);
                xx = originX - s(5);
                yy = originY - s(5);
            }
            case Tiles.DCORNER_SW -> {
                startAngle = 180;
                x  = originX + s(5);
                y  = originY - s(3);
                xx = originX + s(3);
                yy = originY - s(5);
            }
            default -> {}
        };
        g.setStroke(color);
        g.setLineWidth(lineWidth);
        g.strokeArc(x, y, s(6), s(6), startAngle, 90, ArcType.OPEN);
        //g.setStroke(Color.RED);
        g.strokeArc(xx, yy, s(10), s(10), startAngle, 90, ArcType.OPEN);
    }

    public void drawDoor(GraphicsContext g, int row, int col, Color color) {
        double x = col * s(8);
        double y = row * s(8);
        g.setFill(color);
        g.fillRect(x, y + s(3.5), s(8), s(1));
    }

    public void drawPellet(GraphicsContext g, int row, int col, Color color) {
        double size = scaling * TS;
        double x = col * size + 0.375 * size;
        double y = row * size + 0.375 * size;
        g.setFill(color);
        g.fillRect(x, y, 0.25 * size, 0.25 * size);
    }

    public void drawEnergizer(GraphicsContext g, int row, int col, Color color) {
        double size = scaling * TS;
        double x = col * size;
        double y = row * size;
        g.setFill(color);
        //g.fillOval(x, y, size, size);
        g.fillRect(x + 2 * scaling, y, 4 * scaling, 8 * scaling);
        g.fillRect(x, y + 2 * scaling, 8 * scaling, 4 * scaling);
        g.fillRect(x + scaling, y + scaling, 6 * scaling, 6 * scaling);
    }
}
