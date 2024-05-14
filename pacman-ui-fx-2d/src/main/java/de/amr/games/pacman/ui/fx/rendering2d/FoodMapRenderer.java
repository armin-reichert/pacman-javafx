package de.amr.games.pacman.ui.fx.rendering2d;

import de.amr.games.pacman.lib.Vector2i;
import de.amr.games.pacman.model.world.Tiles;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class FoodMapRenderer implements TileMapRenderer {

    private float scaling;

    public void setScaling(double scaling) {
        this.scaling = (float) scaling;
    }

    private float s(float times) {
        return scaling * times;
    }

    private Color pelletColor = Color.PINK;
    private Color energizerColor = Color.YELLOW;

    public void setEnergizerColor(Color energizerColor) {
        this.energizerColor = energizerColor;
    }

    public void setPelletColor(Color pelletColor) {
        this.pelletColor = pelletColor;
    }

    public void drawTile(GraphicsContext g, Vector2i tile, byte content) {
        g.save();
        switch (content) {
            case Tiles.PELLET -> drawPellet(g, tile);
            case Tiles.ENERGIZER -> drawEnergizer(g, tile);
            default -> {
            }
        }
        g.restore();
    }

    public void drawPellet(GraphicsContext g, Vector2i tile) {
        double x = tile.x() * s(8), y = tile.y() * s(8);
        g.setFill(pelletColor);
        g.fillRect(x + s(3), y + s(3), s(2), s(2));
    }

    public void drawEnergizer(GraphicsContext g, Vector2i tile) {
        double x = tile.x() * s(8), y = tile.y() * s(8);
        g.setFill(energizerColor);
        //g.fillOval(x, y, size, size);
        g.fillRect(x + s(2), y, s(4), s(8));
        g.fillRect(x, y + s(2), s(8), s(4));
        g.fillRect(x + s(1), y + s(1), s(6), s(6));
    }
}