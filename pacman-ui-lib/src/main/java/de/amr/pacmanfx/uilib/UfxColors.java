/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.uilib;

import javafx.scene.paint.Color;

import static java.util.Objects.requireNonNull;

public final class UfxColors {

    private UfxColors() {}

    /**
     * Converts a {@link Color} to a hexadecimal RGBA string compatible with {@link Color#web(String)}.
     *
     * @param color the color to convert
     * @return a string in the form {@code #RRGGBBAA}
     */
    public static String formatColorHex(Color color) {
        return "#%02x%02x%02x%02x".formatted(
            (int) Math.round(color.getRed()     * 255),
            (int) Math.round(color.getGreen()   * 255),
            (int) Math.round(color.getBlue()    * 255),
            (int) Math.round(color.getOpacity() * 255)
        );
    }

    /**
     * Returns a copy of the given color with the specified opacity.
     *
     * @param color   the base color
     * @param opacity the new opacity (0–1)
     * @return a new {@link Color} with the same RGB values and updated opacity
     */
    public static Color colorWithOpacity(Color color, double opacity) {
        requireNonNull(color);
        return Color.color(color.getRed(), color.getGreen(), color.getBlue(), opacity);
    }


    /**
     * Represents a color transformation from one color to another.
     *
     * @param from the original color
     * @param to   the replacement color
     */
    public record ColorChange(Color from, Color to) {}
}
