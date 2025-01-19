/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.tilemap.editor;

import de.amr.games.pacman.lib.tilemap.TileEncoding;
import de.amr.games.pacman.lib.tilemap.TileMap;
import javafx.scene.paint.Color;
import org.tinylog.Logger;

import java.net.URL;

import static java.util.Objects.requireNonNull;

public interface TileMapEditorUtil {

    byte[][] GHOST_HOUSE_SHAPE = {
        {16, 8, 8, 14, 14, 8, 8, 17},
        {9, 0, 0, 0, 0, 0, 0, 9},
        {9, 0, 0, 0, 0, 0, 0, 9},
        {9, 0, 0, 0, 0, 0, 0, 9},
        {19, 8, 8, 8, 8, 8, 8, 18}
    };

    byte[][] CIRCLE_2x2 = {
        {TileEncoding.CORNER_NW, TileEncoding.CORNER_NE},
        {TileEncoding.CORNER_SW, TileEncoding.CORNER_SE}
    };

    static String urlString(String resourcePath) {
        URL url = requireNonNull(TileMapEditorUtil.class.getResource(resourcePath));
        return url.toExternalForm();
    }

    static byte mirroredTileContent(byte content) {
        return switch (content) {
            case TileEncoding.CORNER_NE -> TileEncoding.CORNER_NW;
            case TileEncoding.CORNER_NW -> TileEncoding.CORNER_NE;
            case TileEncoding.CORNER_SE -> TileEncoding.CORNER_SW;
            case TileEncoding.CORNER_SW -> TileEncoding.CORNER_SE;
            case TileEncoding.DCORNER_NE -> TileEncoding.DCORNER_NW;
            case TileEncoding.DCORNER_NW -> TileEncoding.DCORNER_NE;
            case TileEncoding.DCORNER_SE -> TileEncoding.DCORNER_SW;
            case TileEncoding.DCORNER_SW -> TileEncoding.DCORNER_SE;
            case TileEncoding.DCORNER_ANGULAR_NE -> TileEncoding.DCORNER_ANGULAR_NW;
            case TileEncoding.DCORNER_ANGULAR_NW -> TileEncoding.DCORNER_ANGULAR_NE;
            case TileEncoding.DCORNER_ANGULAR_SE -> TileEncoding.DCORNER_ANGULAR_SW;
            case TileEncoding.DCORNER_ANGULAR_SW -> TileEncoding.DCORNER_ANGULAR_SE;
            default -> content;
        };
    }

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
            String colorSpec = map.getStringProperty(key);
            try {
                return Color.web(colorSpec);
            } catch (Exception x) {
                Logger.error("Could not create color from value '{}'", colorSpec);
                return defaultColor;
            }
        }
        return defaultColor;
    }
}
