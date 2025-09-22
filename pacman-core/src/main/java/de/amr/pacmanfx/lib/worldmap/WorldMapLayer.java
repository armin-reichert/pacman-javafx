/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.lib.worldmap;

import de.amr.pacmanfx.lib.Vector2i;

import java.util.*;
import java.util.regex.Pattern;

public class WorldMapLayer {

    public enum PropertyAttribute {
        PREDEFINED, HIDDEN
    }

    public enum PropertyType {
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

    public static class Property {

        public static final Pattern PATTERN_PROPERTY_NAME = Pattern.compile("[a-zA-Z]([a-zA-Z0-9_])*");

        public static boolean isInvalidPropertyName(String s) {
            return s == null || !PATTERN_PROPERTY_NAME.matcher(s).matches();
        }

        public static Set<PropertyAttribute> emptyAttributeSet() {
            return EnumSet.noneOf(PropertyAttribute.class);
        }

        private String name;
        private String value;
        private PropertyType type;
        private final Set<PropertyAttribute> attributes = Property.emptyAttributeSet();

        public String name() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String value() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public PropertyType type() {
            return type;
        }

        public void setType(PropertyType type) {
            this.type = type;
        }

        public Set<PropertyAttribute> attributes() {
            return attributes;
        }

        public boolean is(PropertyAttribute attribute) {
            return attributes.contains(attribute);
        }

        @Override
        public String toString() {
            return "Property{" + "name='" + name + '\'' +
                    ", value='" + value + '\'' +
                    ", type=" + type +
                    ", attributes=" + attributes +
                    '}';
        }
    }

    private final Map<String, String> properties = new HashMap<>();
    private final byte[][] codes;

    public WorldMapLayer(int numRows, int numCols) {
        codes = new byte[numRows][numCols];
    }

    public WorldMapLayer(WorldMapLayer other) {
        int numRows = other.codes.length, numCols = other.codes[0].length;
        properties.putAll(other.properties);
        codes = new byte[numRows][];
        for (int row = 0; row < numRows; ++row) {
            codes[row] = Arrays.copyOf(other.codes[row], numCols);
        }
    }

    public int numRows() { return codes.length; }
    public int numCols() { return codes[0].length; }

    public byte get(int row, int col) {
        return codes[row][col];
    }

    public byte get(Vector2i tile) {
        return get(tile.y(), tile.x()); // Note order y=row, x=col
    }

    public void set(int row, int col, byte code) {
        codes[row][col] = code;
    }

    public void set(Vector2i tile, byte code) {
        set(tile.y(), tile.x(), code);
    }

    public void setAll(byte code) {
        for (byte[] row : codes) {
            Arrays.fill(row, code);
        }
    }

    public Map<String, String> properties() {
        return properties;
    }

    public void replaceProperties(Map<String, String> otherProperties) {
        properties.clear();
        properties.putAll(otherProperties);
    }
}
