package de.amr.games.pacman.ui.fx.util;

import de.amr.games.pacman.lib.Vector2i;
import de.amr.games.pacman.model.world.Tiles;
import de.amr.games.pacman.ui.fx.rendering2d.TileMapRenderer;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.shape.ArcType;

public class TerrainMapRenderer implements TileMapRenderer {

    private float scaling;

    public void setScaling(double scaling) {
        this.scaling = (float) scaling;
    }

    private float s(float times) {
        return scaling * times;
    }

    private Color wallColor = Color.GREEN;

    public void setWallColor(Color wallColor) {
        this.wallColor = wallColor;
    }

    public void drawTile(GraphicsContext g, Vector2i tile, byte content) {
        switch (content) {
            case Tiles.WALL_H -> drawWallH(g, tile);
            case Tiles.WALL_V -> drawWallV(g, tile);
            case Tiles.DWALL_H -> drawDWallH(g, tile);
            case Tiles.DWALL_V -> drawDWallV(g, tile);
            case Tiles.CORNER_NW, Tiles.CORNER_NE, Tiles.CORNER_SW, Tiles.CORNER_SE -> drawCorner(g, tile, content);
            case Tiles.DCORNER_NW, Tiles.DCORNER_NE, Tiles.DCORNER_SW, Tiles.DCORNER_SE ->
                drawDCorner(g, tile, content);
            case Tiles.DOOR -> drawDoor(g, tile, Color.PINK);
            default -> {}
        }
    }

    public void drawWallH(GraphicsContext g, Vector2i tile) {
        double x = tile.x() * s(8), y = tile.y() * s(8);
        g.setFill(wallColor);
        // add 1 pixel to avoid gaps
        g.fillRect(x, y + s(3.5f), s(8) + 1, s(1));
    }

    public void drawDWallH(GraphicsContext g, Vector2i tile) {
        double x = tile.x() * s(8), y = tile.y() * s(8);
        g.setFill(wallColor);
        // add 1 pixel to avoid gaps
        g.fillRect(x, y + s(2.5f), s(8) + 1, s(1));
        g.fillRect(x, y + s(4.5f), s(8) + 1, s(1));
    }

    public void drawWallV(GraphicsContext g, Vector2i tile) {
        double x = tile.x() * s(8), y = tile.y() * s(8);
        g.setFill(wallColor);
        // add 1 pixel to avoid gaps
        g.fillRect(x + s(3.5f), y, s(1), s(8) + 1);
    }

    public void drawDWallV(GraphicsContext g, Vector2i tile) {
        double x = tile.x() * s(8), y = tile.y() * s(8);
        g.setFill(wallColor);
        // add 1 pixel to avoid gaps
        g.fillRect(x + s(2.5f), y, s(1), s(8) + 1);
        g.fillRect(x + s(4.5f), y, s(1), s(8) + 1);
    }

    public void drawCorner(GraphicsContext g, Vector2i tile, byte cornerType) {
        double x = tile.x() * s(8), y = tile.y() * s(8);
        g.setStroke(wallColor);
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
        g.setStroke(wallColor);
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
}