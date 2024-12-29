/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.maps.rendering;

import de.amr.games.pacman.lib.Vector2i;
import de.amr.games.pacman.lib.tilemap.Obstacle;
import de.amr.games.pacman.lib.tilemap.TileMap;
import de.amr.games.pacman.lib.tilemap.TileEncoding;
import javafx.beans.property.FloatProperty;
import javafx.beans.property.SimpleFloatProperty;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import java.util.List;

import static de.amr.games.pacman.lib.Globals.HTS;
import static de.amr.games.pacman.lib.Globals.TS;

/**
 * @author Armin Reichert
 */
public class FoodMapRenderer implements TileMapRenderer {

    static final double PELLET_SIZE = 2;

    public FloatProperty scalingPy = new SimpleFloatProperty(this, "scaling", 1f);

    private Color pelletColor = Color.PINK;
    private Color energizerColor = Color.YELLOW;

    public void setScaling(double scaling) {
        scalingPy.set((float) scaling);
    }

    public float scaling() {
        return scalingPy.get();
    }

    public void setEnergizerColor(Color energizerColor) {
        this.energizerColor = energizerColor;
    }

    public void setPelletColor(Color pelletColor) {
        this.pelletColor = pelletColor;
    }

    @Override
    public void drawTerrain(GraphicsContext g, TileMap terrainMap, List<Obstacle> obstacles) {
    }

    @Override
    public void drawFood(GraphicsContext g, TileMap map) {
        map.tiles().forEach(tile -> drawTile(g, tile, map.get(tile)));
    }

    @Override
    public void drawTile(GraphicsContext g, Vector2i tile, byte content) {
        switch (content) {
            case TileEncoding.PELLET -> drawPellet(g, tile);
            case TileEncoding.ENERGIZER -> drawEnergizer(g, tile);
            default -> {}
        }
    }

    public void drawPellet(GraphicsContext g, Vector2i tile) {
        double offset = 0.5 * (TS - PELLET_SIZE);
        g.save();
        g.scale(scaling(), scaling());
        g.setFill(pelletColor);
        g.fillRect(tile.x() * TS + offset, tile.y() * TS + offset, PELLET_SIZE, PELLET_SIZE);
        g.restore();
    }

    public void drawEnergizer(GraphicsContext g, Vector2i tile) {
        double size = TS;
        double offset = 0.5 * (HTS);
        double x = tile.x() * TS, y = tile.y() * TS;
        g.save();
        g.scale(scaling(), scaling());
        g.setFill(energizerColor);
        // draw pixelized "circle"
        g.fillRect(x + offset, y, HTS, size);
        g.fillRect(x, y + offset, size, HTS);
        g.fillRect(x + 1, y + 1, size - 2, size - 2);
        g.restore();
    }
}