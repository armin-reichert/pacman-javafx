/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.tilemap.editor;

import de.amr.games.pacman.lib.Vector2i;
import de.amr.games.pacman.lib.tilemap.WorldMap;
import de.amr.games.pacman.tilemap.rendering.TileMapRenderer;
import javafx.scene.canvas.GraphicsContext;

/**
 * @author Armin Reichert
 */
public interface Tool {

    String description();

    void apply(WorldMap worldMap, WorldMap.LayerID layerID, Vector2i tile);

    TileMapRenderer renderer();

    void draw(GraphicsContext g, int row, int col);
}
