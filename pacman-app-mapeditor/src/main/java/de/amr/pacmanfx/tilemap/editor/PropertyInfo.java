/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.tilemap.editor;

import java.util.regex.Pattern;

import static java.util.Objects.requireNonNull;

public record PropertyInfo(String name, PropertyType type, boolean permanent) {

    public static final Pattern PATTERN_PROPERTY_NAME = Pattern.compile("[a-zA-Z]([a-zA-Z0-9_])*");

    public static boolean isValidPropertyName(String s) {
        return PATTERN_PROPERTY_NAME.matcher(s).matches();
    }

    public PropertyInfo {
        requireNonNull(name);
        if (!isValidPropertyName(name)) {
            throw new IllegalArgumentException("Invalid property name: '%s'".formatted(name));
        }
        requireNonNull(type);
    }
}
