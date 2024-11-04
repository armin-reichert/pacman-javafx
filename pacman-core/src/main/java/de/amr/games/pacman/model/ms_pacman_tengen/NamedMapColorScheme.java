/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.model.ms_pacman_tengen;

import de.amr.games.pacman.lib.nes.NES;

import java.util.Map;

public enum NamedMapColorScheme {

    MSC_0F_20_1C_BLACK_WHITE_GREEN   (0x0f, 0x20, 0x1c),
    MSC_0F_20_28_BLACK_WHITE_YELLOW  (0x0f, 0x20, 0x28),
    MSC_0F_01_20_BLACK_BLUE_WHITE    (0x0f, 0x01, 0x20),
    MSC_01_38_20_BLUE_YELLOW_WHITE   (0x01, 0x38, 0x20),
    MSC_12_20_28_BLUE_WHITE_YELLOW   (0x12, 0x20, 0x28),
    MSC_21_20_28_BLUE_WHITE_YELLOW   (0x21, 0x20, 0x28),
    MSC_07_20_20_BROWN_WHITE_WHITE   (0x07, 0x20, 0x20),
    MSC_17_20_20_BROWN_WHITE_WHITE   (0x17, 0x20, 0x20),
    MSC_10_20_28_GRAY_WHITE_YELLOW   (0x10, 0x20, 0x28),
    MSC_0C_20_14_GREEN_WHITE_VIOLET  (0x0c, 0x20, 0x14),
    MSC_19_20_20_GREEN_WHITE_WHITE   (0x19, 0x20, 0x20),
    MSC_1A_20_28_GREEN_WHITE_YELLOW  (0x1a, 0x20, 0x28),
    MSC_1B_20_20_GREEN_WHITE_WHITE   (0x1b, 0x20, 0x20),
    MSC_18_20_20_KHAKI_WHITE_WHITE   (0x18, 0x20, 0x20),
    MSC_16_20_15_ORANGE_WHITE_RED    (0x16, 0x20, 0x15),
    MSC_35_28_20_PINK_YELLOW_WHITE   (0x35, 0x28, 0x20),
    MSC_36_15_20_PINK_RED_WHITE      (0x36, 0x15, 0x20),
    MSC_15_25_20_RED_ROSE_WHITE      (0x15, 0x25, 0x20),
    MSC_15_20_20_RED_WHITE_WHITE     (0x15, 0x20, 0x20),
    MSC_25_20_20_ROSE_WHITE_WHITE    (0x25, 0x20, 0x20),
    MSC_14_25_20_VIOLET_ROSE_WHITE   (0x14, 0x25, 0x20),
    MSC_23_20_2B_VIOLET_WHITE_GREEN  (0x23, 0x20, 0x2b),
    MSC_03_20_20_VIOLET_WHITE_WHITE  (0x03, 0x20, 0x20),
    MSC_04_20_20_VIOLET_WHITE_WHITE  (0x04, 0x20, 0x20),
    MSC_13_20_28_VIOLET_WHITE_YELLOW (0x13, 0x20, 0x28),
    MSC_28_20_2A_YELLOW_WHITE_GREEN  (0x28, 0x20, 0x2a);

    /**
     * @param fillIndex index in NES palette of wall fill color
     * @param strokeIndex index in NES palette of wall stroke (and door) color
     * @param pelletIndex index in NES palette of pellet color
     */
    NamedMapColorScheme(int fillIndex, int strokeIndex, int pelletIndex) {
        colorScheme = Map.of(
            "fill",   NES.Palette.color(fillIndex),
            "stroke", NES.Palette.color(strokeIndex),
            "door",   NES.Palette.color(strokeIndex),
            "pellet", NES.Palette.color(pelletIndex)
        );
    }

    public Map<String, String> get() {
        return colorScheme;
    }

    private final Map<String, String> colorScheme;
}