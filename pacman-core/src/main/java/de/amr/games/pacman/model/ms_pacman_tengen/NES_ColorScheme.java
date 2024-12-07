/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.model.ms_pacman_tengen;

import de.amr.games.pacman.lib.nes.NES;

import static de.amr.games.pacman.lib.Globals.randomInt;

/**
 * The color schemes used in the Tengen Ms. Pac-Man maps.
 */
public enum NES_ColorScheme {

    _0F_20_1C_BLACK_WHITE_GREEN   (0x0f, 0x20, 0x1c),
    _0F_20_28_BLACK_WHITE_YELLOW  (0x0f, 0x20, 0x28),
    _0F_01_20_BLACK_BLUE_WHITE    (0x0f, 0x01, 0x20),
    _01_38_20_BLUE_YELLOW_WHITE   (0x01, 0x38, 0x20),
    _12_20_28_BLUE_WHITE_YELLOW   (0x12, 0x20, 0x28),
    _21_20_28_BLUE_WHITE_YELLOW   (0x21, 0x20, 0x28),
    _07_20_20_BROWN_WHITE_WHITE   (0x07, 0x20, 0x20),
    _17_20_20_BROWN_WHITE_WHITE   (0x17, 0x20, 0x20),
    _10_20_28_GRAY_WHITE_YELLOW   (0x10, 0x20, 0x28),
    _00_2A_24_GRAY_GREEN_PINK     (0x00, 0x2a, 0x24),
    _0C_20_14_GREEN_WHITE_VIOLET  (0x0c, 0x20, 0x14),
    _19_20_20_GREEN_WHITE_WHITE   (0x19, 0x20, 0x20),
    _1A_20_28_GREEN_WHITE_YELLOW  (0x1a, 0x20, 0x28),
    _1B_20_20_GREEN_WHITE_WHITE   (0x1b, 0x20, 0x20),
    _18_20_20_KHAKI_WHITE_WHITE   (0x18, 0x20, 0x20),
    _16_20_15_ORANGE_WHITE_RED    (0x16, 0x20, 0x15),
    _35_28_20_PINK_YELLOW_WHITE   (0x35, 0x28, 0x20),
    _36_15_20_PINK_RED_WHITE      (0x36, 0x15, 0x20),
    _15_25_20_RED_ROSE_WHITE      (0x15, 0x25, 0x20),
    _15_20_20_RED_WHITE_WHITE     (0x15, 0x20, 0x20),
    _25_20_20_ROSE_WHITE_WHITE    (0x25, 0x20, 0x20),
    _14_25_20_VIOLET_ROSE_WHITE   (0x14, 0x25, 0x20),
    _23_20_2B_VIOLET_WHITE_GREEN  (0x23, 0x20, 0x2b),
    _03_20_20_VIOLET_WHITE_WHITE  (0x03, 0x20, 0x20),
    _04_20_20_VIOLET_WHITE_WHITE  (0x04, 0x20, 0x20),
    _13_20_28_VIOLET_WHITE_YELLOW (0x13, 0x20, 0x28),
    _28_16_20_YELLOW_RED_WHITE    (0x28, 0x16, 0x20),
    _28_20_2A_YELLOW_WHITE_GREEN  (0x28, 0x20, 0x2a);

    NES_ColorScheme(int fillIndex, int strokeIndex, int pelletIndex) {
        fillColor   = NES.Palette.color(fillIndex);
        strokeColor = NES.Palette.color(strokeIndex);
        pelletColor = NES.Palette.color(pelletIndex);
    }

    public static NES_ColorScheme random() {
        var all = NES_ColorScheme.values();
        return all[randomInt(0, all.length)];
    }

    public String fillColor() {
        return fillColor;
    }

    public String strokeColor() {
        return strokeColor;
    }

    public String pelletColor() {
        return pelletColor;
    }

    private final String fillColor;
    private final String strokeColor;
    private final String pelletColor;
}