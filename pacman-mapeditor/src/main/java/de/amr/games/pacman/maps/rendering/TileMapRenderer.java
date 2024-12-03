/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.maps.rendering;

import de.amr.games.pacman.lib.Vector2i;
import de.amr.games.pacman.lib.tilemap.TerrainData;
import de.amr.games.pacman.lib.tilemap.TileMap;
import javafx.scene.canvas.GraphicsContext;

public interface TileMapRenderer {
    void setScaling(double scaling);
    void drawTerrain(GraphicsContext g, TileMap terrainMap, TerrainData terrainData);
    default void drawFood(GraphicsContext g, TileMap foodMap) {}
    void drawTile(GraphicsContext g, Vector2i tile, byte content);
}