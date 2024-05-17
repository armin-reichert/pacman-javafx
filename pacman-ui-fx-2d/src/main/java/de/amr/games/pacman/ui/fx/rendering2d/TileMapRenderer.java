/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui.fx.rendering2d;

import de.amr.games.pacman.lib.Vector2i;
import de.amr.games.pacman.model.world.TileMap;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import org.tinylog.Logger;

/**
 * @author Armin Reichert
 */
public interface TileMapRenderer {

    int TILE_SIZE = 8;

    static Color getTileMapColor(TileMap tileMap, String key, Color defaultColor) {
        if (tileMap.hasProperty(key)) {
            try {
                return Color.web(tileMap.getProperty(key));
            } catch (Exception x) {
                Logger.error("Could not create color from property value '{}'", tileMap.getProperty(key));
                return defaultColor;
            }
        }
        return defaultColor;
    }

    void setScaling(double scaling);

    void drawMap(GraphicsContext g, TileMap map);

    void drawTile(GraphicsContext g, Vector2i tile, byte content);
}