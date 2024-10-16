package de.amr.games.pacman.model.ms_pacman_tengen;

import de.amr.games.pacman.lib.tilemap.MapColorScheme;

//TODO: Colors come from a non-original sprite sheet and are not correct
// Some colors have been corrected using the Mesen NES emulator
public enum MapColoring {
    BLACK_WHITE_GREEN("000000", "ffffff", "ffffff", "007b8c"),
    BLACK_WHITE_YELLOW("000000", "ffffff", "ffffff", "bdbd00"),
    BLACK_DARKBLUE("000000", "00298c", "00298c", "ffffff"),
    BLUE_WHITE_YELLOW("4242ff", "ffffff", "ffffff", "bdbd00"),
    BLUE2_WHITE("3900a5", "ffffff", "ffffff", "ffffff"),
    BLUE_YELLOW("002a88", "e4e594", "e4e594", "ffffff"),
    BROWN_WHITE("522100", "ffffff", "ffffff", "ffffff"),
    BROWN2_WHITE("994e00", "ffffff", "ffffff", "ffffff"),
    GRAY_WHITE_YELLOW("adadad", "ffffff", "ffffff", "bdbd00"),
    GREEN_WHITE_YELLOW("109400", "ffffff", "ffffff", "bdbd00"),
    GREEN_WHITE_WHITE("398400", "ffffff", "ffffff", "ffffff"),
    GREEN_WHITE_VIOLET("00424a", "ffffff", "ffffff", "9c18ce"),
    LIGHTBLUE_WHITE_YELLOW("64b0ff", "ffffff", "ffffff", "bcbe00"),
    KHAKI_WHITE("6b6b00", "ffffff", "ffffff", "ffffff"),
    PINK_ROSE("b5217b", "ff6bce", "ff6bce", "ffffff"),
    PINK_DARKRED("feccc5", "b71e7b", "b71e7b", "ffffff"),
    PINK_WHITE("ff6bce", "ffffff", "ffffff", "ffffff"),
    PINK_YELLOW("fec4ea", "bcbe00", "bcbe00", "ffffff"),
    ORANGE_WHITE("b53120", "ffffff", "ffffff", "b71e7b"),
    DARKBLUE_YELLOW("00298c", "e7e794", "e7e794", "ffffff"),
    RED_WHITE("b5217b", "ffffff", "ffffff", "ffffff"),
    RED_PINK("b5217b", "ff6bce", "ff6bce", "ffc6e7"),
    ROSE_RED("ffcec6", "b5217b", "b5217b", "ffffff"),
    VIOLET_PINK("9c18ce", "ff6bce", "ffffff", "ffffff"),
    VIOLET_WHITE("5a007b", "ffffff", "ffffff", "ffffff"),
    VIOLET_WHITE_YELLOW("7329ff", "ffffff", "ffffff", "bdbd00"),
    VIOLET_WHITE_GREEN("c673ff", "ffffff", "ffffff", "42de84"),
    YELLOW_WHITE_GREEN("bdbd00", "ffffff", "ffffff", "5ae731");

    MapColoring(String fill, String stroke, String door, String pellet) {
        colorScheme = new MapColorScheme(fill, stroke, door, pellet);
    }

    public MapColorScheme colorScheme() {
        return colorScheme;
    }

    private final MapColorScheme colorScheme;
}
