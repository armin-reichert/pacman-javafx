package de.amr.games.pacman.mapeditor;

import de.amr.games.pacman.lib.TileMap;
import de.amr.games.pacman.lib.Tiles;
import de.amr.games.pacman.lib.Vector2i;
import javafx.beans.property.FloatProperty;
import javafx.beans.property.SimpleFloatProperty;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class FoodMapRenderer implements TileMapRenderer {

    public FloatProperty scalingPy = new SimpleFloatProperty(this, "scaling", 1f);

    public void setScaling(double scaling) {
        scalingPy.set((float) scaling);
    }

    public float scaling() {
        return scalingPy.get();
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
        g.scale(scaling(), scaling());
        double x = tile.x() * TILE_SIZE, y = tile.y() * TILE_SIZE;
        g.setFill(pelletColor);
        g.fillRect(x + 3, y + 3, 2, 2);
        g.restore();
    }

    public void drawEnergizer(GraphicsContext g, Vector2i tile) {
        g.save();
        g.scale(scaling(), scaling());
        double x = tile.x() * TILE_SIZE, y = tile.y() * TILE_SIZE;
        g.setFill(energizerColor);
        g.fillRect(x + 2, y, 0.5 * TILE_SIZE, TILE_SIZE);
        g.fillRect(x, y + 2, TILE_SIZE, 0.5 * TILE_SIZE);
        g.fillRect(x + 1, y + 1, TILE_SIZE - 2, TILE_SIZE - 2);
        g.restore();
    }
}