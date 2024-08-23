/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.mapeditor;

import de.amr.games.pacman.lib.Vector2i;
import de.amr.games.pacman.lib.tilemap.TileMap;
import javafx.scene.paint.Color;
import org.tinylog.Logger;

public interface TileMapUtil {

    int TILE_SIZE = 8;

    static Color parseColor(String text) {
        try {
            return Color.web(text);
        } catch (Exception x) {
            Logger.error(x);
            return Color.WHITE;
        }
    }

    static String formatColor(Color color) {
        return String.format("rgb(%d,%d,%d)", (int)(color.getRed()*255), (int)(color.getGreen()*255), (int)(color.getBlue()*255));
    }

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

    static Vector2i getTileFromMap(TileMap map, String key, Vector2i defaultTile) {
        if (map.hasProperty(key)) {
            String spec = map.getProperty(key);
            try {
                return TileMap.parseVector2i(spec);
            } catch (Exception x) {
                Logger.error("Could not create Vector2i from value '{}'", spec);
                return defaultTile;
            }
        }
        return defaultTile;
    }
}
