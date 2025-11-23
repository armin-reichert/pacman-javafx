/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.mapeditor.properties;

import de.amr.pacmanfx.lib.math.Vector2i;
import de.amr.pacmanfx.lib.worldmap.ColorRGBA;

import java.util.Locale;

public enum MapEditorPropertyType {
    STRING {
        @Override
        public String format(Object value) {
            return String.valueOf(value);
        }
    },

    TILE {
        @Override
        public String format(Object value) {
            if (value instanceof Vector2i(int x, int y)) {
                return "(%d,%d)".formatted(x, y);
            }
            throw new IllegalArgumentException("Not a tile value: " + value);
        }
    },

    COLOR_RGBA {
        @Override
        public String format(Object value) {
            if (value instanceof ColorRGBA(byte red, byte green, byte blue, double alpha)) {
                return String.format(Locale.ENGLISH, "rgba(%d,%d,%d,%.2f)", red, green, blue, alpha);
            }
            throw new IllegalArgumentException("Not a RGBA color value: " + value);
        }
    };

    public abstract String format(Object value);
}
