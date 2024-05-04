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
    private double lineWidth = 1;

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
            case Tiles.CORNER_NW, Tiles.CORNER_NE, Tiles.CORNER_SW, Tiles.CORNER_SE -> drawCorner(g, row, col, tile, wallColor);
            default -> {}
        }
    }

    public void drawWallH(GraphicsContext g, int row, int col, Color color) {
        double size = scaling * TS;
        g.setStroke(color);
        g.setLineWidth(lineWidth);
        double x = col * size;
        double y = row * size + 0.5 * size;
        g.strokeLine(x, y, x + size, y);
    }

    public void drawWallV(GraphicsContext g, int row, int col, Color color) {
        double size = scaling * TS;
        g.setStroke(color);
        g.setLineWidth(lineWidth);
        double x = col * size + 0.5 * size;
        double y = row * size;
        g.strokeLine(x, y, x, y + size);
    }

    public void drawCorner(GraphicsContext g, int row, int col, byte cornerTile, Color color) {
        double size = scaling * TS;
        double startAngle = 0;
        double originX = col * size,  originY = row * size;
        double x = originX, y = originY;
        switch (cornerTile) {
            case Tiles.CORNER_NW -> {
                startAngle = 90;
                x = originX + 0.5 * size;
                y = originY + 0.5 * size;
            }
            case Tiles.CORNER_NE -> {
                startAngle = 0;
                x = originX - 0.5 * size;
                y = originY + 0.5 * size;
            }
            case Tiles.CORNER_SE -> {
                startAngle = 270;
                x = originX -  0.5 * size;;
                y = originY -  0.5 * size;;
            }
            case Tiles.CORNER_SW -> {
                startAngle = 180;
                x = originX + 0.5 * size;
                y = originY - 0.5 * size;
            }
            default -> {}
        };
        g.setStroke(color);
        g.setLineWidth(lineWidth);
        g.strokeArc(x, y, size, size, startAngle, 90, ArcType.OPEN);
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
