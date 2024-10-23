/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.model.ms_pacman_tengen;

import de.amr.games.pacman.lib.tilemap.MapColorScheme;

import static de.amr.games.pacman.model.ms_pacman_tengen.NESColorPalette.color;

public class TengenMapColorSchemes {

    private static MapColorScheme create(int fillIndex, int strokeIndex, int pelletIndex) {
        return new MapColorScheme(NESColorPalette.color(fillIndex), NESColorPalette.color(strokeIndex),
            NESColorPalette.color(strokeIndex), NESColorPalette.color(pelletIndex));
    }

    public static final MapColorScheme BLACK_WHITE_GREEN = create(0x0f, 0x20, 0x1c);
    public static final MapColorScheme BLACK_WHITE_YELLOW= create(0x0f, 0x20,0x28);
    public static final MapColorScheme BLACK_DARKBLUE= create(0x0f, 0x01,0x20);
    public static final MapColorScheme BLUE_WHITE_YELLOW= create(0x12, 0x20, 0x28);
    public static final MapColorScheme BLUE_WHITE= create(0x03, 0x20, 0x20);
    public static final MapColorScheme BLUE_YELLOW= create(0x01, 0x38, 0x20);
    public static final MapColorScheme BROWN_WHITE= create(0x07, 0x20, 0x20);
    public static final MapColorScheme BROWN2_WHITE= create(0x17, 0x20, 0x20);
    public static final MapColorScheme GRAY_WHITE_YELLOW= create(0x10, 0x20, 0x28);
    public static final MapColorScheme GREEN_WHITE_YELLOW= create(0x1a, 0x20, 0x28);
    public static final MapColorScheme GREEN_WHITE_WHITE= create(0x1b, 0x20, 0x20);
    public static final MapColorScheme GREEN_WHITE_VIOLET= create(0x0c, 0x20, 0x22);
    public static final MapColorScheme LIGHTBLUE_WHITE_YELLOW= create(0x21, 0x20, 0x28);
    public static final MapColorScheme KHAKI_WHITE= create(0x18, 0x20, 0x20);
    public static final MapColorScheme PINK_ROSE= create(0x15, 0x25, 0x20);
    public static final MapColorScheme PINK_DARKRED= create(0x36, 0x15, 0x20);
    public static final MapColorScheme PINK_WHITE= create(0x25, 0x20, 0x20);
    public static final MapColorScheme PINK_YELLOW= create(0x35, 0x028, 0x20);
    public static final MapColorScheme ORANGE_WHITE= create(0x16, 0x20, 0x15);
    public static final MapColorScheme DARKBLUE_YELLOW= create(0x01, 0x38, 0x20);
    public static final MapColorScheme RED_WHITE= create(0x15, 0x20, 0x20);
    public static final MapColorScheme RED_PINK= create(0x15, 0x25, 0x35);
    public static final MapColorScheme ROSE_RED= create(0x35, 0x15, 0x20);
    public static final MapColorScheme VIOLET_PINK= create(0x14,  0x24, 0x20);
    public static final MapColorScheme VIOLET_WHITE= create(0x04, 0x20, 0x20);
    public static final MapColorScheme VIOLET_WHITE_YELLOW= create(0x13, 0x20, 0x28);
    public static final MapColorScheme VIOLET_WHITE_GREEN= create(0x23, 0x20, 0x2b);
    public static final MapColorScheme YELLOW_WHITE_GREEN= create(0x28, 0x20, 0x2a);

    private TengenMapColorSchemes(int fillIndex, int strokeIndex, int pelletIndex) {
        colorScheme = new MapColorScheme(color(fillIndex), color(strokeIndex), color(strokeIndex), color(pelletIndex));
    }

    public MapColorScheme colorScheme() {
        return colorScheme;
    }

    private final MapColorScheme colorScheme;
}
