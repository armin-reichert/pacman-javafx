package de.amr.games.pacman.model.ms_pacman_tengen;

import de.amr.games.pacman.lib.tilemap.MapColorScheme;

import static de.amr.games.pacman.model.ms_pacman_tengen.NESColorPalette.c;

//TODO: Colors come from a non-original sprite sheet and are not correct
// Some colors have been corrected using the Mesen NES emulator
public enum TengenMapColoring {
    BLACK_WHITE_GREEN(c(0x0f), c(0x20), c(0x3b)),
    BLACK_WHITE_YELLOW(c(0x0f), c(0x20), c(0x28)),
    BLACK_DARKBLUE(c(0x0f), c(0x01), c(0x20)),
    BLUE_WHITE_YELLOW(c(0x12), c(0x20), c(0x28)),
    BLUE_WHITE(c(0x03), c(0x20), c(0x20)),
    BLUE_YELLOW(c(0x01), c(0x38), c(0x20)),
    BROWN_WHITE(c(0x07), c(0x20), c(0x20)),
    BROWN2_WHITE(c(0x17), c(0x20), c(0x20)),
    GRAY_WHITE_YELLOW(c(0x10), c(0x20), c(0x28)),
    GREEN_WHITE_YELLOW(c(0x1a), c(0x20), c(0x28)),
    GREEN_WHITE_WHITE(c(0x19), c(0x20), c(0x20)),
    GREEN_WHITE_VIOLET("00424a", c(0x20), "9c18ce"),
    LIGHTBLUE_WHITE_YELLOW(c(0x21), c(0x20), c(0x028)),
    KHAKI_WHITE(c(0x18), c(0x20), c(0x20)),
    PINK_ROSE("b5217b", "ff6bce", c(0x20)),
    PINK_DARKRED(c(0x36), "b71e7b", c(0x20)),
    PINK_WHITE("ff6bce", c(0x20), c(0x20)),
    PINK_YELLOW(c(0x35), c(0x028), c(0x20)),
    ORANGE_WHITE("b53120", c(0x20), "b71e7b"),
    DARKBLUE_YELLOW("00298c", "e7e794", c(0x20)),
    RED_WHITE("b5217b", c(0x20), c(0x20)),
    RED_PINK("b5217b", "ff6bce", "ffc6e7"),
    ROSE_RED("ffcec6", "b5217b", c(0x20)),
    VIOLET_PINK("9c18ce",  c(0x20), c(0x20)),
    VIOLET_WHITE(c(0x04), c(0x20), c(0x20)),
    VIOLET_WHITE_YELLOW(c(0x13), c(0x20), c(0x28)),
    VIOLET_WHITE_GREEN("c673ff", c(0x20), "42de84"),
    YELLOW_WHITE_GREEN(c(0x28), c(0x20), "5ae731");

    TengenMapColoring(String fill, String stroke, String pellet) {
        // Tengen maps have no separate door color
        colorScheme = new MapColorScheme(fill, stroke, stroke, pellet);
    }

    TengenMapColoring(byte fillIndex, byte strokeIndex, byte pelletIndex) {
        this(c(fillIndex), c(strokeIndex), c(pelletIndex));
    }

    public MapColorScheme colorScheme() {
        return colorScheme;
    }

    private final MapColorScheme colorScheme;
}
