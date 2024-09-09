/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.mapeditor;

import de.amr.games.pacman.lib.Vector2i;
import de.amr.games.pacman.lib.tilemap.TileMap;
import javafx.scene.canvas.GraphicsContext;

/**
 * @author Armin Reichert
 */
public interface Tool {

    String description();

    void apply(TileMap tileMap, Vector2i tile);

    TileMapRenderer renderer();

    void draw(GraphicsContext g, int row, int col);
}
