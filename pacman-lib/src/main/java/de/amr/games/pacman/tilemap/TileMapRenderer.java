/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.tilemap;

import de.amr.games.pacman.lib.Vector2i;
import de.amr.games.pacman.lib.TileMap;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import org.tinylog.Logger;

/**
 * @author Armin Reichert
 */
public interface TileMapRenderer {

    int TILE_SIZE = 8;

    static Color getColorFromMap(TileMap map, String key, Color defaultColor) {
        if (map.hasProperty(key)) {
            String colorSpec = map.getProperty(key);
            try {
                return Color.web(colorSpec);
            } catch (Exception x) {
                Logger.error("Could not create color from value '{}'", colorSpec);
                return defaultColor;
            }
        }
        return defaultColor;
    }

    void setScaling(double scaling);

    void drawMap(GraphicsContext g, TileMap map);

    void drawTile(GraphicsContext g, Vector2i tile, byte content);
}