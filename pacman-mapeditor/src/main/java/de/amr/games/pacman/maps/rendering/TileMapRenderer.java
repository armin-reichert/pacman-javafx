/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.maps.rendering;

import de.amr.games.pacman.lib.Vector2i;
import de.amr.games.pacman.lib.tilemap.Obstacle;
import de.amr.games.pacman.lib.tilemap.TileMap;
import javafx.scene.canvas.GraphicsContext;

import java.util.List;

public interface TileMapRenderer {
    void setScaling(double scaling);
    void drawTerrain(GraphicsContext g, TileMap terrainMap, List<Obstacle> obstacles);
    default void drawFood(GraphicsContext g, TileMap foodMap) {}
    void drawTile(GraphicsContext g, Vector2i tile, byte content);
}