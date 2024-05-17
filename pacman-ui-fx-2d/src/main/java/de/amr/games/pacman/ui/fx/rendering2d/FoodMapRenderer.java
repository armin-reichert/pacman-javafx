package de.amr.games.pacman.ui.fx.rendering2d;

import de.amr.games.pacman.lib.Vector2i;
import de.amr.games.pacman.model.world.TileMap;
import de.amr.games.pacman.model.world.Tiles;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class FoodMapRenderer implements TileMapRenderer {

    private float scaling;

    public void setScaling(double scaling) {
        this.scaling = (float) scaling;
    }

    private Color pelletColor = Color.PINK;
    private Color energizerColor = Color.YELLOW;

    public void setEnergizerColor(Color energizerColor) {
        this.energizerColor = energizerColor;
    }

    public void setPelletColor(Color pelletColor) {
        this.pelletColor = pelletColor;
    }

    @Override
    public void drawMap(GraphicsContext g, TileMap map) {
        map.tiles().forEach(tile -> drawTile(g, tile, map.get(tile)));
    }

    public void drawTile(GraphicsContext g, Vector2i tile, byte content) {
        switch (content) {
            case Tiles.PELLET -> drawPellet(g, tile);
            case Tiles.ENERGIZER -> drawEnergizer(g, tile);
            default -> {}
        }
    }

    public void drawPellet(GraphicsContext g, Vector2i tile) {
        g.save();
        g.scale(scaling, scaling);
        double x = tile.x() * TILE_SIZE, y = tile.y() * TILE_SIZE;
        g.setFill(pelletColor);
        g.fillRect(x + 3, y + 3, 2, 2);
        g.restore();
    }

    public void drawEnergizer(GraphicsContext g, Vector2i tile) {
        g.save();
        g.scale(scaling, scaling);
        double x = tile.x() * TILE_SIZE, y = tile.y() * TILE_SIZE;
        g.setFill(energizerColor);
        g.fillRect(x + 2, y, 0.5 * TILE_SIZE, TILE_SIZE);
        g.fillRect(x, y + 2, TILE_SIZE, 0.5 * TILE_SIZE);
        g.fillRect(x + 1, y + 1, TILE_SIZE - 2, TILE_SIZE - 2);
        g.restore();
    }
}