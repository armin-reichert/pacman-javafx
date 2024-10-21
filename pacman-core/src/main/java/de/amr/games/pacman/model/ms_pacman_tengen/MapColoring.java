package de.amr.games.pacman.model.ms_pacman_tengen;

import de.amr.games.pacman.lib.tilemap.MapColorScheme;

import static de.amr.games.pacman.model.ms_pacman_tengen.MsPacManPaletteColors.c;

//TODO: Colors come from a non-original sprite sheet and are not correct
// Some colors have been corrected using the Mesen NES emulator
public enum MapColoring {
    BLACK_WHITE_GREEN(c(0x0f), c(0x20), c(0x20), "007b8c"),
    BLACK_WHITE_YELLOW("000000", c(0x20), c(0x20), "bdbd00"),
    BLACK_DARKBLUE("000000", "00298c", "00298c", c(0x20)),
    BLUE_WHITE_YELLOW("4242ff", c(0x20), c(0x20), "bdbd00"),
    BLUE2_WHITE("3b00a4", c(0x20), c(0x20), c(0x20)),
    BLUE_YELLOW("002a88", "e4e594", "e4e594", c(0x20)),
    BROWN_WHITE("522100", c(0x20), c(0x20), c(0x20)),
    BROWN2_WHITE("994e00", c(0x20), c(0x20), c(0x20)),
    GRAY_WHITE_YELLOW("adadad", c(0x20), c(0x20), "bdbd00"),
    GREEN_WHITE_YELLOW("109400", c(0x20), c(0x20), "bdbd00"),
    GREEN_WHITE_WHITE("398400", c(0x20), c(0x20), c(0x20)),
    GREEN_WHITE_VIOLET("00424a", c(0x20), c(0x20), "9c18ce"),
    LIGHTBLUE_WHITE_YELLOW("64b0ff", c(0x20), c(0x20), "bcbe00"),
    KHAKI_WHITE("6b6b00", c(0x20), c(0x20), c(0x20)),
    PINK_ROSE("b5217b", "ff6bce", "ff6bce", c(0x20)),
    PINK_DARKRED(c(0x36), "b71e7b", "b71e7b", c(0x20)),
    PINK_WHITE("ff6bce", c(0x20), c(0x20), c(0x20)),
    PINK_YELLOW("fec4ea", "bcbe00", "bcbe00", c(0x20)),
    ORANGE_WHITE("b53120", c(0x20), c(0x20), "b71e7b"),
    DARKBLUE_YELLOW("00298c", "e7e794", "e7e794", c(0x20)),
    RED_WHITE("b5217b", c(0x20), c(0x20), c(0x20)),
    RED_PINK("b5217b", "ff6bce", "ff6bce", "ffc6e7"),
    ROSE_RED("ffcec6", "b5217b", "b5217b", c(0x20)),
    VIOLET_PINK("9c18ce", "ff6bce", c(0x20), c(0x20)),
    VIOLET_WHITE("5a007b", c(0x20), c(0x20), c(0x20)),
    VIOLET_WHITE_YELLOW("7329ff", c(0x20), c(0x20), "bdbd00"),
    VIOLET_WHITE_GREEN("c673ff", c(0x20), c(0x20), "42de84"),
    YELLOW_WHITE_GREEN("bdbd00", c(0x20), c(0x20), "5ae731");

    MapColoring(String fill, String stroke, String door, String pellet) {
        colorScheme = new MapColorScheme(fill, stroke, door, pellet);
    }

    public MapColorScheme colorScheme() {
        return colorScheme;
    }

    private final MapColorScheme colorScheme;
}
